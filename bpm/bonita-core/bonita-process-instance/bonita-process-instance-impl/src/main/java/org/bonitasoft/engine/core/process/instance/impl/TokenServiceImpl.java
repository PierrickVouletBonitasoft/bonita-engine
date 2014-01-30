/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.process.instance.impl;

import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.model.SToken;
import org.bonitasoft.engine.core.process.instance.model.builder.STokenBuilderFactory;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;

/**
 * @author Celine Souchet
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class TokenServiceImpl implements TokenService {

    private final Recorder recorder;

    private final ReadPersistenceService persistenceRead;

    private final TechnicalLoggerService logger;

    public TokenServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead, final TechnicalLoggerService logger) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.logger = logger;
    }

    @Override
    public SToken createToken(final Long processInstanceId, final Long refId, final Long parentRefId) throws SObjectCreationException {
        final SToken token = BuilderFactory.get(STokenBuilderFactory.class).createNewInstance(processInstanceId, refId, parentRefId).done();
        final InsertRecord insertRecord = new InsertRecord(token, PROCESS_INSTANCE_TOKEN_COUNT);
        try {
            recorder.recordInsert(insertRecord);
        } catch (final SRecorderException sre) {
            throw new SObjectCreationException(sre);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "create token:id=" + token.getId() + ", pId=" + processInstanceId + ", refId=" + refId
                    + ", parentRefId=" + parentRefId);
        }
        return token;
    }

    @Override
    public void createTokens(final Long processInstanceId, final Long refId, final Long parentRefId, final int numberOfToken) throws SObjectCreationException {
        if (numberOfToken > 0) {
            for (int i = 0; i < numberOfToken; i++) {
                createToken(processInstanceId, refId, parentRefId);
            }
        }
    }

    @Override
    public void deleteTokens(final Long processInstanceId, final Long refId, final int numberOfToken) throws SObjectModificationException,
            SObjectNotFoundException, SObjectReadException {
        if (numberOfToken > 0) {
            for (int i = 0; i < numberOfToken; i++) {
                // delete get...
                final SToken token = getToken(processInstanceId, refId);
                deleteToken(token);
            }
        }
    }

    @Override
    public void deleteToken(final SToken token) throws SObjectModificationException {
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(token, PROCESS_INSTANCE_TOKEN_COUNT);
            recorder.recordDelete(deleteRecord);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "deleted token:id=" + token.getId() + ", pId=" + token.getProcessInstanceId()
                        + ", refId=" + token.getRefId() + ", parentRefId=" + token.getParentRefId());
            }
        } catch (final SBonitaException e) {
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public void deleteTokens(final long processInstanceId) throws SObjectReadException, SObjectModificationException {
        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.DEFAULT_NUMBER_OF_RESULTS);
        List<SToken> tokens;
        do {
            tokens = getTokens(processInstanceId, queryOptions);
            for (final SToken token : tokens) {
                deleteToken(token);
            }
        } while (tokens.size() == QueryOptions.DEFAULT_NUMBER_OF_RESULTS);
    }

    @Override
    public void deleteAllTokens() throws SObjectReadException, SObjectModificationException {
        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.DEFAULT_NUMBER_OF_RESULTS);
        List<SToken> tokens;
        do {
            tokens = getTokens(queryOptions);
            for (final SToken token : tokens) {
                deleteToken(token);
            }
        } while (tokens.size() == QueryOptions.DEFAULT_NUMBER_OF_RESULTS);
    }

    public SToken getToken(final long tokenId) throws SObjectNotFoundException, SObjectReadException {
        SToken instance;
        try {
            instance = persistenceRead.selectById(SelectDescriptorBuilder.getElementById(SToken.class, "ProcessInstanceTokenCount", tokenId));
        } catch (final SBonitaReadException sbre) {
            throw new SObjectReadException(sbre);
        }
        if (instance == null) {
            throw new SObjectNotFoundException(tokenId);
        }
        return instance;
    }

    @Override
    public int getNumberOfToken(final long processInstanceId) throws SObjectReadException {
        try {
            return persistenceRead.selectOne(SelectDescriptorBuilder.getNumberOfTokensOfProcessInstance(processInstanceId)).intValue();
        } catch (final SBonitaReadException e) {
            throw new SObjectReadException(e);
        }
    }

    public List<SToken> getTokens(final QueryOptions queryOptions) throws SObjectReadException {
        try {
            return persistenceRead.selectList(SelectDescriptorBuilder.getTokens(queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SObjectReadException(e);
        }
    }

    public List<SToken> getTokens(final long processInstanceId, final QueryOptions queryOptions) throws SObjectReadException {
        try {
            return persistenceRead.selectList(SelectDescriptorBuilder.getTokensOfProcessInstance(processInstanceId, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SObjectReadException(e);
        }
    }

    @Override
    public int getNumberOfToken(final long processInstanceId, final long refId) throws SObjectReadException {
        try {
            return persistenceRead.selectOne(SelectDescriptorBuilder.getNumberOfToken(processInstanceId, refId)).intValue();
        } catch (final SBonitaReadException e) {
            throw new SObjectReadException(e);
        }
    }

    @Override
    public SToken getToken(final long processInstanceId, final long refId) throws SObjectNotFoundException, SObjectReadException {
        try {
            final List<SToken> selectList = persistenceRead.selectList(SelectDescriptorBuilder.getToken(processInstanceId, refId));
            if (selectList.isEmpty()) {
                throw new SObjectNotFoundException("no token found for process " + processInstanceId + " and reference " + refId
                        + " , the design may be invalid, check that all branches are correctly merged");
            } else {
                return selectList.get(0);
            }
        } catch (final SBonitaReadException e) {
            throw new SObjectReadException(e);
        }
    }

}
