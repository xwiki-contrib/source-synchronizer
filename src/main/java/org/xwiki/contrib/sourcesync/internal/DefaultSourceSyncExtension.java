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
        return descriptorDate;
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
