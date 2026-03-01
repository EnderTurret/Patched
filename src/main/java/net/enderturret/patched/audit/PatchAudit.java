package net.enderturret.patched.audit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.patch.FindPatch;
import net.enderturret.patched.patch.context.ConfigurablePatchContext;
import net.enderturret.patched.patch.context.ElementContext;

/**
 * Tracks changes that patches make to a file, which can be later used for debugging or other uses.
 * @author EnderTurret
 * @see ConfigurablePatchContext#audit(PatchAudit)
 * @since 1.2.0
 */
public final class PatchAudit {

	private final Map<String, String> records = new HashMap<>();

	private final Map<String, List<RemovalRecord>> removals = new HashMap<>();

	private String patchPath;
	@Nullable
	private String pathPrefix;
	private String pathKey;

	/**
	 * Constructs a new {@code PatchAudit}.
	 * @param patchPath A string representing the path to the patch being applied. This is used for informational purposes.
	 * @since 1.2.0
	 */
	public PatchAudit(String patchPath) {
		this.patchPath = patchPath;
	}

	private void record(String path, String comment) {
		if (pathPrefix != null && !path.startsWith("^"))
			path = pathPrefix + "/" + pathKey + path;

		if (path.startsWith("^"))
			path = path.substring(1);

		records.put(path, comment);
	}

	/**
	 * Records an {@code add} operation into this audit's tracked changes.
	 * @param path The path up to and including the name of the added element.
	 * @param added The added element.
	 */
	public void recordAdd(String path, ElementContext added) {
		record(fixPath(path, added), "added by " + patchPath);
	}

	/**
	 * Records a {@code copy} operation into this audit's tracked changes.
	 * @param path The path up to and including the name of the cloned element.
	 * @param from The path to the original element.
	 * @param copied The cloned element.
	 */
	public void recordCopy(String path, String from, ElementContext copied) {
		record(fixPath(path, copied),
				"copied from " + (from.isEmpty() && pathPrefix != null ? pathPrefix + "/" + pathKey : from) + " by " + patchPath);
	}

	/**
	 * Records a {@code move} operation into this audit's tracked changes.
	 * @param path The path up to and including the name of the destination element.
	 * @param from The path to the source element.
	 * @param moved The moved element.
	 */
	public void recordMove(String path, String from, ElementContext moved) {
		record(fixPath(path, moved),
				"moved from " + (from.isEmpty() && pathPrefix != null ? pathPrefix + "/" + pathKey : from) + " by " + patchPath);
	}

	/**
	 * Records a {@code remove} operation into this audit's tracked changes.
	 * @param path The path up to and including the name of the removed element.
	 * @param value The removed element.
	 */
	public void recordRemove(String path, JsonElement value) {
		final int slash = path.lastIndexOf('/');
		String leading = slash == -1 ? "" : path.substring(0, slash);
		String last = slash == -1 ? "" : path.substring(slash + 1);

		if (pathPrefix != null)
			leading = pathPrefix + leading;

		removals.computeIfAbsent(leading, k -> new ArrayList<>(1))
		.add(new RemovalRecord((pathKey != null ? pathKey : "") + last, value, patchPath));
	}

	/**
	 * Records a {@code replace} operation into this audit's tracked changes.
	 * @param path The path up to and including the name of the replaced element.
	 */
	public void recordReplace(String path) {
		record(path, "replaced by " + patchPath);
	}

	/**
	 * Begins a path prefix -- something to prefix every following record with.
	 * This is useful in situations where you're delegating patches, such as in the {@linkplain FindPatch find patch}.
	 * @param pathPrefix The path prefix. This is prepended to each following path.
	 * @param pathKey The key prefix. This is prepended to each following key (the last path element). This mainly matters in {@link #recordRemove(String, JsonElement)}.
	 * @since 1.2.0
	 */
	public void beginPrefix(String pathPrefix, String pathKey) {
		this.pathPrefix = pathPrefix;
		this.pathKey = pathKey;
	}

	/**
	 * Marks the end of a {@linkplain #beginPrefix(String, String) path prefix}.
	 * Following this point, new records will not be prefixed with anything.
	 * @since 1.2.0
	 */
	public void endPrefix() {
		pathPrefix = null;
		pathKey = null;
	}

	/**
	 * Correctly resolves paths that reference nonexistent parts of an array, such as out-of-bounds indices and end references ({@code -}).
	 * @param name The path to fix.
	 * @param context The element.
	 * @return The fixed path.
	 * @since 1.2.0
	 */
	private static String fixPath(String name, ElementContext context) {
		if (!name.contains("/")) return name;
		final int slash = name.lastIndexOf('/');
		final String leading = name.substring(0, slash + 1);
		final String last = name.substring(slash + 1);

		if (context.parent() instanceof JsonArray a) {
			if ("-".equals(last))
				return leading + Integer.toString(a.size() - 1);

			try {
				final int idx = Integer.parseInt(last);
				if (idx > a.size())
					return leading + Integer.toString(a.size() - 1);
			} catch (NumberFormatException ignored) {}
		}

		return name;
	}

	/**
	 * Sets the patch path. See {@link #PatchAudit(String)}.
	 * @param value The new value.
	 * @since 1.2.0
	 */
	public void setPatchPath(String value) {
		patchPath = value;
	}

	/**
	 * Returns the record corresponding to the given path, or {@code null} if one doesn't exist.
	 * @param path The path, in {@link JsonSelector}-like form.
	 * @return The record, or {@code null}.
	 * @since 1.2.0
	 */
	public String getRecord(String path) {
		return records.get(path);
	}

	/**
	 * @return {@code true} if this audit has any records.
	 * @since 1.2.0
	 */
	public boolean hasRecords() {
		return !records.isEmpty() || !removals.isEmpty();
	}

	/**
	 * <p>Converts the given root document element to a "pretty-printed" string and decorates it with comments indicating the changes patches have made to it.</p>
	 *
	 * <p>For example:
	 * <pre><code>{
	 *   "object": {
	 *     "value": 3 // replaced by patches/a_patch
	 * //  "gone": false // removed by patches/a_patch
	 *   }
	 * }</code></pre>
	 * </p>
	 * @param root The root document.
	 * @return The string.
	 * @since 1.2.0
	 */
	public String toString(JsonElement root) {
		final StringBuilder sb = new StringBuilder();

		final List<Element> queue = new ArrayList<>();
		queue.add(new Element("", false, 0, root));

		final Deque<Element> path = new ArrayDeque<>();

		int depth = 0;
		String lastIndent = "";
		String comment = null;

		while (!queue.isEmpty()) {
			final Element elem = queue.remove(0);

			if (elem.elem() == null) { // Ending element.
				depth--;
				lastIndent = "  ".repeat(depth);
				path.pollLast();

				if (comment != null) {
					sb.append(" // ").append(comment);
					comment = null;
				}

				if (elem.index() != 0)
					sb.append("\n").append(lastIndent);

				sb.append(elem.inArray() ? "]" : "}");

				continue;
			} else if (elem.elem() != root) {
				if (elem.index() != 0)
					sb.append(",");

				if (comment != null) {
					sb.append(" // ").append(comment);
					comment = null;
				}

				sb.append("\n").append(lastIndent);

				if (!elem.inArray())
					sb.append("\"").append(elem.name()).append("\": ");
			}

			if (elem.elem() instanceof JsonObject obj) {
				depth++;
				lastIndent = "  ".repeat(depth);

				path.add(elem);

				int idx = 0;

				sb.append("{");

				if (!records.isEmpty()) {
					final String comment2 = getRecord(path(path, null));
					if (comment2 != null)
						sb.append(" // ").append(comment2);
				}

				for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
					queue.add(idx, new Element(entry.getKey(), false, idx, entry.getValue()));
					idx++;
				}

				int fakeIdx = idx;

				if (processRemovals(sb, depth, path, false))
					fakeIdx++; // Force end to new line if it was not already.

				queue.add(idx, new Element("end", false, fakeIdx, null));
			} else if (elem.elem() instanceof JsonArray arr) {
				// TODO: Fix removed elements losing their ordering -- see audit/remove_array_element.
				depth++;
				lastIndent = "  ".repeat(depth);
				path.add(elem);

				int i = 0;

				sb.append("[");

				if (!records.isEmpty()) {
					final String comment2 = getRecord(path(path, null));
					if (comment2 != null)
						sb.append(" // ").append(comment2);
				}

				for (; i < arr.size(); i++)
					queue.add(i, new Element(Integer.toString(i), true, i, arr.get(i)));

				int fakeI = i;

				if (processRemovals(sb, depth, path, true))
					fakeI++; // Force end to new line if it was not already.

				queue.add(i, new Element("end", true, fakeI, null));
			} else {
				sb.append(elem.elem().toString());

				if (!records.isEmpty()) {
					final String realPath = path(path, elem);
					comment = getRecord(realPath);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Turns the given {@link Deque} into a path, appending {@code last} to it if not {@code null}.
	 * Also handles escaping slashes and tildes.
	 * @param path The path of elements up to {@code last}.
	 * @param last The last element. May be {@code null}.
	 * @return The string path.
	 * @since 1.2.0
	 */
	private static String path(Deque<Element> path, Element last) {
		String ret = path.stream()
				.map(Element::name)
				.map(str -> str.replace("~", "~0").replace("/", "~1"))
				.collect(Collectors.joining("/"));

		if (last != null)
			ret += "/" + last.name().replace("~", "~0").replace("/", "~1");

		return ret;
	}

	private boolean processRemovals(StringBuilder sb, int depth, Deque<Element> path, boolean array) {
		if (!removals.isEmpty()) {
			final List<RemovalRecord> records = removals.get(path(path, null));
			if (records != null) {
				final String indent = "//" + "  ".repeat(depth - 1);
				for (RemovalRecord rec : records)
					rec.into(sb, indent, array);
				return true;
			}
		}

		return false;
	}

	/**
	 * Contains some extra metadata useful for formatting elements in {@link PatchAudit#toString(JsonElement)}.
	 * @param name The name of the element. In an array, this will be the string representation of its index.
	 * @param inArray Whether the element is inside an array.
	 * @param index The index of the element. Determines its ordering, as well as whether to add commas.
	 * @param elem The wrapped element.
	 * @author EnderTurret
	 * @since 1.2.0
	 */
	private static record Element(String name, boolean inArray, int index, JsonElement elem) {}

	/**
	 * Represents a record of removal. It contains more metadata than other records because it needs to recreate the element.
	 * @param name The name of the removed element.
	 * @param value The removed element.
	 * @param patchPath The path to the patch that removed the element.
	 * @author EnderTurret
	 * @since 1.2.0
	 */
	private static record RemovalRecord(String name, JsonElement value, String patchPath) {
		public void into(StringBuilder sb, String indent, boolean array) {
			sb.append("\n").append(indent);

			if (!array)
				sb.append("\"").append(name()).append("\": ");

			final String value;

			if (value() instanceof JsonObject obj)
				value = obj.size() == 0 ? "{}" : "{ ... }";
			else if (value() instanceof JsonArray arr)
				value = arr.size() == 0 ? "[]" : "[ ... ]";
			else
				value = value().toString();

			sb.append(value).append(" // removed by ").append(patchPath());
		}
	}
}