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
package org.iobserve.evaluation.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import teetime.framework.test.StageTester;

import org.iobserve.analysis.clustering.filter.models.BehaviorModel;
import org.iobserve.analysis.clustering.filter.models.CallInformation;
import org.iobserve.analysis.clustering.filter.models.EntryCallEdge;
import org.iobserve.analysis.clustering.filter.models.EntryCallNode;
import org.iobserve.evaluation.data.ComparisonResult;
import org.iobserve.evaluation.data.NodeDifference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the model comparison stage.
 *
 * @author Reiner Jung
 *
 */
public class ModelComparisonStageTest {

    private final EntryCallNode nodeA = new EntryCallNode("A");
    private final EntryCallNode nodeAtest = new EntryCallNode("A");
    private final EntryCallNode nodeB = new EntryCallNode("B");
    private final EntryCallNode nodeBtest = new EntryCallNode("B");

    private final EntryCallNode nodeC = new EntryCallNode("C");
    private final EntryCallNode nodeDtest = new EntryCallNode("D");

    private final ComparisonResult expectedResult = new ComparisonResult();
    private final BehaviorModel referenceModel = this.createReferenceModel();
    private final BehaviorModel testModel = this.createTestModel();

    /**
     * Setup test by creating input and output models.
     */
    @Before
    public void setup() {
        this.expectedResult.setAdditionalEdgeCount(1);
        this.expectedResult.getAdditionalNodes().add(this.nodeDtest);
        this.expectedResult.getBaselineEdges().addAll(this.referenceModel.getEdges());
        this.expectedResult.getBaselineNodes().addAll(this.referenceModel.getNodes());
        this.expectedResult.setMissingEdgeCount(1);
        this.expectedResult.getMissingNodes().add(this.nodeC);

        this.expectedResult.getSimilarNodes().add(this.nodeA);

        final List<CallInformation> missingInformation = new ArrayList<>();
        final List<CallInformation> additionalInformation = new ArrayList<>();
        final NodeDifference nodeDiff = new NodeDifference(this.nodeA, this.nodeAtest, missingInformation,
                additionalInformation);
        final NodeDifference nodeDiff2 = new NodeDifference(this.nodeB, this.nodeBtest, missingInformation,
                additionalInformation);
        this.expectedResult.getNodeDifferences().add(nodeDiff2);
        this.expectedResult.getNodeDifferences().add(nodeDiff);

        this.expectedResult.getSimilarNodes().add(this.nodeB);
        this.expectedResult.getTestModelEdges().addAll(this.testModel.getEdges());
        this.expectedResult.getTestModelNodes().addAll(this.testModel.getNodes());
    }

    private BehaviorModel createTestModel() {
        final BehaviorModel model = new BehaviorModel();

        model.addNode(this.nodeAtest);
        model.addNode(this.nodeBtest);
        model.addNode(this.nodeDtest);

        final EntryCallEdge edgeAB = new EntryCallEdge(this.nodeAtest, this.nodeBtest);
        edgeAB.addCalls(4);
        model.addEdge(edgeAB);

        final EntryCallEdge edgeBA = new EntryCallEdge(this.nodeBtest, this.nodeAtest);
        edgeBA.addCalls(3);
        model.addEdge(edgeBA);

        final EntryCallEdge edgeBD = new EntryCallEdge(this.nodeBtest, this.nodeDtest);
        edgeBA.addCalls(1);
        model.addEdge(edgeBD);

        return model;
    }

    private BehaviorModel createReferenceModel() {
        final BehaviorModel model = new BehaviorModel();

        model.addNode(this.nodeA);
        model.addNode(this.nodeB);
        model.addNode(this.nodeC);

        final EntryCallEdge edgeAB = new EntryCallEdge(this.nodeA, this.nodeB);
        edgeAB.addCalls(4);
        model.addEdge(edgeAB);

        final EntryCallEdge edgeBA = new EntryCallEdge(this.nodeB, this.nodeA);
        edgeBA.addCalls(3);
        model.addEdge(edgeBA);

        final EntryCallEdge edgeBC = new EntryCallEdge(this.nodeB, this.nodeC);
        edgeBA.addCalls(1);
        model.addEdge(edgeBC);

        return model;
    }

    /**
     * Check whether the comparer produces the correct output for the specified test models.
     */
    @Test
    public void testModelCompare() {
        final List<ComparisonResult> results = new ArrayList<>();

        final ModelComparisonStage stage = new ModelComparisonStage();

        StageTester.test(stage).and().send(this.referenceModel).to(stage.getReferenceModelInputPort()).and()
                .send(this.testModel).to(stage.getTestModelInputPort()).and().receive(results)
                .from(stage.getOutputPort()).start();

        Assert.assertTrue("We have no result", results.size() == 1);

        final ComparisonResult actualResult = results.get(0);

        Assert.assertEquals("Additional edges do not match up", this.expectedResult.getAdditionalEdgeCount(),
                actualResult.getAdditionalEdgeCount());
        this.checkNodeLists("Additional nodes", this.expectedResult.getAdditionalNodes(),
                actualResult.getAdditionalNodes());
        this.checkEdgesLists("Reference edges", this.expectedResult.getBaselineEdges(),
                actualResult.getBaselineEdges());
        this.checkNodeLists("Reference nodes", this.expectedResult.getBaselineNodes(), actualResult.getBaselineNodes());
        Assert.assertEquals("Missing edges do not match up", this.expectedResult.getMissingEdgeCount(),
                actualResult.getMissingEdgeCount());
        this.checkNodeLists("Missing nodes", this.expectedResult.getMissingNodes(), actualResult.getMissingNodes());

        this.checkNodeDifferences(this.expectedResult.getNodeDifferences(), actualResult.getNodeDifferences());
        this.checkNodeLists("Similar nodes", this.expectedResult.getSimilarNodes(), actualResult.getSimilarNodes());

        this.checkEdgesLists("Test model edges", this.expectedResult.getTestModelEdges(),
                actualResult.getTestModelEdges());
        this.checkNodeLists("Test model nodes", this.expectedResult.getTestModelNodes(),
                actualResult.getTestModelNodes());
    }

    /**
     * Check whether the edge lists contain the same content.
     *
     * @param label
     * @param expectedEdges
     * @param actualEdges
     */
    private void checkEdgesLists(final String label, final List<EntryCallEdge> expectedEdges,
            final List<EntryCallEdge> actualEdges) {
        this.checkEdgeLists(label, "expected but not in actual", expectedEdges, actualEdges);
        this.checkEdgeLists(label, "actual but not in expected", actualEdges, expectedEdges);
    }

    private void checkEdgeLists(final String label, final String direction, final List<EntryCallEdge> leftEdges,
            final List<EntryCallEdge> rightEdges) {
        for (final EntryCallEdge left : leftEdges) {
            boolean match = false;
            for (final EntryCallEdge right : rightEdges) {
                if (left.getSource().getSignature().equals(right.getSource().getSignature())
                        && left.getTarget().getSignature().equals(right.getTarget().getSignature())) {
                    Assert.assertTrue(label + ": Edges count mismatch for " + left.getSource().getSignature() + " -> "
                            + left.getTarget().getSignature(), left.getCalls() == right.getCalls());
                    match = true;
                    break;
                }
            }
            Assert.assertTrue(label + ": Edges present in " + direction + " model. Missing "
                    + left.getSource().getSignature() + " -> " + left.getTarget().getSignature(), match);
        }
    }

    /**
     * Check whether the node lists contain the same content.
     *
     * @param label
     * @param expectedNodes
     * @param actualNodes
     */
    private void checkNodeLists(final String label, final List<EntryCallNode> expectedNodes,
            final List<EntryCallNode> actualNodes) {
        this.checkNodeLists(label, "expected but not in actual", expectedNodes, actualNodes);
        this.checkNodeLists(label, "actual but not in expected", actualNodes, expectedNodes);

    }

    /**
     * Perform the comparison.
     *
     * @param label
     * @param direction
     * @param leftNodes
     * @param rightNodes
     */
    private void checkNodeLists(final String label, final String direction, final List<EntryCallNode> leftNodes,
            final List<EntryCallNode> rightNodes) {
        for (final EntryCallNode left : leftNodes) {
            boolean match = false;
            for (final EntryCallNode right : rightNodes) {
                if (left.getSignature().equals(right.getSignature())) {
                    this.checkInformationEqual(left.getEntryCallInformation(), right.getEntryCallInformation(), label);
                    match = true;
                    break;
                }
            }
            Assert.assertTrue(label + ": Nodes are present in " + direction + " model. Missing " + left.getSignature(),
                    match);
        }
    }

    private void checkNodeDifferences(final List<NodeDifference> expectedDifferences,
            final List<NodeDifference> actualDifferences) {
        this.areNodeDifferencesEqual(expectedDifferences, actualDifferences, "Node Differences");
        this.areNodeDifferencesEqual(actualDifferences, expectedDifferences, "Node Differences");
    }

    private void areNodeDifferencesEqual(final List<NodeDifference> expectedDifferences,
            final List<NodeDifference> actualDifferences, final String label) {
        for (final NodeDifference expected : expectedDifferences) {
            boolean match = false;
            for (final NodeDifference actual : actualDifferences) {
                if (actual.getReferenceNode().getSignature().equals(expected.getReferenceNode().getSignature())
                        && actual.getTestModelNode().getSignature()
                                .equals(expected.getTestModelNode().getSignature())) {
                    this.checkInformationEqual(actual.getAdditionalInformation(), expected.getAdditionalInformation(),
                            label);
                    this.checkInformationEqual(actual.getMissingInformation(), expected.getMissingInformation(), label);
                    match = true;
                    break;
                }
            }
            Assert.assertTrue(label + ": Node signatures do not match up", match);
        }
    }

    private void checkInformationEqual(final Set<CallInformation> actualInfo, final Set<CallInformation> expectedInfo,
            final String label) {
        this.checkInformationEqualCheck(actualInfo, expectedInfo, label, "actual but not in expected");
        this.checkInformationEqualCheck(expectedInfo, actualInfo, label, "expected but not in actual");
    }

    private void checkInformationEqualCheck(final Set<CallInformation> leftInfo, final Set<CallInformation> rightInfo,
            final String label, final String direction) {
        for (final CallInformation left : leftInfo) {
            boolean match = false;
            for (final CallInformation right : rightInfo) {
                if (left.getInformationCode() == right.getInformationCode()
                        && left.getInformationSignature().equals(right.getInformationSignature())) {
                    match = true;
                    break;
                }
            }
            Assert.assertTrue(label + ": Annotated information is present in " + direction + " model. Missing element "
                    + left.getInformationSignature() + " " + left.getInformationCode(), match);
        }
    }

    private void checkInformationEqual(final Collection<CallInformation> actualInfo,
            final Collection<CallInformation> expectedInfo, final String label) {
        this.checkInformationEqualCheck(actualInfo, expectedInfo, label, "actual but not in expected");
        this.checkInformationEqualCheck(expectedInfo, actualInfo, label, "expected but not in actual");
    }

    private void checkInformationEqualCheck(final Collection<CallInformation> leftInfo,
            final Collection<CallInformation> rightInfo, final String label, final String direction) {
        for (final CallInformation left : leftInfo) {
            boolean match = false;
            for (final CallInformation right : rightInfo) {
                if (left.getInformationCode() == right.getInformationCode()
                        && left.getInformationSignature().equals(right.getInformationSignature())) {
                    match = true;
                    break;
                }
            }
            Assert.assertTrue(label + ": Annotated information is present in " + direction + " model. Missing element "
                    + left.getInformationSignature() + " " + left.getInformationCode(), match);
        }
    }

}
