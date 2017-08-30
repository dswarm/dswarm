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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.lambdaj.Lambda;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.DMPException;
import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A content schema is a schema that builds on top of a structure schema (meta data model), e.g., 'mabxml' is a structure schema
 * for 'mab', which is a content schema.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CONTENT_SCHEMA")
public class ContentSchema extends BasicDMPJPAObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(AttributePath.class);

	/**
	 * The value attribute path of the content schema.
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "RECORD_IDENTIFIER_ATTRIBUTE_PATH")
	@XmlElement(name = "record_identifier_attribute_path")
	private AttributePath recordIdentifierAttributePath;

	/**
	 * All utilised attribute path of the key attribute paths.
	 */
	@JsonIgnore
	@Access(AccessType.FIELD)
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS",
			joinColumns = { @JoinColumn(name = "CONTENT_SCHEMA_UUID", referencedColumnName = "UUID") },
			inverseJoinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_UUID", referencedColumnName = "UUID") })
	private Set<AttributePath> utilisedKeyAttributePaths;

	/**
	 * all attribute paths of the key attribute paths as ordered list.
	 */
	@Transient
	private LinkedList<AttributePath> orderedKeyAttributePaths;

	/**
	 * A JSON object of the ordered list of key attribute paths.
	 */
	@Transient
	private ArrayNode orderedKeyAttributePathsJSON;

	/**
	 * A flag that indicates, whether the key attribute paths are initialised or not.
	 */
	@Transient
	private boolean orderedKeyAttributePathsInitialized;

	/**
	 * A string that holds the serialised JSON object of the key attribute paths (ordered list of attribute paths).
	 */
	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "KEY_ATTRIBUTE_PATHS", columnDefinition = "BLOB")
	private byte[] keyAttributePaths;

	/**
	 * The value attribute path of the content schema.
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "VALUE_ATTRIBUTE_PATH")
	@XmlElement(name = "value_attribute_path")
	private AttributePath valueAttributePath;

	/**
	 * Creates a new content schema.
	 */
	protected ContentSchema() {

	}

	public ContentSchema(final String uuid) {

		super(uuid);
	}

	/**
	 * Creates a new content schema with the given ordered list of key attribute paths and the value attribute path.
	 *
	 * @param recordIdentifierAttributePathArg the attribute path where the (legacy) record identifier is located
	 * @param keyAttributePathsArg             an ordered list of key attribute paths
	 * @param valueAttributePath               the attribute path where the values are located
	 */
	public ContentSchema(final String uuid, final AttributePath recordIdentifierAttributePathArg,
			final LinkedList<AttributePath> keyAttributePathsArg, final AttributePath valueAttributePath) {

		super(uuid);
		setRecordIdentifierAttributePath(recordIdentifierAttributePathArg);
		setKeyAttributePaths(keyAttributePathsArg);
		setValueAttributePath(valueAttributePath);
	}

	/**
	 * Gets the record identifier attribute path of the content schema.
	 *
	 * @return the record attribute path of the content schema
	 */
	public AttributePath getRecordIdentifierAttributePath() {

		return recordIdentifierAttributePath;
	}

	/**
	 * Sets the record identifier attribute path of the content schema.
	 *
	 * @param recordIdentifierAttributePathArg a new value attribute path
	 */
	public void setRecordIdentifierAttributePath(final AttributePath recordIdentifierAttributePathArg) {

		recordIdentifierAttributePath = recordIdentifierAttributePathArg;
	}

	/**
	 * Gets the utilised attribute paths of the ordered list of key attribute paths.<br/>
	 * note: this is not the ordered list of key attribute paths, i.e., the key attribute paths; these are only the utilised
	 * attribute paths, i.e., an attribute path can occur multiple times in the ordered list of key attribute paths.
	 *
	 * @return the utilised attribute paths of the key attribute paths
	 */
	public Set<AttributePath> getUtilisedKeyAttributePaths() {

		return utilisedKeyAttributePaths;
	}

	/**
	 * Gets the key attribute paths, i.e., the ordered list of attribute paths.
	 *
	 * @return the key attribute paths
	 */
	@XmlElement(name = "key_attribute_paths")
	public LinkedList<AttributePath> getKeyAttributePaths() {

		initKeyAttributePaths(false);

		return orderedKeyAttributePaths;
	}

	/**
	 * Sets the key attribute paths (ordered list of attribute paths).
	 *
	 * @param keyAttributePathsArg new key attribute paths (ordered list of attribute paths)
	 */
	@XmlElement(name = "key_attribute_paths")
	public void setKeyAttributePaths(final LinkedList<AttributePath> keyAttributePathsArg) {

		if (keyAttributePathsArg == null && orderedKeyAttributePaths != null) {

			if (utilisedKeyAttributePaths != null) {

				utilisedKeyAttributePaths.clear();
			}

			orderedKeyAttributePaths.clear();
		}

		if (keyAttributePathsArg != null) {

			if (orderedKeyAttributePaths == null) {

				orderedKeyAttributePaths = Lists.newLinkedList();
			}

			if (!DMPPersistenceUtil.getAttributePathUtils().completeEquals(orderedKeyAttributePaths, keyAttributePathsArg)) {

				orderedKeyAttributePaths.clear();
				orderedKeyAttributePaths.addAll(keyAttributePathsArg);

				checkUtilisedKeyAttributePaths();

				utilisedKeyAttributePaths.clear();

				for (final AttributePath keyAttributePath : orderedKeyAttributePaths) {

					utilisedKeyAttributePaths.add(keyAttributePath);
				}
			}
		}

		refreshKeyAttributePathsString();
	}

	/**
	 * Adds a new key attribute path to the end of the ordered list of key attribute paths.<br>
	 * Created by: tgaengler
	 *
	 * @param keyAttributePathArg a new key attribute path
	 */
	public void addKeyAttributePath(final AttributePath keyAttributePathArg) {

		if (keyAttributePathArg != null) {

			checkUtilisedKeyAttributePaths();

			checkOrderedKeyAttributePaths();

			utilisedKeyAttributePaths.add(keyAttributePathArg);
			orderedKeyAttributePaths.add(keyAttributePathArg);

			refreshKeyAttributePathsString();
		}
	}

	/**
	 * Gets the value attribute path of the content schema.
	 *
	 * @return the value attribute path of the content schema
	 */
	public AttributePath getValueAttributePath() {

		return valueAttributePath;
	}

	/**
	 * Sets the value attribute path of the content schema.
	 *
	 * @param valueAttributePathArg a new value attribute path
	 */
	public void setValueAttributePath(final AttributePath valueAttributePathArg) {

		valueAttributePath = valueAttributePathArg;
	}

	private void checkOrderedKeyAttributePaths() {

		if (orderedKeyAttributePaths == null) {

			initKeyAttributePaths(true);

			if (orderedKeyAttributePaths == null) {

				orderedKeyAttributePaths = Lists.newLinkedList();
			}
		}
	}

	private void checkUtilisedKeyAttributePaths() {

		if (utilisedKeyAttributePaths == null) {

			utilisedKeyAttributePaths = Sets.newCopyOnWriteArraySet();
		}
	}

	/**
	 * Adds a new key attribute path to the given position in the ordered list of key attribute paths.<br>
	 * Created by: tgaengler
	 *
	 * @param keyAttributePathArg   a new key attribute path
	 * @param keyAttributePathIndex the position of the key attribute path in the ordered list of key attribute paths
	 */
	protected void addKeyAttributePath(final AttributePath keyAttributePathArg, final int keyAttributePathIndex) {

		if (keyAttributePathArg != null) {

			checkUtilisedKeyAttributePaths();

			checkOrderedKeyAttributePaths();

			if (!keyAttributePathArg.equals(orderedKeyAttributePaths.get(keyAttributePathIndex))) {

				orderedKeyAttributePaths.add(keyAttributePathIndex, keyAttributePathArg);
				utilisedKeyAttributePaths.add(keyAttributePathArg);

				refreshKeyAttributePathsString();
			}
		}
	}

	/**
	 * Removes an existing key attribute path from the key attribute paths.<br>
	 * Created by: tgaengler
	 *
	 * @param keyAttributePath      an existing key attribute path that should be removed
	 * @param keyAttributePathIndex the position of the key attribute path in the ordered list of key attribute paths
	 */
	public void removeKeyAttributePath(final AttributePath keyAttributePath, final int keyAttributePathIndex) {

		if (keyAttributePath != null
				&& ((orderedKeyAttributePaths != null && orderedKeyAttributePaths.contains(keyAttributePath)) || (utilisedKeyAttributePaths != null
				&& utilisedKeyAttributePaths
				.contains(keyAttributePath)))) {

			orderedKeyAttributePaths.remove(keyAttributePathIndex);

			if (!orderedKeyAttributePaths.contains(keyAttributePath)) {

				utilisedKeyAttributePaths.remove(keyAttributePath);
			}

			refreshKeyAttributePathsString();
		}
	}

	/**
	 * Refreshes the string that holds the serialised JSON object of the key attribute paths (ordered list of attribute paths).
	 * This method should be called after every manipulation of the key attribute paths (to keep the states consistent).
	 */
	private void refreshKeyAttributePathsString() {

		if (orderedKeyAttributePaths != null) {

			orderedKeyAttributePathsJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

			for (final AttributePath keyAttributePath : orderedKeyAttributePaths) {

				orderedKeyAttributePathsJSON.add(keyAttributePath.getUuid());
			}
		}

		if (null != orderedKeyAttributePathsJSON && orderedKeyAttributePathsJSON.size() > 0) {

			keyAttributePaths = orderedKeyAttributePathsJSON.toString().getBytes(Charsets.UTF_8);
		} else {

			keyAttributePaths = null;
		}
	}

	/**
	 * Initialises the key attribute paths, collection of attribute paths and JSON object from the string that holds the
	 * serialised JSON object of the key attribute paths.
	 *
	 * @param fromScratch flag that indicates, whether the key attribute paths should be initialised from scratch or not
	 */
	private void initKeyAttributePaths(final boolean fromScratch) {

		if (orderedKeyAttributePathsJSON == null && !orderedKeyAttributePathsInitialized) {

			if (keyAttributePaths == null) {

				ContentSchema.LOG.debug("key attribute paths JSON is null for content schema '" + getUuid() + "'");

				if (fromScratch) {

					orderedKeyAttributePathsJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());
					orderedKeyAttributePaths = Lists.newLinkedList();

					orderedKeyAttributePathsInitialized = true;
				}

				return;
			}

			try {

				orderedKeyAttributePaths = Lists.newLinkedList();

				// parse key attribute paths string
				orderedKeyAttributePathsJSON = DMPPersistenceUtil.getJSONArray(StringUtils.toEncodedString(keyAttributePaths, Charsets.UTF_8));

				if (null != orderedKeyAttributePathsJSON) {

					for (final JsonNode keyAttributePathIdNode : orderedKeyAttributePathsJSON) {

						final AttributePath keyAttributePath = getKeyAttributePath(keyAttributePathIdNode.asText());

						if (null != keyAttributePath) {

							orderedKeyAttributePaths.add(keyAttributePath);
						}
					}
				}
			} catch (final DMPException e) {

				ContentSchema.LOG.debug("couldn't parse key attribute paths JSON for content schema '" + getUuid() + "'");
			}

			orderedKeyAttributePathsInitialized = true;
		}
	}

	/**
	 * Gets the key attribute path for a given key attribute path identifier.
	 *
	 * @param uuid a key attribute path identifier
	 * @return the matched key attribute path or null
	 */
	public AttributePath getKeyAttributePath(final String uuid) {

		if (null == uuid) {

			return null;
		}

		if (null == utilisedKeyAttributePaths) {

			return null;
		}

		if (StringUtils.toEncodedString(keyAttributePaths, Charsets.UTF_8).isEmpty()) {

			return null;
		}

		final List<AttributePath> keyAttributePathsFiltered = Lambda.filter(
				Lambda.having(Lambda.on(AttributePath.class).getUuid(), Matchers.equalTo(uuid)), utilisedKeyAttributePaths);

		if (keyAttributePathsFiltered == null || keyAttributePathsFiltered.isEmpty()) {

			return null;
		}

		return keyAttributePathsFiltered.get(0);
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return ContentSchema.class.isInstance(obj)
				&& super.completeEquals(obj)
				&& DMPPersistenceUtil.getAttributePathUtils().completeEquals(((ContentSchema) obj).getRecordIdentifierAttributePath(),
				getRecordIdentifierAttributePath())
				&& DMPPersistenceUtil.getAttributePathUtils().completeEquals(((ContentSchema) obj).getUtilisedKeyAttributePaths(),
				getUtilisedKeyAttributePaths())
				&& DMPPersistenceUtil.getAttributePathUtils().completeEquals(((ContentSchema) obj).getKeyAttributePaths(), getKeyAttributePaths())
				&& DMPPersistenceUtil.getAttributePathUtils().completeEquals(((ContentSchema) obj).getValueAttributePath(), getValueAttributePath());
	}

}
