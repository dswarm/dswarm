package org.dswarm.converter.schema.test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.AbstractJSONSchemaParser;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * Created by tgaengler on 26.09.16.
 */
public abstract class AbstractJSONSchemaParserTest extends GuicedTest {

	@Override
	public void prepare() throws Exception {
		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.createTables();
		maintainDBService.truncateTables();
	}

	@Override
	public void tearDown3() throws Exception {
		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.truncateTables();
	}

	static Schema parseSchema(final String schemaFileName,
	                          final String recordIdentifier,
	                          final String schemaUUID,
	                          final String schemaName,
	                          final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                          final AbstractJSONSchemaParser schemaParser) throws DMPPersistenceException {

		return parseSchema(schemaFileName, recordIdentifier, schemaUUID, schemaName, optionalAttributePathsSAPIUUIDs, Optional.empty(), schemaParser);
	}

	static Schema parseSchema(final String schemaFileName,
	                          final String recordIdentifier,
	                          final String schemaUUID,
	                          final String schemaName,
	                          final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                          final Optional<Set<String>> optionalExcludeAttributePathStubs,
	                          final AbstractJSONSchemaParser schemaParser) throws DMPPersistenceException {

		final Optional<Schema> optionalSchema = schemaParser.parse(schemaFileName, recordIdentifier, schemaUUID, schemaName, optionalAttributePathsSAPIUUIDs, optionalExcludeAttributePathStubs);

		Assert.assertTrue(optionalSchema.isPresent());

		return optionalSchema.get();
	}

	static void testAttributePathsParsing(final String schemaFileName,
	                                      final String recordIdentifier,
	                                      final String resultFileName,
	                                      final boolean includeRecordTag,
	                                      final AbstractJSONSchemaParser schemaParser) throws IOException {

		testAttributePathsParsing(schemaFileName, recordIdentifier, resultFileName, includeRecordTag, Optional.empty(), schemaParser);
	}

	static Map<String, AttributePathHelper> parseAttributePaths(final String schemaFileName,
	                                                            final String recordIdentifier,
	                                                            final boolean includeRecordTag,
	                                                            final AbstractJSONSchemaParser schemaParser) {

		return parseAttributePaths(schemaFileName, recordIdentifier, includeRecordTag, Optional.empty(), schemaParser);
	}


	static Map<String, AttributePathHelper> parseAttributePaths(final String schemaFileName,
	                                                            final String recordIdentifier,
	                                                            final boolean includeRecordTag,
	                                                            final Optional<Set<String>> optionalExcludeAttributePathStubs,
	                                                            final AbstractJSONSchemaParser schemaParser) {

		schemaParser.setIncludeRecordTag(includeRecordTag);
		final Optional<Map<String, AttributePathHelper>> optionalAttributePaths = schemaParser.parseAttributePathsMap(schemaFileName,
				Optional.ofNullable(recordIdentifier),
				optionalExcludeAttributePathStubs);

		Assert.assertTrue(optionalAttributePaths.isPresent());

		return optionalAttributePaths.get();
	}

	static void testAttributePathsParsing(final String schemaFileName,
	                                      final String recordIdentifier,
	                                      final String resultFileName,
	                                      final boolean includeRecordTag,
	                                      final Optional<Set<String>> optionalExcludeAttributePathStubs,
	                                      final AbstractJSONSchemaParser schemaParser) throws IOException {

		final Map<String, AttributePathHelper> attributePaths = parseAttributePaths(schemaFileName, recordIdentifier, includeRecordTag, optionalExcludeAttributePathStubs, schemaParser);

		compareAttributePaths(resultFileName, attributePaths);
	}

	static void compareAttributePaths(final String resultFileName,
	                                  final Map<String, AttributePathHelper> attributePaths) throws IOException {

		final StringBuilder sb = new StringBuilder();

		for (final AttributePathHelper attributePath : attributePaths.values()) {

			sb.append(attributePath.toString()).append("\n");
		}

		final String expectedAttributePaths = DMPPersistenceUtil.getResourceAsString(resultFileName);
		final String actualAttributePaths = sb.toString();

		Assert.assertEquals(expectedAttributePaths, actualAttributePaths);
	}
}
