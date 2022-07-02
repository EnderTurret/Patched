package tests;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.enderturret.patched.Patches;
import net.enderturret.patched.diff.PatchGenerator;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;

/**
 * Tests the {@link PatchGenerator}.
 * @author EnderTurret
 */
public class DiffTests {

	private static boolean printSuccess = false;
	private static int testCount = 0;
	private static int passed = 0;

	private static final PatchContext CONTEXT = PatchContext.newContext().sbExtensions(true);
	private static final Gson GSON = Patches.patchGson(true, false).setPrettyPrinting().disableHtmlEscaping().create();

	public static void main(String... args) {
		test("array_add");
		test("array_multi_op");
		//test("array_remove");
		test("array_replace");

		test("object_add");
		test("object_remove");
		test("object_replace");

		System.out.printf("%d / %d tests passed.\n", passed, testCount);
	}

	private static Test readTest(String name) {
		testCount++;

		final String root = "/tests/diff/" + name;
		final String orig = TestUtil.read(root + "/orig.json");
		final String target = TestUtil.read(root + "/target.json");
		final String expectedPatch = TestUtil.read(root + "/orig.json.patch");

		final JsonElement origElem;

		try {
			origElem = JsonParser.parseString(orig);
		} catch (JsonSyntaxException e) {
			System.err.println("[" + name + "]: Failed to parse original json: " + e.getMessage());
			return null;
		}

		final JsonElement targetElem;

		try {
			targetElem = JsonParser.parseString(target);
		} catch (JsonSyntaxException e) {
			System.err.println("[" + name + "]: Failed to parse patch:");
			e.printStackTrace();
			return null;
		}

		return new Test(root, origElem, targetElem, expectedPatch);
	}

	private static void test(String name, boolean insertExactTests) {
		try {
			final Test input = readTest(name);
			if (input == null) return;

			final JsonPatch output = PatchGenerator.diff(input.orig, input.target, insertExactTests, false);
			final String outputStr = GSON.toJson(output);

			final JsonElement repatched = input.orig().deepCopy();

			output.patch(repatched, CONTEXT);

			TestUtil.sortHierarchy(repatched);

			if (!input.expectedPatch.equals(outputStr)) {
				System.out.println("Test " + name + " failed!\n");
				System.out.println(input.expectedPatch);
				System.out.println("\n(expected) vs (output)\n");
				System.out.println(outputStr);
			}

			else if (!input.target.equals(repatched)) {
				System.out.println("Test " + name + " failed re-patching!\n");
				System.out.println(GSON.toJson(input.target));
				System.out.println("\n(expected) vs (output)\n");
				System.out.println(GSON.toJson(repatched));
			}

			else {
				if (printSuccess)
					System.out.println("Test " + name + " passed!");
				passed++;
			}
		} catch (Exception e) {
			System.err.println("Exception processing test " + name + ":");
			e.printStackTrace();
		}
	}

	private static void test(String name) {
		test(name, false);
	}

	private static record Test(String path, JsonElement orig, JsonElement target, String expectedPatch) {}
}