package tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.enderturret.patched.JsonDocument;
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

	private static final Gson GSON = Patches.patchGson(false, false).setPrettyPrinting().create();
	private static final PatchContext CONTEXT = PatchContext.newContext().throwOnFailedTest(true).throwOnOobAdd(true);

	private static String mapSpecErrors(String comment, String error, int index) {
		return switch (comment) {
		case "4.1. add with missing object" -> "net.enderturret.patched.exception.TraversalException: /a: No such child a!";
		case "A.12.  Adding to a Non-existent Target" -> "net.enderturret.patched.exception.TraversalException: /baz: No such child baz!";
		case "A.9.  Testing a Value: Error" -> "net.enderturret.patched.exception.PatchingException: Test \"qux\" == \"bar\" failed.";
		case "A.15. Comparing Strings and Numbers" -> "net.enderturret.patched.exception.PatchingException: Test 10 == \"10\" failed.";
		default -> comment;
		};
	}

	private static String mapPatchErrors(String comment, String error, int index) {
		return switch (comment) {
		case "Removing nonexistent field" -> "net.enderturret.patched.exception.TraversalException: /baz: No such child baz!";
		case "Removing deep nonexistent path" -> "net.enderturret.patched.exception.TraversalException: /missing1: No such child missing1!";
		case "Removing nonexistent index" -> "net.enderturret.patched.exception.TraversalException: /2: No such child 2!";
		case "test replace with missing parent key should fail" -> "net.enderturret.patched.exception.TraversalException: /foo: No such child foo!";
		case "add item to array at index > length should fail" -> "net.enderturret.patched.exception.TraversalException: /3: No such child 3!";

		case "missing from location to copy" -> "net.enderturret.patched.exception.TraversalException: /bar: No such child bar!";
		case "missing from location to move" -> "net.enderturret.patched.exception.TraversalException: /bar: No such child bar!";
		case "missing from parameter to copy" -> "net.enderturret.patched.exception.PatchingException: 'from' is missing!";
		case "missing from parameter to move" -> "net.enderturret.patched.exception.PatchingException: 'from' is missing!";
		case "missing 'value' parameter to add" -> "net.enderturret.patched.exception.PatchingException: 'value' is missing!";
		case "missing 'value' parameter to replace" -> "net.enderturret.patched.exception.PatchingException: 'value' is missing!";
		case "missing 'value' parameter to test" -> "net.enderturret.patched.exception.PatchingException: 'value' is missing!";
		case "missing 'path' parameter" -> "net.enderturret.patched.exception.PatchingException: 'path' is missing!";
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
		case "missing value parameter to test - where undef is falsy" -> "net.enderturret.patched.exception.PatchingException: 'value' is missing!";

		case "invalid JSON Pointer token" -> "net.enderturret.patched.exception.TraversalException: Path must begin with a slash!";
		case "unrecognized op should fail" -> "net.enderturret.patched.exception.PatchingException: Unknown operation 'spam'";

		case "test with bad array number that has leading zeros" -> switch (index) {
			case 87 -> "net.enderturret.patched.exception.PatchingException: Test failed: /00: Expected object to find '00' in, found [\"foo\",\"bar\"]!";
			case 88 -> "net.enderturret.patched.exception.PatchingException: Test failed: /01: Expected object to find '01' in, found [\"foo\",\"bar\"]!";
			default -> comment;
		};

		default -> switch (error) {
			case "Out of bounds (upper)" -> "net.enderturret.patched.exception.TraversalException: /bar/8: No such child 8!";
			case "Out of bounds (lower)" -> "net.enderturret.patched.exception.TraversalException: /bar/-1: Attempted to traverse negative index in array (-1)!";
			case "Object operation on array target" -> "net.enderturret.patched.exception.TraversalException: /bar: Expected object to find 'bar' in, found [\"foo\",\"sil\"]!";
			case "test op should fail" -> "net.enderturret.patched.exception.PatchingException: Test {\"bar\":[1,2,5,4]} == [1,2] failed.";
			default -> comment;
			};
		};
	}

	@TestFactory
	Stream<DynamicTest> testSpecTests() {
		return test("/tests/json-patch-tests/spec_tests.json", "RFC", JsonPatchTests::mapSpecErrors, (comment, test) -> true);
	}

	@TestFactory
	Stream<DynamicTest> testPatchTests() {
		return test("/tests/json-patch-tests/tests.json", "json-patch", JsonPatchTests::mapPatchErrors, (comment, test) -> true);
	}

	private static Stream<DynamicTest> test(String path, String name, ErrorMapper errorMapper, BiPredicate<String, Test> filter) {
		final List<Test> tests;

		{
			final String testsInput = TestUtil.read(path);
			final JsonElement testsElem = JsonParser.parseString(testsInput);

			tests = readTests(testsElem);
		}

		return tests.stream()
				.map(test -> DynamicTest.dynamicTest(test.comment(), () -> {
					assumeFalse(test.disabled || !filter.test(test.comment, test), "Test is disabled");

					final JsonElement _patched = test.doc.deepCopy();
					final JsonDocument doc = new JsonDocument(_patched);

					if (test.expected == null) {
						final PatchingException e = assertThrows(PatchingException.class, () -> {
							final JsonPatch patch = Patches.readPatch(GSON, test.patch);
							patch.patch(doc, CONTEXT);
						}, () -> "Resultant document: " + doc.getRoot());
						assertEquals(errorMapper.map(test.comment, test.error, test.index), e.toString());
					} else {
						final JsonPatch patch = assertDoesNotThrow(() -> Patches.readPatch(GSON, test.patch));
						assertDoesNotThrow(() -> patch.patch(doc, CONTEXT));
						assertEquals(test.expected, doc.getRoot());
					}
				}));
	}

	private static List<Test> readTests(JsonElement testsElem) {
		final List<Test> tests = new ArrayList<>();

		int index = 0;
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

			tests.add(new Test(comment, doc, patch, expected, error, disabled, index++));
		}

		return tests;
	}

	private static record Test(String comment, JsonElement doc, JsonElement patch,
			JsonElement expected, String error, boolean disabled, int index) {}

	private static interface ErrorMapper {
		public String map(String comment, String error, int index);
	}
}