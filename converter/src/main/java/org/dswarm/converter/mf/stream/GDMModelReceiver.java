/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.mf.stream;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import org.dswarm.persistence.model.internal.gdm.GDMModel;

/**
 * @author tgaengler
 */
public class GDMModelReceiver implements ObjectReceiver<GDMModel> {

	private static final Logger LOG = LoggerFactory.getLogger(GDMModelReceiver.class);

	private static final Func1<GDMModel, Boolean> NOT_NULL = m -> m != null;

	private final Subject<GDMModel, GDMModel> modelSubject = PublishSubject.create();

	private final AtomicInteger inComingCounter    = new AtomicInteger(0);
	private final AtomicInteger outGoingCounter    = new AtomicInteger(0);
	private final AtomicInteger nonOutGoingCounter = new AtomicInteger(0);

	final Stack<GDMModel> gdmModelStack = new Stack<>();

	@Override
	public void process(final GDMModel gdmModel) {

		inComingCounter.incrementAndGet();
		gdmModelStack.push(gdmModel);

		modelSubject.onNext(gdmModel);
	}

	@Override
	public void resetStream() {
		// TODO: ?
	}

	@Override
	public void closeStream() {

		LOG.debug("close writer stream; received '{}' records + emitted '{}' (left '{}'; discarded '{}') records", inComingCounter.get(),
				outGoingCounter.get(), inComingCounter.get() - outGoingCounter.get(), getNonOutGoingCounter().get());

		modelSubject.onCompleted();
	}

	public void propagateError(final Throwable error) {

		modelSubject.onError(error);
	}

	public Observable<GDMModel> getObservable() {

		return modelSubject.lift(new BufferOperator()).filter(m -> {

			if (!gdmModelStack.empty()) {

				gdmModelStack.pop();
			}

			if (m != null) {

				outGoingCounter.incrementAndGet();

				return true;
			}

			nonOutGoingCounter.incrementAndGet();

			return false;
		}).doOnCompleted(() -> LOG
				.debug("complete writer observable; received '{}' records + emitted '{}' (left '{}'; discarded '{}') records", inComingCounter.get(),
						outGoingCounter.get(), inComingCounter.get() - outGoingCounter.get(), getNonOutGoingCounter().get()));
	}

	public AtomicInteger getInComingCounter() {

		return inComingCounter;
	}

	public AtomicInteger getOutGoingCounter() {

		return outGoingCounter;
	}

	public AtomicInteger getNonOutGoingCounter() {

		return nonOutGoingCounter;
	}

	private class BufferOperator implements Observable.Operator<GDMModel, GDMModel> {

		@Override public Subscriber<? super GDMModel> call(final Subscriber<? super GDMModel> subscriber) {

			return new Subscriber<GDMModel>() {

				@Override public void onCompleted() {

					while (!gdmModelStack.empty()) {

						subscriber.onNext(gdmModelStack.pop());
					}

					subscriber.onCompleted();
				}

				@Override public void onError(final Throwable e) {

					subscriber.onError(e);
				}

				@Override public void onNext(final GDMModel gdmModel) {

					subscriber.onNext(gdmModel);
				}
			};
		}
	}
}
