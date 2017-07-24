<<<<<<< HEAD:org.iobserve.common/src/gen/java-factory/org/iobserve/common/record/EJBUndeployedEventFactory.java
=======
/***************************************************************************
 * Copyright 2017 iObserve Project
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
>>>>>>> master:common/src/gen/java/org/iobserve/common/record/EJBUndeployedEventFactory.java
package org.iobserve.common.record;

import java.nio.ByteBuffer;

import kieker.common.record.factory.IRecordFactory;
import kieker.common.util.registry.IRegistry;

/**
 * @author iObserve
 * 
 * @since 1.10
 */
public final class EJBUndeployedEventFactory implements IRecordFactory<EJBUndeployedEvent> {
	
	@Override
	public EJBUndeployedEvent create(final ByteBuffer buffer, final IRegistry<String> stringRegistry) {
		return new EJBUndeployedEvent(buffer, stringRegistry);
	}
	
	@Override
	public EJBUndeployedEvent create(final Object[] values) {
		return new EJBUndeployedEvent(values);
	}
	
	public int getRecordSizeInBytes() {
		return EJBUndeployedEvent.SIZE;
	}
}
