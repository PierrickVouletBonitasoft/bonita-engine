/**
 * Copyright (C) 2011, 2013-2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.scheduler.impl;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Matthieu Chaffotte
 * @author Baptsite Mesta : the bos job is set before the execution.
 * @author Celine Souchet
 */
public abstract class QuartzJob implements org.quartz.Job {

    private StatelessJob bosJob;

    @SuppressWarnings("unused")
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            bosJob.execute();
        } catch (final SBonitaException e) {
            throw new JobExecutionException(e);
        }
    }

    public StatelessJob getBosJob() {
        return bosJob;
    }

    public void setBosJob(StatelessJob bosJob) {
        this.bosJob = bosJob;
    }

}
