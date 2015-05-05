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

import org.culturegraph.mf.framework.ObjectReceiver;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import org.dswarm.persistence.model.internal.gdm.GDMModel;

/**
 * @author tgaengler
 */
public class GDMModelReceiver implements ObjectReceiver<GDMModel> {

	private static final Func1<GDMModel, Boolean> NOT_NULL = m -> m != null;

	// TODO: ReplaySubject ?
	private final Subject<GDMModel, GDMModel> modelSubject = PublishSubject.create();

	@Override
	public void process(final GDMModel gdmModel) {

		modelSubject.onNext(gdmModel);
	}

	@Override
	public void resetStream() {
		// TODO: ?
	}

	@Override
	public void closeStream() {

		modelSubject.onCompleted();
	}

	public void propagateError(final Throwable error) {
		modelSubject.onError(error);
	}

	public Observable<GDMModel> getObservable() {
		return modelSubject.filter(NOT_NULL);
	}
}
