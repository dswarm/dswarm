/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.monitoring;

import com.codahale.metrics.Timer;
import org.slf4j.Marker;

import static com.google.common.base.Preconditions.checkNotNull;

final class MarkedTimer {
	private final Marker marker;
	private final String name;
	private final Timer timer;

	MarkedTimer(final String name, final Marker marker, final Timer timer) {
		this.name = checkNotNull(name);
		this.marker = checkNotNull(marker);
		this.timer = checkNotNull(timer);
	}

	Marker getMarker() {
		return marker;
	}

	String getName() {
		return name;
	}

	Timer getTimer() {
		return timer;
	}

	boolean matches(final String otherName) {
		return name.equals(otherName);
	}
}
