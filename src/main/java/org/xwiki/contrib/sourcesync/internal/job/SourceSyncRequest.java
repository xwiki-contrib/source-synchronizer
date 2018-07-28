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
package org.xwiki.contrib.sourcesync.internal.job;

import java.util.Collection;

import org.xwiki.contrib.sourcesync.SourceSyncDocument;
import org.xwiki.contrib.sourcesync.SourceSyncExtension;
import org.xwiki.job.AbstractRequest;

public class SourceSyncRequest extends AbstractRequest
{
    /**
     * @see #getExtensions()
     */
    public static final String PROPERTY_EXTENSIONS = "sourcesync.extensions";

    /**
     * @see #getDocuments()
     */
    public static final String PROPERTY_DOCUMENTS = "sourcesync.documents";

    /**
     * @see #isForce()
     */
    public static final String PROPERTY_FORCE = "sourcesync.force";

    public Collection<SourceSyncExtension> getExtensions()
    {
        return getProperty(PROPERTY_EXTENSIONS);
    }

    public void addExtension(SourceSyncExtension extension)
    {
        getExtensions().add(extension);
    }

    public void addExtensions(Collection<SourceSyncExtension> extensions)
    {
        getExtensions().addAll(extensions);
    }

    public Collection<SourceSyncDocument> getDocuments()
    {
        return getProperty(PROPERTY_DOCUMENTS);
    }

    public void addDocument(SourceSyncDocument document)
    {
        getDocuments().add(document);
    }

    public void addDocuments(Collection<SourceSyncDocument> documents)
    {
        getDocuments().addAll(documents);
    }

    public boolean isForce()
    {
        return getProperty(PROPERTY_FORCE, true);
    }

    public void setForce(boolean force)
    {
        setProperty(PROPERTY_FORCE, force);
    }
}
