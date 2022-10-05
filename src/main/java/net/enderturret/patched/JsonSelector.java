package net.enderturret.patched;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.PatchUtil;
import net.enderturret.patched.patch.PatchUtil.Operation;
import net.enderturret.patched.patch.PatchUtil.Operations;

/**
 * <p>Represents a Json path.</p>
 *
 * <p>Some examples include:
 * <pre>
 * /object/one/2
 * /array/-</pre>
 * </p>
 *
 * <p>Real world examples:
 * <pre>
 * /server/server/performance/server/performance/memory/low-mode
 * /sounds/learnBlueprint/-
 * /ingredients/4/tag</pre>
 * </p>
 *
 * @author EnderTurret
 */
public interface JsonSelector {

	/**
	 * Handles selecting an element from the given {@link ElementContext} and applying the given {@link Operation} on it.
	 * @param context The {@link ElementContext} containing the current element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @param op The operation to apply on the found element.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 * @see ElementContext#apply(Operation)
	 */
	public ElementContext select(ElementContext context, boolean throwOnError, PatchUtil.Operation op) throws TraversalException;

	/**
	 * Equivalent to {@link #select(ElementContext, boolean, Operation)} with {@link Operations#NOOP}.
	 * @param context The {@link ElementContext} containing the current element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 * @see #select(ElementContext, boolean, Operation)
	 */
	public default ElementContext select(ElementContext context, boolean throwOnError) throws TraversalException {
		return select(context, throwOnError, PatchUtil.Operations.NOOP);
	}

	/**
	 * Equivalent to {@link #select(ElementContext, boolean, Operation)} with {@link Operations#REMOVE}.
	 * @param context The {@link ElementContext} containing the current element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 * @see #select(ElementContext, boolean, Operation)
	 */
	public default ElementContext remove(ElementContext context, boolean throwOnError) throws TraversalException {
		return select(context, throwOnError, PatchUtil.Operations.REMOVE);
	}

	/**
	 * Equivalent to {@link #select(ElementContext, boolean, Operation)} with an {@link net.enderturret.patched.patch.PatchUtil.AddOperation AddOperation}.
	 * @param context The {@link ElementContext} containing the current element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @param elem The element to add.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 * @see #select(ElementContext, boolean, Operation)
	 */
	public default ElementContext add(ElementContext context, boolean throwOnError, JsonElement elem) throws TraversalException {
		return select(context, throwOnError, new PatchUtil.AddOperation(elem, false));
	}

	/**
	 * Equivalent to {@link #select(ElementContext, boolean, Operation)} with an {@link net.enderturret.patched.patch.PatchUtil.AddOperation AddOperation} configured to replace an existing element.
	 * @param context The {@link ElementContext} containing the current element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @param elem The element to replace with.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 * @see #select(ElementContext, boolean, Operation)
	 */
	public default ElementContext replace(ElementContext context, boolean throwOnError, JsonElement elem) throws TraversalException {
		return select(context, throwOnError, new PatchUtil.AddOperation(elem, true));
	}

	/**
	 * {@link JsonElement} version of {@link #select(ElementContext, boolean)}.
	 * @param from The root or beginning element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 */
	public default ElementContext select(JsonElement from, boolean throwOnError) throws TraversalException {
		return select(new ElementContext.NoParent(from), throwOnError, PatchUtil.Operations.NOOP);
	}

	/**
	 * {@link JsonElement} version of {@link #remove(ElementContext, boolean)}.
	 * @param from The root or beginning element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 */
	public default ElementContext remove(JsonElement from, boolean throwOnError) throws TraversalException {
		return select(new ElementContext.NoParent(from), throwOnError, PatchUtil.Operations.REMOVE);
	}

	/**
	 * {@link JsonElement} version of {@link #add(ElementContext, boolean, JsonElement)}.
	 * @param from The root or beginning element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @param elem The element to add.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 */
	public default ElementContext add(JsonElement from, boolean throwOnError, JsonElement elem) throws TraversalException {
		return select(new ElementContext.NoParent(from), throwOnError, new PatchUtil.AddOperation(elem, false));
	}

	/**
	 * {@link JsonElement} version of {@link #replace(ElementContext, boolean, JsonElement)}.
	 * @param from The root or beginning element.
	 * @param throwOnError Whether a {@link TraversalException} should be thrown if an element doesn't exist. This is {@code false} for the {@code test} operation.
	 * @param elem The element to replace with.
	 * @return A new {@link ElementContext} or {@code null} if an error occurred.
	 * @throws TraversalException If an error occurs traversing the path.
	 */
	public default ElementContext replace(JsonElement from, boolean throwOnError, JsonElement elem) throws TraversalException {
		return select(new ElementContext.NoParent(from), throwOnError, new PatchUtil.AddOperation(elem, true));
	}

	/**
	 * @return {@code true} if this selector is empty.
	 */
	public default boolean isEmpty() {
		return false;
	}

	/**
	 * Parses the given path and returns a new {@link CompoundSelector} that can be used to traverse it.
	 * @param path The input path.
	 * @return The {@link CompoundSelector}.
	 * @throws TraversalException If the path does not begin with a slash.
	 */
	public static CompoundSelector of(String path) {
		if (path.isEmpty())
			return new CompoundSelector(new JsonSelector[0]);

		if (!path.startsWith("/"))
			throw new TraversalException("Path must begin with a slash!");

		path = path.substring(1);

		final String[] paths = path.split("/", -1);
		final JsonSelector[] selectors = new JsonSelector[paths.length];

		for (int i = 0; i < paths.length; i++)
			selectors[i] = ofSingle(paths[i]);

		return new CompoundSelector(selectors);
	}

	/**
	 * <p>Parses the given path into a single {@link JsonSelector}.</p>
	 * <p>This differs from {@link #of(String)} in that it does not handle delimiters.
	 * For example, {@code "/array/1"} via {@link #of(String)} would give you the path {@code ["array", 1]},
	 * but this method will give you the path {@code ["/array/1"]}.</p>
	 * @param path The input path.
	 * @return The selector.
	 */
	public static JsonSelector ofSingle(String path) {
		if (path.isEmpty())
			return new NameSelector("");

		try {
			return new NumericSelector(Integer.parseInt(path), path);
		} catch (NumberFormatException e) {
			// We may need to normalize the path:
			path = path.replace("~1", "/").replace("~0", "~");

			return new NameSelector(path);
		}
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
	 */
	public static ElementContext error(boolean throwOnError, String msg) throws TraversalException {
		if (throwOnError)
			throw new TraversalException(msg);

		return null;
	}

	/**
	 * A selector that returns the input element.
	 * @author EnderTurret
	 */
	public static record EmptySelector() implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, Operation op) throws TraversalException {
			context.apply(op);

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
	 */
	public static record NameSelector(String name) implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, PatchUtil.Operation op) throws TraversalException {
			if (context == null)
				return error(throwOnError, "Attempted to traverse null context!");

			if ("-".equals(name) && context.elem() instanceof JsonArray arr && op.allowsEndOfArrayRef())
				return op.apply(arr, arr.size());

			if (!(context.elem() instanceof JsonObject obj))
				return error(throwOnError, "Expected object to find '" + name + "' in, found " + context.elem() + "!");

			if (op.strictHas() && !obj.has(name))
				return error(throwOnError, "No such child " + name + "!");

			return op.apply(obj, name);
		}

		@Override
		public String toString() {
			return name.replace("~", "~0").replace("/", "~1");
		}
	}

	/**
	 * A selector that finds the element at the given index in an array, or with the string index in an object.
	 * @param index The index to path to in an array.
	 * @param strIndex The string version of the index.
	 * @author EnderTurret
	 */
	public static record NumericSelector(int index, String strIndex) implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, PatchUtil.Operation op) throws TraversalException {
			if (context == null)
				return error(throwOnError, "Attempted to traverse null context!");

			if (context.elem() instanceof JsonArray arr) {
				if (index < 0)
					return error(throwOnError, "Attempted to traverse negative index in array (" + index + ")!");

				if (!op.allowsOutOfBounds() && arr.size() <= index)
					return error(throwOnError, "No such child " + strIndex + "!");

				return op.apply(arr, index);
			} else if (context.elem() instanceof JsonObject obj) {
				if (op.strictHas() && !obj.has(strIndex))
					return error(throwOnError, "No such child " + strIndex + "!");

				return op.apply(obj, strIndex);
			}

			return error(throwOnError, "Expected array or object to find '" + strIndex + "' in, found " + context.elem() + "!");
		}

		@Override
		public String toString() {
			return Integer.toString(index);
		}
	}

	/**
	 * A selector that is made up of multiple other selectors.
	 * @param path The path of selectors.
	 * @author EnderTurret
	 */
	public static record CompoundSelector(JsonSelector[] path) implements JsonSelector {
		@Override
		public ElementContext select(ElementContext context, boolean throwOnError, PatchUtil.Operation op) throws TraversalException {
			ElementContext ctx = context;

			for (int i = 0; i < path.length; i++)
				try {
					ctx = path[i].select(ctx, throwOnError, i < path.length - 1 ? PatchUtil.Operations.NOOP : op);
				} catch (TraversalException e) {
					throw e.withPath(toString(/*0, i + 1*/));
				}

			return ctx;
		}

		/**
		 * Builds and returns an array containing the selectors making up the path segment represented by the given range.
		 * @param from The beginning index of the path (inclusive).
		 * @param to The ending index of the path (exclusive).
		 * @return The built array.
		 */
		public JsonSelector[] path(int from, int to) {
			final JsonSelector[] ret = new JsonSelector[to - from];
			System.arraycopy(path, from, ret, 0, ret.length);
			return ret;
		}

		/**
		 * @return The length of the path represented by this {@link CompoundSelector}.
		 */
		public int size() {
			return path.length;
		}

		/**
		 * @param index The index of the selector to return.
		 * @return The selector at {@code index} in the path.
		 */
		public JsonSelector path(int index) {
			return path[index];
		}

		/**
		 * Constructs and returns a string-representation of the path ranging from {@code from} (inclusive) and {@code to} (exclusive).
		 * @param from The beginning index (inclusive).
		 * @param to The ending index (exclusive).
		 * @return The string path between the given points.
		 */
		public String toString(int from, int to) {
			if (from >= to) throw new IndexOutOfBoundsException("Bounds: (from >= to) " + from + " >= " + to);
			if (to > path.length) throw new IndexOutOfBoundsException(to + " > path length (" + path.length + ")");
			// We don't need to check from >= path.length because we've already verified to can't be larger and that from must be less than to.

			if (from == to - 1)
				return (from == 0 ? "/" : "") + path[from].toString();

			final StringBuilder sb = new StringBuilder();

			for (int i = from; i < to; i++) {
				if (i > from || i == 0)
					sb.append("/");

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
			if (path[0] instanceof EmptySelector) return "";
			return toString(0, path.length);
		}
	}
}