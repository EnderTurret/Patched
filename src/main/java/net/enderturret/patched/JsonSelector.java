package net.enderturret.patched;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.context.ElementContext;
import net.enderturret.patched.patch.context.ElementContexts;

/**
 * <p>
 * Represents a Json path -- a way of identifying a specific element in a Json tree.
 * </p>
 * <p>
 * A Json path consists of a series of "path elements" delimited with slashes.
 * Each path element identifies a kind of Json element, like an object or array.
 * For example, the path element "2" could be the third item in an array, or something associated with the key "2" in an object.
 * </p>
 * <p>
 * Since there's nothing stopping anyone from including a slash inside a key, Json paths come with the following bespoke escape sequences:
 * <table border="1">
 * <tr><th>Escape sequence</th><th>Resulting character</th></tr>
 * <tr><td>~0</td><td>~</td></tr>
 * <tr><td>~1</td><td>/</td></tr>
 * </table>
 * </p>
 *
 * <p>
 * Some examples of Json paths include:
 * <pre>
 * /object/one/2
 * /array/-</pre>
 *
 * Or some real world examples:
 * <pre>
 * /server/server/performance/server/performance/memory/low-mode
 * /sounds/learnBlueprint/-
 * /ingredients/4/tag</pre>
 * </p>
 *
 * @author EnderTurret
 * @since 1.0.0
 */
public interface JsonSelector {

	/**
	 * Handles selecting an element from the given {@link ElementContext}.
	 * @param context The {@link ElementContext} containing the current element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @param mode The mode for traversing elements.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 * @since 2.0.0
	 */
	public ElementContext select(ElementContext context, boolean throwOnError, TraversalMode mode) throws TraversalException;

	/**
	 * Equivalent to {@link #select(ElementContext, boolean, TraversalMode)} with {@link TraversalMode#NORMAL}.
	 * @param context The {@link ElementContext} containing the current element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 * @see #select(ElementContext, boolean, TraversalMode)
	 * @since 1.0.0
	 */
	public default ElementContext select(ElementContext context, boolean throwOnError) throws TraversalException {
		return select(context, throwOnError, TraversalMode.NORMAL);
	}

	/**
	 * Returns whether or not this selector is empty.
	 * @return {@code true} if this selector is empty.
	 * @since 1.0.0
	 */
	public default boolean isEmpty() {
		return false;
	}

	/**
	 * Returns the string representation of this path, for {@linkplain PatchAudit patch audits}.
	 * In many cases this is the same as {@link #toString()}.
	 * @param context The element context.
	 * @return The string representation.
	 * @since 2.0.0
	 */
	public default String toAuditString(ElementContext context) {
		return toString();
	}

	/**
	 * Parses the given path and returns a new {@link CompoundSelector} that can be used to traverse it.
	 * @param path The input path.
	 * @return The {@link CompoundSelector}.
	 * @throws TraversalException If the path does not begin with a slash.
	 * @since 1.0.0
	 */
	public static CompoundSelector of(String path) {
		boolean absolute = false;

		if (path.startsWith("^")) {
			absolute = true;
			path = path.substring(1);
		}

		if (path.isEmpty()) // "" or "^"
			return new CompoundSelector(new JsonSelector[0], absolute);

		if (!path.startsWith("/"))
			throw new TraversalException("Path must begin with a slash!");

		path = path.substring(1);

		final String[] paths = path.split("/", -1);
		final JsonSelector[] selectors = new JsonSelector[paths.length];

		for (int i = 0; i < paths.length; i++)
			selectors[i] = ofSingle(paths[i]);

		return new CompoundSelector(selectors, absolute);
	}

	/**
	 * <p>Parses the given path into a single {@link JsonSelector}.</p>
	 * <p>This differs from {@link #of(String)} in that it does not handle delimiters.
	 * For example, {@code "/array/1"} via {@link #of(String)} would give you the path {@code ["array", 1]},
	 * but this method will give you the path {@code ["/array/1"]}.</p>
	 * @param path The input path.
	 * @return The selector.
	 * @since 1.0.0
	 */
	public static JsonSelector ofSingle(String path) {
		if (path.isEmpty())
			return new NameSelector("");

		if (path.startsWith("{") && path.endsWith("}"))
			return new PlaceholderSelector(path.substring(1, path.length() - 1), path);

		// In case we get something like "07", we shouldn't parse that into 7.
		// However, a single "0" should be parsed into a 0.
		if (path.length() == 1 || !path.startsWith("0"))
			try {
				return new NumericSelector(Integer.parseInt(path), path);
			} catch (NumberFormatException ignored) {}

		// We may need to normalize the path:
		path = unescape(path);

		return new NameSelector(path);
	}

	private static String unescape(String path) {
		final StringBuilder sb = new StringBuilder(path);

		for (int i = 0; i < sb.length(); i++)
			if (sb.codePointAt(i) == '~') {
				if (sb.length() == i + 1)
					throw new TraversalException("Invalid escape sequence: '~'!");

				final int nextCp = sb.codePointAt(i + 1);

				final String replacement = switch (nextCp) {
					case '1' -> "/";
					case '0' -> "~";
					default -> throw new TraversalException("Invalid escape sequence: '~" + (char) nextCp + "'!");
				};

				sb.replace(i, i + 2, replacement);
			}

		return sb.toString();
	}

	/**
	 * <p>If {@code throwOnError} is {@code true}, throws a {@link TraversalException} with the given message.
	 * Otherwise returns {@code null}.</p>
	 * <p>This method is intended for use in situations like the following:
	 * <pre>
	 * {@code
	 * if (child == null)
	 *     return error(throwOnError, "child is null");
	 * }</pre></p>
	 * @param throwOnError {@code true} if an exception should be thrown.
	 * @param msg The detail message of the exception.
	 * @return {@code null}.
	 * @throws TraversalException If {@code throwOnError} is {@code true}.
	 * @since 1.0.0
	 */
	public static ElementContext error(boolean throwOnError, String msg) throws TraversalException {
		if (throwOnError)
			throw new TraversalException(msg);

		return null;
	}

	/**
	 * A selector that returns the input element.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record EmptySelector() implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, TraversalMode mode) throws TraversalException {
			return context;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public String toString() {
			return "";
		}
	}

	/**
	 * A selector that finds the element with the given name in an object,
	 * or the end of an array in the case of {@code '-'}.
	 * @param name The name of the element to find.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record NameSelector(String name) implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, TraversalMode mode) throws TraversalException {
			if (context == null)
				return error(throwOnError, "Attempted to traverse null context!");

			if ("-".equals(name) && context.elem() instanceof JsonArray arr && mode.allowsEndOfArrayRef())
				return new ElementContexts.Array(context, arr, arr.size(), null);

			if (!(context.elem() instanceof JsonObject obj))
				return error(throwOnError, "Expected object to find '" + name + "' in, found " + context.elem() + "!");

			if (mode.strictHas() && !obj.has(name))
				return error(throwOnError, "No such child " + name + "!");

			return context.child(name, obj.get(name));
		}

		@Override
		public String toString() {
			return name.replace("~", "~0").replace("/", "~1");
		}

		@Override
		public String toAuditString(ElementContext context) {
			if ("-".equals(name) && context.elem() instanceof JsonArray arr)
				return Integer.toString(arr.size() - 1);

			return toString();
		}
	}

	/**
	 * A selector that finds the element corresponding to the specified placeholder.
	 * @param placeholder The name of the placeholder.
	 * @author EnderTurret
	 * @param raw The 'raw' version of the placeholder.
	 * @since 2.0.0
	 */
	public static record PlaceholderSelector(String placeholder, String raw) implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, TraversalMode mode) throws TraversalException {
			if (context == null)
				return error(throwOnError, "Attempted to traverse null context!");

			final JsonSelector selector = context.getPlaceholder(placeholder);
			if (selector == null) {
				if (context.elem() instanceof JsonObject obj) {
					if (mode.strictHas() && !obj.has(raw))
						return error(throwOnError, "No such child " + raw + "!");

					return context.child(raw, obj.get(raw));
				}

				return error(throwOnError, "Expected object to find '" + raw + "' in, found " + context.elem() + "!");
			}

			return selector.select(context, throwOnError, mode);
		}

		@Override
		public String toString() {
			return raw;
		}

		@Override
		public String toAuditString(ElementContext context) {
			final JsonSelector selector = context.getPlaceholder(placeholder);
			return selector == null ? toString() : selector.toAuditString(context);
		}
	}

	/**
	 * A selector that finds the element at the given index in an array, or with the string index in an object.
	 * @param index The index to path to in an array.
	 * @param strIndex The string version of the index.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record NumericSelector(int index, String strIndex) implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, TraversalMode mode) throws TraversalException {
			if (context == null)
				return error(throwOnError, "Attempted to traverse null context!");

			if (context.elem() instanceof JsonArray arr) {
				if (index < 0)
					return error(throwOnError, "Attempted to traverse negative index in array (" + index + ")!");

				if (arr.size() <= index && !mode.allowsOutOfBounds(context, index))
					return error(throwOnError, "No such child " + strIndex + "!");

				return context.child(index, arr.size() <= index ? null : arr.get(index));
			} else if (context.elem() instanceof JsonObject obj) {
				if (mode.strictHas() && !obj.has(strIndex))
					return error(throwOnError, "No such child " + strIndex + "!");

				return context.child(strIndex, obj.get(strIndex));
			}

			return error(throwOnError, "Expected array or object to find '" + strIndex + "' in, found " + context.elem() + "!");
		}

		@Override
		public String toString() {
			return Integer.toString(index);
		}

		@Override
		public String toAuditString(ElementContext context) {
			if (context.elem() instanceof JsonArray arr && arr.size() < index)
				return Integer.toString(arr.size() - 1);

			return toString();
		}
	}

	/**
	 * A selector that is made up of multiple other selectors.
	 * @param path The path of selectors.
	 * @param absolute Whether or not the {@code CompoundSelector} is in 'absolute' mode.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record CompoundSelector(JsonSelector[] path, boolean absolute) implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, TraversalMode mode) throws TraversalException {
			try {
				if (absolute && !context.context().patchedExtensions())
					throw new TraversalException("Cannot traverse absolute path with Patched extensions off!");

				ElementContext ctx = context;
				if (absolute) ctx = new ElementContexts.Document(ctx, ctx.doc());

				for (int i = 0; i < path.length; i++) {
					ctx = path[i].select(ctx, throwOnError, i < path.length - 1 ? TraversalMode.NORMAL : mode);
					if (ctx == null) return null; // Avoid invoking more selectors if we've encountered a soft error.
				}

				return ctx;
			} catch (TraversalException e) {
				throw e.withPath(toString(/*0, i + 1*/));
			}
		}

		/**
		 * Builds and returns an array containing the selectors making up the path segment represented by the given range.
		 * @param from The beginning index of the path (inclusive).
		 * @param to The ending index of the path (exclusive).
		 * @return The built array.
		 * @since 1.0.0
		 */
		public JsonSelector[] path(int from, int to) {
			final JsonSelector[] ret = new JsonSelector[to - from];
			System.arraycopy(path, from, ret, 0, ret.length);
			return ret;
		}

		/**
		 * @return The length of the path represented by this {@link CompoundSelector}.
		 * @since 1.0.0
		 */
		public int size() {
			return path.length;
		}

		/**
		 * @param index The index of the selector to return.
		 * @return The selector at {@code index} in the path.
		 * @since 1.0.0
		 */
		public JsonSelector path(int index) {
			return path[index];
		}

		/**
		 * Constructs and returns a string-representation of the path ranging from {@code from} (inclusive) and {@code to} (exclusive).
		 * @param from The beginning index (inclusive).
		 * @param to The ending index (exclusive).
		 * @return The string path between the given points.
		 * @since 1.0.0
		 */
		public String toString(int from, int to) {
			if (from >= to) throw new IndexOutOfBoundsException("Bounds: (from >= to) " + from + " >= " + to);
			if (to > path.length) throw new IndexOutOfBoundsException(to + " > path length (" + path.length + ")");
			// We don't need to check from >= path.length because we've already verified to can't be larger and that from must be less than to.

			if (from == to - 1)
				return (from == 0 ? (absolute ? "^/" : "/") : "") + path[from].toString();

			final StringBuilder sb = new StringBuilder();

			for (int i = from; i < to; i++) {
				if (i == 0)
					sb.append(absolute ? "^/" : "/");
				else if (i > from)
					sb.append('/');

				sb.append(path[i].toString());
			}

			return sb.toString();
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public String toString() {
			if (path.length == 0) return "";
			return toString(0, path.length);
		}

		@Override
		public String toAuditString(ElementContext context) {
			final StringBuilder sb = new StringBuilder();

			ElementContext ctx = context;
			if (absolute) ctx = new ElementContexts.Document(ctx, ctx.doc());

			for (int i = 0; i < path.length; i++) {
				if (i == 0)
					sb.append(absolute ? "^/" : "/");
				else
					sb.append('/');

				sb.append(path[i].toAuditString(ctx));

				// It shouldn't be possible for this to throw a TraversalException unless the input is malformed.
				ctx = path[i].select(ctx, true, i < path.length - 1 ? TraversalMode.NORMAL : TraversalMode.ADD);
			}

			return sb.toString();
		}
	}
}