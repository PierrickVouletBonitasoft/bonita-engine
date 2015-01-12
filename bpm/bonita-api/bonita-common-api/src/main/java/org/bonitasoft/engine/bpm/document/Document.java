/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.bpm.document;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 */
public interface Document extends NamedElement, BaseElement {

    /**
     * Id of the process instance the document is attached to.
     */
    long getProcessInstanceId();

    /**
     * Download URL of the document.
     */
    String getUrl();

    /**
     * Returns true if he document has content.
     */
    boolean hasContent();

    /**
     * Get the username of the user who attached the document to the process instance.
     */
    long getAuthor();

    /**
     * Get the mime type of the document's content.
     */
    String getContentMimeType();

    /**
     * Get the file name of the document.
     */
    String getContentFileName();

    /**
     * Get the date when the document was attached to the process instance.
     */
    Date getCreationDate();

    /**
     * Get the Id to use to retrieve the document's content.
     */
    String getContentStorageId();

    /**
     * @return the description of the document
     * @since 6.4.0
     */
    String getDescription();

    /**
     * The version of the document is starting from 1 and is incremented each time a new version is attached
     *
     * @return the version of the document
     * @since 6.4.0
     */
    String getVersion();

    int getIndex();
}
