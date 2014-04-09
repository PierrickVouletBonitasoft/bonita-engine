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
package org.bonitasoft.engine.process.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ProcessInstanceTest extends AbstractProcessInstanceTest {

    @Test
    public void checkProcessInstanceDescriptionNotNull() throws Exception {
        checkProcessInstanceDescription("My description");
    }

    @Test
    public void checkProcessInstanceDescriptionNull() throws Exception {
        checkProcessInstanceDescription(null);
    }

    private void checkProcessInstanceDescription(final String description) throws Exception {
        // Create process definition with description;
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION)
                .addDescription(description).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        // Start ProcessInstance
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(description, processInstance.getDescription());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void checkArchivedProcessInstanceDescriptionNotNull() throws Exception {
        checkArchivedProcessInstanceDescription("My description");
    }

    @Test
    public void checkArchivedProcessInstanceDescriptionNull() throws Exception {
        checkArchivedProcessInstanceDescription(null);
    }

    private void checkArchivedProcessInstanceDescription(final String description) throws Exception {
        // Create process definition with description;
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION)
                .addDescription(description).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        // Start ProcessInstance
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(description, processInstance.getDescription());

        checkProcessInstanceIsArchived(processInstance);
        final List<ArchivedProcessInstance> processInstances = getProcessAPI().getArchivedProcessInstances(0, 1, ProcessInstanceCriterion.CREATION_DATE_DESC);
        assertEquals(1, processInstances.size());

        // We check that the retrieved processes are the good ones:
        final ArchivedProcessInstance archivedProcessInstance = processInstances.get(0);
        assertEquals(processInstance.getId(), archivedProcessInstance.getSourceObjectId());
        assertEquals(description, archivedProcessInstance.getDescription());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void deleteProcessInstance() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, pedro);

        final ProcessInstance process1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance process2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance process3 = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(5, getProcessAPI().getNumberOfProcessInstances());

        getProcessAPI().deleteProcessInstance(process1.getId());
        getProcessAPI().deleteProcessInstance(process3.getId());
        assertEquals(3, getProcessAPI().getNumberOfProcessInstances());

        getProcessAPI().getProcessInstance(process2.getId());
        try {
            getProcessAPI().getProcessInstance(process1.getId());
            fail("this instance should be deleted");
        } catch (final ProcessInstanceNotFoundException e) {
            // ok
        }
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = DeletionException.class)
    public void deleteUnknownProcessInstance() throws Exception {
        getProcessAPI().deleteProcessInstance(123456789123l);
    }

    @Test
    public void deleteProcessInstances() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, pedro);

        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(5, getProcessAPI().getNumberOfProcessInstances());

        waitForUserTask("step1", processInstance1);
        waitForUserTask("step1", processInstance2);
        waitForUserTask("step1", processInstance3);
        waitForUserTask("step1", processInstance4);
        waitForUserTask("step1", processInstance5);
        getProcessAPI().deleteProcessInstances(processDefinition.getId(), 0, 4);
        assertEquals(1, getProcessAPI().getNumberOfProcessInstances());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Deprecated
    public void oldDeleteProcessInstances() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, pedro);

        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(5, getProcessAPI().getNumberOfProcessInstances());

        waitForUserTask("step1", processInstance1);
        waitForUserTask("step1", processInstance2);
        waitForUserTask("step1", processInstance3);
        waitForUserTask("step1", processInstance4);
        waitForUserTask("step1", processInstance5);
        getProcessAPI().deleteProcessInstances(processDefinition.getId());
        assertEquals(0, getProcessAPI().getNumberOfProcessInstances());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Ignore("ENGINE-915 - ArchivedProcessInstance.getStartDate() returns null")
    public void getArchivedProcessInstance() throws Exception {
        getArchivedProcessInstances();
    }

    @Test
    public void getArchivedProcessInstanceOrderByLastUpdate() throws Exception {
        getArchivedProcessInstances(ProcessInstanceCriterion.LAST_UPDATE_ASC, 0, 1, 2, ProcessInstanceCriterion.LAST_UPDATE_DESC, 2, 1, 0);
    }

    private void getArchivedProcessInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(false, false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance pi1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDefinition.getId());
        // We asked for creation date descending order:
        final List<ArchivedProcessInstance> processInstances;
        checkProcessInstanceIsArchived(pi2);
        processInstances = getProcessAPI().getArchivedProcessInstances(0, 10, ProcessInstanceCriterion.CREATION_DATE_ASC);
        assertEquals(3, processInstances.size());
        //
        final ArchivedProcessInstance returnedPI0 = processInstances.get(0);
        final ArchivedProcessInstance returnedPI1 = processInstances.get(1);
        final ArchivedProcessInstance returnedPI2 = processInstances.get(2);

        System.out.println("process instances : " + processInstances);

        // We check that the retrieved processes are the good ones:
        assertEquals(pi0.getId(), returnedPI0.getId());
        assertEquals(pi1.getId(), returnedPI1.getId());
        assertEquals(pi2.getId(), returnedPI2.getId());

        // First creation date must be after second creation date:
        assertTrue(returnedPI0.getStartDate().before(returnedPI1.getStartDate()));
        // Second creation date must be after third creation date:
        assertTrue(returnedPI1.getStartDate().before(returnedPI2.getStartDate()));

        disableAndDeleteProcess(processDefinition);
        assertEquals(0, getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT).size());
    }

    private void getArchivedProcessInstances(final ProcessInstanceCriterion asc, final int asc1, final int asc2, final int asc3,
            final ProcessInstanceCriterion desc, final int desc1, final int desc2, final int desc3) throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(false, false));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        getProcessAPI().enableProcess(processDefinition.getId());

        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        checkProcessInstanceIsArchived(pi0);

        final ProcessInstance pi1 = getProcessAPI().startProcess(processDefinition.getId());
        checkProcessInstanceIsArchived(pi1);

        final ProcessInstance pi2 = getProcessAPI().startProcess(processDefinition.getId());
        checkProcessInstanceIsArchived(pi2);

        // We asked for creation date descending order:
        List<ArchivedProcessInstance> processInstances = getProcessAPI().getArchivedProcessInstances(0, 10, asc);
        assertEquals(3, processInstances.size());
        assertEquals("completed", processInstances.get(asc1).getState());
        assertEquals(pi0.getId(), processInstances.get(asc1).getSourceObjectId());
        assertEquals("completed", processInstances.get(asc2).getState());
        assertEquals(pi1.getId(), processInstances.get(asc2).getSourceObjectId());
        assertEquals("completed", processInstances.get(asc3).getState());
        assertEquals(pi2.getId(), processInstances.get(asc3).getSourceObjectId());

        processInstances = getProcessAPI().getArchivedProcessInstances(0, 10, desc);
        assertEquals(3, processInstances.size());
        assertEquals(pi0.getId(), processInstances.get(desc1).getSourceObjectId());
        assertEquals(pi1.getId(), processInstances.get(desc2).getSourceObjectId());
        assertEquals(pi2.getId(), processInstances.get(desc3).getSourceObjectId());

        disableAndDeleteProcess(processDefinition);
        assertEquals(0, getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT).size());
    }

    @Test
    public void getNumberOfArchiveProcessInstance() throws Exception {
        getNumberOfArchivedProcessInstances();
    }

    private void getNumberOfArchivedProcessInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        long numberOfProcessInstancesBefore;
        numberOfProcessInstancesBefore = getProcessAPI().getNumberOfArchivedProcessInstances();
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());

        checkProcessInstanceIsArchived(processInstance1);
        checkProcessInstanceIsArchived(processInstance2);
        checkProcessInstanceIsArchived(processInstance3);
        assertEquals(numberOfProcessInstancesBefore + 3, getProcessAPI().getNumberOfArchivedProcessInstances());

        assertTrue(waitForProcessToFinishAndBeArchived(processInstance1));
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance2));
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance3));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getChildrenInstanceIdsOfProcessInstance() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, pedro);

        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final List<Long> ids = getProcessAPI().getChildrenInstanceIdsOfProcessInstance(pi0.getId(), 0, 10, ProcessInstanceCriterion.DEFAULT);
        assertEquals(0, ids.size());
        // TODO FIXME check child more than 0 in order, waiting for CallActivity

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getChildrenInstanceIdsOfUnknownProcessInstance() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, pedro);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final List<Long> childrenInstanceIds = getProcessAPI().getChildrenInstanceIdsOfProcessInstance(processInstance.getId() + 1, 0, 10,
                ProcessInstanceCriterion.DEFAULT);
        assertTrue(childrenInstanceIds.isEmpty());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getProcessInstanceIdFromActivityInstanceId() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, pedro);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi0.getId(), 0, 10);
        for (final ActivityInstance activityInstance : activityInstances) {
            final long processInstanceId = getProcessAPI().getProcessInstanceIdFromActivityInstanceId(activityInstance.getId());
            assertEquals(pi0.getId(), processInstanceId);
        }
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberOfProcessInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, pedro);

        final long initialProcessInstanceNb = getProcessAPI().getNumberOfProcessInstances();

        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());

        final long finalProcessInstanceNb = getProcessAPI().getNumberOfProcessInstances();
        assertEquals(initialProcessInstanceNb + 3, finalProcessInstanceNb);

        // Clean up
        waitForUserTask("step1", processInstance1);
        waitForUserTask("step1", processInstance2);
        waitForUserTask("step1", processInstance3);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getProcessInstances() throws Exception {
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.CREATION_DATE_ASC);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.CREATION_DATE_DESC);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.LAST_UPDATE_DESC);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.LAST_UPDATE_ASC);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.NAME_ASC);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.NAME_DESC);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.DEFAULT);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.STATE_DESC);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.STATE_ASC);
    }

    private void getProcessInstancesOrderByProcessInstanceCriterion(final ProcessInstanceCriterion processInstanceCriterion) throws Exception {
        final List<ProcessDefinition> processDefinitions = createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(3, pedro);
        final List<ProcessInstance> processInstances = startNbProcess(processDefinitions);

        // We asked for creation date descending order:
        final List<ProcessInstance> resultProcessInstances = getProcessAPI().getProcessInstances(0, 10, processInstanceCriterion);
        assertEquals(3, resultProcessInstances.size());

        final ProcessInstance returnedPI0 = resultProcessInstances.get(0);
        final ProcessInstance returnedPI1 = resultProcessInstances.get(1);
        final ProcessInstance returnedPI2 = resultProcessInstances.get(2);

        final ProcessInstance pi0 = processInstances.get(0);
        final ProcessInstance pi1 = processInstances.get(1);
        final ProcessInstance pi2 = processInstances.get(2);

        switch (processInstanceCriterion) {
            case STATE_ASC:
                // First state must be before second state :
                assertTrue(returnedPI0.getState().compareToIgnoreCase(returnedPI1.getState()) <= 0);
                // Second state must be before third state :
                assertTrue(returnedPI1.getState().compareToIgnoreCase(returnedPI2.getState()) <= 0);
                break;
            case STATE_DESC:
                // First state must be after second state :
                assertTrue(returnedPI0.getState().compareToIgnoreCase(returnedPI1.getState()) >= 0);
                // Second state must be after third state :
                assertTrue(returnedPI1.getState().compareToIgnoreCase(returnedPI2.getState()) >= 0);
                break;
            case LAST_UPDATE_ASC:
                // First last update must be before second last update :
                assertTrue(returnedPI0.getLastUpdate().before(returnedPI1.getLastUpdate()));
                // Second last update must be before third last update :
                assertTrue(returnedPI1.getLastUpdate().before(returnedPI2.getLastUpdate()));
                break;
            case LAST_UPDATE_DESC:
                // First last update must be after second last update :
                assertTrue(returnedPI0.getLastUpdate().after(returnedPI1.getLastUpdate()));
                // Second last update must be after third last update :
                assertTrue(returnedPI1.getLastUpdate().after(returnedPI2.getLastUpdate()));
                break;
            case NAME_ASC:
                // We check that the retrieved processes are the good ones:
                assertEquals(pi0.getId(), returnedPI0.getId());
                assertEquals(pi1.getId(), returnedPI1.getId());
                assertEquals(pi2.getId(), returnedPI2.getId());

                // First name must be before second name :
                assertTrue(returnedPI0.getName().compareToIgnoreCase(returnedPI1.getName()) <= 0);
                // Second name must be before third name :
                assertTrue(returnedPI1.getName().compareToIgnoreCase(returnedPI2.getName()) <= 0);
                break;
            case NAME_DESC:
                // We check that the retrieved processes are the good ones:
                assertEquals(pi0.getId(), returnedPI2.getId());
                assertEquals(pi1.getId(), returnedPI1.getId());
                assertEquals(pi2.getId(), returnedPI0.getId());

                // First name must be after second name :
                assertTrue(returnedPI0.getName().compareToIgnoreCase(returnedPI1.getName()) >= 0);
                // Second name must be after third name :
                assertTrue(returnedPI1.getName().compareToIgnoreCase(returnedPI2.getName()) >= 0);
                break;
            case CREATION_DATE_ASC:
                // We check that the retrieved processes are the good ones:
                assertEquals(pi0.getId(), returnedPI0.getId());
                assertEquals(pi1.getId(), returnedPI1.getId());
                assertEquals(pi2.getId(), returnedPI2.getId());

                // First creation date must be before second creation date:
                assertTrue(returnedPI0.getStartDate().before(returnedPI1.getStartDate()));
                // Second creation date must be before third creation date:
                assertTrue(returnedPI1.getStartDate().before(returnedPI2.getStartDate()));
                break;
            case CREATION_DATE_DESC:
            case DEFAULT:
            default:
                // We check that the retrieved processes are the good ones:
                assertEquals(pi0.getId(), returnedPI2.getId());
                assertEquals(pi1.getId(), returnedPI1.getId());
                assertEquals(pi2.getId(), returnedPI0.getId());

                // First creation date must be after second creation date:
                assertTrue(returnedPI0.getStartDate().after(returnedPI1.getStartDate()));
                // Second creation date must be after third creation date:
                assertTrue(returnedPI1.getStartDate().after(returnedPI2.getStartDate()));
                break;
        }

        // Clean up
        disableAndDeleteProcess(processDefinitions);

        // We check that there are no resident process instances in DB:
        assertEquals(0, getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT).size());
    }

    @Test
    public void isInvolvedInProcessInstance() throws Exception {
        // add user
        final UserCreator creator = new UserCreator(USERNAME, PASSWORD);
        creator.setManagerUserId(pedro.getId());
        final User user = createUser(creator);

        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder1.addActor(ACTOR_NAME).addDescription(DESCRIPTION);

        // 1 instance of process def:
        final DesignProcessDefinition designProcessDefinition = processBuilder1.addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME)
                .addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME).addTransition("step1", "step2").addTransition("step1", "step3")
                .addTransition("step1", "step4").getProcess();
        final BusinessArchive businessArchive1 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive1);

        addUserToFirstActorOfProcess(user.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        boolean isInvolved = getProcessAPI().isInvolvedInProcessInstance(user.getId(), processInstance.getId());
        assertTrue(isInvolved);
        isInvolved = getProcessAPI().isInvolvedInProcessInstance(pedro.getId(), processInstance.getId());
        assertTrue(isInvolved);

        deleteUser(user);
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = ProcessInstanceNotFoundException.class)
    public void isInvolvedInProcessInstanceWithProcessInstanceNotFoundException() throws Exception {
        getProcessAPI().isInvolvedInProcessInstance(pedro.getId(), 0);
    }

    @Test
    public void isInvolvedInProcessInstanceWithInvalidUser() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();
        final BusinessArchive businessArchive1 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive1);

        addUserToFirstActorOfProcess(pedro.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step2", processInstance);
        try {
            final long beinglessUserId = pedro.getId() + 1;
            getProcessAPI().isInvolvedInProcessInstance(beinglessUserId, processInstance.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void setProcessInstanceState() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, pedro);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForStep("step1", processInstance);

        final long processInstanceId = processInstance.getId();
        processInstance = getProcessAPI().getProcessInstance(processInstanceId);
        assertEquals("started", processInstance.getState());

        getProcessAPI().setProcessInstanceState(processInstance, "initializing");
        processInstance = getProcessAPI().getProcessInstance(processInstanceId);
        assertEquals("initializing", processInstance.getState());

        getProcessAPI().setProcessInstanceState(processInstance, "started");
        processInstance = getProcessAPI().getProcessInstance(processInstanceId);
        assertEquals("started", processInstance.getState());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class, ProcessInstance.class }, concept = BPMNConcept.PROCESS, keywords = { "Process Instance", "start", "2 times" }, jira = "ENGINE-1094")
    @Test
    public void startProcess2Times() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("initTask1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, APITestUtil.ACTOR_NAME, pedro);

        // Start process instance first time, and complete it
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("initTask1", processInstance.getId(), pedro.getId());
        waitForProcessToFinish(processInstance);

        // Start process instance second time
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("initTask1", processInstance2.getId(), pedro.getId());
        waitForProcessToFinish(processInstance2);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void startProcessInstanceOnBehalfUserId() throws Exception {
        logoutThenloginAs("pedro", "secreto");

        final String otherUserName = "other";
        final User otherUser = createUser(otherUserName, "user");

        // create process definition with integer data;
        final String dataName = "var1";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addIntegerData(dataName, new ExpressionBuilder().createConstantIntegerExpression(1)).addUserTask("step1", ACTOR_NAME)
                .addAutomaticTask("step2").addTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processDef, ACTOR_NAME, pedro);

        // create Operation keyed map
        final Operation integerOperation = buildIntegerOperation(dataName, 2);
        final List<Operation> operations = new ArrayList<Operation>();
        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        contexts.put("page", "1");
        operations.add(integerOperation);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance processInstance = getProcessAPI().startProcess(otherUser.getId(), processDefinitionId, operations, contexts);
        final ProcessInstance processInstance2 = getProcessAPI().getProcessInstance(processInstance.getId());
        assertEquals(otherUser.getId(), processInstance2.getStartedBy());

        disableAndDeleteProcess(processDefinition);
        deleteUser(otherUser);
    }

    private List<ProcessInstance> startNbProcess(final List<ProcessDefinition> processDefinitions) throws Exception {
        final List<ProcessInstance> process = new ArrayList<ProcessInstance>();
        for (final ProcessDefinition processDefinition : processDefinitions) {
            process.add(getProcessAPI().startProcess(processDefinition.getId()));
            Thread.sleep(5);// avoid two instances with the same date
        }
        return process;
    }

    private List<ProcessDefinition> createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(final int nbProcess, final User user) throws BonitaException {
        return createNbProcessDefinitionWithHumanAndAutomaticAndDeployWithActor(nbProcess, user, Arrays.asList("step1", "step2"), Arrays.asList(true, true));
    }

}
