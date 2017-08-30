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
package org.dswarm.converter.flow.model;

import java.util.Optional;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.JsonNode;
import javaslang.Tuple2;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.pipe.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.converter.mf.stream.GDMEncoder;
import org.dswarm.converter.mf.stream.GDMModelReceiver;
import org.dswarm.converter.pipe.timing.ObjectTimer;
import org.dswarm.converter.pipe.timing.StreamTimer;
import org.dswarm.converter.pipe.timing.TimerBasedFactory;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * Created by tgaengler on 03.03.16.
 */
public class MorphTask {

	private static final Logger LOG = LoggerFactory.getLogger(MorphTask.class);

	private final Timer.Context morphContext;

	private final GDMEncoder converter;

	private final GDMModelReceiver writer;

	public MorphTask(final Timer morphTimer,
	                 final TimerBasedFactory timerBasedFactory,
	                 final Optional<DataModel> outputDataModel,
	                 final String transformationEngineIdentifier,
	                 final Optional<Filter> optionalSkipFilter,
	                 final ObjectPipe<Tuple2<String, JsonNode>, StreamReceiver> opener,
	                 final Metamorph transformer) {

		morphContext = morphTimer.time();

		LOG.debug("start processing some records in transformation engine");

		final StreamTimer inputTimer = timerBasedFactory.forStream("stream-input");
		final ObjectTimer gdmModelsTimer = timerBasedFactory.forObject("gdm-models");
		converter = new GDMEncoder(outputDataModel);

		writer = new GDMModelReceiver(transformationEngineIdentifier);

		final StreamPipe<StreamReceiver> starter;
		if (optionalSkipFilter.isPresent()) {

			// skip filter + input timer
			starter = opener
					.setReceiver(optionalSkipFilter.get())
					.setReceiver(inputTimer);

		} else {

			// input timer
			starter = opener.setReceiver(inputTimer);
		}

		starter
				.setReceiver(transformer)
				.setReceiver(converter)
				.setReceiver(gdmModelsTimer)
				.setReceiver(writer);
	}

	public Timer.Context getMorphContext() {

		return morphContext;
	}

	public GDMEncoder getConverter() {

		return converter;
	}

	public GDMModelReceiver getWriter() {

		return writer;
	}
}
