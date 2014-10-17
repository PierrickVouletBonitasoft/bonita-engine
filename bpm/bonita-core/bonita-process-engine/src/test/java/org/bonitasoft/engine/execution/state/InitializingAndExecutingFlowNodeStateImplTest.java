package org.bonitasoft.engine.execution.state;

import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InitializingAndExecutingFlowNodeStateImplTest {

    @Mock
    private StateBehaviors stateBehaviors;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    @Mock
    private SProcessDefinition processDefinition;

    @InjectMocks
    private InitializingAndExecutingFlowNodeStateImpl initializingAndExecutingFlowNodeStateImpl;

    @Test
    public void testBeforeOnEnter() throws Exception {

        // when
        initializingAndExecutingFlowNodeStateImpl.beforeOnEnter(processDefinition, flowNodeInstance);

        // then
        verify(stateBehaviors).handleCatchEvents(processDefinition, flowNodeInstance);
        verify(stateBehaviors).createData(processDefinition, flowNodeInstance);
        verify(stateBehaviors).mapActors(flowNodeInstance, processDefinition.getProcessContainer());
    }

    @Test
    public void testOnEnterToOnFinish() throws Exception {

        // when
        initializingAndExecutingFlowNodeStateImpl.onEnterToOnFinish(processDefinition, flowNodeInstance);

        // then
        verify(stateBehaviors).updateDisplayNameAndDescription(processDefinition, flowNodeInstance);
        verify(stateBehaviors).handleCallActivity(processDefinition, flowNodeInstance);
    }

    @Test
    public void testAfterOnFinish() throws Exception {

        // when
        initializingAndExecutingFlowNodeStateImpl.afterOnFinish(processDefinition, flowNodeInstance);

        // then
        verify(stateBehaviors).updateDisplayDescriptionAfterCompletion(processDefinition, flowNodeInstance);
    }

}
