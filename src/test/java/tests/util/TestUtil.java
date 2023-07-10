package tests.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tests.PatchingTests;

/**
 * Various utilities used across test classes.
 * @author EnderTurret
 */
public final class TestUtil {

	private TestUtil() {}

	/**
	 * <p>Reads the resource at the given path and returns its contents as a {@link String}.</p>
	 * <p>In case it isn't obvious, this reads a file from within either the compiled jar file or the src/test/resources directory.</p>
	 * @param path The path that the file would be located at in the jar.
	 * @return The contents of the resource.
	 */
	public static String read(String path) {
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
}