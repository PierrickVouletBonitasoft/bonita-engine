/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.actor.mapping.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Matthieu Chaffotte
 */
public class SelectDescriptorBuilder {

    public static SelectByIdDescriptor<SActor> getActor(final long actorId) {
        return new SelectByIdDescriptor<SActor>("getActorById", SActor.class, actorId);
    }

    public static SelectOneDescriptor<SActor> getActor(final String actorName, final long scopeId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", actorName);
        parameters.put("scopeId", scopeId);
        return new SelectOneDescriptor<SActor>("getActorFromNameAndScopeId", parameters, SActor.class);
    }

    public static SelectByIdDescriptor<SActorMember> getActorMember(final long actorMmeberId) {
        return new SelectByIdDescriptor<SActorMember>("getActorMemberById", SActorMember.class, actorMmeberId);
    }

    public static SelectListDescriptor<SActorMember> getActorMembers(final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements);
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<SActorMember>("getActorMembers", parameters, SActorMember.class, queryOptions);
    }

    public static SelectListDescriptor<SActorMember> getActorMembers(final long actorId, final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements);
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectListDescriptor<SActorMember>("getActorMembersOfActor", parameters, SActorMember.class, queryOptions);
    }

    public static SelectListDescriptor<SActorMember> getActorMembersOfGroup(final long groupId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("groupId", groupId);
        return new SelectListDescriptor<SActorMember>("getActorMembersOfGroup", parameters, SActorMember.class);
    }

    public static SelectListDescriptor<SActorMember> getActorMembersOfRole(final long roleId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("roleId", roleId);
        return new SelectListDescriptor<SActorMember>("getActorMembersOfRole", parameters, SActorMember.class);
    }

    public static SelectListDescriptor<SActorMember> getActorMembersOfUser(final long userId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        return new SelectListDescriptor<SActorMember>("getActorMembersOfUser", parameters, SActorMember.class);
    }

    public static SelectListDescriptor<SActor> getActors(final Set<Long> scopeIds) {
        final Map<String, Object> parameters = Collections.singletonMap("scopeIds", (Object) scopeIds);
        return new SelectListDescriptor<SActor>("getActorsOfScopes", parameters, SActor.class);
    }

    public static SelectListDescriptor<SActor> getActorsOfScope(final long scopeId) {
        final Map<String, Object> parameters = Collections.singletonMap("scopeId", (Object) scopeId);
        return new SelectListDescriptor<SActor>("getActorsOfScope", parameters, SActor.class);
    }

    public static SelectListDescriptor<SActor> getActorsOfScope(final long processDefinitionId, final int index, final int numberPerPage, final String field,
            final OrderByType order) {
        final Map<String, Object> parameters = Collections.singletonMap("scopeId", (Object) processDefinitionId);
        final QueryOptions queryOptions = new QueryOptions(index, numberPerPage, SActor.class, field, order);
        return new SelectListDescriptor<SActor>("getActorsOfScope", parameters, SActor.class, queryOptions);
    }

    public static SelectListDescriptor<SActor> getActorsOfUserCanStartProcessDefinition(final long userId, final long processDefinitionid) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("processDefinitionid", processDefinitionid);
        return new SelectListDescriptor<SActor>("getActorsOfUserCanStartProcessDefinition", parameters, SActor.class);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElementsByIds(final Class<T> clazz, final String elementName,
            final Collection<Long> ids) {
        final Map<String, Object> parameters = Collections.singletonMap("ids", (Object) ids);
        return new SelectListDescriptor<T>("get" + elementName + "sByIds", parameters, clazz);
    }

    public static SelectListDescriptor<SActor> getFullActorsListOfUser(final Set<Long> scopeIds, final long userId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("scopeIds", scopeIds);
        parameters.put("userId", userId);
        return new SelectListDescriptor<SActor>("getActorsOfUser", parameters, SActor.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfActorMembers(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfActorMembersOfActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfActorMembersOfGroupWithActor(final long groupId, final long actorId) {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("groupId", groupId);
        parameters.put("actorId", actorId);
        return new SelectOneDescriptor<Long>("getNumberOfActorMembersOfGroupWithActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfGroupsOfActor(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfGroupsOfActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfMembershipsOfActor(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfMembershipsOfActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfRolesOfActor(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfRolesOfActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersOfActor(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfUsersOfActor", parameters, SActorMember.class);
    }

}
