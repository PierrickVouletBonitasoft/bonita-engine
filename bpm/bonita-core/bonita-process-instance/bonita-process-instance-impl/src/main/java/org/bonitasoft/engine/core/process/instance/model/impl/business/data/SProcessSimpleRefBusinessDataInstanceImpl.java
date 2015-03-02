/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.core.process.instance.model.impl.business.data;


import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;

/**
 * @author Matthieu Chaffotte
 */
public class SProcessSimpleRefBusinessDataInstanceImpl extends SSimpleRefBusinessDataInstanceImpl implements SProcessSimpleRefBusinessDataInstance {

    private static final long serialVersionUID = -1612711169274459075L;

    private long processInstanceId;

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

}
