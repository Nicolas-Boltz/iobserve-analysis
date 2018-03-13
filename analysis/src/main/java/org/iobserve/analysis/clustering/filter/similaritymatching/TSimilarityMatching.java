/***************************************************************************
 * Copyright (C) 2018 iObserve Project (https://www.iobserve-devops.net)
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
package org.iobserve.analysis.clustering.filter.similaritymatching;

import org.iobserve.analysis.clustering.behaviormodels.BehaviorModel;
import org.iobserve.analysis.configurations.ConfigurationKeys;
import org.iobserve.analysis.session.data.UserSession;
import org.iobserve.service.InstantiationFactory;
import org.iobserve.stages.general.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kieker.common.configuration.Configuration;
import teetime.framework.CompositeStage;
import teetime.framework.InputPort;
import teetime.framework.OutputPort;

public class TSimilarityMatching extends CompositeStage implements IClassificationStage {
    private static final Logger LOGGER = LoggerFactory.getLogger(BehaviorCompositeStage.class);

    private final InputPort<UserSession> sessionInputPort;
    private final InputPort<Long> timerInputPort;
    private final OutputPort<BehaviorModel[]> outputPort;

    public TSimilarityMatching(final Configuration configuration) throws ConfigurationException {
        /** Instantiate configurable objects/properties */

        /** For TVectorization */
        final String structureMetricClassName = configuration
                .getStringProperty(ConfigurationKeys.SIM_MATCH_STRUCTURE_STRATEGY);
        if (structureMetricClassName.isEmpty()) {
            TSimilarityMatching.LOGGER.error("Initialization incomplete: No structure metric strategy specified.");
            throw new ConfigurationException("Initialization incomplete: No structure metric strategy specified.");
        }
        final IStructureMetricStrategy structureMetric = InstantiationFactory.create(IStructureMetricStrategy.class,
                structureMetricClassName, null);

        /** For TVectorization */
        final String parameterMetricClassName = configuration
                .getStringProperty(ConfigurationKeys.SIM_MATCH_PARAMETER_STRATEGY);
        if (parameterMetricClassName.isEmpty()) {
            TSimilarityMatching.LOGGER.error("Initialization incomplete: No parameter metric strategy specified.");
            throw new ConfigurationException("Initialization incomplete: No parameter metric strategy specified.");
        }
        final IParameterMetricStrategy parameterMetric = InstantiationFactory.create(IParameterMetricStrategy.class,
                parameterMetricClassName, null);

        /** For TModelGeneration */
        final String modelStrategyClassName = configuration
                .getStringProperty(ConfigurationKeys.SIM_MATCH_MODEL_STRATEGY);
        if (modelStrategyClassName.isEmpty()) {
            TSimilarityMatching.LOGGER.error("Initialization incomplete: No model generation strategy specified.");
            throw new ConfigurationException("Initialization incomplete: No model generation strategy specified.");
        }
        final IModelGenerationStrategy modelGenerationStrategy = InstantiationFactory
                .create(IModelGenerationStrategy.class, modelStrategyClassName, null);

        /** For TGroupingStage */
        final double similarityRadius = configuration.getDoubleProperty(ConfigurationKeys.SIM_MATCH_RADIUS, -1);
        if (similarityRadius < 0) {
            TSimilarityMatching.LOGGER.error("Initialization incomplete: No similarity radius specified.");
            throw new ConfigurationException("Initialization incomplete: No similarity radius specified.");
        }

        /** Create individual stages */
        final TSessionToModel sessionToModel = new TSessionToModel();
        final TVectorization vectorization = new TVectorization(structureMetric, parameterMetric);
        final TGroupingStage groupingStage = new TGroupingStage(similarityRadius);
        final TModelGeneration modelGeneration = new TModelGeneration(modelGenerationStrategy);

        /** Connect ports */
        this.sessionInputPort = sessionToModel.getInputPort();
        this.timerInputPort = vectorization.getTimerInputPort();
        this.connectPorts(sessionToModel.getOutputPort(), vectorization.getModelInputPort());
        this.connectPorts(vectorization.getVectorsOutputPort(), groupingStage.getInputPort());
        this.connectPorts(groupingStage.getOutputPort(), modelGeneration.getGroupsInputPort());
        this.connectPorts(vectorization.getModelsOutputPort(), modelGeneration.getModelsInputPort());
        this.outputPort = modelGeneration.getOutputPort();
    }

    @Override
    public InputPort<UserSession> getSessionInputPort() {
        return this.sessionInputPort;
    }

    @Override
    public InputPort<Long> getTimerInputPort() {
        return this.timerInputPort;
    }

    @Override
    public OutputPort<BehaviorModel[]> getOutputPort() {
        return this.outputPort;
    }
}
