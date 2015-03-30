/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.job.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.test.utils.ProjectServiceTestUtils;
import org.dswarm.persistence.service.test.BasicJPAServiceTest;

public class ProjectServiceTest extends BasicJPAServiceTest<ProxyProject, Project, ProjectService> {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceTest.class);

	private ProjectServiceTestUtils projectServiceTestUtils;

	public ProjectServiceTest() {

		super("project", ProjectService.class);
	}

	@Override protected void initObjects() {
		super.initObjects();

		projectServiceTestUtils = new ProjectServiceTestUtils();
	}

	@Test
	public void testSimpleObject() throws Exception {

		ProjectServiceTest.LOG.debug("start simple project test");

		final Project project = projectServiceTestUtils.createAndPersistDefaultObject();

		final Project updatedProject = projectServiceTestUtils.updateAndCompareObject(project, project);

		logObjectJSON(updatedProject);

		ProjectServiceTest.LOG.debug("end simple project test");
	}

	@Test
	public void testComplexObject() throws Exception {

		ProjectServiceTest.LOG.debug("start complex project test");

		final Project project = projectServiceTestUtils.createAndPersistDefaultCompleteObject();

		final Project updatedProject = projectServiceTestUtils.updateAndCompareObject(project, project);

		logObjectJSON(updatedProject);

		ProjectServiceTest.LOG.debug("end complex project test");
	}
}
