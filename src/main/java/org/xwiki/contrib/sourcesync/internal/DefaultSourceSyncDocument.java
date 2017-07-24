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
