/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine;

import org.bonitasoft.engine.business.application.ApplicationAPIITs;
import org.bonitasoft.engine.business.data.BDRepositoryLocalIT;
import org.bonitasoft.engine.page.PageAPIIT;
import org.bonitasoft.engine.tenant.TenantMaintenanceLocalIT;
import org.bonitasoft.engine.test.APIMethodIT;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        BPMLocalSuiteTests.class,
        BPMRemoteTests.class,
        PageAPIIT.class,
        ApplicationAPIITs.class,
        APIMethodIT.class,
        TenantMaintenanceLocalIT.class,
        BDRepositoryLocalIT.class,
})
@Initializer(TestsInitializer.class)
public class LocalIntegrationTests {

    @BeforeClass
    public static void beforeClass() {
        System.err.println("=================== LocalIntegrationTests setup");
    }

    @AfterClass
    public static void afterClass() {
        System.err.println("=================== LocalIntegrationTests afterClass");
    }

}
