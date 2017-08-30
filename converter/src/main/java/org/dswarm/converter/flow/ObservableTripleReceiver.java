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
package org.dswarm.converter.flow;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.types.Triple;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

final class ObservableTripleReceiver implements ObjectReceiver<Triple> {

	private static final Func1<Triple, Boolean> NOT_NULL = m -> m != null;

	private final Subject<Triple, Triple> tripleSubject = PublishSubject.create();

	@Override
	public void process(final Triple triple) {

		tripleSubject.onNext(triple);
	}

	@Override
	public void resetStream() {

		// TODO ?
	}

	@Override
	public void closeStream() {

		tripleSubject.onCompleted();
	}

	public void propagateError(final Throwable error) {

		tripleSubject.onError(error);
	}

	public Observable<Triple> getObservable() {

		return tripleSubject.filter(NOT_NULL);
	}
}
