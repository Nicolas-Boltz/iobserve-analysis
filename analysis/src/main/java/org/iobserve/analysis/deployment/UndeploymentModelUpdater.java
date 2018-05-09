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
package org.iobserve.analysis.deployment;

import java.util.List;

import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

import org.iobserve.analysis.deployment.data.PCMUndeployedEvent;
import org.iobserve.model.provider.neo4j.IModelProvider;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;

/**
 * This class contains the transformation for updating the PCM allocation model with respect to
 * undeployment. It processes undeployment events and uses the correspondence information in the RAC
 * to update the PCM allocation model.
 *
 * @author Robert Heinrich
 * @author Reiner Jung
 * @author Josefine Wegner
 *
 */
public final class UndeploymentModelUpdater extends AbstractConsumerStage<PCMUndeployedEvent> {

    /** reference to allocation model provider. */
    private final IModelProvider<Allocation> allocationModelGraphProvider;
    /** reference to system model provider. */
    private final IModelProvider<AllocationContext> allocationContextModelGraphProvider;

    private final OutputPort<PCMUndeployedEvent> outputPort = this.createOutputPort();

    /**
     * Most likely the constructor needs an additional field for the PCM access. But this has to be
     * discussed with Robert.
     *
     * @param allocationModelGraphProvider
     *            allocation model access
     * @param allocationContextModelGraphProvider
     *            system model access
     */
    public UndeploymentModelUpdater(final IModelProvider<Allocation> allocationModelGraphProvider,
            final IModelProvider<AllocationContext> allocationContextModelGraphProvider) {
        this.allocationModelGraphProvider = allocationModelGraphProvider;
        this.allocationContextModelGraphProvider = allocationContextModelGraphProvider;
    }

    /**
     * This method is triggered for every undeployment event.
     *
     * @param event
     *            undeployment event
     */
    @Override
    protected void execute(final PCMUndeployedEvent event) {
        final String allocationContextName = event.getAssemblyContext().getEntityName() + " : "
                + event.getResourceContainer().getEntityName();

        final List<AllocationContext> allocationContexts = this.allocationContextModelGraphProvider
                .readOnlyComponentByName(AllocationContext.class, allocationContextName);

        if (allocationContexts.size() == 1) {
            final AllocationContext allocationContext = allocationContexts.get(0);
            this.allocationContextModelGraphProvider.deleteComponent(AllocationContext.class,
                    allocationContext.getId());
            this.outputPort.send(event);
        } else if (allocationContexts.size() > 1) {
            this.logger.error("Undeployment failed: More than one allocation found for allocation {}",
                    allocationContextName);
        } else {
            this.logger.error("Undeployment failed: No allocation found for allocation {}", allocationContextName);
        }
    }

    /**
     *
     * @return output port that signals a model update to the deployment visualization
     */
    public OutputPort<PCMUndeployedEvent> getOutputPort() {
        return this.outputPort;
    }

}
