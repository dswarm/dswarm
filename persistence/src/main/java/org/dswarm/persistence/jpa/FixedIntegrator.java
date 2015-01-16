/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.jpa;

import java.util.ArrayList;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.event.internal.core.JpaMergeEventListener;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;


public final class FixedIntegrator implements Integrator {

	@Override
	public void integrate(final Configuration configuration, final SessionFactoryImplementor sessionFactory, final SessionFactoryServiceRegistry serviceRegistry) {
		updateMergeEventListener(serviceRegistry);
	}

	@Override
	public void integrate(final MetadataImplementor metadata, final SessionFactoryImplementor sessionFactory, final SessionFactoryServiceRegistry serviceRegistry) {
		updateMergeEventListener(serviceRegistry);
	}

	@Override
	public void disintegrate(final SessionFactoryImplementor sessionFactory, final SessionFactoryServiceRegistry serviceRegistry) {
	}

	private static void updateMergeEventListener(final SessionFactoryServiceRegistry serviceRegistry) {
		final EventListenerRegistry registry = serviceRegistry.getService(EventListenerRegistry.class);
		final EventListenerGroup<MergeEventListener> listeners = registry.getEventListenerGroup(EventType.MERGE);
		final ArrayList<MergeEventListener> updatedListeners = new ArrayList<>();

		boolean jpaMergeEventListener = false;

		for (final MergeEventListener listener : listeners.listeners()) {
			if (listener instanceof JpaMergeEventListener) {
				jpaMergeEventListener = true;
			} else {
				updatedListeners.add(listener);
			}
		}

		if(jpaMergeEventListener) {
			listeners.clear();
			listeners.appendListener(new FixedMergeEventListener());
			listeners.appendListeners(updatedListeners.toArray(new MergeEventListener[updatedListeners.size()]));
		}

	}
}
