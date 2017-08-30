/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A transformation is a complex {@link Function} that consists of {@link Component}s.<br/>
 * <br/>
 * TODO: maybe add some methods to retrieve starting and finishing components
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DiscriminatorValue("Transformation")
@Table(name = "TRANSFORMATION")
public class Transformation extends Function {

	private static final Logger LOG = LoggerFactory.getLogger(Transformation.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The components of the transformation.
	 */
	@OneToMany(/* mappedBy = "transformation", */fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
	@JoinColumn(name = "TRANSFORMATION", referencedColumnName = "UUID")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlList
	private Set<Component> components;

	/**
	 * Creates a new transformation.
	 */
	protected Transformation() {

		super(FunctionType.Transformation);
	}

	public Transformation(final String uuid) {

		super(uuid, FunctionType.Transformation);
	}

	/**
	 * Gets the components of the transformation.
	 *
	 * @return the components of the transformation
	 */
	public Set<Component> getComponents() {

		return components;
	}

	/**
	 * Sets the components of the transformation
	 *
	 * @param componentsArg a new collection of components
	 */
	public void setComponents(final Set<Component> componentsArg) {

		if (componentsArg == null && components != null) {

			// remove transformation from components, if component will be prepared for removal

			// for (final Component component : components) {
			//
			// if (component.getTransformation() != null) {
			//
			// component.setTransformation(null);
			// }
			// }

			components.clear();
		}

		if (componentsArg != null) {

			if (components == null) {

				components = Sets.newCopyOnWriteArraySet();
			}

			if (!DMPPersistenceUtil.getComponentUtils().completeEquals(components, componentsArg)) {

				components.clear();
				components.addAll(componentsArg);
			}

			// for (final Component component : componentsArg) {
			//
			// if (component.getTransformation() == null) {
			//
			// component.setTransformation(this);
			// }
			// }
		}
	}

	/**
	 * Adds a new component to the collection of components of this transformation.<br>
	 * Created by: tgaengler
	 *
	 * @param component a new component
	 */
	public void addComponent(final Component component) {

		if (component != null) {

			if (components == null) {

				components = Sets.newLinkedHashSet();
			}

			if (!components.contains(component)) {

				components.add(component);

				// if (component.getTransformation() == null) {
				//
				// component.setTransformation(this);
				// }
			}
		}
	}

	/**
	 * Removes an existing component from the collection of components of this transformation.<br>
	 * Created by: tgaengler
	 *
	 * @param component an existing component that should be removed
	 */
	public void removeComponent(final Component component) {

		if (components != null && component != null && components.contains(component)) {

			components.remove(component);

			// if (component.getTransformation() != null) {
			//
			// component.setTransformation(null);
			// }
		}
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Transformation.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getComponentUtils().completeEquals(((Transformation) obj).getComponents(), getComponents());
	}

	/**
	 * Create a new {@code Transformation} as a copy from a existing transformation with a specific id. <br>
	 * <b>Use with care!</b>
	 * <p>
	 * This factory is to be used by {@link org.dswarm.persistence.model.job.utils.TransformationDeserializer} to avoid reflection
	 * based access to a private/protected field, since the Json deserializer needs a way to set the id that was provided by the
	 * JSON.
	 * </p>
	 * <p>
	 * The id is otherwise assigned by the database/Hibernate layer. You should never need this outside of
	 * {@code TransformationDeserializer}.
	 * </p>
	 *
	 * @param transformation the base transformation that will be copied
	 * @param uuid           the target transformation's id value
	 * @return a new transformation with the given id and all other attributes copied from the provided transformation.
	 */
	public static Transformation withId(final Transformation transformation, final String uuid) {
		final Transformation newTransformation = new Transformation(uuid);

		newTransformation.setComponents(transformation.getComponents());
		newTransformation.setFunctionDescription(transformation.getFunctionDescription());
		newTransformation.setParameters(transformation.getParameters());
		newTransformation.setDescription(transformation.getDescription());
		newTransformation.setName(transformation.getName());

		return newTransformation;
	}
}
