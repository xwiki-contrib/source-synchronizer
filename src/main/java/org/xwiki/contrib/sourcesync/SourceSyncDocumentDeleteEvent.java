package org.xwiki.contrib.sourcesync;

/**
 * Please comment here
 *
 * @version $Id$
 */
public class SourceSyncDocumentDeleteEvent extends AbstractSourceSyncDocumentEvent
{
    public SourceSyncDocumentDeleteEvent()
    {
    }

    public SourceSyncDocumentDeleteEvent(SourceSyncDocument document)
    {
        setDocument(document);
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof SourceSyncDocumentDeleteEvent;
    }
}
