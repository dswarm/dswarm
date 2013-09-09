package de.avgl.dmp.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

abstract public class AbstractBaseTest {
	protected String getResourceAsString(String resource) throws IOException {
		URL url = Resources.getResource(resource);
		return Resources.toString(url, Charsets.UTF_8);
	}
}
