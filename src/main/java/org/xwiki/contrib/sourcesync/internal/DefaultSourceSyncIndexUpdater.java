package org.xwiki.contrib.sourcesync.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.contrib.sourcesync.SourceSyncIndex;

/**
 * Updater job to maintain the {@link org.xwiki.contrib.sourcesync.SourceSyncIndex} up to date.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultSourceSyncIndexUpdater implements Runnable, SourceSyncIndexUpdater
{
    @Inject
    private Logger logger;

    @Inject
    private ExecutionContextManager executionContextManager;

    @Inject
    private SourceSyncIndex sourceSyncIndex;

    private volatile boolean shouldStop;

    private final Collection<Path> rootFolders = new ArrayList<>();

    @Override
    public void stopProcessing()
    {
        this.shouldStop = true;
    }

    @Override
    public void addFolder(Path path)
    {
        rootFolders.add(path);
    }

    @Override
    public void removeFolder(Path path)
    {
        rootFolders.remove(path);
    }

    private void prepareContext() throws ExecutionContextException
    {
        // Create a single execution context and use it for this thread.
        ExecutionContext ec = new ExecutionContext();
        this.executionContextManager.initialize(ec);
    }

    public void run()
    {
        try {
            // Make sure we initialize an execution context.
            prepareContext();

            runInternal();
        } catch (ExecutionContextException e) {
            // Not much to do but log.
            logger.error("Failed to initialize the Source Sync Index Updater thread's execution context", e);
        }
    }

    private void runInternal()
    {
        do {
            try {
                for (Path path : rootFolders) {
                    indexPath(path);
                }
                // Pause to be kind on on resource.
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                // Thread has been stopped, exit
                this.logger.debug("Source Sync Index Updater Thread was forcefully stopped", e);
                break;
            } catch (Exception e) {
                // There was an unexpected problem, we just log the problem but keep the thread alive!
                this.logger.error("Unexpected error in the Source Sync Index Updater", e);
            }
        } while (!this.shouldStop);
    }

    private void indexPath(Path path)
    {

    }
}
