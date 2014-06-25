package org.dswarm.controller.resources.job.test.utils;

import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyTransformation;
import org.dswarm.persistence.service.job.TransformationService;
import org.dswarm.persistence.service.job.test.utils.TransformationServiceTestUtils;

public class TransformationsResourceTestUtils extends
		BasicFunctionsResourceTestUtils<TransformationServiceTestUtils, TransformationService, ProxyTransformation, Transformation> {

	public TransformationsResourceTestUtils() {

		super("transformations", Transformation.class, TransformationService.class, TransformationServiceTestUtils.class);
	}
}
