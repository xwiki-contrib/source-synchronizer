package org.xwiki.contrib.sourcesync;

/**
 * Please comment here
 *
 * @version $Id$
 */
public class SourceSyncDocumentCreatedEvent extends AbstractSourceSyncDocumentEvent
{
    public SourceSyncDocumentCreatedEvent()
    {
    }

    public SourceSyncDocumentCreatedEvent(SourceSyncDocument document)
    {
        setDocument(document);
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof SourceSyncDocumentCreatedEvent;
    }
}
