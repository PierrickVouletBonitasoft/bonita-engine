/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.bar;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.io.IOUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Celine Souchet
 */
public class ProcessDefinitionBARContributionTest {

    private static final String PROCESS_NAME = "Name";

    private static final String PROCESS_VERSION = "1.0";

    private static final String DESCRIPTION = "Description";

    private static final String ACTOR_NAME = "Actor Name";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public final void serializeDeserializeProcessDefinition() throws Exception {
        checkSerializeDeserializeProcessDefinition(createDesignProcessDefinition());
    }

    @Test
    public final void serializeDeserializeProcessDefinitionWithContract() throws Exception {
        final ProcessDefinitionBuilder processBuilder = createProcessBuilderDefinition();
        processBuilder.addUserTask("taskWithInput", ACTOR_NAME).addContract();
        processBuilder.addUserTask("taskWithConstraint", ACTOR_NAME).addContract().addSimpleInput("simpleInput", Type.INTEGER, DESCRIPTION)
                .addConstraint("Mandatory", "in != null", "in must be set", "in");
        processBuilder.addUserTask("taskWithComplexInput", ACTOR_NAME).addContract()
                .addComplexInput("complexInput", DESCRIPTION,
                        Arrays.asList((SimpleInputDefinition) new SimpleInputDefinition("simple", Type.TEXT, DESCRIPTION)), null);
        processBuilder
                .addUserTask("taskWithComplexComplexInput", ACTOR_NAME)
                .addContract()
                .addComplexInput("complexInput", DESCRIPTION,
                        Arrays.asList((SimpleInputDefinition) new SimpleInputDefinition("simple", Type.TEXT, DESCRIPTION)),
                        Arrays.asList(createComplexInputs()));

        checkSerializeDeserializeProcessDefinition(processBuilder.done());
    }

    private ComplexInputDefinition createComplexInputs() {
        final SimpleInputDefinition name = new SimpleInputDefinition("name", Type.TEXT, DESCRIPTION);
        final SimpleInputDefinition amount = new SimpleInputDefinition("amount", Type.DECIMAL, DESCRIPTION);
        final SimpleInputDefinition date = new SimpleInputDefinition("date", Type.DATE, DESCRIPTION);
        final SimpleInputDefinition proof = new SimpleInputDefinition("proof", Type.BYTE_ARRAY, DESCRIPTION);

        final SimpleInputDefinition city = new SimpleInputDefinition("city", Type.TEXT, DESCRIPTION);
        final SimpleInputDefinition zip = new SimpleInputDefinition("zip", Type.INTEGER, DESCRIPTION);

        final ComplexInputDefinition adress = new ComplexInputDefinition("adress", DESCRIPTION, Arrays.asList(city, zip), null);

        final ComplexInputDefinition expense = new ComplexInputDefinition("expense", DESCRIPTION, Arrays.asList(name, amount, date, proof),
                Arrays.asList(adress));
        return expense;
    }

    @Test
    public void should_deserializeProcessDefinition_of_old_process_throw_exception() throws Exception {
        exception.expect(InvalidBusinessArchiveFormatException.class);
        exception.expectMessage("Wrong version");
        final String allContentFrom = IOUtil.read(getClass().getResourceAsStream("/old-process.xml"));
        final File createTempFile = IOUtil.createTempFileInDefaultTempDirectory("old", "process.xml");

        try {
            IOUtil.writeContentToFile(allContentFrom, createTempFile);
            new ProcessDefinitionBARContribution().deserializeProcessDefinition(createTempFile);
        } finally {
            createTempFile.delete();
        }
    }

    @Test
    public void should_checkVersion_with_old_content_thrown_exception() throws Exception {
        exception.expect(InvalidBusinessArchiveFormatException.class);
        exception.expectMessage("6.0 namespace is not compatible with your current version");

        new ProcessDefinitionBARContribution().checkVersion(IOUtil.read(getClass().getResourceAsStream("/old-process.xml")));
    }

    @Test
    public void should_checkVersion_with_bad_content_thrown_exception() throws Exception {
        exception.expect(InvalidBusinessArchiveFormatException.class);
        exception.expectMessage("There is no bonitasoft process namespace declaration");

        new ProcessDefinitionBARContribution().checkVersion("invalid");
    }

    private void checkSerializeDeserializeProcessDefinition(final DesignProcessDefinition designProcessDefinition) throws IOException,
            InvalidBusinessArchiveFormatException {
        // Serialize designProcessDefinition
        final File processDesignFolder = File.createTempFile("serializeDeserialize", "ProcessDefinition");
        processDesignFolder.delete();
        processDesignFolder.mkdir();
        processDesignFolder.deleteOnExit();
        final ProcessDefinitionBARContribution processDefinitionBARContribution = new ProcessDefinitionBARContribution();
        processDefinitionBARContribution.serializeProcessDefinition(processDesignFolder, designProcessDefinition);

        // Deserialize designProcessDefinition
        final File processDesignFile = new File(processDesignFolder, ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        final DesignProcessDefinition resultDesignProcessDefinition = processDefinitionBARContribution.deserializeProcessDefinition(processDesignFile);
        assertThat(resultDesignProcessDefinition).isEqualTo(designProcessDefinition);

        // Clean up
        final File[] listFiles = processDesignFolder.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            listFiles[i].delete();
        }
        if (!processDesignFolder.delete()) {
            System.out.println("Could not delete temporary test folder");
        }
    }

    private DesignProcessDefinition createDesignProcessDefinition() throws InvalidExpressionException, InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = createProcessBuilderDefinition();
        return processBuilder.done();
    }

    private ProcessDefinitionBuilder createProcessBuilderDefinition() throws InvalidExpressionException {
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_NAME);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME, true);
        processBuilder.addActor("actor2").addDescription(DESCRIPTION);
        processBuilder.addDescription(DESCRIPTION);
        processBuilder.addAutomaticTask("AutomaticTask").addCallActivity("CallActivity", targetProcessNameExpr, targetProcessVersionExpr)
                .addManualTask("ManualTask", ACTOR_NAME).addBoundaryEvent("BoundaryEvent").addSignalEventTrigger("signalName");
        processBuilder.addUserTask("UserTask", ACTOR_NAME).addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0")
                .addInput("userId", new ExpressionBuilder().createConstantLongExpression(3));
        processBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER);
        processBuilder.addDocumentDefinition("Doc").addUrl("plop");
        processBuilder.addGateway("Gateway", GatewayType.PARALLEL).addDescription(DESCRIPTION);
        processBuilder.addBlobData("BlobData", null).addDescription("blolbDescription").addBooleanData("BooleanData", null);
        processBuilder.addDisplayName("plop").addDisplayDescription("plop2").addEndEvent("EndEvent");
        processBuilder.addIntermediateCatchEvent("IntermediateCatchEvent").addIntermediateThrowEvent("IntermediateThrowEvent");
        processBuilder.addReceiveTask("ReceiveTask", "messageName");
        processBuilder.addSendTask("SendTask", "messageName", targetProcessNameExpr);
        processBuilder.addTransition("BoundaryEvent", "ManualTask");
        return processBuilder;
    }

}
