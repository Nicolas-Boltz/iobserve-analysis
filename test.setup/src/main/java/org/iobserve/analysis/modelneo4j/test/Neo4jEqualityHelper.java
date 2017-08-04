/***************************************************************************
 * Copyright (C) 2017 iObserve Project (https://www.iobserve-devops.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package org.iobserve.analysis.modelneo4j.test;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;

/**
 * Modified {@link EqualityHelper} which handles proxies differently. In the neo4j database a proxy
 * node does not reference other nodes and only contains attributes and an uri. For this reason we
 * only check if these properties are equal here.
 *
 * @author Lars Bluemke
 *
 * @see EqualityHelper
 *
 */
public class Neo4jEqualityHelper extends EqualityHelper {

    private static final long serialVersionUID = 1L;

    /**
     * Returns whether <code>eObject1</code> and <code>eObject2</code> are {@link EqualityHelper
     * equal} in the context of this helper instance.
     *
     * @return whether <code>eObject1</code> and <code>eObject2</code> are equal.
     * @since 2.1.0
     */
    @Override
    public boolean equals(final EObject eObject1, final EObject eObject2) {
        // If the first object is null, the second object must be null.
        //
        if (eObject1 == null) {
            return eObject2 == null;
        }

        // We know the first object isn't null, so if the second one is, it can't be equal.
        //
        if (eObject2 == null) {
            return false;
        }

        // Both eObject1 and eObject2 are not null.
        // If eObject1 has been compared already...
        //
        final Object eObject1MappedValue = this.get(eObject1);
        if (eObject1MappedValue != null) {
            // Then eObject2 must be that previous match.
            //
            return eObject1MappedValue == eObject2;
        }

        // If eObject2 has been compared already...
        //
        final Object eObject2MappedValue = this.get(eObject2);
        if (eObject2MappedValue != null) {
            // Then eObject1 must be that match.
            //
            return eObject2MappedValue == eObject1;
        }

        // Neither eObject1 nor eObject2 have been compared yet.

        // If eObject1 and eObject2 are the same instance...
        //
        if (eObject1 == eObject2) {
            // Match them and return true.
            //
            this.put(eObject1, eObject2);
            this.put(eObject2, eObject1);
            return true;
        }

        // If they don't have the same class, they can't be equal.
        //
        final EClass eClass = eObject1.eClass();
        if (eClass != eObject2.eClass()) {
            return false;
        }

        // Modified proxy check: Only attributes have to be equal
        if (eObject1.eIsProxy() || eObject2.eIsProxy()) {
            for (final EAttribute attr : eClass.getEAllAttributes()) {
                if (!this.haveEqualAttribute(eObject1, eObject2, attr)) {
                    return false;
                }
            }
        } else {
            // Assume from now on that they match.
            //
            this.put(eObject1, eObject2);
            this.put(eObject2, eObject1);

            // Check all the values.
            //
            for (int i = 0, size = eClass.getFeatureCount(); i < size; ++i) {
                // Ignore derived features.
                //
                final EStructuralFeature feature = eClass.getEStructuralFeature(i);
                if (!feature.isDerived()) {
                    if (!this.haveEqualFeature(eObject1, eObject2, feature)) {
                        this.remove(eObject1);
                        this.remove(eObject2);
                        return false;
                    }
                }
            }
        }
        // There's no reason they aren't equal, so they are.
        //
        return true;
    }

}
