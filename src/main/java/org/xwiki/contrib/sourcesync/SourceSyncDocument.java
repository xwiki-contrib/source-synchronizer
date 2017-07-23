package org.xwiki.contrib.sourcesync;

import java.util.Date;

import org.xwiki.model.reference.LocalDocumentReference;

/**
 * Indexed information about the source of a document.
 * 
 * @version $Id$
 */
public interface SourceSyncDocument
{
    /**
     * @return the local reference of the document
     */
    LocalDocumentReference getReference();

    /**
     * @return the date of the document (inside the file)
     */
    Date getDocumentDate();

    /**
     * @return the date of the file
     */
    Date getPathDate();
}
