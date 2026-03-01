package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.JsonSelector.CompoundSelector;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchUtil;
import net.enderturret.patched.patch.TestPatch;
import net.enderturret.patched.patch.context.ElementContext;
import net.enderturret.patched.patch.context.ElementContexts;
import net.enderturret.patched.patch.context.ImmutablePatchContext;
import net.enderturret.patched.patch.context.MutablePatchContext;

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
		patches.add(PatchUtil.find("/find", List.of(), PatchUtil.remove(""), "location", true));

		final JsonPatch result = PatchUtil.compound(patches.toArray(JsonPatch[]::new));

		final String target = TestUtil.read("/tests/built_patch.json");
		final String output = PatchingTests.GSON.toJson(result);

		assertEquals(target, output, "Built patch and target file should be identical");
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
		assertThrows(IllegalArgumentException.class, () -> new TestPatch(null, null, null, false) {}, "Should throw for ambiguous type, path, and test");
	}

	@Test
	void testAuditMethods() {
		final ImmutablePatchContext context = ImmutablePatchContext.newContext();
		final ElementContext elem = new ElementContexts.Document(context, null, new JsonDocument(JsonParser.parseString("{\"fake\":[1,2,3,4,5]}")));

		PatchAudit audit = new PatchAudit("what");
		audit.setPatchPath("something else");

		assertFalse(audit.hasRecords());

		audit.recordReplace(elem, JsonSelector.of("/fake/2"));

		assertTrue(audit.hasRecords());

		audit = new PatchAudit("what");

		audit.recordRemove(elem, JsonSelector.of("/fake/2"), new JsonPrimitive(true));

		assertTrue(audit.hasRecords());
	}

	@Test
	void testCompoundSelector() {
		final CompoundSelector selector = JsonSelector.of("/a/b/c");

		assertThrows(IndexOutOfBoundsException.class, () -> selector.toString(3, 2));
		assertThrows(IndexOutOfBoundsException.class, () -> selector.toString(0, 7));

		assertEquals("b", selector.toString(1, 2));
		assertEquals("b/c", selector.toString(1, 3));

		assertEquals(JsonSelector.ofSingle("a"), selector.path(0));
		assertArrayEquals(JsonSelector.of("/b/c").path(), selector.path(1, 3));
	}

	@Test
	void testElementContexts() {
		final ElementContext context = new ElementContexts.NoParent(ImmutablePatchContext.newContext(), new JsonDocument(JsonNull.INSTANCE), null, JsonNull.INSTANCE);
		assertNull(context.parent());
		assertThrows(TraversalException.class, () -> context.child(0, JsonNull.INSTANCE));
		assertThrows(TraversalException.class, () -> context.child("some path", JsonNull.INSTANCE));

		final ElementContext document = new ElementContexts.Document(context, context.doc());
		assertNull(document.parent());
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

	@Test
	void testMutableContext() {
		final ImmutablePatchContext expected = ImmutablePatchContext.newContext()
				.testExtensions(true).patchedExtensions(true).throwOnOobAdd(true).throwOnFailedTest(true)
				.testEvaluator(null).fileAccess(null).dataSource(null).audit(null);
		final MutablePatchContext mutable = new MutablePatchContext()
				.testExtensions(true).patchedExtensions(true).throwOnOobAdd(true).throwOnFailedTest(true)
				.testEvaluator(null).fileAccess(null).dataSource(null).audit(null);

		assertEquals(expected, mutable.asImmutableContext());
		assertEquals(expected, new MutablePatchContext(expected).asImmutableContext());
		assertSame(expected, expected.asImmutableContext());
	}

	@Test
	void testNullContexts() {
		// These can appear when cascading errors happen via e.g. the test patch.
		assertThrows(TraversalException.class, () -> new JsonSelector.NameSelector("name").select(null, true));
		assertThrows(TraversalException.class, () -> new JsonSelector.NumericSelector(0, "0").select(null, true));
		assertThrows(TraversalException.class, () -> new JsonSelector.PlaceholderSelector("holder", "{holder}").select(null, true));

		assertNull(new JsonSelector.NameSelector("name").select(null, false));
		assertNull(new JsonSelector.NumericSelector(0, "0").select(null, false));
		assertNull(new JsonSelector.PlaceholderSelector("holder", "{holder}").select(null, false));
	}
}