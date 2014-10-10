/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution;

import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;

/**
 * @author Elias Ricken de Medeiros
 */
public class NormalStateTransitionsManager {

    protected Map<Integer, FlowNodeState> stateTransitions;

    private final SFlowNodeInstance flowNodeInstance;

    public NormalStateTransitionsManager(Map<Integer, FlowNodeState> stateTransitions, SFlowNodeInstance flowNodeInstance) {
        this.stateTransitions = stateTransitions;
        this.flowNodeInstance = flowNodeInstance;
    }

    public FlowNodeState getNextState(final FlowNodeState currentState) throws SIllegalStateTransition {
        FlowNodeState nextState = getNextStateFromMap(currentState);
        if (nextState == null) {
            throw new SIllegalStateTransition(getMessage(currentState), currentState.isTerminal());
        }
        return nextState;
    }

    private String getMessage(FlowNodeState currentState) {
        StringBuilder stb = new StringBuilder();
        stb.append("no state found after ");
        stb.append(currentState.getClass().getName());
        stb.append(" for flow node of type ");
        stb.append(flowNodeInstance.getClass().getName());
        stb.append(" in state category ");
        stb.append(flowNodeInstance.getStateCategory());
        stb.append(". Flow node instance id = ");
        stb.append(flowNodeInstance.getId());
        return stb.toString();
    }

    protected FlowNodeState getNextStateFromMap(FlowNodeState currentState) {
        return stateTransitions.get(currentState.getId());
    }

}
