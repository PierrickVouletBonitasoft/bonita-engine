/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * @author Matthieu Chaffotte
 */
public class SAXValidator implements XMLSchemaValidator {

    private final SchemaFactory factory;

    private Schema schema;

    public SAXValidator() {
        this.factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    @Override
    public void setSchemaSource(final Source source) throws SInvalidSchemaException {
        try {
            this.schema = this.factory.newSchema(source);
        } catch (final SAXException saxe) {
            throw new SInvalidSchemaException(saxe);
        }
    }

    @Override
    public void validate(final InputStream stream) throws SValidationException, IOException {
        final Source source = new StreamSource(stream);
        this.validate(source);
    }

    @Override
    public void validate(final String filePath) throws SValidationException, IOException {
        final Source source = new StreamSource(filePath);
        this.validate(source);
    }

    @Override
    public void validate(final File file) throws SValidationException, IOException {
        final Source source = new StreamSource(file);
        this.validate(source);
    }

    @Override
    public void validate(final Reader reader) throws SValidationException, IOException {
        final Source source = new StreamSource(reader);
        this.validate(source);
    }

    private void validate(final Source source) throws IOException, SValidationException {
        try {
            if (this.schema == null) {
                throw new SValidationException("No schema defined");
            }
            final Validator validator = this.schema.newValidator();
            validator.validate(source);
        } catch (final SAXException e) {
            throw new SValidationException(e);
        }
    }

}
