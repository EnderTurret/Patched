package tests;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import net.enderturret.patched.Patches;
import net.enderturret.patched.patcher.Patcher;
import net.enderturret.patched.patcher.path.PathDumper;
import net.enderturret.patched.patcher.path.PathPathHandler;
import net.enderturret.patched.patcher.path.PathSourceAdapter;

/**
 * Tests the {@link Patcher} framework.
 * @author EnderTurret
 */
public final class PatcherTest {

	public static void main(String... args) {
		try {
			final Path patcherTest = Paths.get("build", "patcher_test");
			final Path output = patcherTest.resolve("output");
			final Path mods = patcherTest.resolve("mods");
			final Path src = patcherTest.resolve("src").toAbsolutePath();
			final Path expected = patcherTest.resolve("expected");

			if (Files.exists(patcherTest))
				PatcherTestUtil.deleteHierarchy(patcherTest);

			Files.createDirectories(patcherTest);

			final URL url = PatcherTest.class.getResource("/patcher_test");
			final Path inputFiles = Paths.get(url.toURI());

			PatcherTestUtil.copyHierarchy(inputFiles, patcherTest);

			final Patcher<Path> patcher = new Patcher<>(new PathPathHandler(Throwable::printStackTrace), new PathDumper(output), Patches.patchGson(true, true).setPrettyPrinting().create());

			patcher.addSource(0, new PathSourceAdapter(src));
			patcher.addSource(1, new PathSourceAdapter(mods.resolve("DataBreaker")));
			patcher.addSource(2, new PathSourceAdapter(mods.resolve("More Data")));
			patcher.addSource(1, new PathSourceAdapter(mods.resolve("Ultimate Meme Mod")));
			patcher.addSource(1, new PathSourceAdapter(mods.resolve("Video Expansion")));

			//System.out.println("Sources: " + patcher.sources().toString());
			patcher.sources().toString();

			try (Stream<Path> stream = Files.walk(src)) {
				stream.forEach(path -> {
					if (!Files.isDirectory(path))
						patcher.read(src.relativize(path.toAbsolutePath()));
				});
			}

			PatcherTestUtil.hierarchyEquals(expected, output);

			try {
				patcher.read(Paths.get("memes/pixel.png"));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

			try {
				patcher.read(Paths.get("something nonexistent"));
			} catch (IllegalArgumentException e) {
				
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}