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
package org.bonitasoft.engine.bpm.bar.formmapping.builder;

import static org.bonitasoft.engine.bpm.bar.formmapping.builder.FormMappingBuilder.aFormMapping;
import static org.bonitasoft.engine.bpm.bar.formmapping.model.FormMappingDefinition.FormMappingType.HUMAN_TASK;

import org.bonitasoft.engine.bpm.bar.formmapping.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.formmapping.model.FormMappingModel;

/**
 * @author Emmanuel Duchastenier
 */
public class FormMappingModelBuilder {

    private final FormMappingModel formMappingModel = new FormMappingModel();

    public static FormMappingModelBuilder aFormMappingModel() {
        return new FormMappingModelBuilder();
    }

    public FormMappingModelBuilder withFormMapping(final FormMappingDefinition mapping) {
        formMappingModel.addFormMapping(mapping);
        return this;
    }

    public FormMappingModel build() {
        return formMappingModel;
    }

    private FormMappingDefinition buildMyFormMapping() {
        return aFormMapping("/FormMapping.html", HUMAN_TASK, false).withTaskname("aTask").build();
    }

    public FormMappingModel buildDefaultModelWithOneFormMapping() {
        final FormMappingModel model = buildEmptyDefaultModel();
        model.addFormMapping(buildMyFormMapping());
        return model;
    }

    public FormMappingModel buildEmptyDefaultModel() {
        return new FormMappingModel();
    }

}
