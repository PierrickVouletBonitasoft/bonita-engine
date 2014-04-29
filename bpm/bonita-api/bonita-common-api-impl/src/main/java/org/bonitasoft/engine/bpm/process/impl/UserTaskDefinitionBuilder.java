/**
 * Copyright (C) 2011-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.HumanTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.UserTaskDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Feng Hui
 * @author Celine Souchet
 */
public class UserTaskDefinitionBuilder extends ActivityDefinitionBuilder {

    public UserTaskDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final String name, final String actorName) {
        super(container, processDefinitionBuilder, getUserTaskActivity(name, actorName));
    }

    private static UserTaskDefinitionImpl getUserTaskActivity(final String name, final String actorName) {
        return new UserTaskDefinitionImpl(name, actorName);
    }

    public UserFilterDefinitionBuilder addUserFilter(final String name, final String userFilterId, final String version) {
        return new UserFilterDefinitionBuilder(getProcessBuilder(), getContainer(), name, userFilterId, version, (HumanTaskDefinitionImpl) getActivity());
    }

    public UserTaskDefinitionBuilder addExpectedDuration(final long time) {
        ((UserTaskDefinitionImpl) getActivity()).setExpectedDuration(time);
        return this;
    }

    public UserTaskDefinitionBuilder addPriority(final String priority) {
        ((UserTaskDefinitionImpl) getActivity()).setPriority(priority);
        return this;
    }

}
