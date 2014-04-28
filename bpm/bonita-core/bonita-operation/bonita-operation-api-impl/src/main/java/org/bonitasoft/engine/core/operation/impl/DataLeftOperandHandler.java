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
 ** 
 * @since 6.2
 */
package org.bonitasoft.engine.core.operation.impl;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * 
 */
public class DataLeftOperandHandler implements LeftOperandHandler {

    private final DataInstanceService dataInstanceService;

    @Override
    public String getType() {
        return "DATA";
    }

    public DataLeftOperandHandler(final DataInstanceService dataInstanceService) {
        this.dataInstanceService = dataInstanceService;
    }

    @Override
    public void update(final SLeftOperand leftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
            updateDataInstance(leftOperand, containerId, containerType, newValue);
        }

    protected void update(final SDataInstance sDataInstance, final Object content) throws SDataInstanceException {
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
        updateDescriptor.addField(fact.getValueKey(), content);

        dataInstanceService.updateDataInstance(sDataInstance, updateDescriptor);
    }

    private void updateDataInstance(final SLeftOperand leftOperand, final long containerId, final String containerType, final Object expressionResult)
            throws SOperationExecutionException {
        final String dataInstanceName = leftOperand.getName();
        SDataInstance sDataInstance;
        try {
            sDataInstance = getDataInstance(dataInstanceName, containerId, containerType);
            update(sDataInstance, expressionResult);
        } catch (final SDataInstanceException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public Object retrieve(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext) throws SBonitaReadException {
        try {
            return getDataInstance(sLeftOperand.getName(), expressionContext.getContainerId(), expressionContext.getContainerType()).getValue();
        } catch (SDataInstanceException e) {
            throw new SBonitaReadException("Unable to retrieve the data", e);
        }
    }

    protected SDataInstance getDataInstance(final String dataInstanceName, final long containerId, final String containerType) throws SDataInstanceException {
        return dataInstanceService.getDataInstance(dataInstanceName, containerId, containerType);
    }

}
