package org.dswarm.controller.resources.job.test.utils;

import org.dswarm.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.test.utils.ProjectServiceTestUtils;

public class ProjectsResourceTestUtils extends ExtendedBasicDMPResourceTestUtils<ProjectServiceTestUtils, ProjectService, ProxyProject, Project> {

	public ProjectsResourceTestUtils() {

		super("projects", Project.class, ProjectService.class, ProjectServiceTestUtils.class);
	}
}
