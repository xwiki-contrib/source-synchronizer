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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.contrib.sourcesync.SourceSyncDocument;
import org.xwiki.contrib.sourcesync.SourceSyncExtension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.model.reference.LocalDocumentReference;

/**
 * Default implementation for {@link SourceSyncDocument}.
 *
 * @version $Id$
 */
class DefaultSourceSyncExtension implements SourceSyncExtension
{
    private final ExtensionId extensionId;

    private final Path path;

    private Date descriptorDate;

    private final Map<LocalDocumentReference, SourceSyncDocument> documents = new ConcurrentHashMap<>();

    DefaultSourceSyncExtension(ExtensionId extensionId, Path path, Date date)
    {
        this.extensionId = extensionId;
        this.path = path;
        this.descriptorDate = date;
    }

    DefaultSourceSyncExtension(ExtensionId extensionId, Path path, long date)
    {
        this(extensionId, path, new Date(date));
    }

    void addDocument(SourceSyncDocument document)
    {
        this.documents.put(document.getReference(), document);
    }

    void removeDocument(LocalDocumentReference reference)
    {
        this.documents.remove(reference);
    }

    @Override
    public ExtensionId getExtensionId()
    {
        return this.extensionId;
    }

    @Override
    public Path getPath()
    {
        return this.path;
    }

    @Override
    public Date getDescriptorDate()
    {
        return this.descriptorDate;
    }

    @Override
    public SourceSyncDocument getDocument(LocalDocumentReference documentReference)
    {
        return this.documents.get(documentReference);
    }

    @Override
    public Collection<SourceSyncDocument> getDocuments()
    {
        return this.documents.values();
    }
}
