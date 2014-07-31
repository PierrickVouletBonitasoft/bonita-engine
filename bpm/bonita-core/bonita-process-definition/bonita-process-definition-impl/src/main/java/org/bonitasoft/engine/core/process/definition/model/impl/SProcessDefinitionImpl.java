/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Matthieu Chaffotte
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class SProcessDefinitionImpl extends SNamedElementImpl implements SProcessDefinition {

    private static final long serialVersionUID = -8961400385282752731L;

    private final String version;

    private String description;

    private final Set<SParameterDefinition> parameters;

    private final Set<SActorDefinition> actors;

    private SActorDefinition sActorInitiator;

    private SFlowElementContainerDefinition container;

    private String stringIndexLabel1;

    private String stringIndexLabel2;

    private String stringIndexLabel3;

    private String stringIndexLabel4;

    private String stringIndexLabel5;

    private SExpression stringIndexValue1;

    private SExpression stringIndexValue2;

    private SExpression stringIndexValue3;

    private SExpression stringIndexValue4;

    private SExpression stringIndexValue5;

    public SProcessDefinitionImpl(final DesignProcessDefinition processDefinition) {
        super(processDefinition.getName());
        description = processDefinition.getDescription();
        version = processDefinition.getVersion();
        actors = new HashSet<SActorDefinition>();
        stringIndexLabel1 = processDefinition.getStringIndexLabel(1);
        stringIndexLabel2 = processDefinition.getStringIndexLabel(2);
        stringIndexLabel3 = processDefinition.getStringIndexLabel(3);
        stringIndexLabel4 = processDefinition.getStringIndexLabel(4);
        stringIndexLabel5 = processDefinition.getStringIndexLabel(5);
        stringIndexValue1 = ServerModelConvertor.convertExpression(processDefinition.getStringIndexValue(1));
        stringIndexValue2 = ServerModelConvertor.convertExpression(processDefinition.getStringIndexValue(2));
        stringIndexValue3 = ServerModelConvertor.convertExpression(processDefinition.getStringIndexValue(3));
        stringIndexValue4 = ServerModelConvertor.convertExpression(processDefinition.getStringIndexValue(4));
        stringIndexValue5 = ServerModelConvertor.convertExpression(processDefinition.getStringIndexValue(5));
        for (final ActorDefinition actor : processDefinition.getActorsList()) {
            actors.add(new SActorDefinitionImpl(actor));
        }

        parameters = new HashSet<SParameterDefinition>();
        for (final ParameterDefinition parameterDefinition : processDefinition.getParameters()) {
            parameters.add(new SParameterDefinitionImpl(parameterDefinition));
        }

        final ActorDefinition actorInitiator = processDefinition.getActorInitiator();
        if (actorInitiator != null) {
            sActorInitiator = new SActorDefinitionImpl(actorInitiator);
        }

        container = new SFlowElementContainerDefinitionImpl(this, processDefinition.getProcessContainer());
    }

    public SProcessDefinitionImpl(final String name, final String version) {
        super(name);
        this.version = version;
        actors = new HashSet<SActorDefinition>();
        parameters = new HashSet<SParameterDefinition>();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Set<SParameterDefinition> getParameters() {
        return parameters;
    }

    @Override
    public SParameterDefinition getParameter(final String parameterName) {
        final Iterator<SParameterDefinition> iterator = parameters.iterator();
        SParameterDefinition found = null;
        while (found == null && iterator.hasNext()) {
            final SParameterDefinition next = iterator.next();
            if (next.getName().equals(parameterName)) {
                found = next;
            }
        }
        return found;
    }

    @Override
    public Set<SActorDefinition> getActors() {
        return actors;
    }

    public void addActor(final SActorDefinition actor) {
        actors.add(actor);
    }

    public void addParameter(final SParameterDefinition parameter) {
        parameters.add(parameter);
    }

    @Override
    public SActorDefinition getActorInitiator() {
        return sActorInitiator;
    }

    public void setActorInitiator(final SActorDefinition sActorInitiator) {
        this.sActorInitiator = sActorInitiator;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public SFlowElementContainerDefinition getProcessContainer() {
        return container;
    }

    public void setProcessContainer(final SFlowElementContainerDefinition processContainer) {
        container = processContainer;
    }

    @Override
    public String getStringIndexLabel(final int index) {
        switch (index) {
            case 1:
                return stringIndexLabel1;
            case 2:
                return stringIndexLabel2;
            case 3:
                return stringIndexLabel3;
            case 4:
                return stringIndexLabel4;
            case 5:
                return stringIndexLabel5;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    public void setStringIndex(final int index, final String label, final SExpression initialValue) {
        switch (index) {
            case 1:
                stringIndexLabel1 = label;
                stringIndexValue1 = initialValue;
                break;
            case 2:
                stringIndexLabel2 = label;
                stringIndexValue2 = initialValue;
                break;
            case 3:
                stringIndexLabel3 = label;
                stringIndexValue3 = initialValue;
                break;
            case 4:
                stringIndexLabel4 = label;
                stringIndexValue4 = initialValue;
                break;
            case 5:
                stringIndexLabel5 = label;
                stringIndexValue5 = initialValue;
                break;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public SExpression getStringIndexValue(final int index) {
        switch (index) {
            case 1:
                return stringIndexValue1;
            case 2:
                return stringIndexValue2;
            case 3:
                return stringIndexValue3;
            case 4:
                return stringIndexValue4;
            case 5:
                return stringIndexValue5;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public boolean hasConnectors() {
        return getProcessContainer().getConnectors().size() > 0;
    }

}
