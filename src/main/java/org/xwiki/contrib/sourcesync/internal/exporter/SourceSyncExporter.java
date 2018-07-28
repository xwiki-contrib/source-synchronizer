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
package org.xwiki.contrib.sourcesync.internal.exporter;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.contrib.sourcesync.SourceSyncDocument;
import org.xwiki.contrib.sourcesync.SourceSyncExtension;
import org.xwiki.contrib.sourcesync.SourceSyncIndex;
import org.xwiki.contrib.sourcesync.internal.job.SourceSyncRequest;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.xar.internal.handler.XarExtensionHandler;
import org.xwiki.extension.xar.internal.repository.XarInstalledExtension;
import org.xwiki.extension.xar.internal.repository.XarInstalledExtensionRepository;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.instance.input.DocumentInstanceInputProperties;
import org.xwiki.filter.output.DefaultOutputStreamOutputTarget;
import org.xwiki.filter.output.OutputStreamOutputTarget;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.event.status.JobProgressManager;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.filter.XWikiDocumentFilterUtils;

@Component(roles = SourceSyncExporter.class)
@Singleton
public class SourceSyncExporter
{
    @Inject
    @Named(XarExtensionHandler.TYPE)
    private InstalledExtensionRepository xarRepository;

    @Inject
    private SourceSyncIndex index;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private JobExecutor executor;

    @Inject
    private JobProgressManager progress;

    @Inject
    private XWikiDocumentFilterUtils documentUtils;

    @Inject
    private Logger logger;

    private XarInstalledExtensionRepository getXarInstalledExtensionRepository()
    {
        return (XarInstalledExtensionRepository) this.xarRepository;
    }

    public void exportExtension(SourceSyncExtension sourceExtension, boolean force)
    {
        this.progress.pushLevelProgress(sourceExtension.getDocuments().size(), sourceExtension);

        XWikiContext xcontext = this.xcontextProvider.get();

        try {
            for (SourceSyncDocument sourceDocument : sourceExtension.getDocuments()) {
                this.progress.startStep(sourceExtension);

                try {
                    XWikiDocument xdocument = xcontext.getWiki().getDocument(sourceDocument.getReference(), xcontext);
                    if (xdocument.isNew()) {
                        // Document deleted on wiki side
                        Files.delete(sourceDocument.getPath());
                        sourceDocument.update();
                    } else {
                        // Export document
                        if (force || sourceDocument.getDocumentDate().before(xdocument.getDate())) {
                            try {
                                exportDocument(xdocument, sourceDocument);
                            } catch (Exception e) {
                                this.logger.error("Failed to export document [{}]",
                                    xdocument.getDocumentReferenceWithLocale(), e);
                            }
                        }
                    }
                } catch (Exception e) {
                    this.logger.error("Failed to load document [{}] from wiki [{}]", sourceDocument.getReference(),
                        xcontext.getWikiId(), e);
                }

                this.progress.endStep(sourceExtension);
            }
        } finally {
            this.progress.popLevelProgress(sourceExtension);
        }
    }

    public void exportDocument(DocumentReference documentReference)
        throws FilterException, IOException, XWikiException, ComponentLookupException
    {
        XWikiContext xcontext = this.xcontextProvider.get();

        XWikiDocument document = xcontext.getWiki().getDocument(documentReference, xcontext);

        exportDocument(document);
    }

    public void exportDocument(SourceSyncDocument document)
        throws XWikiException, FilterException, IOException, ComponentLookupException
    {
        XWikiContext xcontext = this.xcontextProvider.get();

        XWikiDocument xdocument = xcontext.getWiki().getDocument(document.getReference(), xcontext);

        exportDocument(xdocument, document);
    }

    public void exportDocument(XWikiDocument document) throws FilterException, IOException, ComponentLookupException
    {
        Collection<XarInstalledExtension> extensions =
            getXarInstalledExtensionRepository().getXarInstalledExtensions(document.getDocumentReferenceWithLocale());

        if (extensions.isEmpty()) {
            return;
        }

        XarInstalledExtension extension = extensions.iterator().next();

        SourceSyncExtension sourceExtension = this.index.getExtension(extension.getId());
        SourceSyncDocument sourceDocument =
            sourceExtension.getDocument(document.getDocumentReferenceWithLocale().getLocalDocumentReference());

        exportDocument(document, sourceDocument);
    }

    public void exportDocument(XWikiDocument document, SourceSyncDocument sourceDocument)
        throws FilterException, IOException, ComponentLookupException
    {
        // Input
        DocumentInstanceInputProperties documentProperties = new DocumentInstanceInputProperties();
        documentProperties.setWithJRCSRevisions(false);
        documentProperties.setWithRevisions(false);

        // Cleanup
        XWikiDocument cleanDocument = document.clone();
        cleanDocument.setVersion("1.1");
        cleanDocument.setAuthorReference(new DocumentReference("xwiki", "XWiki", "Admin"));
        cleanDocument.setContentAuthorReference(cleanDocument.getAuthorReference());

        OutputStreamOutputTarget target =
            new DefaultOutputStreamOutputTarget(Files.newOutputStream(sourceDocument.getPath()));

        this.documentUtils.exportEntity(cleanDocument, target, documentProperties);
    }

    // Async

    private Job execute(SourceSyncRequest request) throws JobException
    {
        return this.executor.execute(SourceSyncExportJob.JOBTYPE, request);
    }

    public Job startExport() throws JobException
    {
        SourceSyncRequest request = new SourceSyncRequest();

        request.addExtensions(this.index.getExtensions());

        return execute(request);
    }

    public Job startExportExtension(ExtensionId extensionId) throws JobException
    {
        SourceSyncExtension sourceSyncExtension = this.index.getExtension(extensionId);

        SourceSyncRequest request = new SourceSyncRequest();

        request.addExtension(sourceSyncExtension);

        return execute(request);
    }

}
