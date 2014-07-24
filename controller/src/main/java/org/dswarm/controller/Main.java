package org.dswarm.controller;

import java.io.IOException;

/**
 * The main class of the backend API. Wraps the backend web server where the backend API is located.
 * 
 * @author phorn
 * @author tgaengler
 */
public final class Main {

	/**
	 * Creates and starts the backend API (incl. its hosting backend web server).
	 * 
	 * @param args main args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {

		EmbeddedServer.main(args);
	}
}
