package de.avgl.dmp.persistence.model.job;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;

/**
 * TODO: maybe add some methods to retrieve starting and finishing components
 * 
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "TRANSFORMATION")
public class Transformation extends Function {

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(Transformation.class);

	/**
	 * 
	 */
	private static final long						serialVersionUID	= 1L;

	@OneToMany(/* mappedBy = "transformation", */fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH,
			CascadeType.REMOVE }, orphanRemoval = true)
	@JoinColumn(name = "TRANSFORMATION", referencedColumnName = "ID")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlIDREF
	@XmlList
	private Set<Component>							components;

	public Set<Component> getComponents() {

		return components;
	}

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

				components = Sets.newLinkedHashSet();
			}

			if (!components.equals(componentsArg)) {

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
	public boolean equals(final Object obj) {

		if (!Transformation.class.isInstance(obj)) {

			return false;
		}

		return super.equals(obj);
	}
}
