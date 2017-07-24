/***************************************************************************
 * Copyright (C) 2017 iObserve Project (https://www.iobserve-devops.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.iobserve.analysis.cdoruserbehavior.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iobserve.analysis.cdoruserbehavior.filter.models.BehaviorModelTable;
import org.iobserve.analysis.data.EntryCallEvent;
import org.iobserve.analysis.filter.models.EntryCallSequenceModel;
import org.iobserve.analysis.filter.models.UserSession;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

/**
 * Prepares EntryCallSequenceModels for Clustering
 *
 * @author Christoph Dornieden
 *
 */

public final class TBehaviorModelPreperation extends AbstractConsumerStage<Object> {
    /** logger. */
    private static final Log LOG = LogFactory.getLog(TBehaviorModelPreperation.class);
    private final OutputPort<BehaviorModelTable> outputPort = this.createOutputPort();

    private BehaviorModelTable behaviorModelTable = null;
    private final Set<EntryCallSequenceModel> sequenceModelCache;

    private final boolean keepEmptyTransitions;

    public TBehaviorModelPreperation(final boolean keepEmptyTransitions) {
        super();
        this.behaviorModelTable = null;
        this.sequenceModelCache = new HashSet<>();
        this.keepEmptyTransitions = keepEmptyTransitions;
    }

    @Override
    protected void execute(final Object object) {
        if (object instanceof EntryCallSequenceModel) {
            final EntryCallSequenceModel entryCallSequenceModel = (EntryCallSequenceModel) object;
            this.executeEntryCallSequenceModel(entryCallSequenceModel);

        } else if (object instanceof BehaviorModelTable) {
            final BehaviorModelTable modelTable = (BehaviorModelTable) object;
            this.executeBehaviorModelTable(modelTable);

        } else {
            TBehaviorModelPreperation.LOG
                    .error("input is nether of type EntryCallSequenceModel nor BehaviorModelTable");
        }
    }

    /**
     * execute case object instanceof EntryCallSequenceModel
     *
     * @param entryCallSequenceModel
     *            entryCallSequenceModel
     */
    private void executeEntryCallSequenceModel(final EntryCallSequenceModel entryCallSequenceModel) {

        if (this.behaviorModelTable == null) {
            this.sequenceModelCache.add(entryCallSequenceModel);

        } else {
            final List<UserSession> userSessions = entryCallSequenceModel.getUserSessions();

            for (final UserSession userSession : userSessions) {
                final BehaviorModelTable modelTable = this.behaviorModelTable.getClearedCopy(this.keepEmptyTransitions);
                final List<EntryCallEvent> entryCalls = userSession.getEvents();

                EntryCallEvent lastCall = null;
                for (final EntryCallEvent eventCall : entryCalls) {

                    final boolean isAllowed = modelTable.isAllowedSignature(eventCall);

                    if ((lastCall != null) && isAllowed) {
                        modelTable.addTransition(lastCall, eventCall);
                        modelTable.addInformation(eventCall);

                    } else if (isAllowed) { // only called at first valid event
                                            // (condition lastCall == null is not needed
                        modelTable.addInformation(eventCall);
                    }

                    lastCall = isAllowed ? eventCall : lastCall;
                }
                this.outputPort.send(modelTable);
            }

        }

    }

    /**
     * execute case object instanceof BehaviorModelTable
     *
     * @param behaviorModelTable
     *            behaviorModelTable
     */
    private void executeBehaviorModelTable(final BehaviorModelTable behaviorModelTable) {
        if (this.behaviorModelTable == null) {
            this.behaviorModelTable = behaviorModelTable;
            this.sequenceModelCache.forEach(this::executeEntryCallSequenceModel);
            this.sequenceModelCache.clear();
        } else {
            TBehaviorModelPreperation.LOG.warn("behaviorModelTable already assigned");
        }
    }

    /**
     * getter
     *
     * @return the outputPort
     */
    public OutputPort<BehaviorModelTable> getOutputPort() {
        return this.outputPort;
    }

}