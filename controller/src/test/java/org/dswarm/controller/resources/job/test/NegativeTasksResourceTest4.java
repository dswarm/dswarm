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
	private static final String expectedResponse          = "{\"error\":{\"message\":\"couldn't process task (maybe XML export) successfully\",\"stacktrace\":\"java.lang.RuntimeException: couldn't transform any record from xml data resource at '/home/tgaengler/git/tgaengler/dswarm/tmp/resources/controller_test-mabxml.xml' to GDM for data model 'DataModel-2e0c9850-6def-4942-abed-b513d3f56eba'; maybe you set a wrong record tag (current one = 'record')";
	private static final boolean prepateInputDataResource = true;
	private static final int    expectedResponseCode      = 200;

	public NegativeTasksResourceTest4() {

		super(taskJSONFileName, inputDataResourceFileName, recordTag, storageType, testPostfix, expectedResponse, prepateInputDataResource, expectedResponseCode);
	}
}
