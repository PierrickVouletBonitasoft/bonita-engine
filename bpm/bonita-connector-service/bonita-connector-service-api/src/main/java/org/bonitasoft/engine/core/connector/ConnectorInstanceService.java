/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.connector;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceCreationException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceDeletionException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceModificationException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceNotFoundException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface ConnectorInstanceService {

    String CONNECTOR_INSTANCE = "CONNECTOR_INSTANCE";

    String CONNECTOR_INSTANCE_STATE = "CONNECTOR_INSTANCE_STATE";

    String CONNECTOR_INSTANCE_STATE_UPDATED = "CONNECTOR_INSTANCE_STATE_UPDATED";

    /**
     * Get a list of connectorInstances for specified container
     * 
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container
     * @param activationEvent
     *            The event to indicate when the connector will be activated
     * @return list of connectorInstance objects
     * @throws SConnectorInstanceReadException
     *             Error thrown if has exceptions during the connector retrieve
     */
    List<SConnectorInstance> getConnectorInstances(long containerId, String containerType, ConnectorEvent activationEvent, int from, int numberOfResult,
            String state) throws SConnectorInstanceReadException;

    /**
     * Create connector instance by give connector instance, the connector instance will be stored in database
     * 
     * @param connectorInstance
     *            Connector instance
     * @throws SConnectorInstanceCreationException
     *             Error thrown if has exceptions during the connector instance creation
     */
    void createConnectorInstance(SConnectorInstance connectorInstance) throws SConnectorInstanceCreationException;

    /**
     * Delete the given connector instance from the database
     * 
     * @param connectorInstance
     *            the connector instance
     * @throws SConnectorInstanceDeletionException
     *             if has exceptions during the connector instance deletion
     */
    void deleteConnectorInstance(SConnectorInstance connectorInstance) throws SConnectorInstanceDeletionException;

    /**
     * @param sConnectorInstance
     * @param state
     * @throws SConnectorInstanceModificationException
     */
    void setState(SConnectorInstance sConnectorInstance, String state) throws SConnectorInstanceModificationException;

    /**
     * Defines the exception associated to the connector failure
     * @param connectorInstanceWithFailure failed connector instance
     * @param throwable exception responsible for connector failure
     * @throws SConnectorInstanceModificationException
     * @since 6.1
     */
    void setConnectorInstanceFailureException(SConnectorInstanceWithFailureInfo connectorInstanceWithFailure, Throwable throwable)
            throws SConnectorInstanceModificationException;

    /**
     * @param connectorInstanceId
     * @return
     * @throws SConnectorInstanceReadException
     * @throws SConnectorInstanceNotFoundException
     */
    SConnectorInstance getConnectorInstance(long connectorInstanceId) throws SConnectorInstanceReadException, SConnectorInstanceNotFoundException;

    /**
     * Retrieves the connector instance with failure information for the given connector instance id
     * 
     * @param connectorInstanceId
     * @return the connector instance with failure information for the given connector instance id
     * @throws SConnectorInstanceReadException
     * @throws SConnectorInstanceNotFoundException
     * @since 6.1
     */
    SConnectorInstanceWithFailureInfo getConnectorInstanceWithFailureInfo(long connectorInstanceId) throws SConnectorInstanceReadException,
            SConnectorInstanceNotFoundException;

    /**
     * @param containerId
     * @param containerType
     * @param activationEvent
     * @param state
     * @return
     * @throws SConnectorInstanceReadException
     */
    long getNumberOfConnectorInstances(long containerId, String containerType) throws SConnectorInstanceReadException;

    /**
     * @param activityInstanceId
     * @param flownodeType
     * @param pageNumber
     * @param numberPerPage
     * @throws SConnectorInstanceReadException
     */
    List<SConnectorInstance> getConnectorInstances(long containerId, String containerType, int from, int numberOfResult, String fieldName,
            OrderByType orderByType) throws SConnectorInstanceReadException;

    /**
     * @param containerId
     * @param containerType
     * @return
     * @throws SConnectorInstanceReadException
     */
    SConnectorInstance getNextExecutableConnectorInstance(long containerId, String containerType, ConnectorEvent activationEvent)
            throws SConnectorInstanceReadException;

    /**
     * @param searchOptions
     * @return
     */
    long getNumberOfConnectorInstances(QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * @param searchOptions
     * @return
     */
    List<SConnectorInstance> searchConnectorInstances(QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * @param connectorInstance
     * @param archiveDate
     * @throws SConnectorInstanceCreationException
     */
    void archiveConnectorInstance(SConnectorInstance connectorInstance, long archiveDate) throws SConnectorInstanceCreationException;

    /**
     * @param searchOptions
     * @param persistenceService
     * @return
     * @throws SBonitaSearchException
     */
    long getNumberArchivedConnectorInstance(QueryOptions searchOptions, ReadPersistenceService persistenceService) throws SBonitaSearchException;

    /**
     * @param searchOptions
     * @param persistenceService
     * @return
     * @throws SBonitaSearchException
     */
    List<SAConnectorInstance> searchArchivedConnectorInstance(QueryOptions searchOptions, ReadPersistenceService persistenceService)
            throws SBonitaSearchException;

    /**
     * @param sConnectorInstance
     * @throws SConnectorInstanceDeletionException
     */
    void deleteArchivedConnectorInstance(SAConnectorInstance sConnectorInstance) throws SConnectorInstanceDeletionException;

    /**
     * @param containerId
     * @param containerType
     * @throws SConnectorInstanceReadException
     * @throws SConnectorInstanceDeletionException
     * @since 6.1
     */
    void deleteConnectors(long containerId, String containerType) throws SConnectorInstanceReadException, SConnectorInstanceDeletionException;

    /**
     * @param containerId
     * @param containerType
     * @throws SBonitaSearchException
     * @throws SConnectorInstanceDeletionException
     * @since 6.1
     */
    void deleteArchivedConnectorInstances(long containerId, String containerType) throws SBonitaSearchException, SConnectorInstanceDeletionException;

}
