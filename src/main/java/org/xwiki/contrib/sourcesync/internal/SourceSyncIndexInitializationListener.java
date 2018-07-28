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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.event.ApplicationReadyEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * Listener to initialize the SourceSyncIndexUpdate.
 *
 * @version $Id$
 */
@Component
@Named(SourceSyncIndexInitializationListener.LISTENER_NAME)
@Singleton
public class SourceSyncIndexInitializationListener implements EventListener, Disposable
{
    /**
     * The name of the listener.
     */
    public static final String LISTENER_NAME = "SourceSyncIndexInitializationListener";

    /**
     * The events observed by this event listener.
     */
    private static final List<Event> EVENTS = new ArrayList<>(Collections.singletonList(new ApplicationReadyEvent()));

    /**
     * Logger to use to log shutdown information (opposite of initialization).
     */
    private static final Logger SHUTDOWN_LOGGER = LoggerFactory.getLogger("org.xwiki.shutdown");

    @Inject
    private DefaultSourceSyncIndexMonitor sourceSyncIndexUpdater;

    private Thread sourceSyncIndexUpdaterThread;

    @Override
    public String getName()
    {
        return LISTENER_NAME;
    }

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public void onEvent(Event event, Object o, Object o1)
    {
        this.sourceSyncIndexUpdaterThread = new Thread(this.sourceSyncIndexUpdater);
        this.sourceSyncIndexUpdaterThread.setName("Source Sync Index Updater Thread");
        this.sourceSyncIndexUpdaterThread.setDaemon(true);
        this.sourceSyncIndexUpdaterThread.start();
    }

    /**
     * Stops threads. Should be called when the application is stopped for a clean shutdown.
     *
     * @throws InterruptedException if a thread fails to be stopped
     */
    private void stopThreads() throws InterruptedException
    {
        // Step 1: Stop the Mail Sender Thread

        if (this.sourceSyncIndexUpdaterThread != null) {
            this.sourceSyncIndexUpdater.stopProcessing();
            // Make sure the Thread goes out of sleep if it's sleeping so that it stops immediately.
            this.sourceSyncIndexUpdaterThread.interrupt();
            // Wait till the thread goes away
            this.sourceSyncIndexUpdaterThread.join();
            SHUTDOWN_LOGGER.debug("Source Sync Index Updater has been stopped");
        }
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        try {
            stopThreads();
        } catch (InterruptedException e) {
            SHUTDOWN_LOGGER.debug("Source Sync Index Updater has been interrupted", e);
        }
    }
}
