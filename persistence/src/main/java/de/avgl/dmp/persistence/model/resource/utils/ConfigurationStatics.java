package de.avgl.dmp.persistence.model.resource.utils;

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
	public static final String		ENCODING					= "encoding";

	/**
	 * The escape character of a CSV interpretation.
	 */
	public static final String		ESCAPE_CHARACTER			= "escape_character";

	/**
	 * The quote character of a CSV interpretation.
	 */
	public static final String		QUOTE_CHARACTER				= "quote_character";

	/**
	 * The column delimiter of a CSV interpretation.
	 */
	public static final String		COLUMN_DELIMITER			= "column_delimiter";

	/**
	 * The row delimiter of a CSV interpretation-
	 */
	public static final String		ROW_DELIMITER				= "row_delimiter";

	/**
	 * The number of lines that should be ignored at the beginning of a CSV data resource.
	 */
	public static final String		IGNORE_LINES				= "ignore_lines";

	/**
	 * The number of rows that should be discarded at the beginning of a CSV data resource.
	 */
	public static final String		DISCARD_ROWS				= "discard_rows";

	/**
	 * The number of rows that should be processed at most of a CSV data resource.
	 */
	public static final String		AT_MOST						= "at_most_rows";

	/**
	 * The record tag of an XML data resource.
	 */
	public static final String		RECORD_TAG					= "record_tag";

	/**
	 * The XML name space of an XML data resource.
	 */
	public static final String		XML_NAMESPACE				= "xml_namespace";

	/**
	 * If true, the first row contains the column headers.
	 */
	public static final String FIRST_ROW_IS_HEADINGS = "first_row_is_headings";

	/**
	 * The default encoding that should be applied to interpret the data resource.
	 */
	public static final String		DEFAULT_ENCODING			= Charsets.UTF_8.name();

	/**
	 * The default escape character of a CSV interpretation.
	 */
	public static final Character	DEFAULT_ESCAPE_CHARACTER	= '\\';

	/**
	 * The default quote character of a CSV interpretation-
	 */
	public static final Character	DEFAULT_QUOTE_CHARACTER		= '"';

	/**
	 * The default column delimiter of a CSV interpretation.
	 */
	public static final Character	DEFAULT_COLUMN_DELIMITER	= ';';

	/**
	 * The default row delimiter of a CSV interpretation.
	 */
	public static final String		DEFAULT_ROW_DELIMITER		= "\n";

	/**
	 * The default number of lines that should be ignored at the beginning of a CSV data resource.
	 */
	public static final int			DEFAULT_IGNORE_LINES		= 0;

	/**
	 * The default number of rows that should be discarded at the beginning of a CSV data resource.
	 */
	public static final int			DEFAULT_DISCARD_ROWS		= 0;

	/**
	 * The default value for FIRST_ROW_IS_HEADINGS
	 */
	public static final boolean DEFAULT_FIRST_ROW_IS_HEADINGS = true;

	/**
	 * The storage type of the data resource.
	 */
	public static final String		STORAGE_TYPE				= "storage_type";
}
