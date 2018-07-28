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
package org.xwiki.contrib.sourcesync.internal.importer;

import java.io.IOException;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.contrib.sourcesync.SourceSyncDocument;
import org.xwiki.contrib.sourcesync.SourceSyncExtension;
import org.xwiki.contrib.sourcesync.SourceSyncIndex;
import org.xwiki.contrib.sourcesync.internal.exporter.SourceSyncExportJob;
import org.xwiki.contrib.sourcesync.internal.job.SourceSyncRequest;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.DefaultInputStreamInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.instance.output.DocumentInstanceOutputProperties;
import org.xwiki.filter.xar.input.XARInputProperties;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.internal.filter.XWikiDocumentFilterUtils;

@Component(roles = SourceSyncImporter.class)
@Singleton
public class SourceSyncImporter
{
    @Inject
    private SourceSyncIndex index;

    @Inject
    private JobExecutor executor;

    @Inject
    private XWikiDocumentFilterUtils documentUtils;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    public void importExtension(SourceSyncExtension extension, boolean force)
    {

    }

    public void importDocument(SourceSyncDocument document)
        throws FilterException, IOException, ComponentLookupException
    {
        // XAR input properties
        XARInputProperties xarProperties = new XARInputProperties();
        xarProperties.setVerbose(true);

        // Source output properties
        DocumentInstanceOutputProperties instanceProperties = new DocumentInstanceOutputProperties();
        instanceProperties.setSaveComment("Imported from sources");
        instanceProperties.setPreviousDeleted(false);
        instanceProperties.setVersionPreserved(false);
        instanceProperties.setAuthorPreserved(true);
        instanceProperties.setVerbose(true);

        InputSource source = new DefaultInputStreamInputSource(Files.newInputStream(document.getPath()), true);

        this.documentUtils.importDocument(source, xarProperties, instanceProperties);
    }

    // Async

    private Job execute(SourceSyncRequest request) throws JobException
    {
        return this.executor.execute(SourceSyncExportJob.JOBTYPE, request);
    }

    public Job startImport() throws JobException
    {
        SourceSyncRequest request = new SourceSyncRequest();

        request.addExtensions(this.index.getExtensions());

        return execute(request);
    }
}
