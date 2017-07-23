package org.xwiki.contrib.sourcesync.internal;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private Date date;

    private final Map<LocalDocumentReference, SourceSyncDocument> documents = new HashMap<>();

    DefaultSourceSyncExtension(ExtensionId extensionId, Path path, Date date)
    {
        this.extensionId = extensionId;
        this.path = path;
        this.date = date;
    }

    void addDocument(SourceSyncDocument document)
    {
        documents.put(document.getReference(), document);
    }

    void removeDocument(LocalDocumentReference reference)
    {
        documents.remove(reference);
    }

    @Override
    public ExtensionId getExtensionId()
    {
        return extensionId;
    }

    @Override
    public Path getPath()
    {
        return path;
    }

    @Override
    public Date getDescriptorDate()
    {
        return date;
    }

    @Override
    public SourceSyncDocument getDocument(LocalDocumentReference documentReference)
    {
        return documents.get(documentReference);
    }

    @Override
    public Collection<SourceSyncDocument> getDocuments()
    {
        return documents.values();
    }
}
