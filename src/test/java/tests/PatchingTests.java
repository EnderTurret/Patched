package tests;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.Patches;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;

import tests.util.SimpleTestEvaluator;
import tests.util.TestUtil;

/**
 * <p>Tests all of the patching-related functionality. This is where most of the testing occurs.</p>
 * <p>See {@link JsonPatchTests} for similar testing.</p>
 * @author EnderTurret
 */
public final class PatchingTests {

	private static boolean printSuccess = false;
	private static int testCount = 0;
	private static int passed = 0;

	private static final Map<PatchContext, Gson> GSONS = new HashMap<>(4);

	static final Gson GSON = Patches.patchGson(true, true).disableHtmlEscaping().setPrettyPrinting().create();

	static {
		GSONS.put(PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true), GSON);
	}

	public static void main(String[] args) {
		test("add/to_array");
		test("add/to_array_absolute");
		test("add/to_array_oob_lenient");
		test("add/to_object");
		test("add/to_object_dash");
		test("add/to_object_number");
		test("add/to_object_overwrite");

		test("test/presence/success");
		test("test/presence/fail");

		test("test/custom/basic");
		test("test/custom/no_path");
		test("test/custom/no_value");

		test("test/equal/string/success");
		test("test/equal/string/fail");
		test("test/equal/number/success");
		test("test/equal/number/fail");
		test("test/equal/array/success");
		test("test/equal/array/fail");
		test("test/equal/object/success");
		test("test/equal/object/fail");
		test("test/equal/object/different_order");

		test("test/inverse/presence/success");
		test("test/inverse/presence/fail");
		test("test/inverse/presence/false_inverse", false); // Patch output omits the redundant inverse field.

		test("test/inverse/equal/string/success");
		test("test/inverse/equal/string/fail");
		test("test/inverse/equal/number/success");
		test("test/inverse/equal/number/fail");
		test("test/inverse/equal/array/success");
		test("test/inverse/equal/array/fail");
		test("test/inverse/equal/object/success");
		test("test/inverse/equal/object/fail");

		test("test/edges/negative_index");
		test("test/edges/no_child_in_array");
		test("test/edges/no_numeric_child_in_object");
		test("test/edges/no_numeric_subchild");
		test("test/edges/no_subchild");
		test("test/edges/traverse_number");
		test("test/edges/traverse_number_numeric");

		test("replace/in_array");
		test("replace/in_object");
		test("replace/in_object_dash");
		test("replace/in_object_number");

		test("remove/in_array");
		test("remove/in_object");
		test("remove/in_object_dash");
		test("remove/array");
		test("remove/object");

		test("copy/in_array");
		test("copy/in_object");
		test("copy/in_object_dash");
		test("copy/into_array");
		test("copy/into_array_oob_lenient");
		test("copy/into_object");
		test("copy/into_object_dash");
		test("copy/into_nonexistent");

		test("move/in_array");
		test("move/in_object");
		test("move/in_object_dash");
		test("move/into_array");
		test("move/into_array_oob_lenient");
		test("move/into_object");
		test("move/into_object_dash");
		test("move/into_nonexistent");

		test("find/basic_in_array");
		test("find/basic_in_object");
		test("find/basic_in_object_multi_test");
		test("find/multi_in_array");
		test("find/multi_in_object");
		test("find/remove_value");
		test("find/replace_value");
		test("find/remove_unspecific", false);
		test("find/redundant_multi", false);

		test("selectors/double_empty");
		test("selectors/empty");
		test("selectors/empty2");
		test("selectors/numeric_in_object");
		test("selectors/escapes");

		test("audit/add_array");
		test("audit/add_object");
		test("audit/remove_array_element");

		testThrows("error/add/to_array_oob", TraversalException.class, "/array/91: No such child 91!");
		testThrows("error/replace/in_array_oob", TraversalException.class, "/array/23: No such child 23!");
		testThrows("error/replace/nonexistent", TraversalException.class, "/obj/foo: No such child foo!");
		testThrows("error/replace/nonexistent_number_in_object", TraversalException.class, "/obj/3: No such child 3!");
		testThrows("error/replace/end_of_array", TraversalException.class, "/array/-: Expected object to find '-' in, found [1,2]!");
		testThrows("error/remove/in_array_oob", TraversalException.class, "/array/23: No such child 23!");
		testThrows("error/remove/nonexistent", TraversalException.class, "/obj/foo: No such child foo!");
		testThrows("error/remove/end_of_array", TraversalException.class, "/array/-: Expected object to find '-' in, found [1,2,3]!");
		testThrows("error/remove/document", PatchingException.class, "Attempted to remove root element!");
		testThrows("error/copy/in_array_oob", TraversalException.class, "/array/23: No such child 23!");
		testThrows("error/copy/into_array_oob", TraversalException.class, "/array/23: No such child 23!");
		testThrows("error/copy/from_nonexistent", TraversalException.class, "/obj/foo: No such child foo!");
		testThrows("error/copy/to_nonexistent", TraversalException.class, "/nested/copied: No such child nested!");
		testThrows("error/move/in_array_oob", TraversalException.class, "/array/23: No such child 23!");
		testThrows("error/move/into_array_oob", TraversalException.class, "/array/23: No such child 23!");
		testThrows("error/move/from_nonexistent", TraversalException.class, "/obj/foo: No such child foo!");
		testThrows("error/move/to_nonexistent", TraversalException.class, "/nested/moved: No such child nested!");

		testThrows("error/find/disabled_in_deserialization", PatchingException.class, "Unsupported operation 'find': Patched extensions are not enabled.");
		testThrows("error/find/disabled_in_runtime", PatchingException.class, "find: Patched extensions are not enabled.");

		testThrows("error/find/other_op_in_test", PatchingException.class, "Unexpected operation \"add\": only test is allowed here.");

		testThrows("error/selectors/negative_array_index", TraversalException.class, "/array/-3: Attempted to traverse negative index in array (-3)!");
		testThrows("error/selectors/unexpected_array", TraversalException.class, "/array/test: Expected object to find 'test' in, found [1,2,3]!");
		testThrows("error/selectors/unexpected_primitive", TraversalException.class, "/array/1/2: Expected array or object to find '2' in, found 2!");

		testThrows("error/parsing/invalid_op", PatchingException.class, "Unknown operation 'discombobulate'");
		testThrows("error/parsing/missing_op", PatchingException.class, "Missing operation!");
		testThrows("error/parsing/missing_path", PatchingException.class, "'path' is missing!");
		testThrows("error/parsing/missing_value", PatchingException.class, "'value' is missing!");
		testThrows("error/parsing/missing_value_test", PatchingException.class, "'value' is missing!");
		testThrows("error/parsing/op_not_string", PatchingException.class, "Unknown operation '3'");
		testThrows("error/parsing/path_not_string", PatchingException.class, "'path' must be a string (was: true)!");

		testThrows("error/test/no_custom_evaluator", PatchingException.class, "Cannot handle custom test type 'custom' as no evaluator is installed!");

		System.out.printf("%d / %d Patched tests passed.\n", passed, testCount);

		JsonPatchTests.main();

		MiscTests.main();
	}

	private static PatchContext[] readConfig(String root) {
		final String path = root + "/config.json";

		if (PatchingTests.class.getResource(path) != null) {
			final String config = TestUtil.read(path);
			final JsonObject obj = JsonParser.parseString(config).getAsJsonObject();

			final PatchContext input;
			if (obj.has("input")) {
				final JsonObject o = obj.get("input").getAsJsonObject();
				input = PatchContext.newContext()
						.testExtensions(!o.has("testExtensions") || o.get("testExtensions").getAsBoolean())
						.patchedExtensions(!o.has("patchedExtensions") || o.get("patchedExtensions").getAsBoolean())
						.throwOnOobAdd(!o.has("throwOnOobAdd") || o.get("throwOnOobAdd").getAsBoolean());
			} else input = PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true);

			final PatchContext runtime;
			if (obj.has("runtime")) {
				final JsonObject o = obj.get("runtime").getAsJsonObject();
				runtime = PatchContext.newContext()
						.testExtensions(!o.has("testExtensions") || o.get("testExtensions").getAsBoolean())
						.patchedExtensions(!o.has("patchedExtensions") || o.get("patchedExtensions").getAsBoolean())
						.throwOnOobAdd(!o.has("throwOnOobAdd") || o.get("throwOnOobAdd").getAsBoolean())
						.testEvaluator(o.has("customTests") ? new SimpleTestEvaluator(o.get("customTests")) : null);
			} else runtime = PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true);

			return new PatchContext[] { input, runtime };
		}

		return new PatchContext[] {
				PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true),
				PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true)
		};
	}

	private static Test readTest(String name, boolean doOutputTest) {
		testCount++;

		final String root = "/tests/" + name;
		final String input = TestUtil.read(root + "/input.json");
		final String patchSrc = TestUtil.read(root + "/input.json.patch");
		String expectedAudit;
		try {
			expectedAudit = TestUtil.read(root + "/audit.json");
			//if (!expectedAudit.contains("//"))
			//System.err.println("Note: " + name + "'s audit has no notes.");
		} catch (Exception e) {
			//System.err.println("Note: " + name + " is missing an audit.json.");
			expectedAudit = null;
		}

		final JsonElement inputElem;

		try {
			inputElem = JsonParser.parseString(input);
		} catch (JsonSyntaxException e) {
			System.err.println("[" + name + "]: Failed to parse input: " + e.getMessage());
			return null;
		}

		final PatchContext[] contexts = readConfig(root);

		final Gson gson = GSONS.computeIfAbsent(contexts[0],
				c -> Patches.patchGson(c).setPrettyPrinting().create());

		final JsonPatch patch;

		try {
			patch = Patches.readPatch(gson, patchSrc);
		} catch (JsonSyntaxException e) {
			System.err.println("[" + name + "]: Failed to parse patch:");
			e.printStackTrace();
			return null;
		}

		if (doOutputTest) {
			final String patchOut = GSON.toJson(patch);

			if (!patchOut.equals(patchSrc)) {
				System.out.println("Patch output test for " + name + " failed!\n");
				System.out.println(patchSrc);
				System.out.println("\n(expected) vs (output)\n");
				System.out.println(patchOut);
			}
		}

		return new Test(root, inputElem, patch, expectedAudit, contexts[1]);
	}

	private static void testThrows(String name, Class<? extends Exception> clazz, String message, boolean doOutputTest) {
		try {
			final Test input = readTest(name, doOutputTest);
			final JsonDocument doc = new JsonDocument(input.input());

			input.patch().patch(doc, input.context());

			System.out.println("Test " + name + " failed!");
			System.out.println("Output:\n");
			System.out.println(GSON.toJson(doc.getRoot()));
		} catch (Exception e) {
			if (e.getClass() == clazz && e.getMessage().equals(message)) {
				if (printSuccess)
					System.out.println("Test " + name + " passed!");
				passed++;
			}
			else {
				System.err.println("Exception processing test " + name + ":");
				e.printStackTrace();
			}
		}
	}

	private static void testThrows(String name, Class<? extends Exception> clazz, String message) {
		testThrows(name, clazz, message, true);
	}

	private static void test(String name, boolean doOutputTest) {
		try {
			final Test input = readTest(name, doOutputTest);
			if (input == null) return;
			String expected = TestUtil.read(input.path() + "/result.json");
			final JsonElement expectedElem = JsonParser.parseString(expected);

			// -----

			final String auditBaseTest = new PatchAudit(name).toString(expectedElem);

			if (!auditBaseTest.equals(expected)) {
				System.out.println("Test " + name + " unchanged audit failed!\n");
				System.out.println(expected);
				System.out.println("\n(expected) vs (output)\n");
				System.out.println(auditBaseTest);
			}

			// -----

			final PatchAudit audit = new PatchAudit(name);
			final JsonDocument doc = new JsonDocument(input.input());

			input.patch().patch(doc, input.context().audit(audit));

			if (!expectedElem.equals(doc.getRoot())) {
				expected = GSON.toJson(expectedElem);
				final String output = GSON.toJson(doc.getRoot());

				System.out.println("Test " + name + " failed!\n");
				System.out.println(expected);
				System.out.println("\n(expected) vs (output)\n");
				System.out.println(output);
				return;
			}

			if (input.expectedAudit() != null) {
				final String auditOut = audit.toString(doc.getRoot());

				if (!input.expectedAudit().equals(auditOut)) {
					System.out.println("Test " + name + " audit failed!\n");
					System.out.println(input.expectedAudit());
					System.out.println("\n(expected) vs (output)\n");
					System.out.println(auditOut);
					return;
				}
			}

			if (printSuccess)
				System.out.println("Test " + name + " passed!");
			passed++;
		} catch (Exception e) {
			System.err.println("Exception processing test " + name + ":");
			e.printStackTrace();
		}
	}

	private static void test(String name) {
		test(name, true);
	}

	private static record Test(String path, JsonElement input, JsonPatch patch, String expectedAudit, PatchContext context) {}
}