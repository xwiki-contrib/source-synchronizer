/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.sourcesync.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.contrib.sourcesync.SourceSyncIndex;
import org.xwiki.extension.ExtensionId;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.xar.XarException;
import org.xwiki.xar.internal.XarUtils;
import org.xwiki.xar.internal.model.XarDocumentModel;
import org.xwiki.xml.stax.StAXUtils;

/**
 * Keep the {@link org.xwiki.contrib.sourcesync.SourceSyncIndex} up to date.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultSourceSyncIndexMonitor implements Runnable, SourceSyncIndexMonitor, Initializable
{
    private static final String POM = "pom.xml";

    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    @Inject
    private Logger logger;

    @Inject
    private ExecutionContextManager executionContextManager;

    @Inject
    private SourceSyncIndex sourceSyncIndex;

    private WatchService watcher;

    private DefaultSourceSyncIndex defaultSourceSyncIndex;

    private volatile boolean shouldStop;

    private final Collection<Path> rootFolders = new ArrayList<>();

    @Override
    public void initialize() throws InitializationException
    {
        this.defaultSourceSyncIndex = (DefaultSourceSyncIndex) sourceSyncIndex;

        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new InitializationException("Failed to initalize filesystem watch service", e);
        }
    }

    @Override
    public void stopProcessing()
    {
        this.shouldStop = true;
    }

    @Override
    public void addFolder(Path path)
    {
        this.rootFolders.add(path);
    }

    @Override
    public void removeFolder(Path path)
    {
        this.rootFolders.remove(path);
    }

    private void prepareContext() throws ExecutionContextException
    {
        // Create a single execution context and use it for this thread.
        ExecutionContext ec = new ExecutionContext();
        this.executionContextManager.initialize(ec);
    }

    @Override
    public void run()
    {
        try {
            // Make sure we initialize an execution context.
            prepareContext();

            runInternal();
        } catch (ExecutionContextException e) {
            // Not much to do but log.
            this.logger.error("Failed to initialize the Source Sync Index Updater thread's execution context", e);
        }
    }

    private void runInternal()
    {
        do {
            try {
                for (Path path : this.rootFolders) {
                    indexPath(path);
                }
                // Pause to be kind on on resource.
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                // Thread has been stopped, exit
                this.logger.debug("Source Sync Index Updater Thread was forcefully stopped", e);
                break;
            } catch (Exception e) {
                // There was an unexpected problem, we just log the problem but keep the thread alive!
                this.logger.error("Unexpected error in the Source Sync Index Updater", e);
            }
        } while (!this.shouldStop);
    }

    private void indexPath(Path path) throws IOException
    {
        if (path.getFileName().toString().equals(POM)) {
            indexMavenModule(path);
        } else if (path.toFile().isDirectory()) {
            indexDirectory(path);
        }
    }

    private void indexDirectory(Path directory) throws IOException
    {
        Path pomPath = directory.resolve(POM);

        // If Maven module swicth to #indexMavenModule
        if (pomPath.toFile().exists()) {
            indexMavenModule(pomPath);

            return;
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path child : directoryStream) {
                indexPath(child);
            }
        }
    }

    private void indexMavenModule(Path pomPath)
    {
        Model mavenModel;
        try (InputStream inputStream = Files.newInputStream(pomPath)) {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            mavenModel = reader.read(inputStream);
        } catch (Exception e) {
            this.logger.warn("Faied to parse Maven descriptor file [{}]: {}", pomPath,
                ExceptionUtils.getRootCauseMessage(e));

            return;
        }

        Path moduleDirectory = pomPath.getParent();

        if (mavenModel.getPackaging().equals("pom")) {
            // Index sub modules
            for (String module : mavenModel.getModules()) {
                Path modulePath = moduleDirectory.resolve(module);
                Path modulePomPath = modulePath.resolve(POM);

                if (modulePomPath.toFile().exists()) {
                    indexMavenModule(modulePomPath);
                }
            }
        } else if (mavenModel.getPackaging().equals("xar")) {
            // Watch the pom for any modification
            try {
                pomPath.register(this.watcher, StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                this.logger.error("Failed to watch path [{}]", pomPath, e);
            }

            // Add the extension to the index
            ExtensionId extensionId = new ExtensionId(mavenModel.getGroupId() + ":" + mavenModel.getArtifactId());

            DefaultSourceSyncExtension extension =
                new DefaultSourceSyncExtension(extensionId, moduleDirectory, pomPath.toFile().lastModified());

            // Scan the module for wiki pages
            try {
                indexModulePages(extension);

                this.defaultSourceSyncIndex.addExtension(extension);
            } catch (IOException e) {
                this.logger.warn("Failed to scan pages of the module [{}]: {}", moduleDirectory,
                    ExceptionUtils.getRootCauseMessage(e));
            }
        }
    }

    private void indexModulePages(DefaultSourceSyncExtension extension) throws IOException
    {
        Path pagesRootPath = extension.getPath().resolve("src/main/resources");

        Files.walk(pagesRootPath).filter(p -> p.toString().endsWith(".xml")).forEach(this::indexModulePage);
    }

    private void indexModulePage(Path pagePath)
    {
        try (FileInputStream stream = new FileInputStream(documentFile)) {
            return getReference(stream);
        } catch (FileNotFoundException e) {
            throw new XarException("Cannot find file", e);
        } catch (IOException e) {
            throw new XarException("Failed close file stream", e);
        }
    }

    /**
     * Extract {@link LocalDocumentReference} from a XAR document XML stream.
     * 
     * @param documentStream the stream to parse
     * @return the reference extracted from the stream
     * @throws XarException when failing to parse the document stream
     * @since 5.4M1
     */
    public static LocalDocumentReference getReference(InputStream documentStream) throws XarException
    {
        XMLStreamReader xmlReader;
        try {
            xmlReader = XML_INPUT_FACTORY.createXMLStreamReader(documentStream);
        } catch (XMLStreamException e) {
            throw new XarException("Failed to create a XML read", e);
        }

        EntityReference reference = null;
        Locale locale = null;

        String legacySpace = null;
        String legacyPage = null;

        try {
            // <xwikidoc>

            xmlReader.nextTag();

            xmlReader.require(XMLStreamReader.START_ELEMENT, null, XarDocumentModel.ELEMENT_DOCUMENT);

            // Reference
            String referenceString = xmlReader.getAttributeValue(null, XarDocumentModel.ATTRIBUTE_DOCUMENT_REFERENCE);
            if (referenceString != null) {
                reference = RESOLVER.resolve(referenceString, EntityType.DOCUMENT);
            }

            // Locale
            String localeString = xmlReader.getAttributeValue(null, XarDocumentModel.ATTRIBUTE_DOCUMENT_LOCALE);
            if (localeString != null) {
                if (localeString.isEmpty()) {
                    locale = Locale.ROOT;
                } else {
                    locale = LocaleUtils.toLocale(localeString);
                }
            }

            // Legacy fallback
            if (reference == null || locale == null) {
                for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
                    String elementName = xmlReader.getLocalName();

                    if (XarDocumentModel.ELEMENT_NAME.equals(elementName)) {
                        if (reference == null) {
                            legacyPage = xmlReader.getElementText();

                            if (legacySpace != null && locale != null) {
                                break;
                            }
                        } else if (locale != null) {
                            break;
                        }
                    } else if (XarDocumentModel.ELEMENT_SPACE.equals(elementName)) {
                        if (reference == null) {
                            legacySpace = xmlReader.getElementText();

                            if (legacyPage != null && locale != null) {
                                break;
                            }
                        } else if (locale != null) {
                            break;
                        }
                    } else if (XarDocumentModel.ELEMENT_LOCALE.equals(elementName)) {
                        if (locale == null) {
                            String value = xmlReader.getElementText();
                            if (value.length() == 0) {
                                locale = Locale.ROOT;
                            } else {
                                locale = LocaleUtils.toLocale(value);
                            }
                        }

                        if (reference != null || (legacySpace != null && legacyPage != null)) {
                            break;
                        }
                    } else {
                        StAXUtils.skipElement(xmlReader);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new XarException("Failed to parse document", e);
        } finally {
            try {
                xmlReader.close();
            } catch (XMLStreamException e) {
                throw new XarException("Failed to close XML reader", e);
            }
        }

        if (reference == null) {
            if (legacySpace == null) {
                throw new XarException("Missing space element");
            }
            if (legacyPage == null) {
                throw new XarException("Missing page element");
            }

            reference = new LocalDocumentReference(legacySpace, legacyPage);
        }

        if (locale == null) {
            throw new XarException("Missing locale element");
        }

        LocalDocumentReference reference = new LocalDocumentReference(reference, locale);

        DefaultSourceSyncDocument document = new DefaultSourceSyncDocument(reference, path, date, pathDate);
    }
}
