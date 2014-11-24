package org.bonitasoft.engine.process.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HumanTasksIT extends CommonAPITest {

    protected User user;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(USERNAME, PASSWORD);
    }

    @After
    public void after() throws Exception {
        deleteUser(user);
        logoutOnTenant();
    }

    @Test
    public void cannotGetHumanTaskInstances() throws Exception {
        // First process def with 2 instances:
        final DesignProcessDefinition designProcessDef1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("initTask1"),
                Arrays.asList(true));
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(designProcessDef1, ACTOR_NAME, user);
        getProcessAPI().startProcess(processDef1.getId());
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDef1.getId());

        final List<HumanTaskInstance> humanTaskInstances = getProcessAPI().getHumanTaskInstances(pi2.getId(), "initTsk2", 0, 10);
        assertTrue(humanTaskInstances.isEmpty());
        disableAndDeleteProcess(processDef1);
    }

    @Cover(classes = { ProcessAPI.class, HumanTaskInstance.class }, concept = BPMNConcept.PROCESS, keywords = { "Last", "Human", "Task Instance" }, jira = "ENGINE-772")
    @Test
    public void getLastHumanTaskInstance() throws Exception {
        // First process def with 2 instances:
        final DesignProcessDefinition designProcessDef1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("initTask1"),
                Arrays.asList(true));
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(designProcessDef1, ACTOR_NAME, user);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDef1.getId());
        waitForUserTask("initTask1", processInstance1);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDef1.getId());
        waitForUserTask("initTask1", processInstance2);

        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME + 2, PROCESS_VERSION);
        definitionBuilder.addStartEvent("start");
        definitionBuilder.addActor(ACTOR_NAME);
        definitionBuilder.addUserTask("initTask2", ACTOR_NAME);
        definitionBuilder.addEndEvent("end");
        definitionBuilder.addTransition("start", "initTask2");
        definitionBuilder.addTransition("initTask2", "end");
        final DesignProcessDefinition designProcessDef2 = definitionBuilder.done();

        final ProcessDefinition processDef2 = deployAndEnableProcessWithActor(designProcessDef2, ACTOR_NAME, user);
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask("initTask2", processInstance3);
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask("initTask2", processInstance4);
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask("initTask2", processInstance5);

        final HumanTaskInstance taskInstance = getProcessAPI().getLastStateHumanTaskInstance(processInstance3.getId(), "initTask2");
        assertNotNull(taskInstance);
        assertEquals("initTask2", taskInstance.getName());

        disableAndDeleteProcess(processDef1);
        disableAndDeleteProcess(processDef2);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Task failure", "FlowNodeExecutor" }, jira = "ENGINE-1134")
    @Test
    public void taskExecutionFailureLogsPrettyMessage() throws Exception {
        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance("taskExecutionFailureLogsPrettyMessage", "1.01");
        definitionBuilder.addStartEvent("start");
        definitionBuilder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskDef = definitionBuilder.addUserTask("initTask", ACTOR_NAME);
        userTaskDef.addData("aData", String.class.getName(), new ExpressionBuilder().createConstantStringExpression("initValue"));
        userTaskDef.addOperation(new OperationBuilder().createSetDataOperation("aData", new ExpressionBuilder().createConstantIntegerExpression(18)));
        final DesignProcessDefinition designProcessDef = definitionBuilder.done();

        final ProcessDefinition processDef = deployAndEnableProcessWithActor(designProcessDef, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDef.getId());
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final ActivityInstance task = waitForUserTask("initTask", processInstance);
        getProcessAPI().assignUserTask(task.getId(), user.getId());
        try {
            getProcessAPI().executeFlowNode(task.getId());
        } catch (final FlowNodeExecutionException e) {
            assertTrue("wrong exception message", e.getMessage().contains("Incompatible assignment operation type"));
        }
        disableAndDeleteProcess(processDef);
    }

    @Cover(classes = { ProcessAPI.class, HumanTaskInstance.class }, concept = BPMNConcept.PROCESS, keywords = { "Last", "Human", "Task Instance" }, jira = "ENGINE-772", exceptions = { NotFoundException.class })
    @Test(expected = NotFoundException.class)
    public void cannotGetLastHumanTaskInstance() throws Exception {
        // First process def with 2 instances:
        final DesignProcessDefinition designProcessDef1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("initTask1"),
                Arrays.asList(true));
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(designProcessDef1, ACTOR_NAME, user);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDef1.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDef1.getId());
        waitForUserTask("initTask1", processInstance1);
        waitForUserTask("initTask1", processInstance2);

        try {
            getProcessAPI().getLastStateHumanTaskInstance(processInstance2.getId(), "initTsk2");
        } finally {
            // Clean up
            disableAndDeleteProcess(processDef1);
        }
    }

    @Test
    public void getHumanTaskInstances() throws Exception {
        // First process def with 2 instances:
        final DesignProcessDefinition designProcessDef1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("initTask1"),
                Arrays.asList(true));
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(designProcessDef1, ACTOR_NAME, user);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDef1.getId());
        waitForUserTask("initTask1", processInstance1);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDef1.getId());
        waitForUserTask("initTask1", processInstance2);

        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME + 2, PROCESS_VERSION);
        definitionBuilder.addStartEvent("start");
        definitionBuilder.addActor(ACTOR_NAME);
        definitionBuilder.addUserTask("initTask2", ACTOR_NAME);
        definitionBuilder.addEndEvent("end");
        definitionBuilder.addTransition("start", "initTask2");
        definitionBuilder.addTransition("initTask2", "end");
        final DesignProcessDefinition designProcessDef2 = definitionBuilder.done();

        final ProcessDefinition processDef2 = deployAndEnableProcessWithActor(designProcessDef2, ACTOR_NAME, user);
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask("initTask2", processInstance3);
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask("initTask2", processInstance4);
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask("initTask2", processInstance5);

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getHumanTaskInstances(processInstance3.getId(), "initTask2", 0, 10);
        assertEquals(1, taskInstances.size());

        disableAndDeleteProcess(processDef1);
        disableAndDeleteProcess(processDef2);
    }

    @Test
    public void getOneAssignedHumanTaskInstance() throws Exception {
        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndStringData();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ActivityInstance activityInstance = createProcessAndAssignUserTask(user, processDefinition);

        final HumanTaskInstance humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, null).get(0);
        assertEquals(activityInstance.getId(), humanTask.getId());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getAssignedHumanTaskInstances() throws Exception {
        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        definitionBuilder.addStartEvent("start");
        definitionBuilder.addActor(ACTOR_NAME);
        definitionBuilder.addUserTask("initTask1", ACTOR_NAME).addPriority(TaskPriority.NORMAL.name());
        definitionBuilder.addUserTask("initTask2", ACTOR_NAME).addPriority(TaskPriority.LOWEST.name());
        definitionBuilder.addUserTask("initTask3", ACTOR_NAME).addPriority(TaskPriority.HIGHEST.name());
        definitionBuilder.addUserTask("initTask4", ACTOR_NAME).addPriority(TaskPriority.NORMAL.name());
        definitionBuilder.addUserTask("initTask5", ACTOR_NAME);
        definitionBuilder.addEndEvent("end");
        definitionBuilder.addTransition("start", "initTask1");
        definitionBuilder.addTransition("start", "initTask2");
        definitionBuilder.addTransition("start", "initTask3");
        definitionBuilder.addTransition("start", "initTask4");
        definitionBuilder.addTransition("initTask4", "initTask5");
        final DesignProcessDefinition designProcessDef = definitionBuilder.done();

        final ProcessDefinition processDef = deployAndEnableProcessWithActor(designProcessDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDef.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDef.getId());
        waitForUserTaskAndAssigneIt("initTask1", processInstance, user);
        waitForUserTaskAndAssigneIt("initTask2", processInstance, user);
        waitForUserTaskAndAssigneIt("initTask3", processInstance, user);
        waitForUserTaskAndAssigneIt("initTask4", processInstance, user);

        // The task with the lowest priority is "initTask2"
        HumanTaskInstance humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.PRIORITY_ASC).get(0);
        assertEquals("initTask2", humanTask.getName());

        // The task with the highest priority is "initTask3"
        humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.PRIORITY_DESC).get(0);
        assertEquals("initTask3", humanTask.getName());

        // The task with the highest priority is "initTask3"
        humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.DEFAULT).get(0);
        assertEquals("initTask3", humanTask.getName());

        // The task expected is "initTask1"
        humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.NAME_ASC).get(0);
        assertEquals("initTask1", humanTask.getName());

        // The task expected is "initTask4"
        humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.NAME_DESC).get(0);
        assertEquals("initTask4", humanTask.getName());

        disableAndDeleteProcess(processDef);
    }

    @Test
    public void getAssignedHumanTaskInstancesOrderByDates() throws Exception {
        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        definitionBuilder.addStartEvent("start");
        definitionBuilder.addActor(ACTOR_NAME);
        definitionBuilder.addUserTask("initTask1", ACTOR_NAME);
        definitionBuilder.addUserTask("initTask4", ACTOR_NAME);
        definitionBuilder.addUserTask("initTask5", ACTOR_NAME);
        definitionBuilder.addEndEvent("end");
        definitionBuilder.addTransition("start", "initTask1");
        definitionBuilder.addTransition("start", "initTask4");
        definitionBuilder.addTransition("initTask4", "initTask5");
        final DesignProcessDefinition designProcessDef = definitionBuilder.done();

        final ProcessDefinition processDef = deployAndEnableProcessWithActor(designProcessDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDef.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDef.getId());
        waitForUserTaskAndAssigneIt("initTask1", processInstance, user);
        waitForUserTaskAndAssigneIt("initTask4", processInstance, user);

        HumanTaskInstance humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.NAME_DESC).get(0);
        assertEquals("initTask4", humanTask.getName());

        assignAndExecuteStep(humanTask, user.getId());
        waitForUserTaskAndAssigneIt("initTask5", processInstance, user);

        humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.REACHED_STATE_DATE_ASC).get(0);
        assertEquals("initTask1", humanTask.getName());

        humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.REACHED_STATE_DATE_DESC).get(0);
        assertEquals("initTask5", humanTask.getName());

        humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.LAST_UPDATE_ASC).get(0);
        assertEquals("initTask1", humanTask.getName());

        humanTask = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 1, ActivityInstanceCriterion.LAST_UPDATE_DESC).get(0);
        assertEquals("initTask5", humanTask.getName());

        disableAndDeleteProcess(processDef);
    }

    @Test
    public void getHumanTaskInstance() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi0.getId(), 0, 10);
        final long activityInstanceId = activityInstances.get(0).getId();
        final HumanTaskInstance userTaskInstance = getProcessAPI().getHumanTaskInstance(activityInstanceId);
        assertEquals("step1", userTaskInstance.getName());

        disableAndDeleteProcess(processDefinition);
    }

    private DesignProcessDefinition createProcessWithActorAndHumanTaskAndStringData() throws Exception {
        return new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addUserTask("step1", ACTOR_NAME)
                .addShortTextData("dataName", new ExpressionBuilder().createConstantStringExpression("beforeUpdate")).getProcess();
    }

    private ActivityInstance createProcessAndAssignUserTask(final User user, final ProcessDefinition processDefinition) throws Exception {
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForUserTask("step1", processInstance);

        final List<ActivityInstance> activityInstances = new ArrayList<ActivityInstance>(getProcessAPI().getActivities(processInstance.getId(), 0, 20));
        final ActivityInstance activityInstance = activityInstances.get(activityInstances.size() - 1);

        assertEquals("ready", activityInstance.getState());

        getProcessAPI().assignUserTask(activityInstance.getId(), user.getId());
        return activityInstance;
    }

    @Test
    public void setTaskPriority() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance step1 = waitForUserTask("step1", processInstance);
        assertEquals(TaskPriority.NORMAL, step1.getPriority());

        final long step1Id = step1.getId();
        getProcessAPI().setTaskPriority(step1Id, TaskPriority.HIGHEST);
        step1 = getProcessAPI().getHumanTaskInstance(step1Id);
        assertEquals(TaskPriority.HIGHEST, step1.getPriority());

        getProcessAPI().setTaskPriority(step1Id, TaskPriority.LOWEST);
        step1 = getProcessAPI().getHumanTaskInstance(step1Id);
        assertEquals(TaskPriority.LOWEST, step1.getPriority());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void setState() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance step1 = waitForUserTask("step1", processInstance);
        assertEquals(ActivityStates.READY_STATE, step1.getState());

        final long activityInstanceId = step1.getId();
        getProcessAPI().setActivityStateById(activityInstanceId, 32);
        step1 = getProcessAPI().getHumanTaskInstance(activityInstanceId);
        assertEquals(ActivityStates.INITIALIZING_STATE, step1.getState());

        // test set state by stateName
        getProcessAPI().setActivityStateByName(activityInstanceId, ActivityStates.CANCELLING_SUBTASKS_STATE);
        step1 = getProcessAPI().getHumanTaskInstance(activityInstanceId);
        assertEquals(ActivityStates.CANCELLING_SUBTASKS_STATE, step1.getState());

        getProcessAPI().setActivityStateByName(activityInstanceId, ActivityStates.SKIPPED_STATE);
        // will skip task and finish process
        waitForProcessToFinish(processInstance.getId());

        disableAndDeleteProcess(processDefinition);
    }
}
