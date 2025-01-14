/***************************************************************************
 * Copyright (C) 2014 iObserve Project (https://www.iobserve-devops.net)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

/**
 * Model builder for resource environment models.
 *
 * @author Nicolas Boltz
 * @author Robert Heinrich
 *
 */
public final class ResourceEnvironmentModelBuilder {

    /**
     * Constructor for resource models with a start model.
     *
     * @param modelToStartWith
     *            model provider.
     */
    private ResourceEnvironmentModelBuilder() {
    }

    /**
     * Create a {@link ResourceContainer} with the given name, without checking if it already
     * exists. Use {@link #createResourceContainerIfAbsent(String)} instead if you wont create the
     * container if it is already available.
     *
     * @param model
     *            resource environment model
     * @param name
     *            name of the new container
     * @return builder
     */
    public static ResourceContainer createResourceContainer(final ResourceEnvironment model, final String name) {
        final ResourceContainer resContainer = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        resContainer.setEntityName(name);
        model.getResourceContainer_ResourceEnvironment().add(resContainer);
        return resContainer;
    }

    /**
     * Creates a link between the given two container.
     *
     * @param model
     *            resource environment model
     * @param res1
     *            first container
     * @param res2
     *            second container
     * @return link instance, already added to the model
     */
    public static LinkingResource connectResourceContainer(final ResourceEnvironment model,
            final ResourceContainer res1, final ResourceContainer res2) {
        final LinkingResource link = ResourceenvironmentFactory.eINSTANCE.createLinkingResource();
        link.getConnectedResourceContainers_LinkingResource().add(res1);
        link.getConnectedResourceContainers_LinkingResource().add(res2);
        model.getLinkingResources__ResourceEnvironment().add(link);

        return link;
    }

    /**
     * Get the {@link ResourceContainer} by its {@link ResourceContainer#getEntityName()}.
     *
     * @param resourceEnvironment
     *            the resource environment model
     * @param name
     *            name
     * @return resource container instance or null if no resource container available with the given
     *         name.
     */
    public static Optional<ResourceContainer> getResourceContainerByName(final ResourceEnvironment resourceEnvironment,
            final String name) {
        return resourceEnvironment.getResourceContainer_ResourceEnvironment().stream()
                .filter(container -> container.getEntityName().equals(name)).findFirst();
    }
    
    /**
     * Removes the {@link ResourceContainer} and every affiliated {@link LinkingResource} from the model.
     * 
     * @param model
     * 			resource environment model
     * @param container
     * 			targeted ResourceContainer
     * @return true: ResourceContainer removed; false: ResourceContainer not present in model
     */
    public static boolean removeResourceContainer(final ResourceEnvironment model, final ResourceContainer container) {
		removeAllLinkingResourcesToResourceContainer(model, container);
		return model.getResourceContainer_ResourceEnvironment().remove(container);
    }
    
    /**
     * Removes all {@link LinkingResource} affiliated with the {@link ResourceContainer}.
     * @param model
     * 			resource environment model
     * @param container
     * 			targeted ResourceContainer
     */
    public static void removeAllLinkingResourcesToResourceContainer(final ResourceEnvironment model, final ResourceContainer container) {
    	List<LinkingResource> linksToRemove = new ArrayList<>();
		for(LinkingResource link : model.getLinkingResources__ResourceEnvironment()) {
			if(link.getConnectedResourceContainers_LinkingResource().contains(container)) {
				linksToRemove.add(link);
			}
		}
		
		model.getLinkingResources__ResourceEnvironment().removeAll(linksToRemove);
    }

}
