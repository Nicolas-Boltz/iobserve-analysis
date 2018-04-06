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
package org.iobserve.analysis.clustering.birch;

import kieker.common.configuration.Configuration;

import teetime.framework.CompositeStage;
import teetime.framework.InputPort;
import teetime.framework.OutputPort;

import org.iobserve.analysis.clustering.birch.model.ICFComparisonStrategy;
import org.iobserve.analysis.clustering.filter.TBehaviorModelCreation;
import org.iobserve.analysis.clustering.filter.models.BehaviorModel;
import org.iobserve.analysis.clustering.filter.models.configuration.IRepresentativeStrategy;
import org.iobserve.analysis.clustering.filter.models.configuration.examples.JPetstoreStrategy;
import org.iobserve.analysis.clustering.shared.IClassificationStage;
import org.iobserve.analysis.configurations.ConfigurationKeys;
import org.iobserve.analysis.session.data.UserSession;
import org.iobserve.service.InstantiationFactory;
import org.iobserve.stages.general.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/** This class handles the classification process with the
 * birch algorithm. Transforms user sessions to behavior
 * models.
 * @author Melf Lorenzen
 *
 */
public class BirchClassificaton extends CompositeStage implements IClassificationStage {
	private static final Logger LOGGER = LoggerFactory.getLogger(BirchClassificaton.class);
    private InputPort<UserSession> sessionInputPort;
    private InputPort<Long> timerInputPort;
    
    private OutputPort<BehaviorModel> outputPort;

//    /**
//     * constructor for the BirchClassificaton composite stage.
//     * @param keepTime the time interval to keep user sessions
//     * @param minCollectionSize  minimal number of collected user session
//     * @param representativeStrategy representative strategy for behavior model table generation
//     * @param keepEmptyTransitions allows behavior model table generation to keep empty transitions
//	 * @param leafThresholdValue the merge threshold for the underlying cf tree
//	 * @param maxLeafSize the maximum number of entries in a leaf
//	 * @param maxNodeSize the maximum number of entries in a node
//	 * @param maxLeafEntries the maximum number of leaf entries in the underlying cf tree
//	 * @param expectedNumberOfClusters the expected number of clusters in the data
//	 * @param useClusterNumberMetric whether to use the expected number or 
//	 * number calculated by the cluster number metric
//	 * @param clusterComparisonStrategy the cluster comparison strategy 
//	 * @param evalStrategy the strategy for the l-method evaluation graph
//     */
//    public BirchClassificaton(final long keepTime, final int minCollectionSize, 
//    		final IRepresentativeStrategy representativeStrategy, final boolean keepEmptyTransitions,
//    		final double leafThresholdValue, final int maxLeafSize, final int maxNodeSize,
//    		final int maxLeafEntries, final int expectedNumberOfClusters, final boolean useClusterNumberMetric, 
//    		final ICFComparisonStrategy clusterComparisonStrategy,
//    		final ILMethodEvalStrategy evalStrategy) {
//
//        final SessionsToInstances sessionsToInstances = new SessionsToInstances(keepTime, minCollectionSize, 
//        		representativeStrategy, keepEmptyTransitions);
//        final BirchClustering birchClustering = new BirchClustering(leafThresholdValue, maxLeafSize,
//        		maxNodeSize, maxLeafEntries, expectedNumberOfClusters, useClusterNumberMetric, 
//        		clusterComparisonStrategy, evalStrategy);
//        final TBehaviorModelCreation tBehaviorModelCreation = new TBehaviorModelCreation("birch-");   
//        this.sessionInputPort = sessionsToInstances.getSessionInputPort();
//        this.timerInputPort = sessionsToInstances.getTimerInputPort();
//        this.outputPort = tBehaviorModelCreation.getOutputPort();
//        
//        this.connectPorts(sessionsToInstances.getOutputPort(), birchClustering.getInputPort());
//        this.connectPorts(birchClustering.getOutputPort(), tBehaviorModelCreation.getInputPort());
//    }

	@Override
	public void setupStage(final Configuration configuration) throws ConfigurationException {
        /** Get keep time for user sessions*/
        final long keepTime = configuration.getLongProperty(ConfigurationKeys.KEEP_TIME, -1);
        if (keepTime < 0) {
        	BirchClassificaton.LOGGER.error("Initialization incomplete: No keep time interval specified.");
            throw new ConfigurationException("Initialization incomplete: No keep time interval specified.");
        }
        
        final int minCollectionSize = configuration.getIntProperty(ConfigurationKeys.MIN_SIZE, -1);
        if (minCollectionSize < 0) {
        	BirchClassificaton.LOGGER.error("Initialization incomplete: No min size for user sessions specified.");
            throw new ConfigurationException("Initialization incomplete: No min size for user sessions specified.");
        }
        
        final boolean keepEmptyTransitions = configuration.getBooleanProperty(ConfigurationKeys.KEEP_EMPTY_TRANS, true);
        
		final boolean useClusterNumberMetric = configuration.getBooleanProperty(ConfigurationKeys.USE_CNM, true);
		
        /** Todo: incoperate to config */
		final IRepresentativeStrategy representativeStrategy = new JPetstoreStrategy();
		
        final String clusterComparisonStrategyClassName = configuration
                .getStringProperty(ConfigurationKeys.CLUSTER_METRIC_STRATEGY);
        BirchClassificaton.LOGGER.error("clusterComparisonStrategyClassName: " + clusterComparisonStrategyClassName);
        final ICFComparisonStrategy clusterComparisonStrategy = InstantiationFactory
                .create(ICFComparisonStrategy.class, clusterComparisonStrategyClassName, null);
        final String lmethodStrategyClassName = configuration
                .getStringProperty(ConfigurationKeys.LMETHOD_EVAL_STRATEGY);
        BirchClassificaton.LOGGER.error("lmethodStrategyClassName: " + lmethodStrategyClassName);
        final ILMethodEvalStrategy evalStrategy = InstantiationFactory
                .create(ILMethodEvalStrategy.class, lmethodStrategyClassName, null);
		
		final double leafThresholdValue = configuration.getDoubleProperty(ConfigurationKeys.LEAF_TH, -1.0);
        if (leafThresholdValue < 0) {
        	BirchClassificaton.LOGGER.error("Initialization incomplete: No threshold for leafs specified.");
            throw new ConfigurationException("Initialization incomplete: No threshold for leafs specified.");
        }
        
		final int maxLeafSize = configuration.getIntProperty(ConfigurationKeys.MAX_LEAF_SIZE, 7);
        if (maxLeafSize < 0) {
        	BirchClassificaton.LOGGER.error("Initialization incomplete: No max leaf size specified.");
            throw new ConfigurationException("Initialization incomplete: No max leaf size specified.");
        }
        
		final int maxNodeSize = configuration.getIntProperty(ConfigurationKeys.MAX_NODE_SIZE, -1);
        if (maxNodeSize < 0) {
        	BirchClassificaton.LOGGER.error("Initialization incomplete: No max node size specified.");
            throw new ConfigurationException("Initialization incomplete: No max node size specified..");
        }
        
		final int maxLeafEntries = configuration.getIntProperty(ConfigurationKeys.MAX_LEAF_ENTRIES, -1);
        if (maxLeafEntries < 0) {
        	BirchClassificaton.LOGGER.error("Initialization incomplete: No max number of leaf entries specified.");
            throw new ConfigurationException("Initialization incomplete: No max number of leaf entries specified.");
        }
        
		final int expectedNumberOfClusters = configuration.getIntProperty(ConfigurationKeys.EXP_NUM_OF_CLUSTERS, -1);
        if (expectedNumberOfClusters < 0) {
        	BirchClassificaton.LOGGER.error("Initialization incomplete: No expected numbers of clusters specified.");
            throw new ConfigurationException("Initialization incomplete: No expected numbers of clusters specified.");
        }
        
        final SessionsToInstances sessionsToInstances = new SessionsToInstances(keepTime, minCollectionSize, 
        		representativeStrategy, keepEmptyTransitions);
        final BirchClustering birchClustering = new BirchClustering(leafThresholdValue, maxLeafSize,
        		maxNodeSize, maxLeafEntries, expectedNumberOfClusters, useClusterNumberMetric, 
        		clusterComparisonStrategy, evalStrategy);
        final TBehaviorModelCreation tBehaviorModelCreation = new TBehaviorModelCreation("birch-");   

        this.sessionInputPort = sessionsToInstances.getSessionInputPort();
        this.timerInputPort = sessionsToInstances.getTimerInputPort();
        this.outputPort = tBehaviorModelCreation.getOutputPort();
        
        this.connectPorts(sessionsToInstances.getOutputPort(), birchClustering.getInputPort());
        this.connectPorts(birchClustering.getOutputPort(), tBehaviorModelCreation.getInputPort());
	}
    
    /**
     * get matching input port.
     *
     * @return input port
     */

    public InputPort<UserSession> getSessionInputPort() {
        return this.sessionInputPort;
    }

    public InputPort<Long> getTimerInputPort() {
        return this.timerInputPort;
    }
    
    /**
     * get suitable output port.
     *
     * @return outputPort
     */
    public OutputPort<BehaviorModel> getOutputPort() {
        return this.outputPort;
    }

}

