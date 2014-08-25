package org.bonitasoft.engine.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.RuleDefinition;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnectorWithAPICall;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserTaskContractTest extends CommonAPITest {

    private User matti;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        matti = createUser("matti", "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith(matti.getUserName(), "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(matti);
        logoutOnTenant();
    }

    @Test
    public void createAContractAndGetIt() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("task1", ACTOR_NAME).addContract().addInput("numberOfDays", Integer.class.getName(), null)
        .addRule("mandatory", "numberOfDays != null", "numberOfDays must be set", "numberOfDays");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("task1");

        final ContractDefinition contract = getProcessAPI().getUserTaskContract(userTask.getId());

        assertThat(contract.getInputs()).hasSize(1);
        final InputDefinition input = contract.getInputs().get(0);
        assertThat(input.getName()).isEqualTo("numberOfDays");
        assertThat(input.getType()).isEqualTo(Integer.class.getName());
        assertThat(input.getDescription()).isNull();

        assertThat(contract.getRules()).hasSize(1);
        final RuleDefinition rule = contract.getRules().get(0);
        assertThat(rule.getName()).isEqualTo("mandatory");
        assertThat(rule.getExpression()).isEqualTo("numberOfDays != null");
        assertThat(rule.getExplanation()).isEqualTo("numberOfDays must be set");
        assertThat(rule.getInputNames()).containsExactly("numberOfDays");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getExceptionWhenContractIsNotValid() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("task1", ACTOR_NAME).addContract().addInput("numberOfDays", Integer.class.getName(), null);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("task1");
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            getProcessAPI().executeFlowNode(userTask.getId());
            fail("The contract is not enforced");
        } catch (final FlowNodeExecutionException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
        }
        try {
            getProcessAPI().executeFlowNode(userTask.getId(), Collections.<String, Object> emptyMap());
            fail("The contract is not enforced");
        } catch (final ContractViolationException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
        }
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void runTaskWhenContractIsValid() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("task1", ACTOR_NAME);
        userTaskBuilder.addContract().addInput("numberOfDays", Integer.class.getName(), null)
        .addRule("mandatory", "numberOfDays != null", "numberOfDays must be set", "numberOfDays");
        userTaskBuilder.addIntegerData("result", null);
        userTaskBuilder.addOperation(new OperationBuilder().createSetDataOperation("result",
                new ExpressionBuilder().createGroovyScriptExpression("input", "numberOfDays", Integer.class.getName())));
        builder.addUserTask("task2", ACTOR_NAME).addTransition("task1", "task2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("task1");
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("numberOfDays", 8);
        getProcessAPI().executeFlowNode(userTask.getId(), inputs);
        waitForUserTask("task2");

        final ArchivedDataInstance archivedResult = getProcessAPI().getArchivedActivityDataInstance("result", userTask.getId());
        assertThat(archivedResult.getValue()).isEqualTo(8);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getExceptionWhenContractIsNotValidWithRules() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("task1", ACTOR_NAME).addContract().addInput("numberOfDays", Integer.class.getName(), null)
        .addRule("mandatory", "numberOfDays != null", "numberOfDays must be set", "numberOfDays");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("task1");
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            final Map<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("numberOfDays", null);
            getProcessAPI().executeFlowNode(userTask.getId(), inputs);
            fail("The contract is not enforced");
        } catch (final ContractViolationException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
        }

        try {
            final Map<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("numberOfDay", 9);
            getProcessAPI().executeFlowNode(userTask.getId(), inputs);
            fail("The contract is not enforced");
        } catch (final ContractViolationException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
        }

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorWithJNDILookupAndAPICall() throws Exception {
        final Expression localDataExpression = new ExpressionBuilder().createConstantLongExpression(0L);
        final Expression processNameExpression = new ExpressionBuilder().createConstantStringExpression(PROCESS_NAME);
        final Expression processVersionExpression = new ExpressionBuilder()
        .createGroovyScriptExpression("inputVersion", "inputVersion", String.class.getName());
        final Expression outputExpression = new ExpressionBuilder().createInputExpression("processInputId", Long.class.getName());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addLongData("processId", localDataExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("task3", ACTOR_NAME);
        userTaskBuilder.addConnector("myConnector", "org.bonitasoft.engine.connectors.TestConnectorWithAPICall", "1.0", ConnectorEvent.ON_FINISH)
        .addInput("processName", processNameExpression).addInput("processVersion", processVersionExpression)
        .addOutput(new OperationBuilder().createSetDataOperation("processId", outputExpression));
        userTaskBuilder.addContract().addInput("inputVersion", String.class.getName(), null).addInput("processInputId", Long.class.getName(), null);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnectorWithAPICall(designProcessDefinition);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("task3");

        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("inputVersion", PROCESS_VERSION);
        inputs.put("processInputId", 45L);
        getProcessAPI().executeFlowNode(userTask.getId(), inputs);

        waitForProcessToBeInState(processInstance, ProcessInstanceState.COMPLETED);

        disableAndDeleteProcess(processDefinition);
    }

    public ProcessDefinition deployAndEnableProcessWithTestConnectorWithAPICall(final ProcessDefinitionBuilder processDefinitionBuilder)
            throws InvalidBusinessArchiveFormatException, BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, ACTOR_NAME, matti, "TestConnectorWithAPICall.impl",
                TestConnectorWithAPICall.class, "TestConnectorWithAPICall.jar");
    }

}