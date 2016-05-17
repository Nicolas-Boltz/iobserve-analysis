/***************************************************************************
 * Copyright 2015 iObserve Project (http://dfg-spp1593.de/index.php?id=44)
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
package org.iobserve.analysis.model;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

/**
 *
 * @author Robert Heinrich, Alessandro Giusa, alessandrogiusa@gmail.com
 * @version 1.0, 20.01.2015
 */
public class AllocationModelProvider extends AbstractModelProvider<Allocation> {

	// ********************************************************************
	// * INITIALIZATION
	// ********************************************************************

	public AllocationModelProvider(final URI uriModelInstance, final ModelProviderPlatform thePlatform) {
		super(uriModelInstance, thePlatform);
	}

	// ********************************************************************
	// * GETTER / SETTER
	// ********************************************************************

	@Override
	public EPackage getPackage() {
		return AllocationPackage.eINSTANCE;
	}
	
//	@Override
//	public void resetModel() {
//		final Allocation alloction = this.getModel();
//		alloction.getAllocationContexts_Allocation().clear();
//	}

	// ********************************************************************
	// * ADD ALLOCATION CONTEXT
	// ********************************************************************

	public void addAllocationContext(final Class<?> type) {
		// TODO add an allocation
	}

	public void removeAllocationContext(final Class<?> type) {
		// TODO remove allocation context
	}

	
	public AllocationContext addAllocationContext(final ResourceContainer resContainer,
			final AssemblyContext asmCtx) {
		
		final AllocationFactory factory = AllocationFactory.eINSTANCE;
		final AllocationContext allocationCtx = factory.createAllocationContext();
		allocationCtx.setEntityName(asmCtx.getEntityName());
		allocationCtx.setAssemblyContext_AllocationContext(asmCtx);
		allocationCtx.setResourceContainer_AllocationContext(resContainer);
		this.getModel().getAllocationContexts_Allocation().add(allocationCtx);
		return allocationCtx;
	}

	// TODO testing
	public void removeAllocationContext(final String id) {
		final AllocationContext ctx = (AllocationContext) this.getIdentifiableComponent(id,
				this.getModel().getAllocationContexts_Allocation());
		this.getModel().getAllocationContexts_Allocation().remove(ctx);
	}
}
