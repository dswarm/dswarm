package org.dswarm.init.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CmdUtil.class);
	
	public static String executeCommand(final String command) throws Exception {
		final Process process = Runtime.getRuntime().exec(command);
		return executeProcess( process, command );
	}

	public static String executeCommand(final String command, final String[] envp ) throws Exception {
		final Process process = Runtime.getRuntime().exec(command, envp);
		return executeProcess(process, command);
	}

	
	private static String executeProcess( Process process, String command ) throws InterruptedException, IOException {
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
	
	
	public static void runCommand( final String command, final String output ) {
		String[] osSpecificCommand;
		if( System.getenv("OS") != null && System.getenv("OS").startsWith("Windows") ) {
			osSpecificCommand = new String[] { "cmd.exe", "/c", command };				
		} else {
			osSpecificCommand = new String[] { command };	
		}
		
		try {
			runProcess( osSpecificCommand, output );
		} catch( IOException | InterruptedException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void runProcess( final String[] command, final String output ) throws IOException, InterruptedException {
		final Process process = Runtime.getRuntime().exec(command);
		
		if( process != null ) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						try (BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(process.getInputStream())));
								BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
							String line;
							while( (line = reader.readLine()) != null ) {
								writer.write(line);
								writer.newLine();
							}
						}
					} catch( Exception ex ) {
						ex.printStackTrace();
					}
				}
			}).start();
		}
		
		if( process != null && process.waitFor() == 0 ) {
			// success ...
		} else {
			// failed
		}
	}
}
