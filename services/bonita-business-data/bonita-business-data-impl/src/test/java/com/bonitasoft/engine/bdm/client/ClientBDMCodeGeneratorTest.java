package com.bonitasoft.engine.bdm.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Strings.concat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.assertj.core.util.FilesException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import com.bonitasoft.engine.bdm.CompilableCode;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.QueryParameter;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;

public class ClientBDMCodeGeneratorTest extends CompilableCode {

    private static final String EMPLOYEE_QUALIFIED_NAME = "org.bonitasoft.hr.Employee";

    private AbstractBDMCodeGenerator bdmCodeGenerator;

    private File destDir;

    @Before
    public void setUp() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bdmCodeGenerator = new ClientBDMCodeGenerator(bom);
        try {
            destDir = Files.newTemporaryFolder();
        } catch (final FilesException fe) {
            System.err.println("Seems we cannot create temporary folder. Retrying...");
            final String tempFileName = String.valueOf(UUID.randomUUID().getLeastSignificantBits());
            destDir = Files.newFolder(concat(Files.temporaryFolderPath(), tempFileName));
        }
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(destDir);
    }

    @Test
    public void shouldbuildAstFromBom_FillModel() throws Exception {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("Employee");
        bom.addBusinessObject(employeeBO);
        bdmCodeGenerator = new ClientBDMCodeGenerator(bom);
        bdmCodeGenerator.buildJavaModelFromBom();
        assertThat(bdmCodeGenerator.getModel()._getClass("Employee")).isNotNull();
    }

    @Test
    public void shouldToJavaClass_ReturnIntegerClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.INTEGER).name()).isEqualTo(Integer.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnStringClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.STRING).name()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnLongClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.LONG).name()).isEqualTo(Long.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnDoubleClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.DOUBLE).name()).isEqualTo(Double.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnFloatClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.FLOAT).name()).isEqualTo(Float.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnBooleanClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.BOOLEAN).name()).isEqualTo(Boolean.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnDateClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.DATE).name()).isEqualTo(Date.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnStringTextClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.TEXT).name()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void should_AddDao_generate_Dao_interface_with_query_methods_signature() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        employeeBO.getFields().add(nameField);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        bdmCodeGenerator = new ClientBDMCodeGenerator(bom);
        bdmCodeGenerator.generate(destDir);
        final String daoContent = readGeneratedDAOInterface();
        assertThat(daoContent).contains("public List<Employee> findByName(String name, int startIndex, int maxResults)");
    }

    @Test
    public void queryGenerationReturningListShouldAddPaginationParameters() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        employeeBO.getFields().add(nameField);
        final SimpleField ageField = new SimpleField();
        ageField.setName("age");
        ageField.setType(FieldType.INTEGER);
        employeeBO.getFields().add(ageField);

        final Query query = new Query("getEmployeesByNameAndAge", "SELECT e FROM Employee e WHERE e.name = :myName AND e.age = :miEdad", List.class.getName());
        query.addQueryParameter("miEdad", Integer.class.getName());
        query.addQueryParameter("myName", String.class.getName());
        employeeBO.getQueries().add(query);
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        bdmCodeGenerator = new ClientBDMCodeGenerator(bom);
        bdmCodeGenerator.generate(destDir);
        final String daoContent = readGeneratedDAOInterface();

        assertThat(daoContent).contains("public List<Employee> getEmployeesByNameAndAge(Integer miEdad, String myName, int startIndex, int maxResults)");
    }

    protected String getQueryMethodSignature(final Query query, final String queryReturnType, final String businessObjectName, final boolean returnsList) {
        String signature = "public " + getSimpleClassName(queryReturnType) + "<" + getSimpleClassName(businessObjectName) + "> " + query.getName() + "(";
        boolean first = true;
        for (final QueryParameter param : query.getQueryParameters()) {
            signature = appendCommaIfNotFirstParam(signature, first);
            signature += getSimpleClassName(param.getClassName()) + " " + param.getName();
            first = false;
        }
        if (returnsList) {
            signature = appendCommaIfNotFirstParam(signature, first);
            signature += "int startIndex, int maxResults";
        }
        signature += ")";
        return signature;
    }

    protected String appendCommaIfNotFirstParam(String signature, final boolean first) {
        if (!first) {
            signature += ", ";
        }
        return signature;
    }

    private String getSimpleClassName(final String qualifedClassName) {
        return qualifedClassName.substring(qualifedClassName.lastIndexOf('.') + 1);
    }

    @Test
    public void should_AddDao_generate_Dao_interface_with_unique_constraint_methods_signature() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("firstName");
        nameField.setType(FieldType.STRING);

        final SimpleField lastnameField = new SimpleField();
        lastnameField.setName("lastName");
        lastnameField.setType(FieldType.STRING);
        employeeBO.getFields().add(nameField);
        employeeBO.getFields().add(lastnameField);

        employeeBO.addUniqueConstraint("TOTO", "firstName", "lastName");
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        bdmCodeGenerator = new ClientBDMCodeGenerator(bom);
        bdmCodeGenerator.generate(destDir);
    }

    private String readGeneratedDAOInterface() throws IOException {
        final File daoInterface = new File(destDir, EMPLOYEE_QUALIFIED_NAME.replace(".", File.separator) + "DAO.java");
        return FileUtils.readFileToString(daoInterface);
    }

    public JAnnotationUse getAnnotation(final JDefinedClass definedClass, final String annotationClassName) {
        final Iterator<JAnnotationUse> iterator = definedClass.annotations().iterator();
        JAnnotationUse annotation = null;
        while (annotation == null && iterator.hasNext()) {
            final JAnnotationUse next = iterator.next();
            if (next.getAnnotationClass().fullName().equals(annotationClassName)) {
                annotation = next;
            }
        }
        return annotation;
    }

    @Test
    public void addIndexAnnotation() throws Exception {
        final BusinessObjectModel model = new BusinessObjectModel();
        final SimpleField field = new SimpleField();
        field.setName("firstName");
        field.setType(FieldType.STRING);
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("Employee");
        employeeBO.addField(field);
        employeeBO.addIndex("IDX_1", "firstName, lastName");
        model.addBusinessObject(employeeBO);

        bdmCodeGenerator = new ClientBDMCodeGenerator(model);
        bdmCodeGenerator.generate(destDir);

        assertFilesAreEqual("Employee.java", "Employee.test");
    }

    @Test
    public void addSimpleReferenceWithComposition() throws Exception {
        final BusinessObjectModel model = build(true, false);

        bdmCodeGenerator = new ClientBDMCodeGenerator(model);
        bdmCodeGenerator.generate(destDir);

        assertFilesAreEqual("Employee.java", "EmployeeSimpleComposition.test");
    }

    @Test
    public void addListReferenceWithComposition() throws Exception {
        final BusinessObjectModel model = build(true, true);

        bdmCodeGenerator = new ClientBDMCodeGenerator(model);
        bdmCodeGenerator.generate(destDir);

        assertFilesAreEqual("Employee.java", "EmployeeListComposition.test");
    }

    @Test
    public void addSimpleReferenceWithAggregation() throws Exception {
        final BusinessObjectModel model = build(false, false);

        bdmCodeGenerator = new ClientBDMCodeGenerator(model);
        bdmCodeGenerator.generate(destDir);

        assertFilesAreEqual("Employee.java", "EmployeeSimpleAggregation.test");
    }

    @Test
    public void addListReferenceWithAggregation() throws Exception {
        final BusinessObjectModel model = build(false, true);

        bdmCodeGenerator = new ClientBDMCodeGenerator(model);
        bdmCodeGenerator.generate(destDir);

        assertFilesAreEqual("Employee.java", "EmployeeListAggregation.test");
    }

    @Test
    public void addList() throws Exception {
        final BusinessObjectModel model = build();

        bdmCodeGenerator = new ClientBDMCodeGenerator(model);
        bdmCodeGenerator.generate(destDir);

        assertFilesAreEqual("Forecast.java", "ForecastList.test");
    }

    private BusinessObjectModel build() {
        final SimpleField field = new SimpleField();
        field.setName("temperatures");
        field.setType(FieldType.DOUBLE);
        field.setCollection(Boolean.TRUE);

        final BusinessObject forecastBO = new BusinessObject();
        forecastBO.setQualifiedName("Forecast");
        forecastBO.addField(field);

        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(forecastBO);
        return model;
    }

    private BusinessObjectModel build(final boolean composition, final boolean collection) {
        final SimpleField street = new SimpleField();
        street.setName("street");
        street.setType(FieldType.STRING);
        final SimpleField city = new SimpleField();
        city.setName("city");
        city.setType(FieldType.STRING);
        final BusinessObject addressBO = new BusinessObject();
        addressBO.setQualifiedName("Address");
        addressBO.addField(street);
        addressBO.addField(city);

        final SimpleField field = new SimpleField();
        field.setName("firstName");
        field.setType(FieldType.STRING);
        final RelationField address = new RelationField();
        if (composition) {
            address.setType(Type.COMPOSITION);
        } else {
            address.setType(Type.AGGREGATION);
        }
        if (collection) {
            address.setName("addresses");
            address.setCollection(Boolean.TRUE);
        } else {
            address.setName("address");
            address.setCollection(Boolean.FALSE);
        }
        address.setReference(addressBO);

        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("Employee");
        employeeBO.addField(field);
        employeeBO.addField(address);

        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(employeeBO);
        model.addBusinessObject(addressBO);
        return model;
    }

    private void assertFilesAreEqual(final String qualifiedName, final String resourceName) throws URISyntaxException, IOException {
        final File file = new File(destDir, qualifiedName);
        final URL resource = ClientBDMCodeGeneratorTest.class.getResource(resourceName);
        final File expected = new File(resource.toURI());

        assertThat(file).hasContentEqualTo(expected);
    }

}