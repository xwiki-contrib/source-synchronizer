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
package org.xwiki.contrib.sourcesync.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.sourcesync.SourceSyncExtension;
import org.xwiki.contrib.sourcesync.SourceSyncIndex;
import org.xwiki.extension.ExtensionId;

@Component
@Singleton
public class DefaultSourceSyncIndex implements SourceSyncIndex
{
    private final Map<ExtensionId, SourceSyncExtension> extensions = new HashMap<>();

    void addExtension(SourceSyncExtension extension)
    {
        extensions.put(extension.getExtensionId(), extension);
    }

    void removeExtension(ExtensionId extensionId)
    {
        extensions.remove(extensionId);
    }


    @Override
    public Collection<SourceSyncExtension> getExtensions()
    {
        return extensions.values();
    }

    @Override
    public SourceSyncExtension getExtension(ExtensionId extensionId)
    {
        return extensions.get(extensionId);
    }

    @Override
    public SourceSyncExtension refreshExtension(SourceSyncExtension entry)
    {
        // TODO: refresh the extention
        return extensions.get(entry.getExtensionId());
    }
}
