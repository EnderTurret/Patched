package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.JsonSelector.CompoundSelector;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patched.patch.PatchUtil;
import net.enderturret.patched.patch.TestPatch;

import tests.util.TestUtil;

/**
 * Miscellaneous tests for miscellaneous things.
 * @author EnderTurret
 */
final class MiscTests {

	@Test
	void testPatchBuilding() {
		final List<JsonPatch> patches = new ArrayList<>();

		patches.add(PatchUtil.add("/add", new JsonPrimitive(true)));
		patches.add(PatchUtil.copy("/copied", "/from"));
		patches.add(PatchUtil.move("/moved", "/from"));
		patches.add(PatchUtil.remove("/remove"));
		patches.add(PatchUtil.replace("/replace", new JsonPrimitive(3)));
		patches.add(PatchUtil.compound(
				PatchUtil.test("/test", new JsonPrimitive(false), true),
				PatchUtil.test("custom", "/test", new JsonPrimitive("is this really a primitive? the answer may surprise you"), false)));
		patches.add(PatchUtil.find("/find", List.of(), PatchUtil.remove(""), true));
		patches.add(PatchUtil.include("test"));
		patches.add(PatchUtil.paste("/test", "test", "/value", new JsonPrimitive("some input value")));

		final JsonPatch result = PatchUtil.compound(patches.toArray(JsonPatch[]::new));

		final String target = TestUtil.read("/tests/built_patch.json");
		final String output = PatchingTests.GSON.toJson(result);

		assertEquals(target, output);
	}

	@Test
	void testExceptions() {
		// Instantiate some exceptions. They don't really need testing.
		// This might be coverage hacking, but it's fine I swear.
		final PatchingException e = new PatchingException();
		new PatchingException(e);
		new PatchingException("this is a message", e);
		new TraversalException();
		new TraversalException(e);
		new TraversalException("this is also a message", e);
	}

	@Test
	void testPatchSerializer() {
		new JsonPatch.Serializer();

		assertThrows(IllegalArgumentException.class, () -> new JsonPatch.Serializer(null, true, false, false));
	}

	@Test
	void testBadPatch() {
		final JsonPatch bad = new JsonPatch(null) {
			@Override
			protected String operation() {
				return "bad";
			}
			@Override
			protected void patchJson(ElementContext elem, PatchContext context) throws PatchingException, TraversalException {}
		};

		{
			final JsonDocument doc = new JsonDocument(new JsonObject());
			assertThrows(UnsupportedOperationException.class, () -> bad.patch(doc, PatchContext.newContext()), "Should throw for null path");
		}

		assertThrows(IllegalArgumentException.class, () -> new TestPatch(null, null, null, false) {}, "Should throw for ambiguous type, path, and test");
	}

	@Test
	void testAuditMethods() {
		PatchAudit audit = new PatchAudit("what");
		audit.setPatchPath("something else");

		assertFalse(audit.hasRecords());

		audit.recordReplace("/fake", "2");

		assertTrue(audit.hasRecords());

		audit = new PatchAudit("what");

		audit.recordRemove("/fake", "2", new JsonPrimitive(true));

		assertTrue(audit.hasRecords());
	}

	@Test
	void testCompoundSelector() {
		final CompoundSelector selector = JsonSelector.of("/a/b/c");

		assertThrows(IndexOutOfBoundsException.class, () -> selector.toString(3, 2));
		assertThrows(IndexOutOfBoundsException.class, () -> selector.toString(0, 7));

		assertEquals("b", selector.toString(1, 2));
		assertEquals("b/c", selector.toString(1, 3));
	}

	@Test
	void testElementContexts() {
		final ElementContext context = new ElementContext.NoParent(PatchContext.newContext(), JsonNull.INSTANCE);
		assertNull(context.parent());
		assertThrows(TraversalException.class, () -> context.child(0, JsonNull.INSTANCE));
		assertThrows(TraversalException.class, () -> context.child("some path", JsonNull.INSTANCE));
	}

	@Test
	void testJsonDocuments() {
		final JsonDocument doc = new JsonDocument(new JsonPrimitive("this"));
		assertEquals("\"this\"", doc.toString());
		assertEquals(new JsonPrimitive("this").hashCode(), doc.hashCode());

		assertEquals(doc, doc);
		assertEquals(new JsonDocument(new JsonPrimitive("this")), doc);
		assertNotEquals(doc, null);
		assertNotEquals("this", doc);
	}
}