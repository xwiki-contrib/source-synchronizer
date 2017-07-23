package org.xwiki.contrib.sourcesync;

public class SourceSyncDocumentUpdatedEvent extends AbstractSourceSyncDocumentEvent
{
    public SourceSyncDocumentUpdatedEvent()
    {
    }

    public SourceSyncDocumentUpdatedEvent(SourceSyncDocument document)
    {
        setDocument(document);
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof SourceSyncDocumentUpdatedEvent;
    }
}
