package org.bonitasoft.engine.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.junit.Test;

public class AbstractDBPersistenceServiceTest {

    /**
     * Dummy implementation for testing purpose : we are not interested in the data manipulation behaviour.
     * 
     * @author Laurent Vaills
     * 
     */
    class DummyDBPersistenceService extends AbstractDBPersistenceService {

        public DummyDBPersistenceService(final String name, final DBConfigurationsProvider dbConfigurationsProvider, final String statementDelimiter,
                final String likeEscapeCharacter, final SequenceManager sequenceManager,
                final DataSource datasource, final boolean enableWordSearch, final Set<String> wordSearchExclusionMappings, final TechnicalLoggerService logger)
                throws ClassNotFoundException {
            super(name, dbConfigurationsProvider, statementDelimiter, likeEscapeCharacter, sequenceManager, datasource, enableWordSearch,
                    wordSearchExclusionMappings, logger);
        }

        @Override
        public <T> T selectOne(final SelectOneDescriptor<T> selectDescriptor) throws SBonitaReadException {
            return null;
        }

        @Override
        public <T> List<T> selectList(final SelectListDescriptor<T> selectDescriptor) throws SBonitaReadException {
            return null;
        }

        @Override
        public <T extends PersistentObject> T selectById(final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
            return null;
        }

        @Override
        public void update(final UpdateDescriptor desc) throws SPersistenceException {

        }

        @Override
        public void purge(final String classToPurge) throws SPersistenceException {

        }

        @Override
        public void purge() throws SPersistenceException {

        }

        @Override
        public void insertInBatch(final List<PersistentObject> entities) throws SPersistenceException {

        }

        @Override
        public void insert(final PersistentObject entity) throws SPersistenceException {

        }

        @Override
        public void flushStatements() throws SPersistenceException {

        }

        @Override
        public void deleteByTenant(final Class<? extends PersistentObject> entityClass, final List<FilterOption> filters) throws SPersistenceException {

        }

        @Override
        public void deleteAll(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {

        }

        @Override
        public void delete(final List<Long> ids, final Class<? extends PersistentObject> entityClass) throws SPersistenceException {

        }

        @Override
        public void delete(final long id, final Class<? extends PersistentObject> entityClass) throws SPersistenceException {

        }

        @Override
        public void delete(final PersistentObject entity) throws SPersistenceException {

        }

        @Override
        protected long getTenantId() throws STenantIdNotSetException {
            return 0;
        }

        @Override
        protected void doExecuteSQL(final String sqlResource, final String statementDelimiter, final Map<String, String> replacements,
                final boolean useDataSourceConnection) throws SPersistenceException, IOException {

        }

        @Override
        public int update(final String updateQueryName) throws SPersistenceException {
            return 0;
        }

        @Override
        public int update(final String updateQueryName, final Map<String, Object> inputParameters) throws SPersistenceException {
            return 0;
        }
    }

    class ParentDummyPersistentObject implements TenantPersistentObject {

        private static final long serialVersionUID = 1L;

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public String getDiscriminator() {
            return null;
        }

        @Override
        public void setId(final long id) {

        }

        @Override
        public void setTenantId(final long id) {

        }

    }

    class Child1DummyPersistentObject extends ParentDummyPersistentObject {

        private static final long serialVersionUID = 1L;

    }

    class Child2DummyPersistentObject extends ParentDummyPersistentObject {

        private static final long serialVersionUID = 1L;

    }

    class DummyPersistentObject2 implements TenantPersistentObject {

        private static final long serialVersionUID = 1L;

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public String getDiscriminator() {
            return null;
        }

        @Override
        public void setId(final long id) {

        }

        @Override
        public void setTenantId(final long id) {

        }

    }

    @Test
    public void should_word_search_returns_false_when_entity_class_is_null() throws Exception {
        boolean enableWordSearch = true;
        Set<String> wordSearchExclusionMappings = Collections.<String> emptySet();
        Class<? extends PersistentObject> entityClass = null;
        final boolean expectedResult = false;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_true_when_feature_is_enabled_and_exclusion_is_empty() throws Exception {
        boolean enableWordSearch = true;
        Set<String> wordSearchExclusionMappings = Collections.<String> emptySet();
        Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = true;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_false_when_feature_is_disabled_and_exclusion_is_empty() throws Exception {
        boolean enableWordSearch = false;
        Set<String> wordSearchExclusionMappings = Collections.<String> emptySet();
        Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = false;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_false_when_feature_is_enabled_and_entity_class_is_excluded() throws Exception {
        boolean enableWordSearch = true;
        Set<String> wordSearchExclusionMappings = Collections.singleton(ParentDummyPersistentObject.class.getName());
        Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = false;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_true_when_feature_is_enabled_and_entity_class_is_not_excluded() throws Exception {
        boolean enableWordSearch = true;
        Set<String> wordSearchExclusionMappings = Collections.singleton(DummyPersistentObject2.class.getName());
        Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = true;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_false_when_feature_is_enabled_and_parent_entity_class_is_excluded() throws Exception {
        boolean enableWordSearch = true;
        Set<String> wordSearchExclusionMappings = Collections.singleton(ParentDummyPersistentObject.class.getName());
        Class<? extends PersistentObject> entityClass = Child1DummyPersistentObject.class;
        final boolean expectedResult = false;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_true_when_feature_is_enabled_and_child_entity_class_is_excluded() throws Exception {
        boolean enableWordSearch = true;
        Set<String> wordSearchExclusionMappings = Collections.singleton(Child1DummyPersistentObject.class.getName());
        Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = true;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_true_when_feature_is_enabled_and_brother_entity_class_is_excluded() throws Exception {
        boolean enableWordSearch = true;
        Set<String> wordSearchExclusionMappings = Collections.singleton(Child1DummyPersistentObject.class.getName());
        Class<? extends PersistentObject> entityClass = Child2DummyPersistentObject.class;
        final boolean expectedResult = true;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }

    private void executeIsWordSearchEnabled(final boolean enableWordSearch, final Set<String> wordSearchExclusionMappings,
            final Class<? extends PersistentObject> entityClass, final boolean expectedResult)
            throws ClassNotFoundException {
        DBConfigurationsProvider dbConfigurationsProvider = mock(DBConfigurationsProvider.class);
        SequenceManager sequenceManager = mock(SequenceManager.class);
        DataSource datasource = mock(DataSource.class);
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        AbstractDBPersistenceService persistenceService = new DummyDBPersistenceService("name", dbConfigurationsProvider, ";", "#", sequenceManager,
                datasource, enableWordSearch, wordSearchExclusionMappings, logger);

        assertThat(persistenceService.isWordSearchEnabled(entityClass), is(expectedResult));
    }
}
