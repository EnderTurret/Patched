package net.enderturret.patched;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;
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
public class Patched {

	public static void main(String... args) throws IOException {
		final Settings settings = readArgs(args);
		if (settings == null || settings.src == null || settings.output == null || settings.patchSources.isEmpty())
			return;

		System.out.println("Patching " + settings.src + " to " + settings.output + " with patches " + settings.patchSources + "...");

		if (settings.multi)
			multiPatch(settings);
		else
			singlePatch(settings);
	}

	private static void multiPatch(Settings settings) throws IOException {
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

	private static void singlePatch(Settings settings) throws IOException {
		final JsonElement source = JsonParser.parseString(Files.readString(settings.src()));
		final JsonDocument doc = new JsonDocument(source);

		final Gson gson = Patches.patchGson(settings.context)
				.setPrettyPrinting()
				.create();

		record Patch(Path src, JsonPatch patch) {}

		final List<Patch> patches = settings.patchSources.stream()
				.map(path -> {
					try {
						return new Patch(path, Patches.readPatch(gson, Files.readString(path)));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				})
				.toList();

		for (Patch patch : patches) {
			try {
				patch.patch.patch(doc, settings.context);
			} catch (PatchingException e) {
				System.out.println("Patch '" + patch.src + "' could not be applied:");
				e.printStackTrace(System.out);
			}
		}

		final String out = gson.toJson(doc.getRoot());

		try (BufferedWriter bw = Files.newBufferedWriter(settings.output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			bw.write(out);
		}
	}

	private static final String HELP_TEXT = "Usage: java -jar Patched-cli.jar <args>"
			+ "\nEx: java -jar Patched-cli.jar --source myFile.json --patch myFile.json.patch --output myFile-patched.json"
			+ "\nEx 2: java -jar Patched-cli.jar --multi --source \"My Files\" --patches Patches --output \"Patched Files\""
			+ "\nAccepted Arguments:"
			+ "\n--help"
			+ "\n        Display this text and exit."
			+ "\n--multi"
			+ "\n        Enable multi-source mode."
			+ "\n        This mode lets you patch"
			+ "\n        multiple files at once."
			+ "\n--source <location>"
			+ "\n        Set the source file, or"
			+ "\n        if --multi is set, the"
			+ "\n        source directory tree."
			+ "\n--output <location>"
			+ "\n        Set the output location."
			+ "\n--patch <patch>"
			+ "\n        Add a single patch."
			+ "\n        Only valid if --multi is not set."
			+ "\n--patches <patches>"
			+ "\n        Add a patch directory."
			+ "\n--patch-sources <patch sources>"
			+ "\n        Add a directory containing"
			+ "\n        patch directories."
			+ "\n        Only valid if --multi is set."
			+ "\n--extended"
			+ "\n        Enable all patch extensions.";

	private static Settings readArgs(String[] args) {
		if (args.length == 0) {
			System.out.println(HELP_TEXT);
			return null;
		}

		Settings ret = new Settings(null, null, new ArrayList<>(0), false, PatchContext.newContext());
		int skip = 0;

		for (int i = 0; i < args.length; i++) {
			if (skip > 0) {
				skip--;
				continue;
			}

			final String arg = args[i];

			if ("--help".equals(arg)) {
				System.out.println(HELP_TEXT);
				return null;
			}

			else if ("--multi".equals(arg))
				ret = new Settings(ret.src, ret.output, ret.patchSources, true, ret.context);

			else if ("--source".equals(arg)) {
				final Path src = checkPath("source", args, i + 1, true);

				if (ret.multi && Files.isRegularFile(src))
					System.out.println("source: File \"" + src + "\" is not a directory.");
				else if (!ret.multi && Files.isDirectory(src))
					System.out.println("source: Only a single file is supported. Pass --multi to enable multiple source files.");
				else
					ret = new Settings(src, ret.output, ret.patchSources, ret.multi, ret.context);

				skip++;
			}

			else if ("--output".equals(arg)) {
				ret = new Settings(ret.src, checkPath("out", args, i + 1, false), ret.patchSources, ret.multi, ret.context);
				skip++;
			}

			else if ("--patch".equals(arg) || "--patches".equals(arg)) {
				final Path path = checkPath(arg.substring("--".length()), args, i + 1, true);
				if (path != null) {
					if (ret.multi || Files.isRegularFile(path))
						ret.patchSources.add(path);
					else if (Files.isDirectory(path)) {
						try (Stream<Path> stream = Files.list(path)) {
							final Settings settings = ret;
							stream.forEach(p -> {
								if (Files.isDirectory(p))
									settings.patchSources.add(p);
							});
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				skip++;
			}

			else if ("--patch-sources".equals(arg)) {
				if (!ret.multi) {
					System.out.println("patch-sources: Multi-mode must be enabled to use this.");
					continue;
				}

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
				ret = new Settings(ret.src, ret.output, ret.patchSources, ret.multi, PatchContext.newContext().testExtensions(true).patchedExtensions(true));

			else
				System.out.println("Unrecognized argument: " + arg);
		}

		return ret;
	}

	private static Path checkPath(String arg, String[] args, int index, boolean requireExists) {
		if (index >= args.length)
			System.out.println(arg + ": Missing file path.");
		else {
			final Path path = Paths.get(args[index]);
			if (requireExists && !Files.exists(path))
				System.out.println(arg + ": \"" + path + "\" doesn't exist.");
			else return path;
		}

		return null;
	}

	private static record Settings(Path src, Path output, List<Path> patchSources, boolean multi, PatchContext context) {
		
	}
}