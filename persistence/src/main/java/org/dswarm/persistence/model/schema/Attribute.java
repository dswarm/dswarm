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
package org.dswarm.persistence.model.schema;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.AdvancedDMPJPAObject;

/**
 * An attribute is a property. In a graph an attribute is a relation between a node or subject and an object, e.g.,
 * 'dcterms:title'. In a CSV document an attribute is the header (label) of one column, e.g., 'title'.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "ATTRIBUTE")
@Cacheable(false)
public class Attribute extends AdvancedDMPJPAObject {

	/**
	 * All attribute paths that utilise this attribute
	 */
	// @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
	// CascadeType.REFRESH })
	// @JoinTable(name = "ATTRIBUTE_PATHS_ATTRIBUTES", joinColumns = { @JoinColumn(name = "ATTRIBUTE_ID", referencedColumnName =
	// "ID") }, inverseJoinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_ID", referencedColumnName = "ID") })
	// private Set<AttributePath> attributePaths = null;

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Creates new attribute with no identifier.
	 */
	protected Attribute() {

	}

	/**
	 * Creates a new attribute with the given identifier.
	 *
	 * @param id an attribute identifier
	 */
	public Attribute(final String uuid) {

		super(uuid);
	}

	public Attribute(final String uuid, final String uriArg) {

		super(uuid, uriArg);
	}

	/**
	 * Creates a new attribute with the given identifier and name.
	 *
	 * @param id an attribute identifier
	 * @param name an attribute name
	 */
	public Attribute(final String uuid, final String uriArg, final String name) {

		super(uuid, uriArg, name);
	}

	// public Set<AttributePath> getUniqueAttributePaths() {
	//
	// return attributePaths;
	// }
	//
	// public void setAttributePaths(final Set<AttributePath> attributePathsArg) {
	//
	// if (attributePathsArg == null && attributePaths != null) {
	//
	// // remove configuration from resources, if configuration, will be prepared for removal
	//
	// for (final AttributePath attributePath : attributePaths) {
	//
	// attributePath.removeAttribute(this);
	// }
	// }
	//
	// attributePaths = attributePathsArg;
	//
	// if (attributePathsArg != null) {
	//
	// for (final AttributePath attributePath : attributePathsArg) {
	//
	// attributePath.addAttribute(this);
	// }
	// }
	// }
	//
	// public AttributePath getAttributePath(final String id) {
	//
	// if (id == null) {
	//
	// return null;
	// }
	//
	// if (attributePaths == null || attributePaths.isEmpty()) {
	//
	// return null;
	// }
	//
	// final List<AttributePath> attributePathsFiltered = Lambda.filter(Lambda.having(Lambda.on(AttributePath.class).getId(),
	// Matchers.equalTo(id)),
	// attributePaths);
	//
	// if (attributePathsFiltered == null || attributePathsFiltered.isEmpty()) {
	//
	// return null;
	// }
	//
	// return attributePathsFiltered.get(0);
	// }
	//
	// /**
	// * Adds a new resource to the collection of resources of this configuration.<br>
	// * Created by: tgaengler
	// *
	// * @param resource a new export definition revision
	// */
	// public void addAttributePath(final AttributePath attributePath) {
	//
	// if (attributePath != null) {
	//
	// if (attributePaths == null) {
	//
	// attributePaths = Sets.newLinkedHashSet();
	// }
	//
	// if (!attributePaths.contains(attributePath)) {
	//
	// attributePaths.add(attributePath);
	// attributePath.addAttribute(this);
	// }
	// }
	// }
	//
	// /**
	// * Adds a new resource to the collection of resources of this configuration.<br>
	// * Created by: tgaengler
	// *
	// * @param resource a new export definition revision
	// */
	// protected void addAttributePath(final AttributePath attributePath, final int attributeIndex) {
	//
	// if (attributePath != null) {
	//
	// if (attributePaths == null) {
	//
	// attributePaths = Sets.newLinkedHashSet();
	// }
	//
	// if (!attributePaths.contains(attributePath)) {
	//
	// attributePaths.add(attributePath);
	// attributePath.addAttribute(this, attributeIndex);
	// }
	// }
	// }
	//
	// /**
	// * Removes an existing resource from the collection of resources of this configuration.<br>
	// * Created by: tgaengler
	// *
	// * @param resource an existing resource that should be removed
	// */
	// public void removeAttributePath(final AttributePath attributePath) {
	//
	// if (attributePaths != null && attributePath != null && attributePaths.contains(attributePath)) {
	//
	// attributePaths.remove(attributePath);
	//
	// attributePath.removeAttribute(this);
	// }
	// }

	@Override
	public boolean completeEquals(final Object obj) {

		return Attribute.class.isInstance(obj) && super.completeEquals(obj);
	}
}
