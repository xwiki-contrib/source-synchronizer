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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.sourcesync.SourceSyncDocument;
import org.xwiki.contrib.sourcesync.SourceSyncExtension;
import org.xwiki.contrib.sourcesync.internal.job.SourceSyncRequest;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.event.status.JobStatus;

@Component
@Named(SourceSyncExportJob.JOBTYPE)
@Singleton
public class SourceSyncExportJob extends AbstractJob<SourceSyncRequest, JobStatus>
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "sourcesync.export";

    private SourceSyncExporter exporter;

    @Override
    public String getType()
    {
        return JOBTYPE;
    }

    @Override
    protected void runInternal() throws Exception
    {
        this.progressManager.pushLevelProgress(getRequest().getExtensions().size() + getRequest().getDocuments().size(),
            this);

        try {
            // Extensions
            for (SourceSyncExtension extension : getRequest().getExtensions()) {
                this.progressManager.startStep(this, extension.getExtensionId().toString());

                this.exporter.exportExtension(extension, getRequest().isForce());
            }

            // Documents
            for (SourceSyncDocument document : getRequest().getDocuments()) {
                this.progressManager.startStep(this, document.getReference().toString());

                this.exporter.exportDocument(document);
            }
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }
}
