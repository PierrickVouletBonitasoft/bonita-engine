/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.model.field;

import static org.bonitasoft.engine.bdm.model.assertion.FieldAssert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class SimpleFieldTest {

    @Test
    @Ignore
    public void should_not_be_marshallizable_without_name() {
        final SimpleField field = new SimpleField();
        field.setType(FieldType.BOOLEAN);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_not_be_marshallizable_without_type() {
        final SimpleField field = new SimpleField();
        field.setName("aName");

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_be_marshallizable_with_name_and_type() {
        final SimpleField field = new SimpleField();
        field.setName("aName");
        field.setType(FieldType.BOOLEAN);

        assertThat(field).canBeMarshalled();
    }

    @Test
    public void should_have_other_optionnal_attributes() {
        final SimpleField field = new SimpleField();
        field.setName("aName");
        field.setType(FieldType.BOOLEAN);
        field.setCollection(true);
        field.setLength(123);
        field.setNullable(true);

        assertThat(field).canBeMarshalled();
    }
}
