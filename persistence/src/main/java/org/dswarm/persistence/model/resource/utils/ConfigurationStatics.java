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

import com.google.common.base.Charsets;

/**
 * Holds references for keys of configuration parameters and default values of configuration parameters.
 *
 * @author tgaengler
 */
public interface ConfigurationStatics {

	/**
	 * The encoding that should be applied to interpret the data resource.
	 */
	String ENCODING = "encoding";

	/**
	 * The escape character of a CSV interpretation.
	 */
	String ESCAPE_CHARACTER = "escape_character";

	/**
	 * The quote character of a CSV interpretation.
	 */
	String QUOTE_CHARACTER = "quote_character";

	/**
	 * The column delimiter of a CSV interpretation.
	 */
	String COLUMN_DELIMITER = "column_delimiter";

	/**
	 * The row delimiter of a CSV interpretation-
	 */
	String ROW_DELIMITER = "row_delimiter";

	/**
	 * The number of lines that should be ignored at the beginning of a CSV data resource.
	 */
	String IGNORE_LINES = "ignore_lines";

	/**
	 * The number of rows that should be discarded at the beginning of a CSV data resource.
	 */
	String DISCARD_ROWS = "discard_rows";

	/**
	 * The number of rows that should be processed at most of a CSV data resource.
	 */
	String AT_MOST = "at_most_rows";

	/**
	 * The record tag of an XML data resource.
	 */
	String RECORD_TAG = "record_tag";

	/**
	 * The XML name space of an XML data resource.
	 */
	String XML_NAMESPACE = "xml_namespace";

	/**
	 * If true, the first row contains the column headers.
	 */
	String FIRST_ROW_IS_HEADINGS = "first_row_is_headings";

	/**
	 * The default encoding that should be applied to interpret the data resource.
	 */
	String DEFAULT_ENCODING = Charsets.UTF_8.name();

	/**
	 * The default escape character of a CSV interpretation.
	 */
	Character DEFAULT_ESCAPE_CHARACTER = '\\';

	/**
	 * The default quote character of a CSV interpretation-
	 */
	Character DEFAULT_QUOTE_CHARACTER = '"';

	/**
	 * The default column delimiter of a CSV interpretation.
	 */
	Character DEFAULT_COLUMN_DELIMITER = ';';

	/**
	 * The default row delimiter of a CSV interpretation.
	 */
	String DEFAULT_ROW_DELIMITER = "\n";

	/**
	 * The default number of lines that should be ignored at the beginning of a CSV data resource.
	 */
	int DEFAULT_IGNORE_LINES = 0;

	/**
	 * The default number of rows that should be discarded at the beginning of a CSV data resource.
	 */
	int DEFAULT_DISCARD_ROWS = 0;

	/**
	 * The default value for FIRST_ROW_IS_HEADINGS
	 */
	boolean DEFAULT_FIRST_ROW_IS_HEADINGS = true;

	/**
	 * The storage type of the data resource.
	 */
	String STORAGE_TYPE = "storage_type";

	String SCHEMA_STORAGE_TYPE = "schema";

	String CSV_STORAGE_TYPE = "csv";

	String XML_STORAGE_TYPE = "xml";

	String JSON_STORAGE_TYPE = "json";

	String MABXML_STORAGE_TYPE = "mabxml";

	String MARCXML_STORAGE_TYPE = "marcxml";

	String PICAPLUSXML_STORAGE_TYPE = "picaplusxml";

	String PICAPLUSXML_GLOBAL_STORAGE_TYPE = "picaplusxml-global";

	String PNX_STORAGE_TYPE = "pnx";

	String OAI_PMH_DC_ELEMENTS_STORAGE_TYPE = "oai-pmh+dce";

	String OAI_PMH_DCE_AND_EDM_ELEMENTS_STORAGE_TYPE = "oai-pmh+dce+edm";

	String OAIPMH_DC_TERMS_STORAGE_TYPE = "oai-pmh+dct";

	String OAIPMH_MARCXML_STORAGE_TYPE = "oai-pmh+marcxml";

	String SRU_11_PICAPLUSXML_GLOBAL_STORAGE_TYPE = "sru11+picaplusxml-global";

	String SPRINGER_JOURNALS_STORAGE_TYPE = "springer-journals";
}
