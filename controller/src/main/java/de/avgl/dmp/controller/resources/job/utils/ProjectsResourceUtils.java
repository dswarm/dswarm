package de.avgl.dmp.controller.resources.job.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.service.job.ProjectService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ProjectsResourceUtils extends ExtendedBasicDMPResourceUtils<ProjectService, Project> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ProjectsResourceUtils.class);

	@Inject
	public ProjectsResourceUtils(final Provider<ProjectService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Project.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}
}
