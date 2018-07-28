/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.sourcesync;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;

import org.xwiki.extension.ExtensionId;
import org.xwiki.model.reference.LocalDocumentReference;

/**
 * An entry in the index of extension/source couples. Contains informations found during the last indexing.
 * 
 * @version $Id$
 */
public interface SourceSyncExtension
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
    SourceSyncDocument getDocument(LocalDocumentReference documentReference);

    /**
     * @return the documents found in that source
     */
    Collection<SourceSyncDocument> getDocuments();
}
