package org.xwiki.contrib.sourcesync.internal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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

    private void indexPath(Path path) throws IOException
    {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                //Files.newBufferedReader(file, Charset.defaultCharset());
                if (file.getFileName().toString().endsWith(".pom")) {
                    return processPomFile(file, attrs);
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }
        });
    }

    private FileVisitResult processPomFile(Path file, BasicFileAttributes attrs) throws IOException
    {
        return FileVisitResult.CONTINUE;
    }
}
