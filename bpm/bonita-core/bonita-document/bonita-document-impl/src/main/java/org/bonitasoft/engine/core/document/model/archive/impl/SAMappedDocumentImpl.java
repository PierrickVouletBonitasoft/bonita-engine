/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.document.model.archive.impl;

import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.document.model.impl.SMappedDocumentImpl;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SAMappedDocumentImpl extends SMappedDocumentImpl implements SAMappedDocument {

    private static final long serialVersionUID = 1L;
    private long archiveDate;
    private long sourceObjectId;

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SMappedDocument.class;
    }

    @Override
    public long getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final long archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public String getDiscriminator() {
        return SAMappedDocument.class.getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SAMappedDocumentImpl that = (SAMappedDocumentImpl) o;

        if (archiveDate != that.archiveDate) {
            return false;
        }
        if (sourceObjectId != that.sourceObjectId) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (archiveDate ^ archiveDate >>> 32);
        result = 31 * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
        return result;
    }

    @Override
    public String toString() {
        return "SAMappedDocumentImpl{" +
                "sourceObjectId=" + sourceObjectId +
                ", archiveDate=" + archiveDate +
                "} " + super.toString();
    }
}
