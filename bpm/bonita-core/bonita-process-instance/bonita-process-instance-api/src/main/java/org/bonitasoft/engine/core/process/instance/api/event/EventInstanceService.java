/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.core.process.instance.api.event;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface EventInstanceService extends FlowNodeInstanceService {

    String EVENT_INSTANCE = "EVENT_INSTANCE";

    String EVENT_TRIGGER_INSTANCE = "EVENT_TRIGGER_INSTANCE";

    String EVENT_VISIBILITY_MAPPING = "EVENT_VISIBILITY_MAPPING";

    String MESSAGE_INSTANCE = "MESSAGE_INSTANCE";

    void createEventInstance(SEventInstance eventInstance) throws SEventInstanceCreationException;

    void createEventTriggerInstance(SEventTriggerInstance eventTriggerInstance) throws SEventTriggerInstanceCreationException;

    void createMessageInstance(SMessageInstance messageInstance) throws SMessageInstanceCreationException;

    void createWaitingEvent(SWaitingEvent waitingEvent) throws SWaitingEventCreationException;

    SWaitingErrorEvent getBoundaryWaitingErrorEvent(long relatedActivityInstanceId, String errorCode) throws SWaitingEventReadException;

    SEventInstance getEventInstance(long eventInstanceId) throws SEventInstanceNotFoundException, SEventInstanceReadException;

    List<SEventInstance> getEventInstances(long rootContainerId, int fromIndex, int maxResults, String fieldName, OrderByType orderByType)
            throws SEventInstanceReadException;

    List<SBoundaryEventInstance> getActivityBoundaryEventInstances(long activityInstanceId) throws SEventInstanceReadException;

    SEventTriggerInstance getEventTriggerInstance(long eventTriggerInstanceId) throws SEventTriggerInstanceNotFoundException,
            SEventTriggerInstanceReadException;

    List<SEventTriggerInstance> getEventTriggerInstances(long eventInstanceId, int fromIndex, int maxResults, String fieldName, OrderByType orderByType)
            throws SEventTriggerInstanceReadException;

    List<SEventTriggerInstance> getEventTriggerInstances(long eventInstanceId) throws SEventTriggerInstanceReadException;

    List<SMessageInstance> getThrownMessages(String messageName, String targetProcess, String targetFlowNode) throws SEventTriggerInstanceReadException;

    List<SWaitingMessageEvent> getWaitingMessages(String messageName, String processName, String flowNodeName) throws SEventTriggerInstanceReadException;

    void deleteMessageInstance(SMessageInstance messageInstance) throws SMessageModificationException;

    void deleteWaitingEvent(SWaitingEvent waitingEvent) throws SWaitingEventModificationException;

    List<SWaitingSignalEvent> getWaitingSignalEvents(String signalName) throws SEventTriggerInstanceReadException;

    List<SWaitingEvent> getStartWaitingEvents(long processDefinitionId) throws SEventTriggerInstanceReadException;

    List<SMessageEventCouple> getMessageEventCouples() throws SEventTriggerInstanceReadException, SMessageModificationException,
            SWaitingEventModificationException;

    SWaitingMessageEvent getWaitingMessage(long waitingMessageId) throws SWaitingEventNotFoundException, SWaitingEventReadException;

    SMessageInstance getMessageInstance(long messageInstanceId) throws SMessageInstanceNotFoundException, SMessageInstanceReadException;

    void updateWaitingMessage(SWaitingMessageEvent waitingMessageEvent, EntityUpdateDescriptor descriptor) throws SWaitingEventModificationException;

    void updateMessageInstance(SMessageInstance messageInstance, EntityUpdateDescriptor descriptor) throws SMessageModificationException;

    <T extends SWaitingEvent> List<T> searchWaitingEvents(Class<T> entityClass, QueryOptions searchOptions) throws SBonitaSearchException;

    long getNumberOfWaitingEvents(Class<? extends SWaitingEvent> entityClass, QueryOptions countOptions) throws SBonitaSearchException;

    <T extends SEventTriggerInstance> List<T> searchEventTriggerInstances(Class<T> entityClass, QueryOptions searchOptions) throws SBonitaSearchException;

    long getNumberOfEventTriggerInstances(Class<? extends SEventTriggerInstance> entityClass, QueryOptions countOptions) throws SBonitaSearchException;

    SWaitingEvent getWaitingEvent(Long waintingEventId) throws SWaitingEventNotFoundException, SWaitingEventReadException;

    /**
     * @param eventInstanceId
     * @throws SEventTriggerInstanceReadException
     * @throws SEventTriggerInstanceDeletionException
     * @since 6.1
     */
    void deleteEventTriggerInstances(long eventInstanceId) throws SEventTriggerInstanceReadException, SEventTriggerInstanceDeletionException;

    /**
     * @param eventTriggerInstance
     * @throws SEventTriggerInstanceDeletionException
     * @since 6.1
     */
    void deleteEventTriggerInstance(SEventTriggerInstance eventTriggerInstance) throws SEventTriggerInstanceDeletionException;

    /**
     * @param flowNodeInstance
     * @throws SWaitingEventModificationException
     * @throws SFlowNodeReadException
     * @since 6.1
     */
    void deleteWaitingEvents(SFlowNodeInstance flowNodeInstance) throws SWaitingEventModificationException, SFlowNodeReadException;

    /**
     * Get the list of all {@link SMessageInstance}s that are "In Progress", that is, that a work is running or should be running.
     * 
     * @return the list of all matching {@link SMessageInstance}s
     * @throws SMessageInstanceReadException
     *             if a read error occurs.
     */
    public List<SMessageInstance> getInProgressMessageInstances() throws SMessageInstanceReadException;

    /**
     * Get the list of all {@link WaitingEvent}s that are "In Progress", that is, that a work is running or should be running.
     * 
     * @return the list of all matching {@link WaitingEvent}s
     * @throws SWaitingEventReadException
     *             if a read error occurs.
     */
    public List<SWaitingMessageEvent> getInProgressWaitingMessageEvents() throws SWaitingEventReadException;
}
