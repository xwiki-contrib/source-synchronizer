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
import java.util.Date;

import org.xwiki.contrib.sourcesync.SourceSyncDocument;
import org.xwiki.model.reference.LocalDocumentReference;

/**
 * Default implementation of {@link SourceSyncDocument}.
 *
 * @version $Id$
 */
public class DefaultSourceSyncDocument implements SourceSyncDocument
{
    private final LocalDocumentReference reference;

    private final Path path;

    private final Date documentDate;

    private final Date pathDate;

    DefaultSourceSyncDocument(LocalDocumentReference reference, Path path, Date date, Date pathDate)
    {
        this.reference = reference;
        this.path = path;
        this.documentDate = date;
        this.pathDate = pathDate;
    }

    @Override
    public LocalDocumentReference getReference()
    {
        return this.reference;
    }

    @Override
    public Path getPath()
    {
        return this.path;
    }

    @Override
    public Date getDocumentDate()
    {
        return this.documentDate;
    }

    @Override
    public Date getPathDate()
    {
        return this.pathDate;
    }
}
