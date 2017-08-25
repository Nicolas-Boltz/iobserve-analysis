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
package org.iobserve.analysis.cdoruserbehavior.filter.composite;

import org.iobserve.analysis.cdoruserbehavior.filter.TBehaviorModelCreation;
import org.iobserve.analysis.cdoruserbehavior.filter.TBehaviorModelVisualization;
import org.iobserve.analysis.cdoruserbehavior.filter.TVectorQuantizationClustering;
import org.iobserve.analysis.cdoruserbehavior.filter.models.configuration.BehaviorModelConfiguration;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import teetime.framework.CompositeStage;
import teetime.framework.InputPort;
import weka.core.Instances;

/**
 *
 * @author Christoph Dornieden
 */
public class TBehaviorModelAggregation extends CompositeStage {
    /** logger. */
    private static final Log LOG = LogFactory.getLog(TBehaviorModelAggregation.class);
    // private final EMClusteringProcess tClustering;
    private final TVectorQuantizationClustering tClustering;
    private final TBehaviorModelCreation tBehaviorModelCreation;
    private final TBehaviorModelVisualization tIObserveUBM;

    private final BehaviorModelConfiguration configuration;

    /**
     * Constructor configuration of the aggregation filters.
     */
    public TBehaviorModelAggregation(final BehaviorModelConfiguration configuration) {
        this.configuration = configuration;

        // this.tClustering = new EMClusteringProcess(new ExpectationMaximizationClustering());
        this.tClustering = new TVectorQuantizationClustering(this.configuration.getClustering());
        this.tBehaviorModelCreation = new TBehaviorModelCreation(configuration.getNamePrefix());
        this.tIObserveUBM = new TBehaviorModelVisualization(configuration.getVisualizationUrl(),
                configuration.getSignatureCreationStrategy());

        this.connectPorts(this.tClustering.getOutputPort(), this.tBehaviorModelCreation.getInputPort());
        this.connectPorts(this.tBehaviorModelCreation.getOutputPort(), this.tIObserveUBM.getInputPort());
    }

    /**
     * getter
     *
     * @return input port
     */
    public InputPort<Instances> getInputPort() {
        return this.tClustering.getInputPort();

    }

}