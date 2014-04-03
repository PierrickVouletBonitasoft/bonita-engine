/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.state;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.StateBehaviors;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ExecutingFlowNodeStateImpl extends FlowNodeStateWithConnectors {

    public ExecutingFlowNodeStateImpl(final StateBehaviors stateBehaviors) {
        super(stateBehaviors, false, true);
    }

    /**
     * @param stateBehaviors
     * @param b
     * @param c
     */
    public ExecutingFlowNodeStateImpl(final StateBehaviors stateBehaviors, final boolean executeOnEnter, final boolean executeOnFinish) {
        super(stateBehaviors, executeOnEnter, executeOnFinish);
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public boolean isInterrupting() {
        return false;
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String getName() {
        return "executing";
    }

    @SuppressWarnings("unused")
    @Override
    public boolean hit(final SProcessDefinition processDefinition, final SFlowNodeInstance parentInstance, final SFlowNodeInstance childInstance) {
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
        return true;
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.NORMAL;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "";
    }

    @SuppressWarnings("unused")
    @Override
    protected void beforeOnEnter(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
    }

    @SuppressWarnings("unused")
    @Override
    protected void onEnterToOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
    }

    @SuppressWarnings("unused")
    @Override
    protected void afterOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
    }

}
