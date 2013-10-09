package de.avgl.dmp.persistence.model.internal.impl.test;

import java.util.Map;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import de.avgl.dmp.persistence.model.internal.MemoryDb;
import de.avgl.dmp.persistence.model.internal.impl.MemoryDbImpl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class MemoryDbImplTest {

	private class TestDb extends MemoryDbImpl<String, String, String> {}

	private MemoryDb<String, String, String> db;

	@Before
	public void setUp() throws Exception {
		db = new TestDb();

	}

	@Test
	public void testPut() throws Exception {
		db.put("foo", "bar", "a");

		assertTrue(db.underlying().containsRow("foo"));
		assertTrue(db.underlying().containsColumn("bar"));
		assertTrue(db.underlying().contains("foo", "bar"));
		assertThat(db.underlying().get("foo", "bar"), equalTo("a"));
	}

	@Test
	public void testGetCell() throws Exception {
		db.put("foo", "bar", "a");

		final Optional<String> actual = db.get("foo", "bar");

		assertTrue(actual.isPresent());
		assertThat(actual.get(), equalTo("a"));

		assertFalse(db.get("baz", "qux").isPresent());

	}

	@Test
	public void testGetColumns() throws Exception {

		db.put("foo", "bar", "a");
		db.put("foo", "baz", "b");

		final Map<String,String> tableMap = db.get("foo");

		assertThat(tableMap.size(), equalTo(2));

		assertThat(tableMap, hasKey("bar"));
		assertThat(tableMap, hasKey("baz"));

		assertThat(tableMap.get("bar"), equalTo("a"));
		assertThat(tableMap.get("baz"), equalTo("b"));

		assertTrue(db.get("qux").isEmpty());
	}

	@Test
	public void testDelete() throws Exception {
		db.put("foo", "bar", "a");
		db.put("foo", "baz", "b");

		db.delete("foo", "bar");

		assertFalse(db.get("foo", "bar").isPresent());
		assertThat(db.get("foo").size(), equalTo(1));
	}
}
