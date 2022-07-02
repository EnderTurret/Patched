package tests;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;
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
				deleteHierarchy(patcherTest);

			Files.createDirectories(patcherTest);

			final URL url = PatcherTest.class.getResource("/patcher_test");
			final Path inputFiles = Paths.get(url.toURI());

			copyHierarchy(inputFiles, patcherTest);

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

			hierarchyEquals(expected, output);

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

	private static void copyHierarchy(Path src, Path dest) throws IOException {
		final Path absSrc = src.toAbsolutePath();
		Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				dir = absSrc.relativize(dir.toAbsolutePath()); // I don't trust it.
				Files.createDirectories(dest.resolve(dir));
				return super.preVisitDirectory(dir, attrs);
			}
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				final Path orig = file;
				file = absSrc.relativize(file.toAbsolutePath()); // I don't trust it.
				Files.copy(orig, dest.resolve(file), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
				return super.preVisitDirectory(file, attrs);
			}
		});
	}

	static void deleteHierarchy(Path src) throws IOException {
		Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return super.postVisitDirectory(dir, exc);
			}
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return super.preVisitDirectory(file, attrs);
			}
		});
	}

	static boolean hierarchyEquals(Path dir1, Path dir2) throws IOException {
		final Path absSrc = dir1.toAbsolutePath();

		// It's what lambdas crave -- mutable final variables.
		final AtomicBoolean ret = new AtomicBoolean(true);

		Files.walkFileTree(dir1, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				final Path orig = file;
				file = absSrc.relativize(file.toAbsolutePath()); // I don't trust it.

				if (!equal(orig, dir2.resolve(file))) {
					System.out.println("File failed equality check: " + file);
					ret.set(false);
				}

				return super.preVisitDirectory(file, attrs);
			}
		});

		return ret.get();
	}

	private static boolean equal(Path path1, Path path2) {
		try {
			if (Files.size(path1) != Files.size(path2)) return false;

			try (BufferedInputStream bis1 = new BufferedInputStream(Files.newInputStream(path1));
					BufferedInputStream bis2 = new BufferedInputStream(Files.newInputStream(path2))) {
				final byte[] buf1 = new byte[1024];
				final byte[] buf2 = new byte[1024];

				int read = 0;

				while ((read = bis1.read(buf1)) != -1) {
					if (bis2.read(buf2) != read) return false;
					for (int i = 0; i < read; i++)
						if (buf1[i] != buf2[i]) return false;
				}
			}

			return true;
		} catch (IOException e) {
			return false;
		}
	}
}