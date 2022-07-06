package tests;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Various utilities used across test classes.
 * @author EnderTurret
 */
public final class PatcherTestUtil {

	private PatcherTestUtil() {}

	/**
	 * <p>Reads the resource at the given path and returns its contents as a {@link String}.</p>
	 * <p>In case it isn't obvious, this reads a file from within either the compiled jar file or the src/test/resources directory.</p>
	 * @param path The path that the file would be located at in the jar.
	 * @return The contents of the resource.
	 */
	static String read(String path) {
		try (InputStream is = PatchingTests.class.getResourceAsStream(path);
				InputStreamReader isr = (is == null ? null : new InputStreamReader(is));
				BufferedReader br = (isr == null ? null : new BufferedReader(isr))) {
			if (br == null)
				throw new FileNotFoundException(path);

			final StringBuilder sb = new StringBuilder();

			String line;
			while ((line = br.readLine()) != null) {
				if (!sb.isEmpty())
					sb.append("\n");
				sb.append(line);
			}

			return sb.toString();
		} catch (IOException e) {
			throw new UncheckedIOException("Exception reading " + path + ":", e);
		}
	}

	static void copyHierarchy(Path src, Path dest) throws IOException {
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
		if (!Files.exists(src)) return;

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

	static boolean hierarchyEquals(Path dir1, Path dir2) {
		final Path absSrc = dir1.toAbsolutePath();

		// It's what lambdas crave -- mutable final variables.
		final AtomicBoolean ret = new AtomicBoolean(true);

		try {
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
		} catch (IOException e) {
			ret.set(false);
		}

		return ret.get();
	}

	static boolean equal(Path path1, Path path2) {
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