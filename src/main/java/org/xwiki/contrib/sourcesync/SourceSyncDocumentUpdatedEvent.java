package org.xwiki.contrib.sourcesync;

import org.xwiki.observation.event.Event;

public class SourceSyncDocumentUpdatedEvent implements Event
{
    private SourceSynchDocument document;

    public SourceSyncDocumentUpdatedEvent()
    {

    }

    public SourceSyncDocumentUpdatedEvent(SourceSynchDocument document)
    {
        this.document = document;
    }

    public SourceSynchDocument getDocument()
    {
        return document;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        // TODO: more filtering

        return otherEvent instanceof SourceSyncDocumentUpdatedEvent;
    }
}
