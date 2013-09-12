package de.avgl.dmp.init.util;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class DMPUtil {

	public static String getResourceAsString(String resource) throws IOException {
		URL url = Resources.getResource(resource);
		return Resources.toString(url, Charsets.UTF_8);
	}
}
