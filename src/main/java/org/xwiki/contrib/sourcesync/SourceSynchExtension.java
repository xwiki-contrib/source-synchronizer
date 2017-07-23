package org.xwiki.contrib.sourcesync;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.xwiki.extension.ExtensionId;
import org.xwiki.model.reference.LocalDocumentReference;

/**
 * An entry in the index of extension/source couples. Contains informations found during the last indexing.
 * 
 * @version $Id$
 */
public interface SourceSynchExtension
{
    /**
     * @return the id of the matching extension
     */
    ExtensionId getExtensionId();

    /**
     * @return the root path of the extension sources
     */
    Path getPath();

    /**
     * @return the date of the descriptor file in the sources (usually the pom.xml)
     */
    Date getDescriptorDate();

    /**
     * @param documentReference the local reference of the document (including the locale for a translation)
     * @return the indexed information related to the sources of that document
     */
    SourceSynchDocument getDocument(LocalDocumentReference documentReference);

    /**
     * @return the documents found in that source
     */
    List<SourceSynchDocument> getDocuments();
}