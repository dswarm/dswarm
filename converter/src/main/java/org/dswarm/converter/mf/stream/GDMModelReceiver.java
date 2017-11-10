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
package org.dswarm.converter.mf.stream;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tgaengler
 */
public class GDMModelReceiver implements ObjectReceiver<GDMModel> {

	private static final Logger LOG = LoggerFactory.getLogger(GDMModelReceiver.class);

	private final Subject<GDMModel, GDMModel> modelSubject = PublishSubject.create();

	private final AtomicInteger inComingCounter = new AtomicInteger(0);
	private final AtomicInteger outGoingCounter = new AtomicInteger(0);
	private final AtomicInteger nonOutGoingCounter = new AtomicInteger(0);
	private final AtomicInteger dequePolledCounter = new AtomicInteger(0);

	private final Deque<GDMModel> gdmModelDeque = new ConcurrentLinkedDeque<>();
	private final AtomicBoolean afterClosedStream = new AtomicBoolean();

	private final String type;

	public GDMModelReceiver(final String typeArg) {

		type = typeArg;
	}

	@Override
	public void process(final GDMModel gdmModel) {

		inComingCounter.incrementAndGet();
		gdmModelDeque.addFirst(gdmModel);

		modelSubject.onNext(gdmModel);
	}

	@Override
	public void resetStream() {
		// TODO: ?
	}

	@Override
	public void closeStream() {

		LOG.info("close {} writer stream; received '{}' records + emitted '{}' (left '{}'; discarded '{}') records", type, inComingCounter.get(),
				outGoingCounter.get(), inComingCounter.get() - outGoingCounter.get(), nonOutGoingCounter.get());

		afterClosedStream.compareAndSet(false, true);

		modelSubject.onCompleted();
	}

	public void propagateError(final Throwable error) {

		modelSubject.onError(error);
	}

	public Observable<GDMModel> getObservable() {

		return modelSubject.lift(new BufferOperator()).filter(m -> {

			if (!afterClosedStream.get() && !gdmModelDeque.isEmpty()) {

				gdmModelDeque.removeLast();
			}

			if (m != null) {

				outGoingCounter.incrementAndGet();

				return true;
			}

			nonOutGoingCounter.incrementAndGet();

			return false;
		}).doOnCompleted(() -> LOG
				.info("complete {} writer observable; received '{}' records + emitted '{}' (left '{}'; discarded '{}'; polled '{}') records", type,
						inComingCounter.get(),
						outGoingCounter.get(), inComingCounter.get() - outGoingCounter.get(), nonOutGoingCounter.get(),
						dequePolledCounter.get()));
	}

	public int getInComingCounter() {

		return inComingCounter.get();
	}

	public int getOutGoingCounter() {

		return outGoingCounter.get();
	}

	public int getNonOutGoingCounter() {

		return nonOutGoingCounter.get();
	}

	public int getDequePolledCounter() {

		return dequePolledCounter.get();
	}

	private class BufferOperator implements Observable.Operator<GDMModel, GDMModel> {

		@Override
		public Subscriber<? super GDMModel> call(final Subscriber<? super GDMModel> subscriber) {

			return new Subscriber<GDMModel>() {

				@Override
				public void onCompleted() {

					if (!gdmModelDeque.isEmpty()) {

						LOG.debug("need to emit buffer @ onCompleted with buffer size = '{}', received records = '{}' and emitted records = '{}'", gdmModelDeque.size(), inComingCounter.get(), outGoingCounter.get());
					}

					while (!gdmModelDeque.isEmpty()) {

						subscriber.onNext(gdmModelDeque.removeLast());

						dequePolledCounter.incrementAndGet();
					}

					subscriber.onCompleted();
				}

				@Override
				public void onError(final Throwable e) {

					subscriber.onError(e);
				}

				@Override
				public void onNext(final GDMModel gdmModel) {

					subscriber.onNext(gdmModel);
				}
			};
		}
	}
}
