package de.avgl.dmp.persistence.services.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.test.BasicJPAServiceTest;
import de.avgl.dmp.persistence.services.ConfigurationService;


public class ConfigurationServiceTest extends BasicJPAServiceTest<Configuration, ConfigurationService> {
	
	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(ConfigurationServiceTest.class);
	
	public ConfigurationServiceTest() {
		
		super("configuration", ConfigurationService.class);
	}
	
	@Test
	public void testSimpleConfiguration() {
		
		Configuration configuration = createObject();
		
		
		final ObjectNode parameters = new ObjectNode(DMPUtil.getJSONFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);
		
		configuration.setParameters(parameters);
		
		updateObject(configuration);
		
		Configuration updatedConfiguration = getUpdatedObject(configuration);
		
		Assert.assertNotNull("the configuration of the updated resource shouldn't be null", updatedConfiguration.getParameters());
		Assert.assertEquals("the configurations of the resource are not equal", configuration.getParameters(), updatedConfiguration.getParameters());
		Assert.assertNotNull("the parameter value shouldn't be null", configuration.getParameter(parameterKey));
		Assert.assertEquals("the parameter value should be equal", configuration.getParameter(parameterKey).asText(), parameterValue);
		
		// clean up DB
		deletedObject(configuration.getId());
	}
}
