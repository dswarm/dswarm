package de.avgl.dmp.persistence.model.internal.test;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Table;
import org.junit.Before;
import org.junit.Test;

import de.avgl.dmp.persistence.model.internal.MemoryDb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MemoryDbTest {

	private MemoryDb<String, String, String, String, String> db;

	private class TestDb extends MemoryDb<String, String, String, String, String> {}

	@Before
	public void setUp() throws Exception {
		db = new TestDb();

	}

	@Test
	public void testPut() throws Exception {
		db.put("foo", "bar", "a", "b", "c");

		assertTrue(db.underlying().containsRow("foo"));
		assertTrue(db.underlying().containsColumn("bar"));
		assertTrue(db.underlying().contains("foo", "bar"));
		assertTrue(db.underlying().get("foo", "bar").containsRow("a"));
		assertTrue(db.underlying().get("foo", "bar").containsColumn("b"));
		assertTrue(db.underlying().get("foo", "bar").contains("a", "b"));
		assertTrue(db.underlying().get("foo", "bar").containsValue("c"));
	}

	@Test
	public void testGetCell() throws Exception {
		db.put("foo", "bar", "a", "b", "c");

		final Optional<Table<String,String,String>> actual = db.get("foo", "bar");

		assertTrue(actual.isPresent());
		assertTrue(actual.get().containsRow("a"));
		assertTrue(actual.get().containsColumn("b"));
		assertTrue(actual.get().contains("a", "b"));
		assertTrue(actual.get().containsValue("c"));

		assertFalse(db.get("baz", "qux").isPresent());
	}

	@Test
	public void testGetColumns() throws Exception {
		db.put("foo", "bar", "a", "b", "c");
		db.put("foo", "baz", "d", "e", "f");

		final Map<String,Table<String,String,String>> tableMap = db.get("foo");

		assertThat(tableMap.size(), equalTo(2));

		assertTrue(tableMap.containsKey("bar"));
		assertTrue(tableMap.containsKey("baz"));

		assertTrue(tableMap.get("bar").contains("a", "b"));
		assertTrue(tableMap.get("baz").contains("d", "e"));

		assertTrue(db.get("qux").isEmpty());
	}

	@Test
	public void testSchema() throws Exception {
		db.put("foo", "bar", "a", "b", "c");
		db.put("foo", "bar", "a", "d", "e");

		final Optional<Set<String>> schema = db.schema("foo", "bar");

		assertTrue(schema.isPresent());

		assertFalse(schema.get().isEmpty());
		assertThat(schema.get().size(), equalTo(2));

		assertTrue(schema.get().contains("b"));
		assertTrue(schema.get().contains("d"));

		assertFalse(schema.get().contains("a"));
		assertFalse(schema.get().contains("c"));
		assertFalse(schema.get().contains("e"));

		assertFalse(db.schema("baz", "qux").isPresent());
	}

	@Test
	public void testDelete() throws Exception {
		db.put("foo", "bar", "a", "b", "c");
		db.put("foo", "baz", "a", "b", "c");

		db.delete("foo", "bar");

		assertFalse(db.get("foo", "bar").isPresent());
		assertThat(db.get("foo").size(), equalTo(1));
	}
}
