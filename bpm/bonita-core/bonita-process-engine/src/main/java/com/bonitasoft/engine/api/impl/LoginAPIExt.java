/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.impl.LoginAPIImpl;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.api.TenantIsPausedException;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class LoginAPIExt extends LoginAPIImpl implements LoginAPI {

    @Override
    @CustomTransactions
    @AvailableWhenTenantIsPaused
    public APISession login(final String userName, final String password) throws LoginException {
        if (!LicenseChecker.getInstance().checkLicence()) {
            throw new LoginException("The node is not started: " + LicenseChecker.getInstance().getErrorMessage());
        }
        return super.login(userName, password);
    }

    @Override
    @CustomTransactions
    @AvailableWhenTenantIsPaused
    public APISession login(final long tenantId, final String userName, final String password) throws LoginException {
        if (!LicenseChecker.getInstance().checkLicence()) {
            throw new LoginException("The node is not started");
        }
        checkUsernameAndPassword(userName, password);
        try {
            return login(userName, password, tenantId);
        } catch (final LoginException e) {
            throw e;
        } catch (final BonitaRuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new LoginException(e);
        }
    }

    @Override
    protected TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

    @Override
    protected void checkThatWeCanLogin(final String userName, final PlatformService platformService, final STenant sTenant) throws LoginException {
        super.checkThatWeCanLogin(userName, platformService, sTenant);
        try {
            if (sTenant.isPaused()) {
                final String technicalUserName = BonitaHomeServer.getInstance().getTenantProperties(sTenant.getId()).getProperty("userName");

                if (!technicalUserName.equals(userName)) {
                    throw new TenantIsPausedException("Tenant with ID " + sTenant.getId()
                            + " is in maintenance, unable to login with other user than the technical user.");
                }
            }
        } catch (BonitaHomeNotSetException e) {
            throw new LoginException(e);
        } catch (IOException e) {
            throw new LoginException(e);
        }
    }

    @Override
    public APISession login(final long tenantId, final Map<String, Serializable> credentials) throws LoginException {
        if (!LicenseChecker.getInstance().checkLicence()) {
            throw new LoginException("The node is not started");
        }
        if (credentials != null) {
            credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        }
        try {
            return login(credentials);
        } catch (Throwable e) {
            throw new LoginException(e);
	}
    }
}
