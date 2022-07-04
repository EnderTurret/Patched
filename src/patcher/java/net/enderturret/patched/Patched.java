package net.enderturret.patched;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patched.patcher.Patcher;
import net.enderturret.patched.patcher.path.PathDumper;
import net.enderturret.patched.patcher.path.PathPathHandler;
import net.enderturret.patched.patcher.path.PathSourceAdapter;

/**
 * The CLI entry point class.
 * @author EnderTurret
 */
// @VisibleForTesting
// TODO: Improve the usability of this for single file patching.
public class Patched {

	public static void main(String... args) throws IOException {
		final Settings settings = readArgs(args);
		if (settings == null || settings.src == null || settings.output == null || settings.patchSources.isEmpty()) return;

		System.out.println("Patching " + settings.src + " to " + settings.output + " with sources " + settings.patchSources + "...");

		if (!Files.exists(settings.output))
			Files.createDirectory(settings.output);

		final Patcher<Path> patcher = new Patcher<>(new PathPathHandler(Throwable::printStackTrace), new PathDumper(settings.output), Patches.patchGson(true, true).setPrettyPrinting().create());

		final Path src = settings.src.toAbsolutePath();

		patcher.addSource(0, new PathSourceAdapter(settings.src));

		for (Path path : settings.patchSources)
			patcher.addSource(10, new PathSourceAdapter(path));

		try (Stream<Path> stream = Files.walk(src)) {
			stream.forEach(path -> {
				if (!Files.isDirectory(path))
					patcher.read(src.relativize(path.toAbsolutePath()));
			});
		}
	}

	private static Settings readArgs(String[] args) {
		Settings ret = new Settings(null, null, new ArrayList<>(0), PatchContext.newContext());
		int skip = 0;

		for (int i = 0; i < args.length; i++) {
			if (skip > 0) {
				skip--;
				continue;
			}

			final String arg = args[i];

			if ("--help".equals(arg)) {
				System.out.println("Usage: java -jar Patched-cli.jar <args>");
				System.out.println("Accepted Arguments:");
				System.out.println("--src"
						+ "\n        Set the source directory."
						+ "\n--out"
						+ "\n        Set the output directory."
						+ "\n--patches"
						+ "\n        Add a patch source directory"
						+ "\n--patch-sources"
						+ "\n        Add a directory of patch sources."
						+ "\n--extended"
						+ "\n        Enable all patch extensions.");
				return null;
			}

			else if ("--src".equals(arg)) {
				ret = new Settings(checkPath("src", args, i + 1, true), ret.output, ret.patchSources, ret.context);
				skip++;
			}

			else if ("--out".equals(arg)) {
				ret = new Settings(ret.src, checkPath("out", args, i + 1, false), ret.patchSources, ret.context);
				skip++;
			}

			else if ("--patches".equals(arg)) {
				final Path path = checkPath("patches", args, i + 1, true);
				if (path != null)
					ret.patchSources.add(path);
				skip++;
			}

			else if ("--patch-sources".equals(arg)) {
				final Path path = checkPath("patch-sources", args, i + 1, true);
				if (path != null) {
					final Settings settings = ret;
					try (Stream<Path> stream = Files.list(path)) {
						stream.forEach(p -> {
							if (Files.isDirectory(p))
								settings.patchSources.add(p);
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				skip++;
			}

			else if ("--extended".equals(arg))
				ret = new Settings(ret.src, ret.output, ret.patchSources, PatchContext.newContext().sbExtensions(true).patchedExtensions(true));

			else
				System.out.println("Unrecognized argument: " + arg);
		}

		return ret;
	}

	private static Path checkPath(String arg, String[] args, int index, boolean requireExists) {
		if (index >= args.length)
			System.out.println(arg + ": missing file path");
		else {
			final Path path = Paths.get(args[index]);
			if (requireExists && !Files.exists(path))
				System.out.println(arg + ": " + path + " doesn't exist");
			else return path;
		}

		return null;
	}

	private static record Settings(Path src, Path output, List<Path> patchSources, PatchContext context) {
		
	}
}