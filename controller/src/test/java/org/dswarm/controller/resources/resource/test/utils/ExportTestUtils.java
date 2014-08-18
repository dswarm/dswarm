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
