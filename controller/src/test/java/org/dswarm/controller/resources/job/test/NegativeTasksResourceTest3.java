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
 * empty file test
 *
 * @author tgaengler
 */
public class NegativeTasksResourceTest3 extends AbstractNegativeTasksResourceTest {

	private static final String taskJSONFileName          = "dd-538/oai-pmh_marcxml_controller_task.01.json";
	private static final String inputDataResourceFileName = "test_csv-controller.empty.csv";
	private static final String recordTag                 = "datensatz";
	private static final String storageType               = "xml";
	private static final String testPostfix               = "empty file";
	private static final String expectedResponse          = "{\"error\":{\"message\":\"couldn't process task (maybe XML export) successfully\",\"stacktrace\":\"org.culturegraph.mf.exceptions.MetafactureException: org.xml.sax.SAXParseException; Premature end of file.\\n\\tat org.culturegraph.mf.stream.converter.xml.XmlDecoder.process(XmlDecoder.java:69)\\n\\tat org.culturegraph.mf.stream.converter.xml.XmlDecoder.process(XmlDecoder.java:41)\\n\\tat org.dswarm.converter.pipe.timing.ObjectTimer.process(ObjectTimer.java:41)\\n\\tat org.dswarm.converter.mf.stream.source.BOMResourceOpener.process(BOMResourceOpener.java:80)\\n\\tat org.dswarm.converter.mf.stream.source.BOMResourceOpener.process(BOMResourceOpener.java:42)\\n\\tat org.dswarm.converter.flow.XMLSourceResourceGDMStmtsFlow$1.call(XMLSourceResourceGDMStmtsFlow.java:154)\\n\\tat org.dswarm.converter.flow.XMLSourceResourceGDMStmtsFlow$1.call(XMLSourceResourceGDMStmtsFlow.java:146)\\n\\tat rx.Observable.unsafeSubscribe(Observable.java:7531)\\n\\tat rx.internal.operators.OperatorMerge$MergeSubscriber.handleNewSource(OperatorMerge.java:215)\\n\\tat rx.internal.operators.OperatorMerge$MergeSubscriber.onNext(OperatorMerge.java:185)\\n\\tat rx.internal.operators.OperatorMerge$MergeSubscriber.onNext(OperatorMerge.java:120)\\n\\tat rx.internal.operators.OperatorMap$1.onNext(OperatorMap.java:55)\\n\\tat rx.internal.operators.OperatorSubscribeOn$1$1$1.onNext(OperatorSubscribeOn.java:76)\\n\\tat rx.internal.operators.OnSubscribeToObservableFuture$ToObservableFuture.call(OnSubscribeToObservableFuture.java:74)\\n\\tat rx.internal.operators.OnSubscribeToObservableFuture$ToObservableFuture.call(OnSubscribeToObservableFuture.java:42)\\n\\tat rx.Observable.unsafeSubscribe(Observable.java:7531)\\n\\tat rx.internal.operators.OperatorSubscribeOn$1$1.call(OperatorSubscribeOn.java:62)\\n\\tat rx.internal.schedulers.ScheduledAction.run(ScheduledAction.java:55)\\n\\tat rx.schedulers.ExecutorScheduler$ExecutorSchedulerWorker.run(ExecutorScheduler.java:98)\\n\\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)\\n\\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)\\n\\tat java.lang.Thread.run(Thread.java:745)\\nCaused by: org.xml.sax.SAXParseException; Premature end of file.\\n\\tat org.apache.xerces.parsers.AbstractSAXParser.parse(Unknown Source)\\n\\tat org.culturegraph.mf.stream.converter.xml.XmlDecoder.process(XmlDecoder.java:65)\\n\\t... 21 more\\n\"}}";
	private static final boolean prepateInputDataResource = true;
	private static final int    expectedResponseCode      = 200;

	public NegativeTasksResourceTest3() {

		super(taskJSONFileName, inputDataResourceFileName, recordTag, storageType, testPostfix, expectedResponse, prepateInputDataResource, expectedResponseCode);
	}
}
