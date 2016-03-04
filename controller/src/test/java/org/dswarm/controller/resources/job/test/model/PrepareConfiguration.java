package org.dswarm.controller.resources.job.test.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.UUIDService;

/**
 * Created by tgaengler on 04.03.16.
 */
public class PrepareConfiguration {

	private Configuration conf1;
	private Configuration configuration;
	private final Resource resource;

	private final ObjectMapper objectMapper;
	private final ResourcesResourceTestUtils resourcesResourceTestUtils;

	public PrepareConfiguration(final PrepareResource prepareResource, final ObjectMapper objectMapper, final ResourcesResourceTestUtils resourcesResourceTestUtils) {
		this.resource = prepareResource.getResource();
		this.objectMapper = objectMapper;
		this.resourcesResourceTestUtils = resourcesResourceTestUtils;
	}

	public Configuration getConf1() {
		return conf1;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public PrepareConfiguration invoke() throws Exception {
		final String configuration1Uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		// process input data model
		conf1 = new Configuration(configuration1Uuid);

		conf1.setName("configuration 1");
		conf1.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		conf1.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));
		conf1.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		final String configurationJSONString = objectMapper.writeValueAsString(conf1);

		// create configuration
		configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);
		return this;
	}
}
