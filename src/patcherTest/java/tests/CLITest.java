package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.enderturret.patched.Patched;

/**
 * Tests the {@linkplain Patched CLI}.
 * @author EnderTurret
 */
public final class CLITest {

	public static void main(String... args) {
		try {
			Patched.main("--help");
			final Path patcherTest = Paths.get("build", "patcher_test");

			if (Files.exists(patcherTest)) {
				PatcherTest.deleteHierarchy(patcherTest.resolve("output"));

				Patched.main("--src", "build/patcher_test/src",
						"--patch-sources", "build/patcher_test/mods",
						"--out", "build/patcher_test/output",
						"--extended");

				/*PatcherTest.hierarchyEquals(patcherTest.resolve("expected"),
						patcherTest.resolve("output"));*/
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}