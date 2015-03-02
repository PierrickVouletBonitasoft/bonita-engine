/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import org.bonitasoft.engine.session.APISession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Romain Bioteau
 */
@RunWith(MockitoJUnitRunner.class)
public class BusinessObjectDAOFactoryTest {

    @Mock
    private APISession session;

    @Spy
    private BusinessObjectDAOFactory factory;

    @Test(expected = IllegalArgumentException.class)
    public void should_create_dao_throw_IllegalArgmumentException_for_null_session() throws Exception {
        factory.createDAO(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_create_dao_throw_IllegalArgmumentException_for_null_interface() throws Exception {
        factory.createDAO(session, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_create_dao_throw_IllegalArgmumentException_if_daoInterface_is_not_an_interface() throws Exception {
        factory.createDAO(session, DummyDAOImpl.class);
    }

    @Test(expected = BusinessObjectDaoCreationException.class)
    public void should_create_dao_throw_BusinessObjectDaoCreationException_if_daoImpl_not_in_classpath() throws Exception {
        Mockito.doThrow(ClassNotFoundException.class).when(factory).loadClass(BusinessObjectDAO.class.getName());
        factory.createDAO(session, BusinessObjectDAO.class);
    }

    @Test(expected = BusinessObjectDaoCreationException.class)
    public void should_create_dao_throw_BusinessObjectDaoCreationException_if_daoImpl_has_no_constructor_with_session() throws Exception {
        Mockito.doReturn(DummyDAOWithoutConstructorImpl.class).when(factory).loadClass(Matchers.anyString());
        factory.createDAO(session, DummyDAO.class);
    }

    @Test
    public void should_create_dao_return_implementation() throws Exception {
        DummyDAO daoInstance = factory.createDAO(session, DummyDAO.class);
        Assertions.assertThat(daoInstance).isNotNull();
    }

    class DummyDAOWithoutConstructorImpl implements DummyDAO {

    }

}
