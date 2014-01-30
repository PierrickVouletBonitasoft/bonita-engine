package org.bonitasoft.engine.identity.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class IdentityServiceImplForUserMembershipTest {

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
    public final void getLightUserMembershipById() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        doReturn(userMembership).when(persistenceService).selectById(any(SelectByIdDescriptor.class));

        assertEquals(userMembership, identityServiceImpl.getLightUserMembership(546L));
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipByIdNotExist() throws SBonitaReadException, SIdentityException {
        doReturn(null).when(persistenceService).selectById(any(SelectByIdDescriptor.class));

        identityServiceImpl.getLightUserMembership(546L);
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipByIdThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(any(SelectByIdDescriptor.class));

        identityServiceImpl.getLightUserMembership(546L);
    }

    @Test
    public final void getLightUserMembershipByUserAndGroupAndRole() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        doReturn(userMembership).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        assertEquals(userMembership, identityServiceImpl.getLightUserMembership(546L, 565L, 54L));
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipByUserAndGroupAndRoleNotExist() throws SBonitaReadException, SIdentityException {
        doReturn(null).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        identityServiceImpl.getLightUserMembership(546L, 565L, 54L);
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipByUserAndGroupAndRoleThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        identityServiceImpl.getLightUserMembership(546L, 565L, 54L);
    }

    @Test
    public final void getLightUserMembershipsPaginated() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final List<SUserMembership> userMemberships = Collections.singletonList(userMembership);
        doReturn(userMemberships).when(persistenceService).selectList(any(SelectListDescriptor.class));

        assertEquals(userMemberships, identityServiceImpl.getLightUserMemberships(546, 565));
    }

    @Test(expected = SIdentityException.class)
    public final void getLightUserMembershipsPaginatedThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(any(SelectListDescriptor.class));

        identityServiceImpl.getLightUserMemberships(546, 565);
    }

    @Test
    public final void getNumberOfUserMemberships() throws SBonitaReadException, SIdentityException {
        final long numberOfUserMemberships = 3;
        doReturn(numberOfUserMemberships).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        assertEquals(numberOfUserMemberships, identityServiceImpl.getNumberOfUserMemberships());
    }

    @Test(expected = SIdentityException.class)
    public final void getNumberOfUserMembershipsThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        identityServiceImpl.getNumberOfUserMemberships();
    }

    @Test
    public final void getNumberOfUserMembershipsOfUser() throws SBonitaReadException, SIdentityException {
        final long numberOfUserMemberships = 3;
        doReturn(numberOfUserMemberships).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        assertEquals(numberOfUserMemberships, identityServiceImpl.getNumberOfUserMembershipsOfUser(554L));
    }

    @Test(expected = SIdentityException.class)
    public final void getNumberOfUserMembershipsOfUserThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        identityServiceImpl.getNumberOfUserMembershipsOfUser(4854L);
    }

    @Test
    public final void getUserMembershipById() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        doReturn(userMembership).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        assertEquals(userMembership, identityServiceImpl.getUserMembership(546L));
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipByIdNotExist() throws SBonitaReadException, SIdentityException {
        doReturn(null).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        identityServiceImpl.getUserMembership(546L);
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipByIdThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        identityServiceImpl.getUserMembership(546L);
    }

    @Test
    public final void getUserMembershipByUserAndGroupAndRole() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        doReturn(userMembership).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        assertEquals(userMembership, identityServiceImpl.getUserMembership(546L, 565L, 54L));
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipByUserAndGroupAndRoleNotExist() throws SBonitaReadException, SIdentityException {
        doReturn(null).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        identityServiceImpl.getUserMembership(546L, 565L, 54L);
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipByUserAndGroupAndRoleThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        identityServiceImpl.getUserMembership(546L, 565L, 54L);
    }

    @Test
    public void getUserMembershipsOfGroup() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        when(persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByGroup(1l, 0, 20))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMembershipsOfGroup(1l, 0, 20);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsOfGroupThrowException() throws Exception {
        when(persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByGroup(1l, 0, 20))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMembershipsOfGroup(1l, 0, 20);
    }

    @Test
    public void getUserMembershipsOfRole() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        when(persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByRole(1l, 0, 20))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMembershipsOfRole(1l, 0, 20);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsOfRoleThrowException() throws Exception {
        when(persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsByRole(1l, 0, 20))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMembershipsOfRole(1l, 0, 20);
    }

    @Test
    public void getUserMembershipsPaginatedWithOrder() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SUserMembership.class, "username", OrderByType.ASC);
        doReturn(Collections.singletonList(userMembership)).when(persistenceService)
                .selectList(
                        SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership",
                                new QueryOptions(0, 10, Collections.singletonList(orderByOption))));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test(expected = SIdentityException.class)
    public void getUserMembershipsPaginatedWithOrderThrowException() throws Exception {
        final OrderByOption orderByOption = new OrderByOption(SUserMembership.class, "username", OrderByType.ASC);
        when(
                persistenceService.selectList(SelectDescriptorBuilder.getElements(SUserMembership.class, "UserMembership",
                        new QueryOptions(0, 10, Collections.singletonList(orderByOption))))).thenThrow(new SBonitaReadException(""));

        identityServiceImpl.getUserMemberships(0, 10, orderByOption);
    }

    @Test
    public void getUserMembershipsOrderByRole() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SRole.class, "name", OrderByType.ASC);
        when(
                persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsWithRole(new QueryOptions(0, 10, Collections
                        .singletonList(orderByOption))))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test
    public void getUserMembershipsOrderByGroup() throws Exception {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final OrderByOption orderByOption = new OrderByOption(SGroup.class, "name", OrderByType.ASC);
        when(
                persistenceService.selectList(SelectDescriptorBuilder.getUserMembershipsWithGroup(new QueryOptions(0, 10, Collections
                        .singletonList(orderByOption))))).thenReturn(Collections.singletonList(userMembership));

        final List<SUserMembership> userMemberships = identityServiceImpl.getUserMemberships(0, 10, orderByOption);

        assertEquals(userMembership, userMemberships.get(0));
    }

    @Test
    public final void getUserMembershipsPaginated() throws SBonitaReadException, SIdentityException {
        final SUserMembership userMembership = mock(SUserMembership.class);
        final List<SUserMembership> userMemberships = Collections.singletonList(userMembership);
        doReturn(userMemberships).when(persistenceService).selectList(any(SelectListDescriptor.class));

        assertEquals(userMemberships, identityServiceImpl.getUserMemberships(0, 10));
    }

    @Test(expected = SIdentityException.class)
    public final void getUserMembershipsPaginatedThrowException() throws SBonitaReadException, SIdentityException {
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(any(SelectListDescriptor.class));

        identityServiceImpl.getUserMemberships(0, 10);
    }

}
