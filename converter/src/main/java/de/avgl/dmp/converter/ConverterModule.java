package de.avgl.dmp.converter;

import com.google.inject.AbstractModule;

/**
 * The Guice configuration of the converter module. Interface/classes that are registered here can be utilised for injection.
 * 
 * @author phorn
 */
public class ConverterModule extends AbstractModule {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ConverterModule.class);

	@Override
	protected void configure() {
	}
}
