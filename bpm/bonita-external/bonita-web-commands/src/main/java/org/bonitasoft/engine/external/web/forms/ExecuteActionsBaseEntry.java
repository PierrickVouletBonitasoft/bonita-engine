/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.external.web.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SLeftOperandBuilderFactory;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilderFactory;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Ruiheng Fan
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class ExecuteActionsBaseEntry extends CommandWithParameters {

    protected static final String ACTIVITY_INSTANCE_ID_KEY = "ACTIVITY_INSTANCE_ID_KEY";

    protected static final String PROCESS_DEFINITION_ID_KEY = "PROCESS_DEFINITION_ID_KEY";

    protected static final String OPERATIONS_LIST_KEY = "OPERATIONS_LIST_KEY";

    protected static final String OPERATIONS_INPUT_KEY = "OPERATIONS_INPUT_KEY";

    protected static final String CONNECTORS_LIST_KEY = "CONNECTORS_LIST_KEY";

    protected static final String USER_ID_KEY = "USER_ID_KEY";

    protected static TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final BonitaRuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected void log(final TenantServiceAccessor tenantAccessor, final Exception e) {
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
    }

    protected long getTenantId() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            return sessionAccessor.getTenantId();
        } catch (final STenantIdNotSetException e) {
            throw new BonitaRuntimeException(e);
        } catch (final BonitaRuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected SOperation toSOperation(final Operation operation) throws SInvalidExpressionException {
        final SExpression rightOperand = toSExpression(operation.getRightOperand());
        final SOperatorType operatorType = SOperatorType.valueOf(operation.getType().name());
        final SLeftOperand sLeftOperand = toSLeftOperand(operation.getLeftOperand());
        final SOperation sOperation = BuilderFactory.get(SOperationBuilderFactory.class).createNewInstance().setOperator(operation.getOperator())
                .setRightOperand(rightOperand).setType(operatorType).setLeftOperand(sLeftOperand).done();
        return sOperation;
    }

    protected SLeftOperand toSLeftOperand(final LeftOperand variableToSet) {
        return BuilderFactory.get(SLeftOperandBuilderFactory.class).createNewInstance().setName(variableToSet.getName()).done();
    }

    protected SExpression toSExpression(final Expression exp) throws SInvalidExpressionException {
        final List<SExpression> dependencies = new ArrayList<SExpression>(exp.getDependencies().size());
        if (!exp.getDependencies().isEmpty()) {
            for (final Expression dependency : exp.getDependencies()) {
                dependencies.add(toSExpression(dependency));
            }
        }
        final SExpression sExpression = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance().setName(exp.getName())
                .setContent(exp.getContent())
                .setExpressionType(exp.getExpressionType()).setInterpreter(exp.getInterpreter()).setReturnType(exp.getReturnType())
                .setDependencies(dependencies).done();
        return sExpression;
    }

    protected List<SOperation> toSOperation(final List<Operation> operations) throws SInvalidExpressionException {
        if (operations == null) {
            return null;
        }
        if (operations.isEmpty()) {
            return Collections.emptyList();
        }
        final List<SOperation> sOperations = new ArrayList<SOperation>(operations.size());
        for (final Operation operation : operations) {
            final SOperation sOperation = toSOperation(operation);
            sOperations.add(sOperation);
        }
        return sOperations;
    }

    protected SActivityInstance getSActivityInstance(final TenantServiceAccessor tenantAccessor, final long activityInstanceId) throws SActivityReadException,
            SActivityInstanceNotFoundException {
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        return activityInstanceService.getActivityInstance(activityInstanceId);
    }

    protected ProcessInstance getProcessInstance(final TenantServiceAccessor tenantAccessor, final long processInstanceId)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SProcessInstance sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
        return ModelConvertor.toProcessInstances(Collections.singletonList(sProcessInstance), processDefinitionService).get(0);
    }

    protected ClassLoader getLocalClassLoader(final TenantServiceAccessor tenantAccessor, final long processDefinitionId) throws SClassLoaderException {
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        return classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
    }

    @Deprecated
    protected SProcessDefinition getServerProcessDefinition(final long processDefinitionId, final ProcessDefinitionService processDefinitionService)
            throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
        return processDefinitionService.getProcessDefinition(processDefinitionId);
    }

    protected SProcessDefinition getProcessDefinition(final TenantServiceAccessor tenantAccessor, final long processDefinitionId)
            throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        return processDefinitionService.getProcessDefinition(processDefinitionId);
    }

}
