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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.sourcesync.SourceSynchIndex;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.xar.internal.handler.XarExtensionHandler;
import org.xwiki.extension.xar.internal.repository.XarInstalledExtension;
import org.xwiki.extension.xar.internal.repository.XarInstalledExtensionRepository;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.event.model.WikiDocumentFilter;
import org.xwiki.filter.input.InputFilterStreamFactory;
import org.xwiki.filter.instance.input.DocumentInstanceInputProperties;
import org.xwiki.filter.instance.input.EntityEventGenerator;
import org.xwiki.filter.output.BeanOutputFilterStream;
import org.xwiki.filter.output.BeanOutputFilterStreamFactory;
import org.xwiki.filter.output.DefaultOutputStreamOutputTarget;
import org.xwiki.filter.output.OutputFilterStreamFactory;
import org.xwiki.filter.output.OutputStreamOutputTarget;
import org.xwiki.filter.xar.internal.XARFilter;
import org.xwiki.filter.xar.internal.XARFilterUtils;
import org.xwiki.filter.xar.output.XAROutputProperties;
import org.xwiki.job.Job;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.filter.XWikiDocumentFilterUtils;

@Component(roles = SourceSyncExporter.class)
@Singleton
public class SourceSyncExporter
{
    @Inject
    @Named(XARFilterUtils.ROLEHINT_CURRENT)
    private InputFilterStreamFactory xarInputFilterStreamFactory;

    @Inject
    @Named(XARFilterUtils.ROLEHINT_CURRENT)
    private OutputFilterStreamFactory xarOutputFilterStreamFactory;

    @Inject
    private EntityEventGenerator<XWikiDocument> documentSerializer;

    @Inject
    private XWikiDocumentFilterUtils filterUtils;

    @Inject
    @Named(XarExtensionHandler.TYPE)
    private InstalledExtensionRepository extensions;

    @Inject
    private SourceSynchIndex index;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    private XarInstalledExtensionRepository getXarInstalledExtensionRepository()
    {
        return (XarInstalledExtensionRepository) this.extensions;
    }

    public Job exportExtension(ExtensionId extensionId)
    {

    }

    public void exportDocument(DocumentReference documentReference) throws FilterException, IOException, XWikiException
    {
        XWikiContext xcontext = this.xcontextProvider.get();

        XWikiDocument document = xcontext.getWiki().getDocument(documentReference, xcontext);

        exportDocument(document);
    }

    public void exportDocument(XWikiDocument document) throws FilterException, IOException
    {
        Collection<XarInstalledExtension> extensions =
            getXarInstalledExtensionRepository().getXarInstalledExtensions(document.getDocumentReference());

        if (extensions.isEmpty()) {
            return;
        }

        XarInstalledExtension extension = extensions.iterator().next();

        Path sourcePath = this.index.getSourcePath(extension.getId());

        OutputStream outputStream = Files.newOutputStream(sourcePath);
        try (OutputStreamOutputTarget target = new DefaultOutputStreamOutputTarget(outputStream, true)) {
            // Input
            DocumentInstanceInputProperties documentProperties = new DocumentInstanceInputProperties();
            documentProperties.setVerbose(false);
            documentProperties.setWithJRCSRevisions(false);
            documentProperties.setWithRevisions(false);

            // Output
            XAROutputProperties xarProperties = new XAROutputProperties();
            xarProperties.setForceDocument(true);
            xarProperties.setTarget(target);
            xarProperties.setVerbose(false);
            BeanOutputFilterStream<XAROutputProperties> xarFilterStream =
                ((BeanOutputFilterStreamFactory<XAROutputProperties>) this.xarOutputFilterStreamFactory)
                    .createOutputFilterStream(xarProperties);
            XARFilter xarFilter = (XARFilter) xarFilterStream.getFilter();

            ///////////////////////////////////////////////
            // Events
            ///////////////////////////////////////////////

            // Begin space(s)
            FilterEventParameters documentParameters = null;
            DocumentReference documentReference = null;
            documentReference = document.getDocumentReference();
            for (SpaceReference spaceReference : documentReference.getSpaceReferences()) {
                xarFilter.beginWikiSpace(spaceReference.getName(), FilterEventParameters.EMPTY);
            }

            // Begin document
            documentParameters = new FilterEventParameters();
            documentParameters.put(WikiDocumentFilter.PARAMETER_LOCALE, document.getDefaultLocale());
            xarFilter.beginWikiDocument(documentReference.getName(), documentParameters);

            // Document Locale events
            this.documentSerializer.write(document, xarFilterStream, documentProperties);

            // End document
            xarFilter.endWikiDocument(documentReference.getName(), documentParameters);

            // End space(s)
            documentReference = document.getDocumentReference();
            for (EntityReference reference =
                documentReference.getParent(); reference instanceof SpaceReference; reference = reference.getParent()) {
                xarFilter.beginWikiSpace(reference.getName(), FilterEventParameters.EMPTY);
            }

            xarFilterStream.close();
        }
    }
}
