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
package org.dswarm.controller.status;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.dswarm.persistence.DatabaseConnectionCheck;

@Singleton
public class DatabaseHealthCheck extends HealthCheck {

	private final DatabaseConnectionCheck	database;

	@Inject
	public DatabaseHealthCheck(final DatabaseConnectionCheck database) {
		this.database = database;
	}

	@Override
	public HealthCheck.Result check() throws Exception {
		if (database.isConnected()) {
			return HealthCheck.Result.healthy();
		} else {
			return HealthCheck.Result.unhealthy("Cannot connect to the database at " + database.getUrl());
		}
	}
}
