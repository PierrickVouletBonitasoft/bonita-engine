/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.work;

import static org.mockito.Mockito.doReturn;

import java.util.Collections;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.execution.work.failurewrapping.FlowNodeDefinitionAndInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.GlobalContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.MessageInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.ProcessDefinitionContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.ProcessInstanceContextWork;
import org.bonitasoft.engine.work.BonitaWork;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Aurelien Pupier
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkFactoryTest {

    @Mock
    private SMessageInstance messageInstance;

    @Mock
    private SWaitingMessageEvent waitingMessageEvent;

    @Test
    public void createExecuteMessageCoupleWorkHasNoLockProcessInstanceWorkIfNoTargetProcess() {
        doReturn(-1L).when(waitingMessageEvent).getParentProcessInstanceId();
        WrappingBonitaWork work = (WrappingBonitaWork) WorkFactory.createExecuteMessageCoupleWork(messageInstance, waitingMessageEvent);
        boolean containsLockProcessInstance = containsLockProcessInstanceWork(work);
        Assert.assertFalse("A lock Process Instance Work is used although there is no Target process", containsLockProcessInstance);
        Assert.assertTrue("A GlobalContextWork is missing", containsGlobalContextWork(work));
    }

    @Test
    public void createExecuteMessageCoupleWorkWithLockProcessInstanceWork() {
        doReturn(1L).when(waitingMessageEvent).getParentProcessInstanceId();
        WrappingBonitaWork work = (WrappingBonitaWork) WorkFactory.createExecuteMessageCoupleWork(messageInstance, waitingMessageEvent);
        boolean containsLockProcessInstance = containsLockProcessInstanceWork(work);
        Assert.assertTrue("A lock Process Instance Work is missing although there is a Target process", containsLockProcessInstance);
        Assert.assertTrue("A GlobalContextWork is missing", containsGlobalContextWork(work));
    }

    @Test
    public void createExecuteMessageCoupleWork() {
        WrappingBonitaWork work = (WrappingBonitaWork) WorkFactory.createExecuteMessageCoupleWork(messageInstance, waitingMessageEvent);
        Assert.assertTrue("A MessageInstanceContextWork is missing", containsFailureHandlingMessageInstance(work));
        Assert.assertTrue("A FailureHandlingProcessDefinitionCOntextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A GlobalContextWork is missing", containsGlobalContextWork(work));
    }

    @Test
    public void createExecuteFlowNode() {
        WrappingBonitaWork work = (WrappingBonitaWork) WorkFactory.createExecuteFlowNodeWork(1L, 1L, 3, Collections.<SOperation> emptyList(), null);
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingFlowNodeInstance(work));
        Assert.assertTrue("A GlobalContextWork is missing", containsGlobalContextWork(work));
    }

    @Test
    public void createExecuteConnectorOfProcess() {
        WrappingBonitaWork work = (WrappingBonitaWork) WorkFactory.createExecuteConnectorOfProcess(1L, 2L, 4L, 3L, "connectorDefName", ConnectorEvent.ON_ENTER);
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A GlobalContextWork is missing", containsGlobalContextWork(work));
    }

    @Test
    public void createExecuteConnectorOfActivity() {
        WrappingBonitaWork work = (WrappingBonitaWork) WorkFactory.createExecuteConnectorOfActivity(1L, 3L, 4L, 5L, 6, "connectorDefName");
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
        Assert.assertTrue("A GlobalContextWork is missing", containsGlobalContextWork(work));
    }

    @Test
    public void createNotifyChildFinishedWork() {
        WrappingBonitaWork work = (WrappingBonitaWork) WorkFactory.createNotifyChildFinishedWork(1L, 2L, 3L, 4L, "parentType");
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingFlowNodeInstance(work));
        Assert.assertTrue("A GlobalContextWork is missing", containsGlobalContextWork(work));
    }

    private boolean containsGlobalContextWork(WrappingBonitaWork work) {
		return containsWorkOfClass(work, GlobalContextWork.class);
	}

	private boolean containsFailureHandlingFlowNodeInstance(WrappingBonitaWork work) {
        return containsWorkOfClass(work, FlowNodeDefinitionAndInstanceContextWork.class);
    }

    private boolean containsFailureHandlingProcessInstance(WrappingBonitaWork work) {
        return containsWorkOfClass(work, ProcessInstanceContextWork.class);
    }

    private boolean containsFailureHandlingProcessDefinition(WrappingBonitaWork work) {
        return containsWorkOfClass(work, ProcessDefinitionContextWork.class);
    }

    private boolean containsFailureHandlingMessageInstance(WrappingBonitaWork work) {
        return containsWorkOfClass(work, MessageInstanceContextWork.class);
    }

    private boolean containsLockProcessInstanceWork(WrappingBonitaWork work) {
        return containsWorkOfClass(work, LockProcessInstanceWork.class);
    }

    private boolean containsWorkOfClass(WrappingBonitaWork work, Class<?> clazz) {
        boolean containsLockProcessInstance = clazz.isAssignableFrom(work.getClass());
        while (work.getWrappedWork() != null && !containsLockProcessInstance) {
            final BonitaWork wrappedWork = work.getWrappedWork();
            if (wrappedWork instanceof WrappingBonitaWork) {
                work = (WrappingBonitaWork) work.getWrappedWork();
                containsLockProcessInstance = clazz.isAssignableFrom(work.getClass());
            } else {
                break;
            }
        }
        return containsLockProcessInstance;
    }

}
