/**
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.scheduler.builder.impl;

import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterQueriableLogBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SJobParameterQueriableLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SJobParameterQueriableLogBuilderFactory {

    private static final String JOB_PARAMETER_INDEX_NAME = "numericIndex2";

    private static final String JOB_INDEX_NAME = "numericIndex1";


    @Override
    public String getObjectIdKey() {
        return JOB_PARAMETER_INDEX_NAME;
    }

    @Override
    public String getJobDescriptorIdKey() {
        return JOB_INDEX_NAME;
    }

}
