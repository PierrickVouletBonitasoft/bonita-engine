/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.compiler;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Colin PUY
 * @author Celine Souchet
 */
public class JDTCompilerTest {

    private static final List<File> EMPTY_CLASSPATH = null;

    private JDTCompiler jdtCompiler;

    private File outputdirectory;

    @Before
    public void instanciateCompiler() {
        jdtCompiler = new JDTCompiler();
        outputdirectory = IOUtil.createTempDirectoryInDefaultTempDirectory("testFolder");
    }

    @After
    public void after() throws IOException {
        IOUtil.deleteDir(outputdirectory);
    }

    private File getTestResourceAsFile(final String fileName) throws URISyntaxException {
        final URL resource = JDTCompilerTest.class.getResource(fileName);
        if (resource == null) {
            throw new RuntimeException("Test resource " + fileName + " not found");
        }
        return new File(resource.toURI());
    }

    @Test
    public void should_compile_files_in_output_directory() throws Exception {
        final File compilableOne = getTestResourceAsFile("CompilableOne.java");
        final File compilableTwo = getTestResourceAsFile("CompilableTwo.java");

        jdtCompiler.compile(asList(compilableOne, compilableTwo), outputdirectory, EMPTY_CLASSPATH);

        assertThat(new File(outputdirectory, "org/bonitasoft/CompilableOne.class")).exists();
        assertThat(new File(outputdirectory, "org/bonitasoft/CompilableTwo.class")).exists();
    }

    @Test(expected = CompilationException.class)
    public void should_throw_exception_if_compilation_errors_occurs() throws Exception {
        final File uncompilable = getTestResourceAsFile("CannotBeResolvedToATypeError.java");

        jdtCompiler.compile(asList(uncompilable), outputdirectory, EMPTY_CLASSPATH);
    }

    @Test
    public void should_show_compilation_errors_in_exception_message() throws Exception {
        final File uncompilable = getTestResourceAsFile("CannotBeResolvedToATypeError.java");

        try {
            jdtCompiler.compile(asList(uncompilable), outputdirectory, EMPTY_CLASSPATH);
        } catch (final CompilationException e) {
            assertThat(e.getMessage()).contains("cannot be resolved to a type");
        }
    }

    @Test
    public void should_compile_class_with_external_dependencies() throws Exception {
        final File compilableWithDependency = getTestResourceAsFile("DependenciesNeeded.java");
        final File externalLib = getTestResourceAsFile("external-lib.jar");

        final String property = System.getProperty("java.class.path");
        final String[] pathnames = property.split(":");
        final List<File> classPathEntries = new ArrayList<File>();
        for (final String pathname : pathnames) {
            classPathEntries.add(new File(pathname));
        }
        classPathEntries.add(externalLib);

        jdtCompiler.compile(asList(compilableWithDependency), outputdirectory, classPathEntries);
    }

}
