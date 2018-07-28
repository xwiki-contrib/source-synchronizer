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
     * @return the path of the actual source
     */
    Path getPath();

    /**
     * @return the date of the document (inside the file)
     */
    Date getDocumentDate();

    /**
     * @return the date of the file
     */
    Date getPathDate();

    /**
     * Force update the entry (remove it when it does not exist in sources anymore, update the dates, etc.).
     */
    void update();
}
