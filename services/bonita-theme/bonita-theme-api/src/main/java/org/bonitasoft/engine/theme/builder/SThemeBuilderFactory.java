/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.theme.builder;

import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;

/**
 * @author Celine Souchet
 */
public interface SThemeBuilderFactory {

    public static final String ID = "id";

    public static final String CONTENT = "content";

    public static final String CSS_CONTENT = "cssContent";

    public static final String LAST_UPDATE_DATE = "lastUpdateDate";

    public static final String TYPE = "type";

    public static final String IS_DEFAULT = "isDefault";

    SThemeBuilder createNewInstance(STheme theme);

    SThemeBuilder createNewInstance(byte[] content, boolean isDefault, SThemeType type, long lastUpdateDate);

}
