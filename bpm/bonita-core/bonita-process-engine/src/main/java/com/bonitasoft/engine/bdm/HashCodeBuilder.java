/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

/**
 * @author Romain Bioteau
 *
 */
public class HashCodeBuilder {

	public JMethod generate(JDefinedClass definedClass) {
		JMethod hashCodeMethod = definedClass.method(JMod.PUBLIC, int.class, "hashCode");
		hashCodeMethod.body()._return(JExpr.direct("0"));
		return hashCodeMethod;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		«FOREACH this.EStructuralFeatureModelGenAnnotations AS featureAnnotation-»
//			«IF featureAnnotation.reference || !featureAnnotation.primitive »
//				result = prime * result
//						+ ((«featureAnnotation.name» == null) ? 0 : «featureAnnotation.name».hashCode());
//			«ELSEIF !featureAnnotation.reference && featureAnnotation.primitive -»
//				«IF featureAnnotation.type == "boolean" »
//				result = prime * result + («featureAnnotation.name» ? 1231 : 1237) ;
//				«ELSEIF featureAnnotation.type == "long"-»
//				result = prime * result + (int) («featureAnnotation.name» ^ («featureAnnotation.name» >>> 32));
//				«ELSEIF featureAnnotation.type == "double"-»
//				long temp;
//				temp = Double.doubleToLongBits(«featureAnnotation.name»);
//				result = prime * result + (int) (temp ^ (temp >>> 32));
//				«ELSEIF featureAnnotation.type == "float"-»
//				result = prime * result + Float.floatToIntBits(«featureAnnotation.name»);
//				«ENDIF-»
//			«ENDIF-»
//	 	«ENDFOREACH-»
//		return result;
//	}
//}
	
}
