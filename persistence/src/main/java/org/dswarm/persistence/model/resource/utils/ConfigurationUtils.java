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
package org.dswarm.persistence.model.resource.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.utils.ExtendedBasicDMPJPAObjectUtils;

/**
 * @author tgaengler
 */
public final class ConfigurationUtils extends ExtendedBasicDMPJPAObjectUtils<Configuration> {

	private static final Collection<String> xmlStorageTypes = new ArrayList<>();

	static {

		xmlStorageTypes.add(ConfigurationStatics.XML_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.MABXML_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.MARCXML_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.OAI_PMH_DCE_AND_EDM_ELEMENTS_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.PICAPLUSXML_GLOBAL_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.PICAPLUSXML_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.PNX_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.SRU_11_PICAPLUSXML_GLOBAL_STORAGE_TYPE);
		xmlStorageTypes.add(ConfigurationStatics.SPRINGER_JOURNALS_STORAGE_TYPE);
	}

	public static Collection<String> getXMLStorageTypes() {

		return xmlStorageTypes;
	}
}
