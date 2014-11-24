/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.event;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CallActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.MultiInstanceActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class ErrorBoundaryEventTest extends AbstractEventTest {

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event", "call activity" }, jira = "ENGINE-501")
    public void errorBoundaryEventTriggeredNamedError() throws Exception {
        executionWitherrorEventTriggered("error1");
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event", "call activity" }, jira = "ENGINE-501")
    public void errorBoundaryEventTriggeredCatchAllError() throws Exception {
        executionWitherrorEventTriggered(null);
    }

    protected void executionWitherrorEventTriggered(final String catchErrorCode) throws Exception {
        final ProcessDefinition calledProcDef = deployAndEnableProcessWithEndThrowErrorEvent("calledProcess", "error1");
        final ProcessDefinition callerProcDef = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("pErrorBoundary", "calledProcess", "callStep",
                catchErrorCode, "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcDef.getId());
        final FlowNodeInstance callActivity = waitForFlowNodeInExecutingState(processInstance, "callStep", false);
        final ActivityInstance calledStep1 = waitForUserTask("calledStep1", processInstance);
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance);
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep1.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep1, donaBenta.getId());

        waitForProcessToFinish(calledProcessInstance);
        try {
            waitForArchivedActivity(calledStep2.getId(), TestStates.ABORTED);
        } catch (final Exception e) {
            final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstance.getId(), 0, 100,
                    ActivityInstanceCriterion.DEFAULT);
            System.out.println("After completion of the called process");
            for (final ArchivedActivityInstance archivedActivityInstance : archivedActivityInstances) {
                System.out.println("name=" + archivedActivityInstance.getName() + ", state=" + archivedActivityInstance.getState() + ", archivedDate="
                        + archivedActivityInstance.getArchiveDate().getTime());
            }
            throw new Exception(archivedActivityInstances.toString(), e);
        }
        final ActivityInstance executionStep = waitForUserTask(EXCEPTION_STEP, processInstance);

        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(callActivity.getId(), TestStates.ABORTED);
        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(calledProcDef, callerProcDef);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event", "call activity" }, jira = "ENGINE-501")
    public void errorBoundaryEventNotTriggered() throws Exception {
        final ProcessDefinition calledProcDef = deployAndEnableProcessWithEndThrowErrorEvent("calledProcess", "error1");
        final ProcessDefinition callerProcDef = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("pErrorBoundary", "calledProcess", "callStep",
                "error1", "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcDef.getId());
        final ActivityInstance calledStep1 = waitForUserTask("calledStep1", processInstance.getId());
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance.getId());
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep1.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep2, donaBenta.getId());

        waitForProcessToFinish(calledProcessInstance);
        waitForFlowNodeInState(processInstance, "calledStep1", TestStates.ABORTED, true);

        final ActivityInstance executionStep = waitForUserTask("step2", processInstance.getId());
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(calledProcDef, callerProcDef);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event" }, jira = "ENGINE-501")
    public void uncaughtThrowErrorEvent() throws Exception {
        final ProcessDefinition calledProcDef = deployAndEnableProcessWithEndThrowErrorEvent("calledProcess", "error1");
        // catch a different error
        final ProcessDefinition callerProcDef = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("pErrorBoundary", "calledProcess", "callStep",
                "error2", "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcDef.getId());
        waitForFlowNodeInExecutingState(processInstance, "callStep", false);
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance.getId());
        waitForUserTaskAndExecuteIt("calledStep1", processInstance, donaBenta);

        waitForArchivedActivity(calledStep2.getId(), TestStates.ABORTED);
        // if there are no catch error able to handle the thrown error, the throw error event has the same behavior as a terminate event.
        waitForUserTaskAndExecuteIt("step2", processInstance, donaBenta);

        waitForProcessToFinish(processInstance);

        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(calledProcDef, callerProcDef);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event" }, jira = "ENGINE-501")
    public void errorEventCaughtAtParentLevel2() throws Exception {
        final ProcessDefinition procDefLevel0 = deployAndEnableProcessWithEndThrowErrorEvent("procDefLevel0", "error1");
        final ProcessDefinition procDefLevel1 = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("procDefLevel1", "procDefLevel0", "callStepL1",
                "error2", "delivery");
        final ProcessDefinition procDefLevel2 = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("procDefLevel2", "procDefLevel1", "callStepL2",
                "error1", "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(procDefLevel2.getId());
        final FlowNodeInstance callActivityL2 = waitForFlowNodeInExecutingState(processInstance, "callStepL2", false);
        final FlowNodeInstance callActivityL1 = waitForFlowNodeInExecutingState(processInstance, "callStepL1", true);
        final HumanTaskInstance calledStep2 = waitForUserTask("calledStep2", processInstance);
        final ActivityInstance calledStep1 = waitForUserTaskAndExecuteIt("calledStep1", processInstance, donaBenta);

        final ProcessInstance calledProcessInstanceL0 = getProcessAPI().getProcessInstance(calledStep1.getParentProcessInstanceId());
        final ProcessInstance calledProcessInstanceL1 = getProcessAPI().getProcessInstance(callActivityL1.getParentProcessInstanceId());

        waitForArchivedActivity(calledStep2.getId(), TestStates.ABORTED);
        final FlowNodeInstance executionStep = waitForFlowNodeInReadyState(processInstance, EXCEPTION_STEP, false);
        waitForProcessToFinish(calledProcessInstanceL0);
        waitForProcessToFinish(calledProcessInstanceL1, TestStates.ABORTED);

        assignAndExecuteStep(executionStep.getId(), donaBenta.getId());
        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(callActivityL1.getId(), TestStates.ABORTED);
        waitForArchivedActivity(callActivityL2.getId(), TestStates.ABORTED);
        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(procDefLevel0, procDefLevel1, procDefLevel2);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event" }, jira = "ENGINE-501")
    public void errorEventTwoCatchErrorMatching() throws Exception {
        final ProcessDefinition procDefLevel0 = deployAndEnableProcessWithEndThrowErrorEvent("procDefLevel0", "error1");
        final ProcessDefinition procDefLevel1 = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("procDefLevel1", "procDefLevel0", "callStepL1",
                "error1", "delivery");
        final ProcessDefinition procDefLevel2 = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("procDefLevel2", "procDefLevel1", "callStepL2",
                "error1", "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(procDefLevel2.getId());
        final FlowNodeInstance callActivityL2 = waitForFlowNodeInExecutingState(processInstance, "callStepL2", false);
        final FlowNodeInstance callActivityL1 = waitForFlowNodeInExecutingState(processInstance, "callStepL1", true);
        final ActivityInstance calledStep1 = waitForUserTask("calledStep1", processInstance);
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance);
        final ProcessInstance calledProcessInstanceL0 = getProcessAPI().getProcessInstance(calledStep1.getParentProcessInstanceId());
        final ProcessInstance calledProcessInstanceL1 = getProcessAPI().getProcessInstance(callActivityL1.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep1, donaBenta.getId());

        waitForArchivedActivity(calledStep2.getId(), TestStates.ABORTED);
        final FlowNodeInstance executionStep = waitForFlowNodeInReadyState(calledProcessInstanceL1, EXCEPTION_STEP, false);
        waitForProcessToFinish(calledProcessInstanceL0);

        assignAndExecuteStep(executionStep.getId(), donaBenta.getId());
        waitForProcessToFinish(calledProcessInstanceL1);

        waitForUserTaskAndExecuteIt("step2", processInstance, donaBenta);
        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(callActivityL1.getId(), TestStates.ABORTED);
        waitForArchivedActivity(callActivityL2.getId(), TestStates.NORMAL_FINAL);
        checkWasntExecuted(calledProcessInstanceL1, "step2");

        disableAndDeleteProcess(procDefLevel0, procDefLevel1, procDefLevel2);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class, MultiInstanceActivityInstance.class, CallActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = {
            "error", "boundary", "event", "call activity", "mutliple instance" }, jira = "ENGINE-9023")
    public void errorCodeThrownBySubProcessShouldBeCatchByMainProcess() throws Exception {
        final ProcessDefinition subProcess = deployAndEnableSubProcessWhichThrowsAnErrorEvent("SubProcess", "Mistake");
        final ProcessDefinition midProcess = deployAndEnableMidProcessWhichContainsACallActivity("MidProcess", "SubProcess");
        final ProcessDefinition mainProcess = deployAndEnableProcessWithBoundaryErrorEventOnMICallActivity("Process", "MidProcess", "Mistake", "acme");

        final ProcessInstance instance = getProcessAPI().startProcess(mainProcess.getId());
        waitForFlowNodeInReadyState(instance, EXCEPTION_STEP, true);
        waitForFlowNodeInState(instance, "step1", TestStates.ABORTED, false);

        disableAndDeleteProcess(mainProcess, midProcess, subProcess);
    }

    @Cover(jira = "BS-9484", classes = { MultiInstanceActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "error event", "multi instance" })
    @Test
    public void processWithMIUserTaskWithErrorEvent_should_take_the_error_flow() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = BuildTestUtil.buildProcessDefinitionWithMultiInstanceUserTaskAndFailedConnector(PROCESS_NAME,
                "step1");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnectorThatThrowException(processDefinitionBuilder);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", donaBenta);
        final ActivityInstance errorStep = waitForUserTask("errorFlow");
        assignAndExecuteStep(errorStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition.getId());
    }

}
