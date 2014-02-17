package de.avgl.dmp.controller.resources.job.test.utils;

import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyTransformation;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.service.job.test.utils.TransformationServiceTestUtils;

public class TransformationsResourceTestUtils extends
		BasicFunctionsResourceTestUtils<TransformationServiceTestUtils, TransformationService, ProxyTransformation, Transformation> {

	public TransformationsResourceTestUtils() {

		super("transformations", Transformation.class, TransformationService.class, TransformationServiceTestUtils.class);
	}
}
