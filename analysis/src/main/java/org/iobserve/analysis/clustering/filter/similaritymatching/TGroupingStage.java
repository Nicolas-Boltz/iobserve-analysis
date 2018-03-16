package org.iobserve.analysis.clustering.filter.similaritymatching;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

public class TGroupingStage extends AbstractConsumerStage<Double[][]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TGroupingStage.class);

    private final OutputPort<Integer[][]> outputPort = this.createOutputPort();

    private final double similarityRadius;

    private final List<List<Integer>> groups = new ArrayList<>();

    private Double[][] vectors;

    public TGroupingStage(final double similarityRadius) {
        super();

        // TODO: check arg validity (non-negative)
        this.similarityRadius = similarityRadius;
    }

    @Override
    protected void execute(final Double[][] vectors) {
        this.vectors = vectors;

        for (int i = 0; i < vectors.length; i++) {
            final List<Integer> group = this.findGroup(vectors[i]);

            if (group == null) {
                final List<Integer> newGroup = new ArrayList<>();
                newGroup.add(i);
                this.groups.add(newGroup);
            } else {
                group.add(i);
            }
        }

        /** Convert List<List<Integer>> to Integer[][] for sending */
        final Integer[][] aGroups = new Integer[this.groups.size()][];
        int i = 0;
        for (final List<Integer> g : this.groups) {
            aGroups[++i] = g.toArray(new Integer[g.size()]);
        }

        this.outputPort.send(aGroups);

        TGroupingStage.LOGGER.debug("Sent grouping to next stage");
    }

    private List<Integer> findGroup(final Double[] vector) {
        /** First matching group found is selected */
        for (final List<Integer> group : this.groups) {
            boolean matchAll = true;
            for (final Integer index : group) {
                if (!this.match(vector, this.vectors[index])) {
                    matchAll = false;
                    break;
                }
            }

            if (matchAll) {
                return group;
            }
        }

        return null;
    }

    private boolean match(final Double[] a, final Double[] b) {
        /** a and b should always have same length */
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) >= this.similarityRadius) {
                return false;
            }
        }

        return true;
    }

    public OutputPort<Integer[][]> getOutputPort() {
        return this.outputPort;
    }
}