package net.enderturret.patched.patcher;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.Patches;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;

public class Patcher<P> {

	private static final Set<String> OBVIOUS_BINARY_EXTENSIONS = Set.of(
			"png", "jpg", "jpeg", // Images
			"zip", "jar", "gz", // Archives
			"ogg", "wav", "mp3", // Audio
			"dat" // Misc
			);

	private final NavigableSet<Source<P>> sources = new TreeSet<>();

	private final PathHandler<P> pathHandler;

	private final Dumper<P> dumper;

	private final Gson patchGson;

	public Patcher(PathHandler<P> pathHandler, Dumper<P> dumper, Gson patchGson) {
		this.pathHandler = Objects.requireNonNull(pathHandler, "pathHandler cannot be null");
		this.dumper = dumper;
		this.patchGson = Objects.requireNonNull(patchGson, "patchGson cannot be null");
	}

	public void addSource(int priority, SourceAdapter<P> adapter) {
		sources.add(new Source<>(adapter, priority));
	}

	public NavigableSet<Source<P>> sources() {
		return Collections.unmodifiableNavigableSet(sources);
	}

	private List<byte[]> findBytes(P path, boolean findAll) {
		final List<byte[]> found = findAll ? new ArrayList<>() : List.of();

		for (Iterator<Source<P>> it = sources.descendingIterator(); it.hasNext(); ) {
			final Source<P> src = it.next();
			final Boolean exists = src.exists(path);

			if (exists != null && !exists)
				continue;

			final byte[] bytes = src.read(path);
			if (bytes == null) continue;

			if (findAll) found.add(bytes);
			else return List.of(bytes);
		}

		return found;
	}

	public byte[] read(P path) {
		final String extension = pathHandler.extension(path);

		byte[] bytes;

		if (OBVIOUS_BINARY_EXTENSIONS.contains(extension)) {
			final List<byte[]> found = findBytes(path, false);

			if (found.isEmpty())
				throw new IllegalArgumentException("File not found: " + path.toString());

			bytes = found.get(0);
		} else
			bytes = readPatched(path);

		if (dumper != null && bytes != null)
			dumper.dump(path, bytes);

		return bytes;
	}

	private byte[] readPatched(P path) {
		final List<byte[]> found = findBytes(path, false);
		if (found.isEmpty())
			throw new IllegalArgumentException("File not found: " + path.toString());

		final byte[] bytes = found.get(0);
		JsonDocument ret;

		try {
			ret = new JsonDocument(JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)));
		} catch (JsonParseException e) {
			// Immediately nope out. Either this isn't a Json file, in which case it will be read properly after;
			// or it's a malformed json file, in which case we can't patch it anyway.
			return bytes;
		}

		final P patchPath = pathHandler.resolvePatch(path);
		final List<byte[]> patches = findBytes(patchPath, true);

		final PatchContext context = PatchContext.newContext().testExtensions(true).patchedExtensions(true);

		for (byte[] patchBytes : patches) {
			final String json = new String(patchBytes, StandardCharsets.UTF_8);

			try {
				final JsonPatch patch = Patches.readPatch(patchGson, json);
				patch.patch(ret, context);
			} catch (Exception e) {
				pathHandler.onError(e);
				continue;
			}
		}

		final String retStr = patchGson.toJson(ret.getRoot());

		return retStr.getBytes(StandardCharsets.UTF_8);
	}
}