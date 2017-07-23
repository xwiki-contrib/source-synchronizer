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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.event.model.WikiDocumentFilter;
import org.xwiki.filter.input.InputFilterStreamFactory;
import org.xwiki.filter.instance.input.DocumentInstanceInputProperties;
import org.xwiki.filter.instance.input.EntityEventGenerator;
import org.xwiki.filter.output.BeanOutputFilterStream;
import org.xwiki.filter.output.BeanOutputFilterStreamFactory;
import org.xwiki.filter.output.OutputFilterStreamFactory;
import org.xwiki.filter.output.StringWriterOutputTarget;
import org.xwiki.filter.output.WriterOutputTarget;
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
    private Provider<XWikiContext> xcontextProvider;

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
        WriterOutputTarget target = new StringWriterOutputTarget();

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
        BeanOutputFilterStream<XAROutputProperties> xarFilter =
            ((BeanOutputFilterStreamFactory<XAROutputProperties>) this.xarOutputFilterStreamFactory)
                .createOutputFilterStream(xarProperties);
        XARFilter filter = (XARFilter) xarFilter.getFilter();

        FilterEventParameters documentParameters = null;
        DocumentReference documentReference = null;
        documentReference = document.getDocumentReference();
        for (SpaceReference spaceReference : documentReference.getSpaceReferences()) {
            filter.beginWikiSpace(spaceReference.getName(), FilterEventParameters.EMPTY);
        }

        documentParameters = new FilterEventParameters();
        documentParameters.put(WikiDocumentFilter.PARAMETER_LOCALE, document.getDefaultLocale());
        filter.beginWikiDocument(documentReference.getName(), documentParameters);

        // Document Locale events
        this.documentSerializer.write(document, xarFilter, documentProperties);

        // Document and spaces events
        filter.endWikiDocument(documentReference.getName(), documentParameters);

        documentReference = document.getDocumentReference();
        for (EntityReference reference = documentReference.getParent(); reference instanceof SpaceReference; reference =
            reference.getParent()) {
            filter.beginWikiSpace(reference.getName(), FilterEventParameters.EMPTY);
        }

        xarFilter.close();
    }
}
