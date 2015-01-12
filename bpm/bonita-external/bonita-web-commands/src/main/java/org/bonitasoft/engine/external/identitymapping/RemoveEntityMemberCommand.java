/**
 * Copyright (C) 2012, 2014 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.external.identitymapping;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Parameter keys: ENTITY_MEMBER_ID_KEY: entity member id to remove.
 * 
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class RemoveEntityMemberCommand extends EntityMemberCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.serviceAccessor = serviceAccessor;
        long entityMemberId;
        try {
            entityMemberId = (Long) parameters.get(ENTITY_MEMBER_ID_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + ENTITY_MEMBER_ID_KEY + " is missing or not convertible to Long.");
        }
        try {
            removeExternalIdentityMapping(entityMemberId);
            // everything went right:
            return Boolean.TRUE;
        } catch (SCommandExecutionException e) {
            throw e;
        } catch (SBonitaException e) {
            throw new SCommandExecutionException("Error executing command 'RemoveEntityMemberCommand'", e);
        }
    }

}
