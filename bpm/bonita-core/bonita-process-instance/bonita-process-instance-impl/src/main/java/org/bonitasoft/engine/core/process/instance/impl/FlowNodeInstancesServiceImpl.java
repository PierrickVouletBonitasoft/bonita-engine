/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceLogBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;

/**
 * @author Elias Ricken de Medeiros
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
public abstract class FlowNodeInstancesServiceImpl implements FlowNodeInstanceService {

    private static final String SUPERVISED_BY = "SupervisedBy";

    private final SUserTaskInstanceBuilderFactory activityInstanceKeyProvider;

    private final EventService eventService;

    private final Recorder recorder;

    private final PersistenceService persistenceService;

    private final TechnicalLoggerService logger;

    public FlowNodeInstancesServiceImpl(final Recorder recorder, final PersistenceService persistenceService, final EventService eventService,
            final TechnicalLoggerService logger) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.logger = logger;
        activityInstanceKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        this.eventService = eventService;
    }

    protected <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    protected <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    protected SFlowNodeInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message, final SFlowElementInstance flowElementInstance) {
        final SFlowNodeInstanceLogBuilder logBuilder = BuilderFactory.get(SFlowNodeInstanceLogBuilderFactory.class).createNewInstance();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.processInstanceId(flowElementInstance.getRootContainerId());
        return logBuilder;
    }

    @Override
    public void setState(final SFlowNodeInstance flowNodeInstance, final FlowNodeState state) throws SFlowNodeModificationException {
        final long now = System.currentTimeMillis();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getPreviousStateIdKey(), flowNodeInstance.getStateId());
        descriptor.addField(activityInstanceKeyProvider.getStateIdKey(), state.getId());
        descriptor.addField(activityInstanceKeyProvider.getStateNameKey(), state.getName());
        descriptor.addField(activityInstanceKeyProvider.getStableKey(), state.isStable());
        descriptor.addField(activityInstanceKeyProvider.getTerminalKey(), state.isTerminal());
        descriptor.addField(activityInstanceKeyProvider.getReachStateDateKey(), now);
        descriptor.addField(activityInstanceKeyProvider.getLastUpdateDateKey(), now);
        descriptor.addField(activityInstanceKeyProvider.getStateExecutingKey(), false);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(
                    getClass(),
                    TechnicalLogSeverity.DEBUG,
                    MessageFormat.format("[{0} with id {1}] changed state {2}->{3}(new={4})", flowNodeInstance.getClass().getSimpleName(),
                            flowNodeInstance.getId(), flowNodeInstance.getStateId(), state.getId(), state.getClass().getSimpleName()));
        }

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(flowNodeInstance, descriptor);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ACTIVITYINSTANCE_STATE)
                .setObject(flowNodeInstance).done();
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SFlowNodeModificationException(e);
        }
    }

    @Override
    public void setExecuting(final SFlowNodeInstance flowNodeInstance) throws SFlowNodeModificationException {
        final long now = System.currentTimeMillis();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getStateExecutingKey(), true);
        descriptor.addField(activityInstanceKeyProvider.getLastUpdateDateKey(), now);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(
                    getClass(),
                    TechnicalLogSeverity.DEBUG,
                    MessageFormat.format("[{0} with id {1}] have executing flag set to true", flowNodeInstance.getClass().getSimpleName(),
                            flowNodeInstance.getId()));
        }

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(flowNodeInstance, descriptor);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ACTIVITYINSTANCE_STATE)
                .setObject(flowNodeInstance).done();
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SFlowNodeModificationException(e);
        }
    }

    @Override
    public void updateDisplayName(final SFlowNodeInstance flowNodeInstance, final String displayName) throws SFlowNodeModificationException {
        if (displayName != null && !displayName.equals(flowNodeInstance.getDisplayName())) {
            final String event = ACTIVITYINSTANCE_DISPLAY_NAME;
            final String key = activityInstanceKeyProvider.getDisplayNameKey();
            updateOneField(flowNodeInstance, displayName, event, key);
        }
    }

    private void updateOneField(final SFlowNodeInstance flowNodeInstance, final String displayDescription, final String event, final String lastUpdateKey)
            throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(lastUpdateKey, displayDescription);

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(flowNodeInstance, descriptor);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(event).setObject(flowNodeInstance)
                .done();
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SFlowNodeModificationException(e);
        }
    }

    @Override
    public void updateDisplayDescription(final SFlowNodeInstance flowNodeInstance, final String displayDescription) throws SFlowNodeModificationException {
        if (displayDescription != null && !displayDescription.equals(flowNodeInstance.getDisplayDescription())) {
            final String event = ACTIVITYINSTANCE_DISPLAY_DESCRIPTION;
            final String key = activityInstanceKeyProvider.getDisplayDescriptionKey();
            updateOneField(flowNodeInstance, displayDescription, event, key);
        }
    }

    @Override
    public void setTaskPriority(final SFlowNodeInstance flowNodeInstance, final STaskPriority priority) throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getPriorityKey(), priority);

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(flowNodeInstance, descriptor);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ACTIVITYINSTANCE_STATE)
                .setObject(flowNodeInstance).done();
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SFlowNodeModificationException(e);
        }
    }

    @Override
    public List<SFlowNodeInstance> getActiveFlowNodes(final long rootContainerId) throws SFlowNodeReadException {
        try {
            final SelectListDescriptor<SFlowNodeInstance> selectListDescriptor = SelectDescriptorBuilder.getActiveFlowNodes(rootContainerId);
            return persistenceService.selectList(selectListDescriptor);
        } catch (final SBonitaReadException bre) {
            throw new SFlowNodeReadException(bre);
        }
    }

    @Override
    public SFlowNodeInstance getFlowNodeInstance(final long flowNodeInstanceId) throws SFlowNodeNotFoundException, SFlowNodeReadException {
        SFlowNodeInstance selectOne;
        try {
            selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SFlowNodeInstance.class, "SFlowNodeInstance", flowNodeInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SFlowNodeReadException(e);
        }
        if (selectOne == null) {
            throw new SFlowNodeNotFoundException(flowNodeInstanceId);
        }
        return selectOne;
    }

    @Override
    public List<SFlowNodeInstance> getFlowNodeInstances(final long rootContainerId, final int fromIndex, final int maxResults) throws SFlowNodeReadException {
        List<SFlowNodeInstance> selectList;
        try {
            selectList = getPersistenceService().selectList(SelectDescriptorBuilder.getFlowNodesFromProcessInstance(rootContainerId, fromIndex, maxResults));
        } catch (final SBonitaReadException e) {
            throw new SFlowNodeReadException(e);
        }
        return getUnmodifiableList(selectList);
    }

    @Override
    public List<SAFlowNodeInstance> getArchivedFlowNodeInstances(final long rootContainerId, final int fromIndex, final int maxResults)
            throws SFlowNodeReadException {
        List<SAFlowNodeInstance> selectList;
        try {
            selectList = getPersistenceService().selectList(
                    SelectDescriptorBuilder.getArchivedFlowNodesFromProcessInstance(rootContainerId, fromIndex, maxResults));
        } catch (final SBonitaReadException e) {
            throw new SFlowNodeReadException(e);
        }
        return getUnmodifiableList(selectList);
    }

    @Override
    public SAFlowNodeInstance getArchivedFlowNodeInstance(final long archivedFlowNodeInstanceId, final ReadPersistenceService persistenceService)
            throws SFlowNodeReadException, SFlowNodeNotFoundException {
        SAFlowNodeInstance selectOne;
        try {
            selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SAFlowNodeInstance.class, "SArchivedFlowNodeInstance",
                    archivedFlowNodeInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SFlowNodeReadException(e);
        }
        if (selectOne == null) {
            throw new SFlowNodeNotFoundException(archivedFlowNodeInstanceId);
        }
        return selectOne;
    }

    @Override
    public void setStateCategory(final SFlowElementInstance flowElementInstance, final SStateCategory stateCategory) throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getStateCategoryKey(), stateCategory);

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(flowElementInstance, descriptor);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(STATE_CATEGORY)
                .setObject(flowElementInstance).done();
        try {
            getRecorder().recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException sre) {
            throw new SFlowNodeModificationException(sre);
        }

    }

    @Override
    public void setExecutedBy(final SFlowNodeInstance flowNodeInstance, final long userId) throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getExecutedBy(), userId);
        updateFlowNode(flowNodeInstance, EXECUTED_BY_MODIFIED, descriptor);
    }

    @Override
    public void setExecutedBySubstitute(final SFlowNodeInstance flowNodeInstance, final long executerSubstituteId) throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getExecutedBySubstitute(), executerSubstituteId);
        updateFlowNode(flowNodeInstance, EXECUTED_BY_SUBSTITUTE_MODIFIED, descriptor);
    }

    @Override
    public void setExpectedEndDate(final SFlowNodeInstance flowNodeInstance, final long dueDate) throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getExpectedEndDateKey(), dueDate);
        updateFlowNode(flowNodeInstance, EXPECTED_END_DATE_MODIFIED, descriptor);
    }

    protected void updateFlowNode(final SFlowNodeInstance flowNodeInstance, final String eventName, final EntityUpdateDescriptor descriptor)
            throws SFlowNodeModificationException {
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(flowNodeInstance, descriptor);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(eventName).setObject(flowNodeInstance)
                .done();
        try {
            getRecorder().recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException sre) {
            throw new SFlowNodeModificationException(sre);
        }
    }

    protected <T> List<T> getUnmodifiableList(List<T> selectList) {
        if (selectList == null) {
            selectList = new ArrayList<T>();
        }
        return Collections.unmodifiableList(selectList);
    }

    @Override
    public long getNumberOfFlowNodeInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions countOptions)
            throws SBonitaSearchException {
        try {
            return getPersistenceService().getNumberOfEntities(entityClass, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SFlowNodeInstance> searchFlowNodeInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions)
            throws SBonitaSearchException {
        try {
            return (List<SFlowNodeInstance>) getPersistenceService().searchEntity(entityClass, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfFlowNodeInstancesSupervisedBy(final Long supervisorId, final Class<? extends PersistentObject> entityClass,
            final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return getPersistenceService().getNumberOfEntities(entityClass, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SFlowNodeInstance> searchFlowNodeInstancesSupervisedBy(final Long supervisorId, final Class<? extends PersistentObject> entityClass,
            final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return (List<SFlowNodeInstance>) getPersistenceService().searchEntity(entityClass, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfArchivedFlowNodeInstances(final Class<? extends SAFlowNodeInstance> entityClass, final QueryOptions countOptions)
            throws SBonitaSearchException {
        try {
            return getPersistenceService().getNumberOfEntities(entityClass, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SAFlowNodeInstance> searchArchivedFlowNodeInstances(final Class<? extends SAFlowNodeInstance> entityClass, final QueryOptions searchOptions)
            throws SBonitaSearchException {
        try {
            return (List<SAFlowNodeInstance>) getPersistenceService().searchEntity(entityClass, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfArchivedFlowNodeInstancesSupervisedBy(final long supervisorId, final Class<? extends SAFlowNodeInstance> entityClass,
            final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return getPersistenceService().getNumberOfEntities(entityClass, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SAFlowNodeInstance> searchArchivedFlowNodeInstancesSupervisedBy(final long supervisorId, final Class<? extends SAFlowNodeInstance> entityClass,
            final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return (List<SAFlowNodeInstance>) getPersistenceService().searchEntity(entityClass, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    protected EventService getEventService() {
        return eventService;
    }

    protected Recorder getRecorder() {
        return recorder;
    }

    protected PersistenceService getPersistenceService() {
        return persistenceService;
    }

    protected TechnicalLoggerService getLogger() {
        return logger;
    }

    @Override
    public void deleteFlowNodeInstance(final SFlowNodeInstance sFlowNodeInstance) throws SFlowNodeDeletionException {
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(sFlowNodeInstance);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(FLOWNODE_INSTANCE, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(FLOWNODE_INSTANCE).setObject(sFlowNodeInstance)
                        .done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SBonitaException e) {
            throw new SFlowNodeDeletionException(e);
        }
    }

    @Override
    public void deleteArchivedFlowNodeInstance(final SAFlowNodeInstance saFlowNodeInstance) throws SFlowNodeDeletionException {
        final DeleteRecord deleteRecord = new DeleteRecord(saFlowNodeInstance);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(ARCHIVED_FLOWNODE_INSTANCE, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ARCHIVED_FLOWNODE_INSTANCE)
                    .setObject(saFlowNodeInstance).done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SFlowNodeDeletionException(e);
        }
    }

    @Override
    public List<SFlowNodeInstance> getFlowNodeInstancesToRestart(final QueryOptions queryOptions) throws SFlowNodeReadException {
        List<SFlowNodeInstance> selectList;
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            selectList = getPersistenceService().selectList(
                    new SelectListDescriptor<SFlowNodeInstance>("getFlowNodeInstancesToRestart", parameters, SFlowNodeInstance.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SFlowNodeReadException(e);
        }
        return getUnmodifiableList(selectList);
    }

    @Override
    public long getProcessInstanceId(final long containerId, final String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException {
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
            return containerId;
        } else if (DataInstanceContainer.ACTIVITY_INSTANCE.name().equals(containerType)) {
            return getFlowNodeInstance(containerId).getParentProcessInstanceId();
        }
        throw new IllegalArgumentException("Invalid container type: " + containerType);
    }

}
