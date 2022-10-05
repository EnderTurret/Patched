package tests;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.enderturret.patched.Patches;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;

import tests.util.TestUtil;

/**
 * Implements the testing framework for the tests at <a href="https://github.com/json-patch/json-patch-tests">json-patch-tests</a>.
 * @author EnderTurret
 */
public final class JsonPatchTests {

	private static final boolean PRINT_TEST_SUCCESS = false;

	private static final Gson GSON = Patches.patchGson(false, false).setPrettyPrinting().create();

	public static void main(String... args) {
		test("/tests/json-patch-tests/spec_tests.json", JsonPatchTests::mapSpecErrors, (comment, test) -> true);

		// --------------------------------------------------------------------------------------------

		final Set<String> disabled1 = Set.of(
				// Replacing the root element is currently unsupported, and it would need a rewrite to work anyway.
				"replace object document with array document?",
				"replace array document with object document?",
				"replacing the root of the document is possible with add",
				"replace whole document",
				// This is actually up to the Json parser -- not the patch applier.
				// Gson parses these numbers into 0 and 1, respectively.
				// There is nothing we can do about this.
				// Side note: there are two tests with this comment.
				"test with bad array number that has leading zeros",
				// Out-of-bounds addition is allowed for the purpose of mod compatibility;
				// we don't error in this case but instead add to the end of the array, like if - was used.
				"add item to array at index > length should fail");

		test("/tests/json-patch-tests/tests.json", JsonPatchTests::mapPatchErrors,
				(comment, test) -> !disabled1.contains(comment)
				&& (!"no comment".equals(comment) || (
						// See disabled1: out-of-bounds addition.
						!"Out of bounds (upper)".equals(test.error)
						)));
	}

	private static String mapSpecErrors(String comment, String error) {
		return switch (comment) {
		case "4.1. add with missing object" -> "net.enderturret.patched.exception.TraversalException: /a: No such child a!";
		case "A.12.  Adding to a Non-existent Target" -> "net.enderturret.patched.exception.TraversalException: /baz: No such child baz!";
		case "A.9.  Testing a Value: Error" -> "net.enderturret.patched.exception.PatchingException: Test \"qux\" == \"bar\" failed.";
		case "A.15. Comparing Strings and Numbers" -> "net.enderturret.patched.exception.PatchingException: Test 10 == \"10\" failed.";
		default -> comment;
		};
	}

	private static String mapPatchErrors(String comment, String error) {
		return switch (comment) {
		case "Removing nonexistent field" -> "net.enderturret.patched.exception.TraversalException: /baz: No such child baz!";
		case "Removing deep nonexistent path" -> "net.enderturret.patched.exception.TraversalException: /missing1: No such child missing1!";
		case "Removing nonexistent index" -> "net.enderturret.patched.exception.TraversalException: /2: No such child 2!";
		case "test replace with missing parent key should fail" -> "net.enderturret.patched.exception.TraversalException: /foo: No such child foo!";

		case "missing from location to copy" -> "net.enderturret.patched.exception.TraversalException: /bar: No such child bar!";
		case "missing from location to move" -> "net.enderturret.patched.exception.TraversalException: /bar: No such child bar!";
		case "missing from parameter to copy" -> "net.enderturret.patched.exception.PatchingException: 'from' must not be missing!";
		case "missing from parameter to move" -> "net.enderturret.patched.exception.PatchingException: 'from' must not be missing!";
		case "missing 'value' parameter to add" -> "net.enderturret.patched.exception.PatchingException: 'value' must not be missing!";
		case "missing 'value' parameter to replace" -> "net.enderturret.patched.exception.PatchingException: 'value' must not be missing!";
		case "missing 'value' parameter to test" -> "net.enderturret.patched.exception.PatchingException: 'value' must not be missing!";
		case "missing 'path' parameter" -> "net.enderturret.patched.exception.PatchingException: 'path' must not be missing!";
		case "'path' parameter with null value" -> "net.enderturret.patched.exception.PatchingException: 'path' must be a string (was: null)!";

		// These all error, but for a different reason -- "1e0" is not interpreted as a number, as it isn't an integer, so it's treated like a string.
		case "test remove with bad number should fail" -> "net.enderturret.patched.exception.TraversalException: /baz/1e0: Expected object to find '1e0' in, found [{\"qux\":\"hello\"}]!";
		case "test remove with bad index should fail" -> "net.enderturret.patched.exception.TraversalException: /1e0: Expected object to find '1e0' in, found [1,2,3,4]!";
		case "test replace with bad number should fail" -> "net.enderturret.patched.exception.TraversalException: /1e0: Expected object to find '1e0' in, found [\"\"]!";
		case "test copy with bad number should fail" -> "net.enderturret.patched.exception.TraversalException: /baz/1e0: Expected object to find '1e0' in, found [1,2,3]!";
		case "test move with bad number should fail" -> "net.enderturret.patched.exception.TraversalException: /baz/1e0: Expected object to find '1e0' in, found [1,2,3,4]!";
		case "test add with bad number should fail" -> "net.enderturret.patched.exception.TraversalException: /1e0: Expected object to find '1e0' in, found [\"foo\",\"sil\"]!";

		case "test with bad number should fail" -> "net.enderturret.patched.exception.PatchingException: Test failed: /1e0: Expected object to find '1e0' in, found [\"foo\",\"bar\"]!";

		// Imagine using JavaScript. Couldn't be me.
		case "missing value parameter to test - where undef is falsy" -> "net.enderturret.patched.exception.PatchingException: 'value' must not be missing!";

		case "invalid JSON Pointer token" -> "net.enderturret.patched.exception.TraversalException: Path must begin with a slash!";
		case "unrecognized op should fail" -> "net.enderturret.patched.exception.PatchingException: Unknown operation 'spam'";

		default -> switch (error) {
			case "Out of bounds (lower)" -> "net.enderturret.patched.exception.TraversalException: /bar/-1: Attempted to traverse negative index in array (-1)!";
			case "Object operation on array target" -> "net.enderturret.patched.exception.TraversalException: /bar: Expected object to find 'bar' in, found [\"foo\",\"sil\"]!";
			case "test op should fail" -> "net.enderturret.patched.exception.PatchingException: Test {\"bar\":[1,2,5,4]} == [1,2] failed.";
			default -> comment;
			};
		};
	}

	private static void test(String path, BinaryOperator<String> errorMapper, BiPredicate<String, Test> filter) {
		final List<Test> tests;

		{
			final String testsInput = TestUtil.read(path);
			final JsonElement testsElem = JsonParser.parseString(testsInput);

			tests = readTests(testsElem);
		}

		int passed = 0;
		int skipped = 0;

		for (int i = 0; i < tests.size(); i++) {
			final Test test = tests.get(i);

			if (test.disabled || !filter.test(test.comment, test)) {
				skipped++;
				continue;
			}

			if (testSingle(test, errorMapper))
				passed++;
		}

		System.out.printf("%d / %d tests passed %s.\n", passed, tests.size() - skipped,
				skipped > 0 ? "(" + skipped + " skipped)" : "");
	}

	private static boolean testSingle(Test test, BinaryOperator<String> errorMapper) {
		try {
			final JsonElement patched = test.doc.deepCopy();
			final JsonPatch patch = Patches.readPatch(GSON, test.patch);
			patch.patch(patched, PatchContext.newContext().throwOnFailedTest(true));

			TestUtil.sortHierarchy(patched);
			TestUtil.sortHierarchy(test.expected);

			if (test.expected == null) {
				System.err.printf("Test '%s': Expected errors but patch applied successfully?"
						+ "\nExpected: %s\nOutput:\n%s\n",
						test.comment, test.error, GSON.toJson(patched));
			}

			else if (patched.equals(test.expected)) {
				if (PRINT_TEST_SUCCESS)
					System.out.println("Test '" + test.comment + "' passed.");
				return true;
			} else {
				System.err.printf("Test '%s' failed!\n\n"
						+ "%s\n(expected) vs (output)\n%s\n",
						test.comment, GSON.toJson(test.expected), GSON.toJson(patched));
			}
		} catch (Exception e) {
			if (test.error != null) {
				final String error = e.toString();
				if (errorMapper.apply(test.comment, test.error).equals(error)) {
					if (PRINT_TEST_SUCCESS)
						System.out.println("Test '" + test.comment + "' passed.");
					return true;
				} else {
					System.out.printf("Test '%s' expected error: %s"
							+ "\nGot error:\n", test.comment, test.error);
					e.printStackTrace();
				}
			} else {
				System.err.println("Test " + test.comment + " failed with error:");
				e.printStackTrace();
			}
		}

		return false;
	}

	private static List<Test> readTests(JsonElement testsElem) {
		final List<Test> tests = new ArrayList<>();

		for (JsonElement elem : testsElem.getAsJsonArray()) {
			if (!(elem instanceof JsonObject obj)) {
				System.err.println("Tried to parse invalid test \"" + elem + "\"!");
				continue;
			}

			final String comment = obj.has("comment") ? obj.get("comment").getAsString()
					: "no comment";
			final JsonElement doc = obj.get("doc");
			final JsonElement patch = obj.get("patch");

			final JsonElement expected;
			final String error;

			if (obj.has("expected")) {
				expected = obj.get("expected");
				error = null;
			} else if (obj.has("error")) {
				expected = null;
				error = obj.get("error").getAsString();
			} else {
				expected = new JsonPrimitive("No Expected Results Provided");
				error = null;
			}

			final boolean disabled = obj.has("disabled") && obj.get("disabled").getAsBoolean();

			tests.add(new Test(comment, doc, patch, expected, error, disabled));
		}

		return tests;
	}

	private static record Test(String comment, JsonElement doc, JsonElement patch,
			JsonElement expected, String error, boolean disabled) {}
}