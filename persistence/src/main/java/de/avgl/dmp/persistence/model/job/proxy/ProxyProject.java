package de.avgl.dmp.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Project}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyProject extends ProxyExtendedBasicDMPJPAObject<Project> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created project, i.e., no updated or already existing project.
	 * 
	 * @param projectArg a freshly created project
	 */
	public ProxyProject(final Project projectArg) {

		super(projectArg);
	}

	/**
	 * Creates a new proxy with the given real project and the type how the project was processed by the project persistence
	 * service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param projectArg a project that was processed by the project persistence service
	 * @param typeArg the type how this project was processed by the project persistence service
	 */
	public ProxyProject(final Project projectArg, final RetrievalType typeArg) {

		super(projectArg, typeArg);
	}
}
