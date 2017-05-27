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
package org.iobserve.analysis.cdoruserbehavior.filter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iobserve.analysis.cdoruserbehavior.filter.models.AbstractBehaviorModelTable;
import org.iobserve.analysis.cdoruserbehavior.filter.models.BehaviorModel;
import org.iobserve.analysis.cdoruserbehavior.filter.models.CallInformation;
import org.iobserve.analysis.cdoruserbehavior.filter.models.EntryCallEdge;
import org.iobserve.analysis.cdoruserbehavior.filter.models.EntryCallNode;

import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Christoph Dornieden
 *
 */
public class TBehaviorModelCreation extends AbstractConsumerStage<Instances> {
    private final String namePrefix;

    private final OutputPort<BehaviorModel> outputPort = this.createOutputPort();

    public TBehaviorModelCreation(final String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    protected void execute(final Instances instances) {

        final int size = instances.numInstances();

        for (int i = 0; i < size; i++) {
            final Instance instance = instances.instance(i);
            final BehaviorModel behaviorModel = this.createBehaviorModel(instances, instance);
            behaviorModel.setName(this.namePrefix + i);
            this.outputPort.send(behaviorModel);
        }
    }

    /**
     * getter
     *
     * @return the outputPort
     */
    public OutputPort<BehaviorModel> getOutputPort() {
        return this.outputPort;
    }

    /**
     * create a BehaviorModel from Instance
     *
     * @param instances
     *            instances containing the attribute names
     * @param instance
     *            instance containing the attributes
     * @return behavior model
     */
    private BehaviorModel createBehaviorModel(final Instances instances, final Instance instance) {
        final int size = instance.numAttributes();
        final BehaviorModel behaviorModel = new BehaviorModel();

        for (int i = 0; i < size; i++) {
            final Attribute attribute = instances.attribute(i);

            final String attributeName = attribute.name();
            final Double attributeValue = instance.value(attribute);

            if (this.matchEdge(attributeName)) {
                final Optional<EntryCallEdge> edge = this.createEdge(attributeName, attributeValue);

                if (edge.isPresent()) {
                    behaviorModel.addEdge(edge.get());
                }

            } else if (this.matchNode(attributeName)) {
                final Optional<EntryCallNode> node = this.createNode(attributeName, attributeValue);

                if (node.isPresent()) {
                    behaviorModel.addNode(node.get());
                }

            }
        }
        return behaviorModel;
    }

    /**
     * Does the attribute name represent an edge?
     *
     * @param name
     *            attribute name
     * @return true if the name represents an edge, false else
     */
    private boolean matchEdge(final String name) {
        return this.matchStart(AbstractBehaviorModelTable.EDGE_INDICATOR_PATTERN, name);
    }

    /**
     * Does the attribute name represent a node?
     *
     * @param name
     *            attribute name
     * @return true if the name represents an node, false else
     */
    private boolean matchNode(final String name) {
        return this.matchStart(AbstractBehaviorModelTable.INFORMATION_INDICATOR_PATTERN, name);
    }

    /**
     * does the pattern matches the start of the string
     *
     * @param pattern
     *            pattern
     * @param string
     *            string
     * @return true if pattern is found at the beginning of the string, false else
     */
    private boolean matchStart(final Pattern pattern, final String string) {
        final Matcher matcher = pattern.matcher(string);
        final boolean match = matcher.find() ? matcher.start() == 0 : false;
        return match;
    }

    /**
     * creates an edge from an edge representing attribute string and value
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     *
     * @return EntryCallEdge
     */
    private Optional<EntryCallEdge> createEdge(final String name, final Double value) {
        if (value > 0) {

            final String[] nodeNames = this.splitSignature(AbstractBehaviorModelTable.EDGE_INDICATOR_PATTERN,
                    AbstractBehaviorModelTable.EDGE_DIVIDER_PATTERN, name);

            if (nodeNames.length == 2) {
                final EntryCallNode from = new EntryCallNode(nodeNames[0]);
                final EntryCallNode to = new EntryCallNode(nodeNames[1]);

                final EntryCallEdge edge = new EntryCallEdge(from, to, value);

                return Optional.of(edge);
            }
        }

        return Optional.empty();

    }

    /**
     * creates an node from a node representing attribute string and value
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     *
     * @return EntryCallNode
     */
    private Optional<EntryCallNode> createNode(final String name, final Double value) {

        final String[] signatures = this.splitSignature(AbstractBehaviorModelTable.INFORMATION_INDICATOR_PATTERN,
                AbstractBehaviorModelTable.INFORMATION_DIVIDER_PATTERN, name);

        if (signatures.length == 2) {
            final EntryCallNode node = new EntryCallNode(signatures[0]);
            final CallInformation callInformation = new CallInformation(signatures[1], value);

            node.getEntryCallInformation().add(callInformation);

            return Optional.of(node);
        } else {
            return Optional.empty();
        }
    }

    /**
     * splits the signature
     *
     * @param indicator
     *            indicator
     * @param divider
     *            divider
     * @param signature
     *            signature
     * @return separate strings
     */
    private String[] splitSignature(final Pattern indicatorPattern, final Pattern dividerPattern,
            final String signature) {
        final String removedIndicator = indicatorPattern.matcher(signature).replaceAll("");
        final String[] dividerSplit = dividerPattern.split(removedIndicator);
        return dividerSplit;

    }

}
