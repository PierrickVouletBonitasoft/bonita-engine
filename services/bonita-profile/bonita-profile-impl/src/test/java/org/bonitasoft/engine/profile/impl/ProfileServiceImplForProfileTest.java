package org.bonitasoft.engine.profile.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilderFactory;
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileDeletionException;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.exception.profile.SProfileReadException;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.profile.persistence.SelectDescriptorBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileServiceImplForProfileTest {

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private Recorder recorder;

    @InjectMocks
    private ProfileServiceImpl profileServiceImpl;

    @Test
    public void getNumberOfProfilesWithOptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(SProfile.class, options, Collections.<String, Object> emptyMap())).thenReturn(1L);

        assertEquals(1L, profileServiceImpl.getNumberOfProfiles(options));
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfProfilesWithOptionsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(SProfile.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getNumberOfProfiles(options);
    }

    @Test
    public void searchProfilesWithOptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(SProfile.class, options, Collections.<String, Object> emptyMap())).thenReturn(new ArrayList<SProfile>());

        assertNotNull(profileServiceImpl.searchProfiles(options));
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchProfilesWithOptionsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(SProfile.class, options, Collections.<String, Object> emptyMap())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.searchProfiles(options);
    }

    @Test
    public void getProfileById() throws Exception {
        final SProfile sProfile = mock(SProfile.class);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());

        assertEquals(sProfile, profileServiceImpl.getProfile(1));
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getNoProfileById() throws Exception {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfile>> any())).thenReturn(null);

        profileServiceImpl.getProfile(1);
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getProfileByIdThrowException() throws Exception {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfile>> any())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getProfile(1);
    }

    @Test
    public void getProfileByName() throws Exception {
        final String profileName = "plop";
        final SProfile sProfile = mock(SProfile.class);

        doReturn(sProfile).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SProfile>> any());

        assertEquals(sProfile, profileServiceImpl.getProfileByName(profileName));
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getNoProfileByName() throws Exception {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SProfile>> any())).thenReturn(null);

        profileServiceImpl.getProfileByName("plop");
    }

    @Test(expected = SProfileNotFoundException.class)
    public void getProfileByNameThrowException() throws Exception {
        when(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SProfile>> any())).thenThrow(new SBonitaReadException(""));

        profileServiceImpl.getProfileByName("plop");
    }

    @Test
    public final void getProfiles() throws SProfileNotFoundException, SBonitaReadException {
        final List<Long> profileIds = new ArrayList<Long>();
        profileIds.add(1L);
        profileIds.add(2L);

        final List<SProfile> sProfiles = new ArrayList<SProfile>();
        final SProfile sProfile = mock(SProfile.class);
        sProfiles.add(sProfile);
        sProfiles.add(sProfile);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());

        assertEquals(sProfiles, profileServiceImpl.getProfiles(profileIds));
    }

    @Test(expected = SProfileNotFoundException.class)
    public final void getNoProfiles() throws SProfileNotFoundException, SBonitaReadException {
        final List<Long> profileIds = new ArrayList<Long>();
        profileIds.add(1L);

        doReturn(null).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());

        profileServiceImpl.getProfiles(profileIds);
    }

    @Test
    public final void getProfilesWithEmptyList() throws SProfileNotFoundException {
        final List<Long> profileIds = new ArrayList<Long>();

        assertTrue(profileServiceImpl.getProfiles(profileIds).isEmpty());
    }

    @Test
    public final void getProfilesWithNullList() throws SProfileNotFoundException {
        assertTrue(profileServiceImpl.getProfiles(null).isEmpty());
    }

    @Test
    public final void getProfilesOfUser() throws SBonitaReadException, SProfileReadException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>();
        final SProfile sProfile = mock(SProfile.class);
        sProfiles.add(sProfile);

        doReturn(sProfiles).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfile>> any());

        assertEquals(sProfiles, profileServiceImpl.getProfilesOfUser(1));
    }

    @Test
    public final void getNoProfilesOfUser() throws SBonitaReadException, SProfileReadException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>();

        doReturn(sProfiles).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfile>> any());

        assertEquals(sProfiles, profileServiceImpl.getProfilesOfUser(1));
    }

    @Test(expected = SProfileReadException.class)
    public final void getProfilesOfUserThrowException() throws SBonitaReadException, SProfileReadException {
        doThrow(new SBonitaReadException("plop")).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SProfile>> any());

        profileServiceImpl.getProfilesOfUser(1);
    }

    @Test
    public final void createProfile() throws SProfileCreationException, SRecorderException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(1L).when(sProfile).getId();

        doNothing().when(recorder).recordInsert(any(InsertRecord.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SProfile result = profileServiceImpl.createProfile(sProfile);
        assertNotNull(result);
        assertEquals(sProfile, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createNullProfile() throws Exception {
        profileServiceImpl.createProfile(null);
    }

    @Test(expected = SProfileCreationException.class)
    public final void createProfileThrowException() throws SRecorderException, SProfileCreationException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(1L).when(sProfile).getId();

        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.createProfile(sProfile);
    }

    @Test
    public final void updateProfile() throws SProfileUpdateException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();
        final SProfileUpdateBuilder sProfileUpdateBuilder = BuilderFactory.get(SProfileUpdateBuilderFactory.class).createNewInstance();
        sProfileUpdateBuilder.setDescription("newDescription").setName("newName");

        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SProfile result = profileServiceImpl.updateProfile(sProfile, sProfileUpdateBuilder.done());
        assertNotNull(result);
        assertEquals(sProfile, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void updateNullProfile() throws SProfileUpdateException {
        final SProfileUpdateBuilder sProfileUpdateBuilder = BuilderFactory.get(SProfileUpdateBuilderFactory.class).createNewInstance();

        profileServiceImpl.updateProfile(null, sProfileUpdateBuilder.done());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void updateProfileWithNullDescriptor() throws SProfileUpdateException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        profileServiceImpl.updateProfile(sProfile, null);
    }

    @Test(expected = SProfileUpdateException.class)
    public final void updateProfileThrowException() throws SRecorderException, SProfileUpdateException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();
        final SProfileUpdateBuilder sProfileUpdateBuilder = BuilderFactory.get(SProfileUpdateBuilderFactory.class).createNewInstance();
        sProfileUpdateBuilder.setDescription("newDescription").setName("newName");

        doThrow(new SRecorderException("plop")).when(recorder).recordUpdate(any(UpdateRecord.class));

        profileServiceImpl.updateProfile(sProfile, sProfileUpdateBuilder.done());
    }

    @Test
    public final void deleteProfileById() throws SProfileNotFoundException, SProfileDeletionException, SRecorderException, SBonitaReadException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();
        sProfileEntries.add(sProfileEntry);

        final List<SProfileMember> sProfileMembers = new ArrayList<SProfileMember>();
        final SProfileMember sProfileMember = mock(SProfileMember.class);
        doReturn(4L).when(sProfileMember).getId();
        sProfileMembers.add(sProfileMember);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(sProfileEntries).doReturn(new ArrayList<SProfileEntry>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        doReturn(sProfileMembers).doReturn(new ArrayList<SProfileMember>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(1);
    }

    @Test(expected = SProfileNotFoundException.class)
    public final void deleteNoProfileById() throws SBonitaReadException, SProfileDeletionException, SProfileNotFoundException {
        when(persistenceService.selectById(Matchers.<SelectByIdDescriptor<SProfile>> any())).thenReturn(null);

        profileServiceImpl.deleteProfile(1);
    }

    @Test(expected = SProfileDeletionException.class)
    public void deleteProfileByIdThrowException() throws Exception {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(new ArrayList<SProfileEntry>()).when(persistenceService).selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        doReturn(new ArrayList<SProfileMember>()).when(persistenceService).selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class));

        profileServiceImpl.deleteProfile(1);
    }

    @Test
    public final void deleteProfileByIdWithNoEntry() throws SProfileNotFoundException, SProfileDeletionException, SRecorderException, SBonitaReadException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>(3);
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();
        sProfiles.add(sProfile);

        final List<SProfileMember> sProfileMembers = new ArrayList<SProfileMember>();
        final SProfileMember sProfileMember = mock(SProfileMember.class);
        doReturn(4L).when(sProfileMember).getId();
        sProfileMembers.add(sProfileMember);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(new ArrayList<SProfileEntry>()).when(persistenceService).selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        doReturn(sProfileMembers).doReturn(new ArrayList<SProfileMember>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(1);
    }

    @Test
    public final void deleteProfileByIdWithNoMember() throws SProfileNotFoundException, SProfileDeletionException, SRecorderException, SBonitaReadException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();
        sProfileEntries.add(sProfileEntry);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(sProfileEntries).doReturn(new ArrayList<SProfileEntry>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        doReturn(new ArrayList<SProfileMember>()).when(persistenceService).selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(1);
    }

    @Test
    public final void deleteProfileByObject() throws SBonitaReadException, SRecorderException, SProfileDeletionException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();
        sProfileEntries.add(sProfileEntry);

        final List<SProfileMember> sProfileMembers = new ArrayList<SProfileMember>();
        final SProfileMember sProfileMember = mock(SProfileMember.class);
        doReturn(4L).when(sProfileMember).getId();
        sProfileMembers.add(sProfileMember);

        doReturn(sProfileEntries).doReturn(new ArrayList<SProfileEntry>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        doReturn(sProfileMembers).doReturn(new ArrayList<SProfileMember>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(sProfile);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteNoProfileByObject() throws SProfileDeletionException {
        profileServiceImpl.deleteProfile(null);
    }

    @Test(expected = SProfileDeletionException.class)
    public void deleteProfileByObjectThrowException() throws Exception {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(new ArrayList<SProfileEntry>()).when(persistenceService).selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        doReturn(new ArrayList<SProfileMember>()).when(persistenceService).selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class));

        profileServiceImpl.deleteProfile(sProfile);
    }

    @Test
    public final void deleteProfileByObjectWithNoEntry() throws SProfileDeletionException, SRecorderException, SBonitaReadException {
        final List<SProfile> sProfiles = new ArrayList<SProfile>(3);
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();
        sProfiles.add(sProfile);

        final List<SProfileMember> sProfileMembers = new ArrayList<SProfileMember>();
        final SProfileMember sProfileMember = mock(SProfileMember.class);
        doReturn(4L).when(sProfileMember).getId();
        sProfileMembers.add(sProfileMember);

        doReturn(sProfile).when(persistenceService).selectById(Matchers.<SelectByIdDescriptor<SProfile>> any());
        doReturn(new ArrayList<SProfileEntry>()).when(persistenceService).selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        doReturn(sProfileMembers).doReturn(new ArrayList<SProfileMember>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(sProfile);
    }

    @Test
    public final void deleteProfileByObjectWithNoMember() throws SProfileDeletionException, SRecorderException, SBonitaReadException {
        final SProfile sProfile = mock(SProfile.class);
        doReturn(3L).when(sProfile).getId();

        final List<SProfileEntry> sProfileEntries = new ArrayList<SProfileEntry>();
        final SProfileEntry sProfileEntry = mock(SProfileEntry.class);
        doReturn(6L).when(sProfileEntry).getId();
        sProfileEntries.add(sProfileEntry);

        doReturn(sProfileEntries).doReturn(new ArrayList<SProfileEntry>()).when(persistenceService)
                .selectList(SelectDescriptorBuilder.getEntriesOfProfile(3, 0, 1000));
        doReturn(new ArrayList<SProfileMember>()).when(persistenceService).selectList(SelectDescriptorBuilder.getSProfileMembersWithoutDisplayName(3));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        profileServiceImpl.deleteProfile(sProfile);
    }
}
