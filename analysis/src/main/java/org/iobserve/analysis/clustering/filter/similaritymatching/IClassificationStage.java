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
import org.iobserve.analysis.session.data.UserSession;

import teetime.framework.InputPort;
import teetime.framework.OutputPort;

/**
 * Generalises stages that aggregate user sessions based on a timer and generate
 * behavior models for them
 * 
 * @author Jannis Kuckei
 *
 */
public interface IClassificationStage {
    InputPort<UserSession> getSessionInputPort();

    InputPort<Long> getTimerInputPort();

    OutputPort<BehaviorModel[]> getOutputPort();
}
