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
 */
package org.bonitasoft.engine.bpm.contract;

import org.bonitasoft.engine.bpm.DescriptionElement;

/**
 * An <code>InputDefinition</code> defines
 *
 * @author Matthieu Chaffotte
 * @since 7.0
 */
public abstract class InputDefinition extends DescriptionElement {

    private static final long serialVersionUID = 2836592506382887928L;

    private final boolean multiple;

    protected InputDefinition(final String name, final String description, final boolean multiple) {
        super(name, description);
        this.multiple = multiple;
    }


    public boolean isMultiple() {
        return multiple;
    }
}
