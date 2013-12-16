package de.avgl.dmp.persistence.model.resource.utils;

import com.google.common.base.Charsets;

public interface ConfigurationStatics {

	public static final String		ENCODING					= "encoding";

	public static final String		ESCAPE_CHARACTER			= "escape_character";

	public static final String		QUOTE_CHARACTER				= "quote_character";

	public static final String		COLUMN_DELIMITER			= "column_delimiter";

	public static final String		ROW_DELIMITER				= "row_delimiter";

	public static final String		IGNORE_LINES				= "ignore_lines";

	public static final String		DISCARD_ROWS				= "discard_rows";

	public static final String		AT_MOST						= "at_most_rows";

	public static final String		RECORD_TAG					= "record_tag";

	public static final String		XML_NAMESPACE				= "xml_namespace";

	public static final String		DEFAULT_ENCODING			= Charsets.UTF_8.name();

	public static final Character	DEFAULT_ESCAPE_CHARACTER	= '\\';

	public static final Character	DEFAULT_QUOTE_CHARACTER		= '"';

	public static final Character	DEFAULT_COLUMN_DELIMITER	= ';';

	public static final String		DEFAULT_ROW_DELIMITER		= "\n";

	public static final int			DEFAULT_IGNORE_LINES		= 0;

	public static final int			DEFAULT_DISCARD_ROWS		= 0;

	public static final String		STORAGE_TYPE				= "storage_type";
}
