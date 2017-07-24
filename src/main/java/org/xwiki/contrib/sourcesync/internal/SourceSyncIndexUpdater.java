package org.xwiki.contrib.sourcesync.internal;

import java.nio.file.Path;

import org.xwiki.component.annotation.Role;

/**
 * Source Sync Index Updater Job.
 *
 * @version $Id$
 */
@Role
public interface SourceSyncIndexUpdater
{
    void stopProcessing();

    void addFolder(Path path);

    void removeFolder(Path path);
}
