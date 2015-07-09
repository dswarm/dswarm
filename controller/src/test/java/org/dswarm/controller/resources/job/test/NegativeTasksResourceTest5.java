package org.dswarm.controller.resources.job.test;

/**
 * not existing input data resource test
 *
 * @author tgaengler
 */
public class NegativeTasksResourceTest5 extends AbstractNegativeTasksResourceTest {

	private static final String taskJSONFileName          = "dd-538/oai-pmh_marcxml_controller_task.01.json";
	private static final String inputDataResourceFileName = "controller_test-mabxml.xml";
	private static final String recordTag                 = "datensatz";
	private static final String storageType               = "xml";
	private static final String testPostfix               = "not existing input data resource";
	private static final String expectedResponse          = "{\"status\":\"nok\",\"status_code\":500,\"error\":\"The data resource 'Resource-f3a537b6-0c96-449b-906f-241b82075190' at path '/home/dmp/tmp/tmp/resources/31927-2.out.xml' of data model 'DataModel-2e0c9850-6def-4942-abed-b513d3f56eba' does not exist. Hence, the data of the data model cannot be processed.\"}";
	private static final boolean prepateInputDataResource = false;
	private static final int    expectedResponseCode      = 500;

	public NegativeTasksResourceTest5() {

		super(taskJSONFileName, inputDataResourceFileName, recordTag, storageType, testPostfix, expectedResponse, prepateInputDataResource, expectedResponseCode);
	}
}
