/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl.transaction.expression.bdm;

import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerLazyLoaderTest {

    public class Addresses implements Serializable {

        private static final long serialVersionUID = 1L;

        public String getCity() {
            return "city";
        }
    }

    public class Employee {

        public Employee() {

        }

        public List<Addresses> getAddresses() {
            return new ArrayList<Addresses>();
        }

        public String getName() {
            return "name";
        }

    }

    @Mock
    private CommandAPI commandAPI;

    @Mock
    private BusinessDataRepository businessDataRepository;

    private ServerLazyLoader serverLazyLoader;

    final long persistenceId = 22L;

    Employee employee = new Employee();

    @Before
    public void setUp() throws Exception {
        serverLazyLoader = spy(new ServerLazyLoader(businessDataRepository));

    }

    @Test
    public void should_load_list_of_objects() throws Exception {
        //given
        final Method method = employee.getClass().getMethod("getAddresses");

        //when
        serverLazyLoader.load(method, persistenceId);

        final String queryName = "Addresses.findAddressesByEmployeePersistenceId";
        final Class<? extends Serializable> resultClass = Addresses.class;
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("persistenceId", persistenceId);
        final int startIndex = 0;
        final int maxResults = Integer.MAX_VALUE;

        //then
        verify(businessDataRepository).findListByNamedQuery(queryName, resultClass, parameters, startIndex, maxResults);
        verify(businessDataRepository, never()).findByNamedQuery(queryName, resultClass, parameters);

    }

    @Test
    public void should_load_single_object() throws Exception {
        //given
        final String queryName = "String.findNameByEmployeePersistenceId";
        final Class<? extends Serializable> resultClass = String.class;
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("persistenceId", persistenceId);
        final int startIndex = 0;
        final int maxResults = Integer.MAX_VALUE;
        final Method method = employee.getClass().getMethod("getName");

        //when
        serverLazyLoader.load(method, persistenceId);

        //then
        verify(businessDataRepository, never()).findListByNamedQuery(queryName, resultClass, parameters, startIndex, maxResults);
        verify(businessDataRepository).findByNamedQuery(queryName, resultClass, parameters);

    }

    @Test(expected = RuntimeException.class)
    public void should_load_single_object_throw_exception() throws Exception {
        final String queryName = "String.findNameByEmployeePersistenceId";
        final Class<? extends Serializable> resultClass = String.class;
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("persistenceId", persistenceId);
        final Method method = employee.getClass().getMethod("getName");

        //given
        doThrow(NonUniqueResultException.class).when(businessDataRepository).findByNamedQuery(queryName, resultClass, parameters);

        //when
        serverLazyLoader.load(method, persistenceId);

        //then exception
    }

}
