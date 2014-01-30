package org.bonitasoft.engine.identity.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class IdentityServiceImplForUserTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private IdentityServiceImpl identityServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getNumberOfUsers() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(1L);
        Assert.assertEquals(1L, identityServiceImpl.getNumberOfUsers());

        verifyZeroInteractions(recorder);
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfUsersThrowException() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsers();
    }

    @Test
    public void getNumberOfUsersWithOptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);

        when(persistenceService.getNumberOfEntities(SUser.class, options, null)).thenReturn(1L);
        Assert.assertEquals(1L, identityServiceImpl.getNumberOfUsers(options));
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfUsersWithOptionsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);

        when(persistenceService.getNumberOfEntities(SUser.class, options, null)).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsers(options);
    }

    @Test
    public void getNumberOfUsersByGroup() throws Exception {
        final long groupId = 1;

        when(persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByGroup(groupId))).thenReturn(3L);
        Assert.assertEquals(3L, identityServiceImpl.getNumberOfUsersByGroup(groupId));
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfUsersByGroupThrowException() throws Exception {
        final long groupId = 1;

        when(persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByGroup(groupId))).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsersByGroup(groupId);
    }

    @Test
    public void getNumberOfUsersByMembership() throws Exception {
        final long groupId = 1;
        final long roleId = 2;

        when(persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByMembership(groupId, roleId))).thenReturn(3L);
        Assert.assertEquals(3L, identityServiceImpl.getNumberOfUsersByMembership(groupId, roleId));
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfUsersByMembershipThrowException() throws Exception {
        final long groupId = 1;
        final long roleId = 2;

        when(persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByMembership(groupId, roleId))).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsersByMembership(groupId, roleId);
    }

    @Test
    public void getNumberOfUsersByRole() throws Exception {
        final long roleId = 2;

        when(persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByRole(roleId))).thenReturn(3L);
        Assert.assertEquals(3L, identityServiceImpl.getNumberOfUsersByRole(roleId));
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfUsersByRoleThrowException() throws Exception {
        final long roleId = 2;

        when(persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfUsersByRole(roleId))).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsersByRole(roleId);
    }

    @Test
    public void getUserById() throws SBonitaReadException, SUserNotFoundException {
        final long userId = 1;
        final SUser sUser = mock(SUser.class);
        when(persistenceService.selectById(SelectDescriptorBuilder.getElementById(SUser.class, "User", userId))).thenReturn(sUser);

        Assert.assertEquals(sUser, identityServiceImpl.getUser(userId));
    }

    @Test(expected = SUserNotFoundException.class)
    public void getUserByIdNotExist() throws SBonitaReadException, SUserNotFoundException {
        final long userId = 455;
        doReturn(null).when(persistenceService).selectById(SelectDescriptorBuilder.getElementById(SUser.class, "User", userId));

        identityServiceImpl.getUser(userId);
    }

    @Test(expected = SUserNotFoundException.class)
    public void getUserByIdThrowException() throws SBonitaReadException, SUserNotFoundException {
        final long userId = 1;
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(SelectDescriptorBuilder.getElementById(SUser.class, "User", userId));

        identityServiceImpl.getUser(userId);
    }

    @Test
    public void getUsersByIds() throws Exception {
        final SUser sUser1 = mock(SUser.class);
        final SUser sUser2 = mock(SUser.class);
        final SUser sUser3 = mock(SUser.class);
        final List<SUser> users = Arrays.asList(sUser1, sUser2, sUser3);
        final List<Long> ids = Arrays.asList(1l, 2l, 3l);
        when(persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SUser.class, "User", ids))).thenReturn(users);

        Assert.assertEquals(users, identityServiceImpl.getUsers(ids));
    }

    @Test
    public void getUsersByNullIds() throws Exception {
        assertTrue(identityServiceImpl.getUsers(null).isEmpty());
    }

    @Test
    public void getUsersByEmptyIds() throws Exception {
        assertTrue(identityServiceImpl.getUsers(Collections.<Long> emptyList()).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SUserNotFoundException.class)
    public void getUsersByIdsThrowException() throws Exception {
        when(persistenceService.selectList(SelectDescriptorBuilder.getElementsByIds(SUser.class, "User", any(Collection.class)))).thenThrow(
                SBonitaReadException.class);

        identityServiceImpl.getUsers(Arrays.asList(1l));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getUsersByNames() throws Exception {
        final SUser sUser1 = mock(SUser.class);
        final SUser sUser2 = mock(SUser.class);
        final List<SUser> users = Arrays.asList(sUser1, sUser2);
        final List<String> names = Arrays.asList("matti", "marja", "taina");
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(users);
        Assert.assertEquals(users, identityServiceImpl.getUsersByUsername(names));
    }

    @Test
    public void getUsersByNullNames() throws Exception {
        assertTrue(identityServiceImpl.getUsersByUsername(null).isEmpty());
    }

    @Test
    public void getUsersByEmptyNames() throws Exception {
        assertTrue(identityServiceImpl.getUsersByUsername(Collections.<String> emptyList()).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SIdentityException.class)
    public void getUsersByNamesThrowException() throws Exception {
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(SBonitaReadException.class);
        identityServiceImpl.getUsersByUsername(Arrays.asList("hannu"));
    }

    @Test
    public void searchUsers() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        final SUser user = mock(SUser.class);
        when(persistenceService.searchEntity(SUser.class, options, null)).thenReturn(Collections.singletonList(user));

        assertEquals(user, identityServiceImpl.searchUsers(options).get(0));
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchUsersThrowException() throws SBonitaSearchException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("")).when(persistenceService).searchEntity(SUser.class, options, null);

        identityServiceImpl.searchUsers(options).get(0);
    }

}
