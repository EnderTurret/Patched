package tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.Patches;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;

import tests.util.SimpleFileAccess;
import tests.util.SimpleTestEvaluator;
import tests.util.TestUtil;

/**
 * <p>Tests all of the patching-related functionality. This is where most of the testing occurs.</p>
 * <p>See {@link JsonPatchTests} for similar testing.</p>
 * @author EnderTurret
 */
public final class PatchingTests {

	private static final Map<PatchContext, Gson> GSONS = new HashMap<>(4);

	static final Gson GSON = Patches.patchGson(true, true).disableHtmlEscaping().setPrettyPrinting().create();

	static {
		GSONS.put(PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true), GSON);
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
						.testEvaluator(o.has("customTests") ? new SimpleTestEvaluator(o.get("customTests")) : null)
						.fileAccess(o.has("include") ? new SimpleFileAccess() : null);
			} else runtime = PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true);

			return new PatchContext[] { input, runtime };
		}

		return new PatchContext[] {
				PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true),
				PatchContext.newContext().testExtensions(true).patchedExtensions(true).throwOnOobAdd(true)
		};
	}

	private static Test readTest(String name, boolean doOutputTest) {
		final String root = "/tests/" + name;
		final String input = TestUtil.read(root + "/input.json");
		final String patchSrc = TestUtil.read(root + "/input.json.patch");

		final PatchContext[] contexts = readConfig(root);

		return new Test(root,
				assertDoesNotThrow(() -> JsonParser.parseString(input), "Invalid input Json"),
				patchSrc, contexts);
	}

	@TestFactory
	Stream<DynamicTest> testPatchedTests() {
		final String json = TestUtil.read("/tests/tests.json");
		final JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

		final List<TestDefinition> normal = new ArrayList<>();
		final List<ThrowingTestDefinition> throwing = new ArrayList<>();

		{
			final JsonArray normalTests = obj.getAsJsonArray("tests");
			for (JsonElement elem : normalTests)
				if (elem.isJsonObject())
					normal.add(new TestDefinition(
							elem.getAsJsonObject().get("path").getAsString(),
							elem.getAsJsonObject().get("doOutputTest").getAsBoolean()));
				else
					normal.add(new TestDefinition(elem.getAsString(), true));
		}

		{
			final JsonObject traversalErrorTests = obj.getAsJsonObject("traversalErrorTests");

			for (Map.Entry<String, JsonElement> entry : traversalErrorTests.entrySet())
				throwing.add(new ThrowingTestDefinition(entry.getKey(), TraversalException.class, entry.getValue().getAsString()));

			final JsonObject patchingErrorTests = obj.getAsJsonObject("patchingErrorTests");

			for (Map.Entry<String, JsonElement> entry : patchingErrorTests.entrySet())
				throwing.add(new ThrowingTestDefinition(entry.getKey(), PatchingException.class, entry.getValue().getAsString()));
		}

		normal.sort(Comparator.comparing(TestDefinition::path));
		throwing.sort(Comparator.comparing(ThrowingTestDefinition::path));

		return Stream.concat(
				normal.stream().map(def -> DynamicTest.dynamicTest(def.path, () -> test(def))),
				throwing.stream().map(def -> DynamicTest.dynamicTest(def.path, () -> testThrows(def))));
	}

	private static void testThrows(ThrowingTestDefinition test) {
		final Test input = readTest(test.path, true);
		final JsonDocument doc = new JsonDocument(input.input());

		final Gson gson = GSONS.computeIfAbsent(input.contexts[0],
				c -> Patches.patchGson(c).setPrettyPrinting().create());

		final PatchingException e = assertThrows(test.type, () -> {
			final JsonPatch patch = Patches.readPatch(gson, input.patchSrc);
			patch.patch(doc, input.contexts()[1]);
		});

		assertEquals(test.message, e.getMessage());
	}

	private static void test(TestDefinition test) {
		String expectedAudit;
		try {
			expectedAudit = TestUtil.read("/tests/" + test.path + "/audit.json");
			//if (!expectedAudit.contains("//"))
			//System.err.println("Note: " + name + "'s audit has no notes.");
		} catch (Exception e) {
			//System.err.println("Note: " + name + " is missing an audit.json.");
			expectedAudit = null;
		}

		final Test input = readTest(test.path, test.doOutputTest);
		if (input == null) return;

		final Gson gson = GSONS.computeIfAbsent(input.contexts[0],
				c -> Patches.patchGson(c).setPrettyPrinting().create());

		final JsonPatch patch = assertDoesNotThrow(() -> Patches.readPatch(gson, input.patchSrc), "Invalid input patch");

		if (test.doOutputTest)
			assertEquals(input.patchSrc, GSON.toJson(patch));

		String expected = TestUtil.read(input.path() + "/result.json");
		final JsonElement expectedElem = JsonParser.parseString(expected);

		// -----

		{
			final String auditBaseTest = new PatchAudit(test.path).toString(expectedElem);
			assertEquals(expected, auditBaseTest);
		}

		// -----

		final PatchAudit audit = new PatchAudit(test.path);
		final JsonDocument doc = new JsonDocument(input.input());

		patch.patch(doc, input.contexts()[1].audit(audit));

		assertEquals(expectedElem, doc.getRoot());

		if (expectedAudit != null) {
			final String auditOut = audit.toString(doc.getRoot());
			assertEquals(expectedAudit, auditOut);
		}
	}

	private static record TestDefinition(String path, boolean doOutputTest) {}

	private static record ThrowingTestDefinition(String path, Class<? extends PatchingException> type, String message) {}

	private static record Test(String path, JsonElement input, String patchSrc, PatchContext[] contexts) {}
}