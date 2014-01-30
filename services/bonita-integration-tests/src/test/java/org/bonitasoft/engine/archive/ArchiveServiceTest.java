package org.bonitasoft.engine.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.archive.model.Address;
import org.bonitasoft.engine.archive.model.Employee;
import org.bonitasoft.engine.archive.model.EmployeeProjectMapping;
import org.bonitasoft.engine.archive.model.Laptop;
import org.bonitasoft.engine.archive.model.Project;
import org.bonitasoft.engine.archive.model.TestLogBuilder;
import org.bonitasoft.engine.archive.model.TestLogBuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArchiveServiceTest extends CommonServiceTest {

    private static final long START_OF_2009 = 1230739500052l;

    private static final long BEFORE_2009 = 1130739200052l;

    private static final int ONE_DAY = 86400000;

    private static ArchiveService archiveService;

    private long sessionId;

    static {
        archiveService = getServicesBuilder().buildArchiveService();
    }

    @Before
    public void createTenant() throws Exception {
        final long tenantId = PlatformUtil.createTenant(getTransactionService(), getPlatformService(), "aTenant", "me", "test");
        createSession(tenantId);
    }

    private void createSession(final long tenantId) throws SBonitaException {
        getTransactionService().begin();
        getSessionAccessor().deleteSessionId();
        final SSession session = getSessionService().createSession(tenantId, "me");
        getSessionAccessor().setSessionInfo(session.getId(), tenantId);
        sessionId = session.getId();
        getTransactionService().complete();
    }

    @After
    public void deleteTenant() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
        getTransactionService().begin();
        final long tenantId = getPlatformService().getTenantByName("aTenant").getId();
        getTransactionService().complete();
        PlatformUtil.deleteTenant(getTransactionService(), getPlatformService(), tenantId);
        getSessionAccessor().deleteSessionId();
        getSessionService().deleteSession(sessionId);
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private TestLogBuilder getLogBuilder(final ActionType actionType, final String message) {
        final TestLogBuilder logModelBuilder = new TestLogBuilderFactory().createNewInstance();
        initializeLogBuilder(logModelBuilder, message);
        updateLog(actionType, logModelBuilder);
        return logModelBuilder;
    }

    @Test
    public void testRecordInsert() throws Exception {
        getTransactionService().begin();

        final SQueriableLog queriableLog = getLogBuilder(ActionType.CREATED,
                "Testing entities insertion with one-to-one, one-to-many and many-to-many relationships").done();

        final Laptop laptop = insertLaptopRecordIntoArchiveWithYesterdayDate(queriableLog);
        assertNotNull(laptop);

        final Employee employee = insertEmployeeWithYesterdayDate(queriableLog, laptop);
        assertNotNull(employee);

        final Address address = insertAddressRecordIntoArchiveWithYesterdayDate(queriableLog, employee);
        assertNotNull(address);

        final Project project = insertProjectRecordIntoArchiveWithYesterdayDate(queriableLog);
        assertNotNull(project);

        final EmployeeProjectMapping epMapping = insertEmployeeProjectMappingRecordIntoArchiveWithYesterDayDate(queriableLog, employee, project);
        assertNotNull(epMapping);

        getTransactionService().complete();
    }

    @Test
    public void archiveInSlidingArchiveNotDone() throws Exception {
        getTransactionService().begin();

        final SQueriableLog queriableLog = getLogBuilder(ActionType.CREATED,
                "Testing entities insertion with one-to-one, one-to-many and many-to-many relationships").done();

        final Laptop laptop = insertLaptopRecordIntoArchiveWithYesterdayDate(queriableLog);
        getTransactionService().complete();

        getTransactionService().begin();
        final Employee employee = insertEmployeeWithFirstJanuary2009Date(queriableLog, laptop);
        getTransactionService().complete();

        getTransactionService().begin();

        final Employee employeeArchiveRecord = selectEmployeeByIdFromDefinitiveArchive(employee);
        assertNotNull("should be in definitive archive", employeeArchiveRecord);
        assertEquals(employee.getName(), employeeArchiveRecord.getName());
        assertEquals(employee.getAge(), employeeArchiveRecord.getAge());
        assertEquals(laptop.getId(), employeeArchiveRecord.getLaptopId());

        getTransactionService().complete();
    }

    @Test
    public void insertWithNoDefinitiveArchiveForThatDate() throws Exception {
        getTransactionService().begin();
        final SQueriableLog queriableLog = getLogBuilder(ActionType.CREATED,
                "Testing entities insertion with one-to-one, one-to-many and many-to-many relationships").done();
        final Laptop laptop = insertLaptopRecordIntoArchiveWithYesterdayDate(queriableLog);
        try {
            insertEmployeeWithBefore2009Date(queriableLog, laptop);
        } finally {
            getTransactionService().complete();
        }
    }

    private Laptop insertLaptopRecordIntoArchiveWithYesterdayDate(final SQueriableLog queriableLog) throws SRecorderException, SDefinitiveArchiveNotFound {
        final Laptop laptop = new Laptop("Dell", "1800");
        archiveService.recordInsert(System.currentTimeMillis() - ONE_DAY, new ArchiveInsertRecord(laptop, "LAPTOP"));
        return laptop;
    }

    private Employee insertEmployeeWithYesterdayDate(final SQueriableLog queriableLog, final Laptop laptop) throws SRecorderException,
            SDefinitiveArchiveNotFound {
        final Employee employee = new Employee("ZhaoDa", 20);
        employee.setLaptopId(laptop.getId());
        archiveService.recordInsert(System.currentTimeMillis() - ONE_DAY, new ArchiveInsertRecord(employee, "EMPLOYEE"));
        return employee;
    }

    private Employee insertEmployeeWithFirstJanuary2009Date(final SQueriableLog queriableLog, final Laptop laptop) throws SRecorderException,
            SDefinitiveArchiveNotFound {
        final Employee employee = new Employee("ZhaoDa", 20);
        employee.setLaptopId(laptop.getId());
        archiveService.recordInsert(START_OF_2009, new ArchiveInsertRecord(employee, "EMPLOYEE"));
        return employee;
    }

    private Employee insertEmployeeWithBefore2009Date(final SQueriableLog queriableLog, final Laptop laptop) throws SRecorderException,
            SDefinitiveArchiveNotFound {
        final Employee employee = new Employee("ZhaoDa", 20);
        employee.setLaptopId(laptop.getId());
        archiveService.recordInsert(BEFORE_2009, new ArchiveInsertRecord(employee, "EMPLOYEE"));
        return employee;
    }

    private Address insertAddressRecordIntoArchiveWithYesterdayDate(final SQueriableLog queriableLog, final Employee employee) throws SRecorderException,
            SDefinitiveArchiveNotFound {
        final Address address = new Address("China");
        address.setEmployeeId(employee.getId());
        archiveService.recordInsert(System.currentTimeMillis() - ONE_DAY, new ArchiveInsertRecord(address, "ADDRESS"));
        return address;
    }

    private Project insertProjectRecordIntoArchiveWithYesterdayDate(final SQueriableLog queriableLog) throws SRecorderException, SDefinitiveArchiveNotFound {
        final Project project = new Project("BOS6");
        archiveService.recordInsert(System.currentTimeMillis() - ONE_DAY, new ArchiveInsertRecord(project, "PROJECT"));
        return project;
    }

    private EmployeeProjectMapping insertEmployeeProjectMappingRecordIntoArchiveWithYesterDayDate(final SQueriableLog queriableLog, final Employee employee,
            final Project project) throws SRecorderException, SDefinitiveArchiveNotFound {
        final EmployeeProjectMapping epMapping = new EmployeeProjectMapping(employee, project);
        archiveService.recordInsert(System.currentTimeMillis() - ONE_DAY, new ArchiveInsertRecord(epMapping, "EMPLOYEE_PROJECt_MAPPING"));
        return epMapping;
    }

    private Employee selectEmployeeByIdFromDefinitiveArchive(final Employee employee) throws SBonitaReadException {
        final SelectByIdDescriptor<Employee> selectByIdDescriptor1 = new SelectByIdDescriptor<Employee>("getEmployeeById", Employee.class, employee.getId());
        return archiveService.getDefinitiveArchiveReadPersistenceService().selectById(selectByIdDescriptor1);
    }

    @Test
    public void testRecordDelete() throws Exception {
        getTransactionService().begin();

        final SQueriableLog queriableLog = getLogBuilder(ActionType.DELETED, "Testing entities deletion").done();

        final Laptop laptop = insertLaptopRecordIntoArchiveWithYesterdayDate(queriableLog);

        final Employee employee = insertEmployeeWithYesterdayDate(queriableLog, laptop);

        final Address address = insertAddressRecordIntoArchiveWithYesterdayDate(queriableLog, employee);

        final Project project = insertProjectRecordIntoArchiveWithYesterdayDate(queriableLog);

        final EmployeeProjectMapping employeeProjectMapping = insertEmployeeProjectMappingRecordIntoArchiveWithYesterDayDate(queriableLog, employee, project);

        getTransactionService().complete();

        getTransactionService().begin();

        archiveService.recordDelete(new DeleteRecord(employeeProjectMapping, "EMPLOYEE_PROJECt_MAPPING"));

        archiveService.recordDelete(new DeleteRecord(project, "PROJECT"));

        archiveService.recordDelete(new DeleteRecord(address, "ADDRESS"));

        archiveService.recordDelete(new DeleteRecord(laptop, "LAPTOP"));

        archiveService.recordDelete(new DeleteRecord(employee, "EMPLOYEE"));

        getTransactionService().complete();
    }

    @Test
    public void testGetDefinitiveArchiveDescriptor() {
        final SArchiveDescriptor archiveDescriptor = archiveService.getDefinitiveArchiveDescriptor();
        assertNotNull(archiveDescriptor);
    }

    @Test
    @Cover(classes = { ReadPersistenceService.class }, concept = BPMNConcept.OTHERS, keywords = { "archived objects" }, jira = "")
    public void testGetDefinitiveArchiveReadPersistenceService() {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        assertNotNull(persistenceService);
    }

}
