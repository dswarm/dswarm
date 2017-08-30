/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.init.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import org.dswarm.init.DMPException;

public class CmdUtil {

	private static final Logger	LOG	= LoggerFactory.getLogger(CmdUtil.class);

	public static String executeCommand(final String command) throws Exception {
		final Process process = Runtime.getRuntime().exec(command);
		return CmdUtil.executeProcess(process, command);
	}

	public static String executeCommand(final String command, final String[] envp) throws Exception {
		final Process process = Runtime.getRuntime().exec(command, envp);
		return CmdUtil.executeProcess(process, command);
	}

	private static String executeProcess(final Process process, final String command) throws InterruptedException, IOException, DMPException {

		final int exitStatus = process.waitFor();

		if (exitStatus != 0) {

			throw new DMPException("couldn't execute command '" + command + "' sucessfully - exit status is '" + exitStatus + "'");
		}

		final StringBuilder sb = new StringBuilder();

		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			line = reader.readLine();
		}

		CmdUtil.LOG.debug("got result from command execution '" + command + "' = '" + sb.toString() + "'");

		return sb.toString();
	}

	public static void runCommand(final String command, final String output) throws Exception {
		String[] osSpecificCommand;
		if (System.getenv("OS") != null && System.getenv("OS").startsWith("Windows")) {
			osSpecificCommand = new String[] { "cmd.exe", "/c", command };

			try {
				CmdUtil.runProcess(osSpecificCommand, output);
			} catch (IOException | InterruptedException e) {
				CmdUtil.LOG.error(e.getMessage(), e);
			}
		} else {

			final Process process = Runtime.getRuntime().exec(command);

			final int exitStatus = process.waitFor();

			if (exitStatus != 0) {

				throw new DMPException("couldn't execute command '" + command + "' sucessfully - exit status is '" + exitStatus + "'");
			}

			final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			final List<String> lines = Lists.newArrayList();

			String line = reader.readLine();

			while (line != null) {

				lines.add(line);
				line = reader.readLine();
			}

			FileUtils.writeLines(new File(output), "UTF-8", lines);
		}
	}

	/**
	 * [@tgaengler]: note, it looks like that this only works for windows machines
	 * 
	 * @param command
	 * @param output
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static void runProcess(final String[] command, final String output) throws IOException, InterruptedException {
		final Process process = Runtime.getRuntime().exec(command);

		if (process != null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						try (BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(process.getInputStream())));
								BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
							String line;
							while ((line = reader.readLine()) != null) {
								writer.write(line);
								writer.newLine();
							}
						}
					} catch (final Exception ex) {
						CmdUtil.LOG.error(ex.getMessage(), ex);
					}
				}
			}).start();
		}

		if (process != null && process.waitFor() == 0) {
			// success ...
		} else {
			// failed
		}
	}
}
