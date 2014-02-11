package de.avgl.dmp.controller.resources.job.test.utils;

import de.avgl.dmp.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.model.job.proxy.ProxyProject;
import de.avgl.dmp.persistence.service.job.ProjectService;
import de.avgl.dmp.persistence.service.job.test.utils.ProjectServiceTestUtils;

public class ProjectsResourceTestUtils extends ExtendedBasicDMPResourceTestUtils<ProjectServiceTestUtils, ProjectService, ProxyProject, Project> {

	public ProjectsResourceTestUtils() {

		super("projects", Project.class, ProjectService.class, ProjectServiceTestUtils.class);
	}
}
