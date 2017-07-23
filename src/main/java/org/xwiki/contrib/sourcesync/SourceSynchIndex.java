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

import java.util.Collection;

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.ExtensionId;

@Role
public interface SourceSynchIndex
{
    /**
     * @return extensions found in configured sources
     */
    Collection<SourceSynchExtension> getExtensions();

    /**
     * @param extensionId the identifier of the extension
     * @return the entry containing information about the extension/source or null if this entry does not exist anymore
     *         (the extension does not match the source anymore)
     */
    SourceSynchExtension getExtension(ExtensionId extensionId);

    /**
     * Force refreshing the index entry and return the new one (or the same if the descriptor did not changed).
     * 
     * @param entry the current entry
     * @return the new entry (or the passed one if nothing changed), null if it does not match the extension id anymore
     */
    SourceSynchExtension refreshExtension(SourceSynchExtension entry);
}
