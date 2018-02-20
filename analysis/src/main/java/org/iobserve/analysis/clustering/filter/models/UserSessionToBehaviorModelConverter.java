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
package org.iobserve.analysis.clustering.filter.models;

import java.util.Iterator;
import java.util.List;

import org.iobserve.analysis.session.data.UserSession;
import org.iobserve.stages.general.data.EntryCallEvent;
import org.iobserve.stages.general.data.PayloadAwareEntryCallEvent;

public class UserSessionToBehaviorModelConverter {
    public static BehaviorModel convert(final UserSession session) {
        final BehaviorModel model = new BehaviorModel();

        final List<EntryCallEvent> entryCalls = session.getEvents();
        final Iterator<EntryCallEvent> iterator = entryCalls.iterator();

        // Assume list has at least one element
        EntryCallNode lastNode = UserSessionToBehaviorModelConverter.addOrExtendNode(model,
                (PayloadAwareEntryCallEvent) iterator.next());
        EntryCallNode currentNode;

        while (iterator.hasNext()) {
            currentNode = UserSessionToBehaviorModelConverter.addOrExtendNode(model,
                    (PayloadAwareEntryCallEvent) iterator.next());

            model.addEdge(new EntryCallEdge(lastNode, currentNode));
            lastNode = currentNode;
        }

        return model;
    }

    private static EntryCallNode addOrExtendNode(final BehaviorModel model, final PayloadAwareEntryCallEvent event) {
        final String signature = event.getOperationSignature();
        final EntryCallNode node = model.findNode(signature).orElse(new EntryCallNode(signature));

        final String[] parameters = event.getParameters();
        final String[] values = event.getValues();
        for (int i = 0; i < parameters.length; i++) {
            // TODO: Adapt or rewrite BehaviorModel, CallInformation
            node.mergeInformation(new CallInformation(parameters[i], values[i]));
        }

        model.addNode(node);
        return node;
    }
}