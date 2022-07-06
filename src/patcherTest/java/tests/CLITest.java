package tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.enderturret.patched.Patched;

/**
 * Tests the {@linkplain Patched CLI}.
 * @author EnderTurret
 */
public final class CLITest {

	private static final Path BUILD = Paths.get("build").toAbsolutePath();

	private static int testCount = 0;
	private static int passed = 0;

	public static void main(String... args) {
		test("help");
		test("no_args");
		test("multi");
		test("single");

		System.out.printf("%d / %d tests passed.\n", passed, testCount);
	}

	private static void test(String name) {
		testCount++;

		final Test test = read(name);

		if (!runSteps(test)) return;

		final PrintStream oldOut = System.out;

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		System.setOut(new PrintStream(baos));

		try {
			Patched.main(test.args.toArray(String[]::new));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		System.setOut(oldOut);

		final String exOutput = test.output.strip();
		final String output = baos.toString(StandardCharsets.UTF_8).strip();

		if (!exOutput.equals(output)) {
			System.out.println("Test " + name + " failed!\n");
			System.out.println(exOutput);
			System.out.println("\n(expected) vs (output)\n");
			System.out.println(output);
			return;
		}

		if (test.outputPath != null && test.expected != null) {
			if (!PatcherTestUtil.hierarchyEquals(test.outputPath, test.expected)) {
				System.out.println("Test " + name + " failed! See above for details.");
				return;
			}
		}

		passed++;
	}

	private static boolean runSteps(Test test) {
		if (test.steps == null) return true;

		for (JsonObject step : test.steps) {
			final String type = step.get("type").getAsString();

			try {
				switch (type) {
					case "delete" -> {
						final Path target = Paths.get(step.get("target").getAsString()).toAbsolutePath();
						if (target.startsWith(BUILD))
							PatcherTestUtil.deleteHierarchy(target);
						else
							System.out.println("Refusing to delete file \"" + target + "\" outside build directory.");
					}
					case "create" -> {
						final Path target = Paths.get(step.get("target").getAsString()).toAbsolutePath();
						Files.createDirectories(target);
					}
					case "copy" -> {
						final Path source = resolve(step.get("source").getAsString());
						final Path target = Paths.get(step.get("target").getAsString());
						PatcherTestUtil.copyHierarchy(source, target);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	private static Test read(String name) {
		final String root = "/cli_tests/" + name;

		final JsonElement testJson = JsonParser.parseString(PatcherTestUtil.read(root + "/test.json"));
		final JsonObject test = testJson.getAsJsonObject();
		final String output = PatcherTestUtil.read(root + "/output.txt");

		final List<String> args = new ArrayList<>();
		for (JsonElement elem : test.get("args").getAsJsonArray())
			args.add(elem.getAsString());

		final List<JsonObject> steps = test.has("steps") ? new ArrayList<>() : null;
		if (steps != null)
			for (JsonElement elem : test.get("steps").getAsJsonArray())
				steps.add(elem.getAsJsonObject());

		final Path outputPath = test.has("output") ? resolve(test.get("output").getAsString()) : null;
		final Path expected = test.has("expected") ? resolve(test.get("expected").getAsString()) : null;

		return new Test(name, args, output, steps, outputPath, expected);
	}

	private static Path resolve(String path) {
		if (path.startsWith("$"))
			try {
				final URL resource = CLITest.class.getResource("/cli_tests/" + path.substring(1));
				if (resource == null) throw new RuntimeException("File '/cli_tests/" + path.substring(1) + "' doesn't exist.");
				return Paths.get(resource.toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		return Paths.get(path);
	}

	private static record Test(String name, List<String> args, String output, List<JsonObject> steps, Path outputPath, Path expected) {}
}