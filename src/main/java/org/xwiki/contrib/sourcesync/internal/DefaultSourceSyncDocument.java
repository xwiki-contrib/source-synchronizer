package org.xwiki.contrib.sourcesync.internal;

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
    private final Date date;
    private final Date pathDate;

    DefaultSourceSyncDocument(LocalDocumentReference reference, Date date, Date pathDate)
    {
        this.reference = reference;
        this.date = date;
        this.pathDate = pathDate;
    }

    @Override
    public LocalDocumentReference getReference()
    {
        return null;
    }

    @Override
    public Date getDocumentDate()
    {
        return null;
    }

    @Override
    public Date getPathDate()
    {
        return null;
    }
}
