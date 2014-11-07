package org.dswarm.init.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CmdUtil.class);
	
	public static String executeCommand(final String command) throws Exception {

		final Process process = Runtime.getRuntime().exec(command);
		final int exitStatus = process.waitFor();

		Assert.assertEquals("exit status should be 0", 0, exitStatus);

		final StringBuilder sb = new StringBuilder();

		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			line = reader.readLine();
		}

		LOG.debug("got result from command execution '" + command + "' = '" + sb.toString() + "'");

		return sb.toString();
	}
	
}
