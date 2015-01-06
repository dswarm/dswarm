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
package org.dswarm.persistence.model.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.dswarm.persistence.model.AdvancedDMPJPAObject;

@XmlRootElement
public class Connection extends AdvancedDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	private ConnectionType		type;

	@XmlTransient
	private Component			source;

	@XmlTransient
	private Component			target;

	public Connection(final String id) {

		super(id);
	}

	public ConnectionType getType() {

		return type;
	}

	public void setType(final ConnectionType type) {

		this.type = type;
	}

	public Component getSource() {

		return source;
	}

	public void setSource(final Component source) {

		this.source = source;
	}

	public Component getTarget() {

		return target;
	}

	public void setTarget(final Component target) {

		this.target = target;
	}

	@XmlElement(name = "source")
	public Long getSourceId() {

		if (source == null) {

			return null;
		}

		return source.getId();
	}

	@XmlElement(name = "target")
	public Long getTargetId() {

		if (target == null) {

			return null;
		}

		return target.getId();
	}
}
