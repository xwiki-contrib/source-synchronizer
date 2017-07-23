package org.xwiki.contrib.sourcesync;

import org.xwiki.observation.event.Event;

/**
 * Abstract class for SourceSyncDocument events.
 *
 * @version $Id$
 */
public abstract class AbstractSourceSyncDocumentEvent implements Event
{
    private SourceSyncDocument document;

    public SourceSyncDocument getDocument()
    {
        return document;
    }

    void setDocument(SourceSyncDocument document) {
        this.document = document;
    }
}
