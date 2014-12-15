/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.resources.job.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.job.test.utils.MappingsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.ProjectsResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.test.utils.ProjectServiceTestUtils;

public class ProjectRemoveMappingResourceTest extends
		BasicResourceTest<ProjectsResourceTestUtils, ProjectServiceTestUtils, ProjectService, ProxyProject, Project> {

	private static final Logger LOG = LoggerFactory
			.getLogger(ProjectRemoveMappingResourceTest.class);

	private MappingsResourceTestUtils mappingsResourceTestUtils;

	private Project initiallyPersistedProject = null;

	public ProjectRemoveMappingResourceTest() {

		super(Project.class, ProjectService.class, "projects", "project_to_remove_mapping_from_with_dummy_IDs.json", new ProjectsResourceTestUtils());
	}

	@Override protected void initObjects() {
		super.initObjects();

		mappingsResourceTestUtils = new MappingsResourceTestUtils();
		pojoClassResourceTestUtils = new ProjectsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {
		super.prepare();

		// persist project via API since dummy IDs need to be replaced with the ones used in database
		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(objectJSONString));
		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);
		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		initiallyPersistedProject = objectMapper.readValue(responseString, Project.class);
		Assert.assertNotNull("the response project shouldn't be null", initiallyPersistedProject);
	}

	/**
	 * Simulate a user loading an already persisted project in front end, removing a mapping (that contains filters and functions)
	 * and saving the updated project (by putting the whole project JSON).<br />
	 * <br />
	 * It is intended that the mapping is removed from the project only, i.e. the relation between the project and the mapping is
	 * removed but the mapping itself and all of its parts (like functions and filters) are still present in the database (to be
	 * used in other projects).
	 *
	 * @throws Exception
	 */
	@Test
	public void testPUTProjectWithRemovedMapping() throws Exception {

		// Start simulate user removing a mapping from the given project
		final String initiallyPersistedProjectJSONString = objectMapper.writeValueAsString(initiallyPersistedProject);
		final Project modifiedProject = objectMapper.readValue(initiallyPersistedProjectJSONString, Project.class);

		final Set<Mapping> persistedMappings = modifiedProject.getMappings();
		final Set<Mapping> reducedMappings = Sets.newHashSet();
		final String mappingToBeRemovedFromProjectName = "first+last-to-contributor";
		Mapping mappingToBeRemovedFromProject = null;

		for (final Mapping mapping : persistedMappings) {

			// the mapping to be removed (by the user in front end)
			if (mapping.getName().equals(mappingToBeRemovedFromProjectName)) {
				mappingToBeRemovedFromProject = mapping;
				continue;
			}

			reducedMappings.add(mapping);
		}

		Assert.assertNotNull("could not find mapping to be removed \"" + mappingToBeRemovedFromProjectName + "\"", mappingToBeRemovedFromProject);

		// re-inject mappings
		modifiedProject.setMappings(reducedMappings);
		final String modifiedProjectJSONString = objectMapper.writeValueAsString(modifiedProject);

		// End simulate user removing a mapping from the given project
		// Start simulate user pushing button 'save project' in front end

		String idEncoded = null;
		try {

			idEncoded = URLEncoder.encode(initiallyPersistedProject.getId().toString(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			ProjectRemoveMappingResourceTest.LOG.debug("couldn't encode id", e);

			Assert.assertTrue(false);
		}

		final Response response = target(idEncoded).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json(modifiedProjectJSONString));

		// End simulate user pushing button 'save project' in front end
		// Start check response

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);
		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Project updatedPersistedProject = objectMapper.readValue(responseString, Project.class);
		Assert.assertNotNull("the response project shouldn't be null", updatedPersistedProject);

		// make sure the project does not contain (the reference to) the mapping anymore
		pojoClassResourceTestUtils.compareObjects(modifiedProject, updatedPersistedProject);

		// End check response
		// Start check db

		// the mapping itself must still be present in database
		Assert.assertNotNull("mapping to be removed \"" + mappingToBeRemovedFromProjectName + "\" has no ID", mappingToBeRemovedFromProject.getId());
		final Mapping persistedMappingToBeRemovedFromProject = mappingsResourceTestUtils.getObject(mappingToBeRemovedFromProject.getId());
		mappingsResourceTestUtils.compareObjects(mappingToBeRemovedFromProject, persistedMappingToBeRemovedFromProject);

	}

	@Ignore
	@Test
	@Override
	public void testPOSTObjects() throws Exception {
	}

	@Ignore
	@Test
	@Override
	public void testGETObjects() throws Exception {
	}

	@Ignore
	@Test
	@Override
	public void testGETObject() throws Exception {
	}

	@Ignore
	@Test
	@Override
	public void testDELETEObject() throws Exception {
	}

	@Ignore
	@Test
	@Override
	public void testPUTObject() throws Exception {
	}

}
