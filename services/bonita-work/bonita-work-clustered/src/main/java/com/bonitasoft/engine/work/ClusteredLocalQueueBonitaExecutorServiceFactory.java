/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.work;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.BonitaExecutorService;
import org.bonitasoft.engine.work.BonitaExecutorServiceFactory;
import org.bonitasoft.engine.work.WorkerThreadFactory;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;

/**
 * Factory that use a hazelcast executor
 * 
 * @author Baptiste Mesta
 * @author Laurent Vaills
 */
public class ClusteredLocalQueueBonitaExecutorServiceFactory implements BonitaExecutorServiceFactory {

    private final HazelcastInstance hazelcastInstance;

    private final int corePoolSize;

    private final int maximumPoolSize;

    private final long keepAliveTimeSeconds;

    private final TechnicalLoggerService logger;

    private final long tenantId;

    public ClusteredLocalQueueBonitaExecutorServiceFactory(final TechnicalLoggerService logger, final long tenantId, final int corePoolSize,
            final int maximumPoolSize, final long keepAliveTimeSeconds, final HazelcastInstance hazelcastInstance) {
        this.logger = logger;
        this.tenantId = tenantId;
        this.hazelcastInstance = hazelcastInstance;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
    }

    @Override
    public BonitaExecutorService createExecutorService() {
        final BlockingQueue<Runnable> queue = createWorkQueue(hazelcastInstance, tenantId);
        final Cluster cluster = hazelcastInstance.getCluster();
        final BlockingQueue<Runnable> executingRunnable = createExecutingWorkQueue(hazelcastInstance, cluster);
        final RejectedExecutionHandler handler = new QueueRejectedExecutionHandler(executingRunnable);
        final WorkerThreadFactory threadFactory = new WorkerThreadFactory("Bonita-Worker", tenantId, maximumPoolSize);
        return new ClusteredThreadPoolExecutorLocalQueue(corePoolSize, maximumPoolSize, keepAliveTimeSeconds,
                TimeUnit.SECONDS, threadFactory, handler, hazelcastInstance, queue, executingRunnable, logger, tenantId);
    }

    private BlockingQueue<Runnable> createExecutingWorkQueue(final HazelcastInstance hazelcastInstance, final Cluster cluster) {
        return new DelegatingQueue(hazelcastInstance.<Runnable> getQueue(ClusteredThreadPoolExecutorLocalQueue.memberExecutingWorkQueueName(
                cluster.getLocalMember(),
                tenantId)));
    }

    private static BlockingQueue<Runnable> createWorkQueue(final HazelcastInstance hazelcastInstance, final long tenantId) {
        final Cluster cluster = hazelcastInstance.getCluster();
        BlockingQueue<Runnable> queue = new DelegatingQueue(hazelcastInstance.<Runnable> getQueue(ClusteredThreadPoolExecutorLocalQueue.memberWorkQueueName(
                cluster.getLocalMember(),
                tenantId)));
        return queue;
    }

    private final class QueueRejectedExecutionHandler implements RejectedExecutionHandler {

        private final BlockingQueue<Runnable> queue;

        public QueueRejectedExecutionHandler(final BlockingQueue<Runnable> queue) {
            this.queue = queue;
        }

        @Override
        public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                logger.log(getClass(), TechnicalLogSeverity.WARNING, "Tried to run work " + task
                        + " but the work service is shutdown. readded it to the queue so other nodes can take it");
                // add to executing jobs queue
                queue.add(task);
            } else {
                throw new RejectedExecutionException(
                        "Unable to run the task "
                                + task
                                + ".\n Your work queue is full, you might consider changing your configuration to scale more. See parameter 'queueCapacity' in bonita.home configuration files.");
            }
        }

    }

}