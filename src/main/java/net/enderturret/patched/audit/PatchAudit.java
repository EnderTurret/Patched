package net.enderturret.patched.audit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.ElementContext;

public final class PatchAudit {

	private final Map<String, String> records = new HashMap<>();

	private final Map<String, List<RemovalRecord>> removals = new HashMap<>();

	private String patchPath;
	private String pathPrefix;
	private String pathKey;

	public PatchAudit(String patchPath) {
		this.patchPath = patchPath;
	}

	private void record(String path, String comment) {
		if (pathPrefix != null)
			path = pathPrefix + "/" + pathKey + path;

		records.put(path, comment);
	}

	public void recordAdd(String parentPath, String name, ElementContext added) {
		record(parentPath + "/" + fixPath(name, added), "added by " + patchPath);
	}

	public void recordCopy(String parentPath, String name, String from, ElementContext copied) {
		record(parentPath + "/" + fixPath(name, copied), "copied from " + from + " by " + patchPath);
	}

	public void recordMove(String parentPath, String name, String from, ElementContext moved) {
		record(parentPath + "/" + fixPath(name, moved), "moved from " + from + " by " + patchPath);
	}

	public void recordRemove(String parentPath, String name, JsonElement value) {
		if (pathPrefix != null)
			parentPath = pathPrefix + parentPath;

		removals.computeIfAbsent(parentPath, k -> new ArrayList<>(1))
		.add(new RemovalRecord((pathKey != null ? pathKey : "") + name, value, patchPath));
	}

	public void recordReplace(String parentPath, String name) {
		final String path = parentPath.isEmpty() && name.isEmpty() && pathPrefix != null ? "" : parentPath + "/" + name;
		record(path, "replaced by " + patchPath);
	}

	public void beginPrefix(String pathPrefix, String pathKey) {
		this.pathPrefix = pathPrefix;
		this.pathKey = pathKey;
	}

	public void endPrefix() {
		pathPrefix = null;
		pathKey = null;
	}

	private static String fixPath(String name, ElementContext context) {
		if (context.parent() instanceof JsonArray a) {
			if ("-".equals(name))
				return Integer.toString(a.size() - 1);
			try {
				final int idx = Integer.parseInt(name);
				if (idx > a.size())
					return Integer.toString(a.size() - 1);
			} catch (NumberFormatException ignored) {}
		}

		return name;
	}

	public void setPatchPath(String value) {
		patchPath = value;
	}

	public String getRecord(String path) {
		return records.get(path);
	}

	public boolean hasRecords() {
		return !records.isEmpty() || !removals.isEmpty();
	}

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

				for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
					queue.add(idx, new Element(entry.getKey(), false, idx, entry.getValue()));
					idx++;
				}

				int fakeIdx = idx;

				if (processRemovals(sb, depth, path, false))
					fakeIdx++; // Force end to new line if it was not already.

				queue.add(idx, new Element("end", false, fakeIdx, null));
			} else if (elem.elem() instanceof JsonArray arr) {
				depth++;
				lastIndent = "  ".repeat(depth);
				path.add(elem);

				int i = 0;

				sb.append("[");

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

	private static record Element(String name, boolean inArray, int index, JsonElement elem) {
		private Element {}
	}

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