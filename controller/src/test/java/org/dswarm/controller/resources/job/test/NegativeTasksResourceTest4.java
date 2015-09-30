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
package org.dswarm.controller.resources.job.test;

/**
 * wrong record tag at xml data test
 *
 * @author tgaengler
 */
public class NegativeTasksResourceTest4 extends AbstractNegativeTasksResourceTest {

	private static final String taskJSONFileName          = "dd-538/oai-pmh_marcxml_controller_task.01.json";
	private static final String inputDataResourceFileName = "controller_test-mabxml.xml";
	private static final String recordTag                 = "record";
	private static final String storageType               = "xml";
	private static final String testPostfix               = "wrong record tag at xml data";
	private static final String expectedResponse          = "{\"error\":{\"message\":\"couldn't process task (maybe XML export) successfully\",\"stacktrace\":\"java.lang.RuntimeException: couldn't transform any record from XML data resource at '/home/tgaengler/git/tgaengler/dswarm/tmp/resources/controller_test-mabxml.xml' to GDM for data model 'DataModel-2e0c9850-6def-4942-abed-b513d3f56eba'; maybe you set a wrong record tag (current one = 'record')\\n\\tat org.dswarm.controller.eventbus.ConverterEventRecorder.lambda$doIngest$10(ConverterEventRecorder.java:234)\\n\\tat rx.Observable$8.onCompleted(Observable.java:4287)\\n\\tat rx.internal.operators.OperatorDoOnEach$1.onCompleted(OperatorDoOnEach.java:46)\\n\\tat rx.internal.operators.OperatorCast$1.onCompleted(OperatorCast.java:39)\\n\\tat rx.internal.operators.OperatorFilter$1.onCompleted(OperatorFilter.java:42)\\n\\tat rx.internal.operators.OperatorMerge$MergeSubscriber.emitLoop(OperatorMerge.java:609)\\n\\tat rx.internal.operators.OperatorMerge$MergeSubscriber.emit(OperatorMerge.java:521)\\n\\tat rx.internal.operators.OperatorMerge$MergeSubscriber.onCompleted(OperatorMerge.java:254)\\n\\tat rx.internal.operators.OperatorMap$1.onCompleted(OperatorMap.java:44)\\n\\tat rx.internal.operators.OperatorSubscribeOn$1$1$1.onCompleted(OperatorSubscribeOn.java:66)\\n\\tat rx.internal.operators.OnSubscribeToObservableFuture$ToObservableFuture.call(OnSubscribeToObservableFuture.java:75)\\n\\tat rx.internal.operators.OnSubscribeToObservableFuture$ToObservableFuture.call(OnSubscribeToObservableFuture.java:42)\\n\\tat rx.Observable.unsafeSubscribe(Observable.java:7710)\\n\\tat rx.internal.operators.OperatorSubscribeOn$1$1.call(OperatorSubscribeOn.java:62)\\n\\tat rx.internal.schedulers.ScheduledAction.run(ScheduledAction.java:55)\\n\\tat rx.schedulers.ExecutorScheduler$ExecutorSchedulerWorker.run(ExecutorScheduler.java:98)\\n\\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)\\n\\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)\\n\\tat java.lang.Thread.run(Thread.java:745)\\n\"}}";
	private static final boolean prepateInputDataResource = true;
	private static final int expectedResponseCode = 200;

	public NegativeTasksResourceTest4() {

		super(taskJSONFileName, inputDataResourceFileName, recordTag, storageType, testPostfix, expectedResponse, prepateInputDataResource,
				expectedResponseCode);
	}
}
