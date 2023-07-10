package tests;

import java.util.ArrayList;
import java.util.List;

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
public final class MiscTests {

	public static void main(String... args) {
		exceptions();
		patchBuilding();
		testCompoundSelector();
		testPatchSerializer();
		testBadPatch();
		testAuditMethods();
	}

	private static void patchBuilding() {
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

		final JsonPatch result = PatchUtil.compound(patches.toArray(JsonPatch[]::new));

		final String target = TestUtil.read("/tests/built_patch.json");
		final String output = PatchingTests.GSON.toJson(result);

		if (!output.equals(target)) {
			System.out.println("Built patch test failed!\n");
			System.out.println(target);
			System.out.println("\n(expected) vs (output)\n");
			System.out.println(output);
		}
	}

	private static void exceptions() {
		// Instantiate some exceptions. They don't really need testing.
		// This might be coverage hacking, but it's fine I swear.
		final PatchingException e = new PatchingException();
		new PatchingException(e);
		new PatchingException("this is a message", e);
		new TraversalException();
		new TraversalException(e);
		new TraversalException("this is also a message", e);
	}

	private static void testPatchSerializer() {
		new JsonPatch.Serializer();

		try {
			new JsonPatch.Serializer(null, true, false, false);
			System.out.println("Null-enforcing op test failed: no exception occured.");
		} catch (IllegalArgumentException ignored) {}
	}

	private static void testBadPatch() {
		final JsonPatch bad = new JsonPatch(null) {
			@Override
			protected String operation() {
				return "bad";
			}
			@Override
			protected void patchJson(ElementContext elem, PatchContext context) throws PatchingException, TraversalException {}
		};

		try {
			bad.patch(new JsonDocument(new JsonObject()), PatchContext.newContext());
			System.out.println("Bad patch implementation test failed: no exception occured.");
		} catch (UnsupportedOperationException ignored) {}

		try {
			new TestPatch(null, null, null, false) {};
			System.out.println("Ambiguous test patch construction test failed: no exception occured.");
		} catch (IllegalArgumentException ignored) {}
	}

	private static void testAuditMethods() {
		PatchAudit audit = new PatchAudit("what");
		audit.setPatchPath("something else");

		if (audit.hasRecords())
			System.out.println("hasRecords() test one failed: audit should not have records yet.");

		audit.recordReplace("/fake", "2");

		if (!audit.hasRecords())
			System.out.println("hasRecords() test two failed: audit should have one record.");

		audit = new PatchAudit("what");

		audit.recordRemove("/fake", "2", new JsonPrimitive(true));

		if (!audit.hasRecords())
			System.out.println("hasRecords() test three failed: audit should have one removal record.");
	}

	// Maybe one day I will convert all this to JUnit like a modern Java developer.
	private static void testCompoundSelector() {
		final CompoundSelector selector = JsonSelector.of("/a/b/c");

		try {
			selector.toString(3, 2);
			System.out.println("Test toString(3, 2) failed: no exception occured.");
		} catch (IndexOutOfBoundsException ignored) {}

		try {
			selector.toString(0, 7);
			System.out.println("Test toString(0, 7) failed: no exception occured.");
		} catch (IndexOutOfBoundsException ignored) {}

		{
			final String str = selector.toString(1, 2);
			if (!"b".equals(str))
				System.out.println("Test toString(1, 2) failed: got " + str);
		}

		{
			final String str = selector.toString(1, 3);
			if (!"b/c".equals(str))
				System.out.println("Test toString(1, 3) failed: got " + str);
		}
	}
}