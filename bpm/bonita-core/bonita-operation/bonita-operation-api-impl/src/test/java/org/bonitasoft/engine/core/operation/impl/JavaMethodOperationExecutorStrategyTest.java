/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.operation.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.model.SExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class JavaMethodOperationExecutorStrategyTest {

    @Test(expected = SOperationExecutionException.class)
    public void dontThrowNPEIfObjectDoesNotExist() throws Exception {
        final JavaMethodOperationExecutorStrategy strategy = new JavaMethodOperationExecutorStrategy();
        final SOperation operation = mock(SOperation.class);
        final SLeftOperand leftOperand = mock(SLeftOperand.class);
        final SExpression rightOperand = mock(SExpression.class);

        when(operation.getLeftOperand()).thenReturn(leftOperand);
        when(leftOperand.getName()).thenReturn("unknownData");
        when(leftOperand.getType()).thenReturn(SLeftOperand.TYPE_EXTERNAL_DATA);
        when(operation.getRightOperand()).thenReturn(rightOperand);
        when(operation.getOperator()).thenReturn("=");
        when(rightOperand.getReturnType()).thenReturn(Object.class.getName());

        final SExpressionContext expressionContext = new SExpressionContext(123L, DataInstanceContainer.PROCESS_INSTANCE.name(), 1234L);
        expressionContext.setInputValues(Collections.<String, Object> emptyMap());
        strategy.computeNewValueForLeftOperand(operation, "Update", expressionContext);
    }

    @Test
    public void computeValue() throws Exception {
        final JavaMethodOperationExecutorStrategy strategy = new JavaMethodOperationExecutorStrategy();
        final SOperation operation = mock(SOperation.class);
        final SLeftOperand leftOperand = mock(SLeftOperand.class);
        final SExpression rightOperand = mock(SExpression.class);

        when(operation.getLeftOperand()).thenReturn(leftOperand);
        when(leftOperand.getName()).thenReturn("myData");
        when(operation.getRightOperand()).thenReturn(rightOperand);
        when(operation.getOperator()).thenReturn("setThing:int");
        when(rightOperand.getReturnType()).thenReturn(Integer.class.getName());

        final SExpressionContext expressionContext = new SExpressionContext(123L, DataInstanceContainer.PROCESS_INSTANCE.name(), 1234L);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("myData", new MyClass());
        expressionContext.setInputValues(map);
        MyClass updated = (MyClass) strategy.computeNewValueForLeftOperand(operation, 12, expressionContext);

        assertEquals(12, updated.getThing());
    }

    public class MyClass {

        private int thing = 0;

        public void setThing(final int thing) {
            this.thing = thing;
        }

        public int getThing() {
            return thing;
        }

    }

}
