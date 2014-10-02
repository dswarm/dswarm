/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.resources.resource.test.utils;

import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Assert;

public class ExportTestUtils {

	private static final String	ATTACHMENT_STRING	= "attachment; filename*=UTF-8''rdf_export";
	private static final String	CONTENT_DISPOSITION	= "Content-Disposition";
	private static final String CONTENT_TYPE 		= "Content-Type";

	/**
	 * Assert a {@link Response} contains exactly one "Content-Disposition" header field and its value contains the expected value.
	 * 
	 * @param response the response to be checked
	 * @param expectedFileEnding the expected file ending
	 */
	public static void checkContentDispositionHeader(final Response response, final String expectedFileEnding) {

		Assert.assertTrue("Header should contain field \"" + CONTENT_DISPOSITION + "\"", response.getHeaders().containsKey(CONTENT_DISPOSITION));
		final List<String> contentDispositionHeaders = response.getStringHeaders().get(CONTENT_DISPOSITION);
		Assert.assertEquals("there should be exactly one header filed \"" + CONTENT_DISPOSITION + "\"", 1, contentDispositionHeaders.size());

		final String contentDispositionValue = contentDispositionHeaders.get(0);
		Assert.assertEquals(CONTENT_DISPOSITION + " header value mismatch.", ExportTestUtils.ATTACHMENT_STRING + expectedFileEnding,
				contentDispositionValue);
	}
	
	
	/**
	 * Assert a {@link Response} contains exactly one "Content-Type" header field and its value contains the expected value.
	 * 
	 * @param response the response to be checked
	 * @param expectedContentType the expected Content-Type
	 */
	public static void checkContentTypeHeader(final Response response, final String expectedContentType){
		
		Assert.assertTrue("Header should contain field \"" + CONTENT_TYPE + "\"", response.getHeaders().containsKey(CONTENT_TYPE));
		final List<String> contentTypeHeaders = response.getStringHeaders().get(CONTENT_TYPE);
		Assert.assertEquals("there should be exactly one header filed \"" + CONTENT_TYPE + "\"", 1, contentTypeHeaders.size());

		final String actualContentTypeValue = contentTypeHeaders.get(0);
		Assert.assertEquals(CONTENT_TYPE + " header value mismatch.", expectedContentType,
				actualContentTypeValue);
	}
	

}
