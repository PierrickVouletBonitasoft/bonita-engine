/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.archive.SDefinitiveArchiveNotFound;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.TransitionState;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionDeletionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SATransitionInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceLogBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Zhao Na
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class TransitionServiceImpl implements TransitionService {

    private final ReadPersistenceService persistenceRead;

    private final ArchiveService archiveService;

    public TransitionServiceImpl(final ReadPersistenceService persistenceRead, final ArchiveService archiveService,
            final QueriableLoggerService queriableLoggerService) {
        this.persistenceRead = persistenceRead;
        this.archiveService = archiveService;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    @Override
    public long getNumberOfArchivedTransitionInstances(final QueryOptions countOptions) throws SBonitaSearchException {
        try {
            return persistenceRead.getNumberOfEntities(SATransitionInstance.class, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SATransitionInstance> searchArchivedTransitionInstances(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return persistenceRead.searchEntity(SATransitionInstance.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void archive(final STransitionDefinition sTransitionDefinition, final SFlowNodeInstance sFlowNodeInstance, final TransitionState transitionState)
            throws STransitionCreationException {
        final SATransitionInstance saTransitionInstance = BuilderFactory.get(SATransitionInstanceBuilderFactory.class)
                .createNewTransitionInstance(sTransitionDefinition, sFlowNodeInstance, transitionState).done();
        final long archiveDate = System.currentTimeMillis();
        try {
            archiveTransitionInstanceInsertRecord(saTransitionInstance, archiveDate);
        } catch (final SRecorderException e) {
            throw new STransitionCreationException(e);
        } catch (final SDefinitiveArchiveNotFound e) {
            throw new STransitionCreationException(e);
        }

    }

    private void archiveTransitionInstanceInsertRecord(final SATransitionInstance saTransitionInstance, final long archiveDate) throws SRecorderException,
            SDefinitiveArchiveNotFound {
        final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saTransitionInstance, null);
        archiveService.recordInsert(archiveDate, insertRecord);
    }

    protected STransitionInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final STransitionInstanceLogBuilder logBuilder = BuilderFactory.get(STransitionInstanceLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public void delete(final SATransitionInstance saTransitionInstance) throws STransitionDeletionException {
        final DeleteRecord deleteRecord = new DeleteRecord(saTransitionInstance, null);
        try {
            archiveService.recordDelete(deleteRecord);
        } catch (final SRecorderException e) {
            throw new STransitionDeletionException(e);
        }
    }

    @Override
    public void deleteArchivedTransitionsOfProcessInstance(final long processInstanceId) throws STransitionDeletionException, SBonitaSearchException {
        final SATransitionInstanceBuilderFactory saTransitionInstanceBuilder = BuilderFactory.get(SATransitionInstanceBuilderFactory.class);
        final String rootContainerIdKey = saTransitionInstanceBuilder.getRootContainerIdKey();
        final String idKey = saTransitionInstanceBuilder.getIdKey();

        List<SATransitionInstance> transitionInstances;
        do {
            final List<FilterOption> filters = Collections.singletonList(new FilterOption(SATransitionInstance.class, rootContainerIdKey, processInstanceId));
            final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SATransitionInstance.class, idKey, OrderByType.ASC));
            final QueryOptions queryOptions = new QueryOptions(0, 10, orderByOptions, filters, null);
            transitionInstances = searchArchivedTransitionInstances(queryOptions);

            for (final SATransitionInstance saTransitionInstance : transitionInstances) {
                delete(saTransitionInstance);
            }
        } while (!transitionInstances.isEmpty());
    }
}
