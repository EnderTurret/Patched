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

	/*private static void oldMain(Settings settings) throws IOException {
		try (Stream<Path> stream = Files.walk(settings.src)) {
			final Path fnRoot = settings.src.toAbsolutePath();
			stream.filter(p -> Files.isRegularFile(p)).forEach(p -> {
				p = p.toAbsolutePath();
				final String filename = p.getFileName().toString();
				final Path relative = fnRoot.relativize(p);

				try {
					final Path out = settings.output.resolve(relative);
					Files.createDirectories(out.getParent());
					Files.copy(p, out, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

		final Map<String,JsonElement> patched = new HashMap<>();

		for (Path root : settings.patchSources) {
			root = root.toAbsolutePath();
			try (Stream<Path> stream = Files.walk(root)) {
				final Path fnRoot = root;
				stream.filter(p -> Files.isRegularFile(p)).forEach(p -> {
					p = p.toAbsolutePath();
					final String filename = p.getFileName().toString();
					final Path relative = fnRoot.relativize(p);
					if (filename.endsWith(".patch")) {
						String relStr = relative.toString();
						relStr = relStr.substring(0, relStr.length() - ".patch".length());

						final JsonElement elem = patched.computeIfAbsent(relStr, path -> {
							try {
								return JsonParser.parseString(Files.readString(settings.src.resolve(path)));
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						});

						try {
							Patches.patch(elem, settings.context, p);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						try {
							final Path out = settings.output.resolve(relative);
							Files.createDirectories(out.getParent());
							Files.copy(p, out, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}

		final Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

		for (Map.Entry<String,JsonElement> entry : patched.entrySet()) {
			final String json = gson.toJson(entry.getValue());
			final Path out = settings.output.resolve(entry.getKey());
			Files.createDirectories(out.getParent());
			Files.writeString(out, json, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		}
	}*/

	private static Settings readArgs(String[] args) {
		Settings ret = new Settings(null, null, new ArrayList<>(0), PatchContext.newContext(), false);
		int skip = 0;
		for (int i = 0; i < args.length; i++) {
			if (skip > 0) {
				skip--;
				continue;
			}

			final String arg = args[i];

			if ("--help".equals(arg)) {
				System.out.println("Usage: java -jar Patched-1.0.0.jar <args>");
				return null;
			}
			else if ("--src".equals(arg)) {
				ret = new Settings(checkPath("src", args, i + 1, true), ret.output, ret.patchSources, ret.context, ret.recursive);
				skip++;
			}
			else if ("--out".equals(arg)) {
				ret = new Settings(ret.src, checkPath("out", args, i + 1, false), ret.patchSources, ret.context, ret.recursive);
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
			else if ("--recursive".equals(arg))
				ret = new Settings(ret.src, ret.output, ret.patchSources, ret.context, true);
			else if ("--extended".equals(arg))
				ret = new Settings(ret.src, ret.output, ret.patchSources, PatchContext.newContext().sbExtensions(true).patchedExtensions(true), true);
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

	private static record Settings(Path src, Path output, List<Path> patchSources, PatchContext context, boolean recursive) {
		
	}
}