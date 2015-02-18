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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.execution.SIllegalStateTransition;
import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.work.BonitaWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class FailureHandlingBonitaWorkTest {

    @Mock
    private BonitaWork wrappedWork;

    @Mock
    private TenantServiceAccessor tenantAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private IncidentService incidentService;

    @Mock
    private TechnicalLoggerService loggerService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private SProcessDefinitionDeployInfo sProcessDefinitionDeployInfo;

    private FailureHandlingBonitaWork txBonitawork;

    @Before
    public void before() {
        txBonitawork = spy(new FailureHandlingBonitaWork(wrappedWork));
        doReturn(false).when(loggerService).isLoggable(eq(txBonitawork.getClass()), any(TechnicalLogSeverity.class));

        when(tenantAccessor.getTechnicalLoggerService()).thenReturn(loggerService);
        when(tenantAccessor.getSessionAccessor()).thenReturn(sessionAccessor);
        when(tenantAccessor.getSessionService()).thenReturn(sessionService);
        when(tenantAccessor.getIncidentService()).thenReturn(incidentService);
        doReturn(tenantAccessor).when(txBonitawork).getTenantAccessor();
    }

    @Test
    public void testWork() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void testWorkFailureIsHandled() throws Throwable {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        final Exception e = new Exception();
        doThrow(e).when(wrappedWork).work(singletonMap);
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
        verify(wrappedWork, times(1)).handleFailure(e, singletonMap);
    }

    @Test
    public void testFailureHandlingFail() throws Throwable {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        final Exception e1 = new Exception();
        final Exception e2 = new Exception();
        doThrow(e1).when(wrappedWork).work(singletonMap);
        doThrow(e2).when(wrappedWork).handleFailure(e1, singletonMap);
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
        verify(wrappedWork, times(1)).handleFailure(e1, singletonMap);
        verify(incidentService, times(1)).report(eq(0l), any(Incident.class));
    }

    @Test
    public void putInMap() {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        txBonitawork.work(singletonMap);
        assertEquals(tenantAccessor, singletonMap.get("tenantAccessor"));
    }

    @Test
    public void getDescription() {
        when(wrappedWork.getDescription()).thenReturn("The description");
        assertEquals("The description", txBonitawork.getDescription());
    }

    @Test
    public void getRecoveryProcedure() {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", txBonitawork.getRecoveryProcedure());
    }

    @Test
    public void handleFailure() throws Throwable {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };
        txBonitawork.handleFailure(e, context);
        verify(wrappedWork).handleFailure(e, context);
    }

    @Test
    public void getTenantId() {
        when(wrappedWork.getTenantId()).thenReturn(12l);
        assertEquals(12, txBonitawork.getTenantId());
    }

    @Test
    public void setTenantId() {
        txBonitawork.setTenantId(12l);
        verify(wrappedWork).setTenantId(12l);
    }

    @Test
    public void getWrappedWork() {
        assertEquals(wrappedWork, txBonitawork.getWrappedWork());
    }

    @Test
    public void testToString() {
        when(wrappedWork.toString()).thenReturn("the to string");
        assertEquals("the to string", txBonitawork.toString());
    }

    @Test
    public void doNotHandleFailureWhenGettingASFlowNodeNotFoundException() throws Throwable {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SFlowNodeNotFoundException(83));
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.ERROR);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.ERROR);
    }

    @Test
    public void doNotHandleFailureWhenGettingASFlowNodeNotFoundExceptionAsMainCause() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception flownodeNotFound = new SFlowNodeNotFoundException(11547L);
        doThrow(flownodeNotFound).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(flownodeNotFound, context);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.ERROR);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.ERROR);
    }

    @Test
    public void doNotHandleFailureWhenGettingASProcessInstanceNotFoundException() throws Throwable {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SProcessInstanceNotFoundException(83));
        doThrow(e).when(wrappedWork).work(context);
        when(wrappedWork.getDescription()).thenReturn("");
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.ERROR);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.ERROR);
    }

    @Test
    public void doNotHandleFailureWhenGettingASProcessDefinitionNotFoundException() throws Throwable {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SProcessDefinitionNotFoundException("message", 2));
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.ERROR);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.ERROR);
    }

    public void doNotHandleFailureWhenIsIllegalTransitionFromTerminalState() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SIllegalStateTransition("message", true));
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(FailureHandlingBonitaWork.class, TechnicalLogSeverity.ERROR);
    }

    @Test
    public void handleFailureWhenIsIllegalTransitionFromNonTerminalState() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SIllegalStateTransition("message", false));
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, times(1)).handleFailure(e, context);
    }

    @Test
    public void handleFailureForAllOtherExceptions() throws Throwable {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception();
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, times(1)).handleFailure(e, context);
    }

}
