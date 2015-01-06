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
package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

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
