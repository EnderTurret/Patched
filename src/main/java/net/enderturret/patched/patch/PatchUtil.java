package net.enderturret.patched.patch;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;

/**
 * Various utilities used in the patching backend.
 * @author EnderTurret
 * @since 1.0.0
 */
public final class PatchUtil {

	private PatchUtil() {}

	/**
	 * <p>Adds the given value to the specified array at the given index.</p>
	 * <p>This is primarily a hack caused by limitations of {@link JsonArray}.</p>
	 * @param array The array to add the element to.
	 * @param index The index to insert it at.
	 * @param value The value to add.
	 * @since 1.0.0
	 */
	public static void add(JsonArray array, int index, JsonElement value) {
		// [1, 2, 3, 4, 5]
		//     ^ 3
		// [1, 3, 2, 3, 4, 5]

		// Because JsonArray doesn't expose an add(int, JsonElement) method,
		// we have to mimic it by shifting all the values over manually.

		JsonElement last = value;

		for (int i = index; i < array.size(); i++)
			last = array.set(i, last);

		array.add(last);
	}

	/**
	 * Creates an {@link AddPatch} from the specified values.
	 * @param path The location the element will be placed.
	 * @param value The element that will be added.
	 * @return A new {@code AddPatch}.
	 * @since 1.0.0
	 */
	public static AddPatch add(String path, JsonElement value) {
		return new AddPatch(path, value);
	}

	/**
	 * Creates a {@link CopyPatch} from the specified values.
	 * @param path The location the element will be copied to.
	 * @param from The path to the element to copy.
	 * @return A new {@code CopyPatch}.
	 * @since 1.0.0
	 */
	public static CopyPatch copy(String path, String from) {
		return new CopyPatch(path, from);
	}

	/**
	 * Creates a {@link MovePatch} from the specified values.
	 * @param path The location the element will be moved to.
	 * @param from The path to the element to move.
	 * @return A new {@code MovePatch}.
	 * @since 1.0.0
	 */
	public static MovePatch move(String path, String from) {
		return new MovePatch(path, from);
	}

	/**
	 * Creates a {@link RemovePatch} from the specified values.
	 * @param path The path to the element to remove.
	 * @return A new {@code RemovePatch}.
	 * @since 1.0.0
	 */
	public static RemovePatch remove(String path) {
		return new RemovePatch(path);
	}

	/**
	 * Creates a {@link ReplacePatch} from the specified values.
	 * @param path The path to the element to replace.
	 * @param value The value to replace the element with.
	 * @return A new {@code ReplacePatch}.
	 * @since 1.0.0
	 */
	public static ReplacePatch replace(String path, JsonElement value) {
		return new ReplacePatch(path, value);
	}

	/**
	 * Creates a {@link TestPatch} from the specified values.
	 * @param type A custom type for {@link ITestEvaluator}.
	 * @param path The path to the element to test. May be {@code null}.
	 * @param test The test element. May be {@code null}.
	 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
	 * @return A new {@code TestPatch}.
	 * @since 1.1.0
	 */
	public static TestPatch test(String type, String path, JsonElement test, boolean inverse) {
		return new TestPatch(type, path, test, inverse);
	}

	/**
	 * Creates a {@link TestPatch} from the specified values.
	 * @param path The path to the element to test.
	 * @param test The test element. May be {@code null}.
	 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
	 * @return A new {@code TestPatch}.
	 * @since 1.0.0
	 */
	public static TestPatch test(String path, JsonElement test, boolean inverse) {
		return new TestPatch(null, path, test, inverse);
	}

	/**
	 * Creates a {@link FindPatch} from the specified values.
	 * @param path The path to the element to find things in.
	 * @param tests A list of tests that an element must pass to have {@code then} applied to it.
	 * @param then A patch to apply to elements passing the tests.
	 * @param multi Whether to continue searching for matching elements after the first one is found.
	 * @return A new {@code FindPatch}.
	 * @since 1.0.0
	 */
	public static FindPatch find(String path, List<TestPatch> tests, JsonPatch then, boolean multi) {
		return new FindPatch(path, tests, then, multi);
	}

	/**
	 * Creates an {@link IncludePatch} from the specified values.
	 * @param path The path to the patch file to include.
	 * @return A new {@code IncludePatch}.
	 * @since 1.4.0
	 */
	public static IncludePatch include(String path) {
		return new IncludePatch(path);
	}

	/**
	 * Creates a {@link PastePatch} from the specified values.
	 * @param path The location the element will be placed.
	 * @param type The type to pass to the data source.
	 * @param from A path to the input element to pass to the data source. May be {@code null}.
	 * @param value Extra context to pass to the data source. May be {@code null}.
	 * @return A new {@code PastePatch}.
	 * @since 1.5.0
	 */
	public static PastePatch paste(String path, String type, @Nullable String from, @Nullable JsonElement value) {
		return new PastePatch(path, type, from, value);
	}

	/**
	 * Creates a {@link CompoundPatch} from the specified values.
	 * @param patches The patches that will be contained within the {@link CompoundPatch}.
	 * @return A new {@code CompoundPatch}.
	 * @since 1.0.0
	 */
	public static CompoundPatch compound(JsonPatch... patches) {
		return new CompoundPatch(patches);
	}

	/**
	 * Represents an operation that can be applied to a specific {@link JsonElement}.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static interface Operation {

		/**
		 * Applies this operation to the element represented by the given context.
		 * @param context The element.
		 * @return An {@link ElementContext} containing the child element and related information.
		 * @since 1.3.0
		 */
		public default ElementContext apply(ElementContext context) {
			if (context instanceof ElementContext.Object obj)
				return apply(obj.parent(), obj.name());
			if (context instanceof ElementContext.Array arr)
				return apply(arr.parent(), arr.index());

			return context;
		}

		/**
		 * Applies this operation to the element with the given name inside the given object.
		 * @deprecated Use {@link #apply(ElementContext)} instead.
		 * @param obj The object to find the element in.
		 * @param name The name of the element to apply on.
		 * @return An {@link ElementContext} containing the child element and related information.
		 * @since 1.0.0
		 */
		@Deprecated(forRemoval = true)
		public default ElementContext apply(JsonObject obj, String name) {
			return apply(new ElementContext.Object(obj, name, JsonNull.INSTANCE));
		}

		/**
		 * Applies this operation to the element at the given index inside the given array.
		 * @deprecated Use {@link #apply(ElementContext)} instead.
		 * @param arr The array to find the element in.
		 * @param idx The index of the element to apply on.
		 * @return An {@link ElementContext} containing the child element and related information.
		 * @since 1.0.0
		 */
		@Deprecated(forRemoval = true)
		public default ElementContext apply(JsonArray arr, int idx) {
			return apply(new ElementContext.Array(arr, idx, JsonNull.INSTANCE));
		}

		/**
		 * <p>Whether a path may include an out-of-bounds index.</p>
		 * <p>
		 * Consider the following:
		 * <pre>
		 * // patch
		 * {
		 *   "op": "add",
		 *   "path": "/array/9",
		 *   "value": "e"
		 * }
		 * // document
		 * {
		 *   "array": [1]
		 * }</pre>
		 * The return value of this method determines whether this patch will succeed.
		 * </p>
		 * @param context The element.
		 * @return {@code true} if out-of-bounds indices are allowed.
		 * @since 1.3.0
		 */
		public default boolean allowsOutOfBounds(ElementContext context) {
			return false;
		}

		/**
		 * <p>Whether a path may include an out-of-bounds index.</p>
		 * <p>
		 * Consider the following:
		 * <pre>
		 * // patch
		 * {
		 *   "op": "add",
		 *   "path": "/array/9",
		 *   "value": "e"
		 * }
		 * // document
		 * {
		 *   "array": [1]
		 * }</pre>
		 * The return value of this method determines whether this patch will succeed.
		 * </p>
		 * @deprecated Use {@link #allowsOutOfBounds(ElementContext)} instead.
		 * @return {@code true} if out-of-bounds indices are allowed.
		 * @since 1.0.0
		 */
		@Deprecated(forRemoval = true)
		public default boolean allowsOutOfBounds() {
			return false;
		}

		/**
		 * <p>Whether a path may use the special end-of-array token '-'.</p>
		 * <p>
		 * Consider the following:
		 * <pre>
		 * // patch
		 * {
		 *   "op": "add",
		 *   "path": "/array/-",
		 *   "value": "e"
		 * }
		 * // document
		 * {
		 *   "array": [1]
		 * }</pre>
		 * The return value of this method determines whether this patch will succeed.
		 * </p>
		 * @return {@code true} if end-of-array references are allowed.
		 * @since 1.0.0
		 */
		public default boolean allowsEndOfArrayRef() {
			return false;
		}

		/**
		 * <p>Whether a path must point to an existing element to be valid.</p>
		 * <p>This is by default {@code true} for the {@code replace} operation,
		 * as it is expected that you will be replacing something.</p>
		 * @return {@code true} if an element is required exist at a path.
		 * @since 1.0.0
		 */
		public default boolean strictHas() {
			return true;
		}
	}

	/**
	 * Implements a few of the operations.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static enum Operations implements Operation {

		/**
		 * Does absolutely nothing. You'd be surprised.
		 * @since 1.0.0
		 */
		NOOP,

		/**
		 * Implements the {@code remove} operation.
		 * @since 1.0.0
		 */
		REMOVE;

		@Override
		public ElementContext apply(ElementContext context) {
			if (context instanceof ElementContext.Object obj)
				return new ElementContext.Object(obj.context(), obj.parent(), obj.name(),
						this == REMOVE ? obj.parent().remove(obj.name()) : obj.parent().get(obj.name()));

			if (context instanceof ElementContext.Array arr) {
				return new ElementContext.Array(arr.context(), arr.parent(), arr.index(),
						this == REMOVE ? arr.parent().remove(arr.index()) : arr.parent().get(arr.index()));
			}

			if (context instanceof ElementContext.Document && this == REMOVE)
				throw new PatchingException("Attempted to remove root element!");

			return context;
		}
	}

	/**
	 * Implements the {@code add} and {@code replace} operations.
	 * @param elem The element to add or replace with. A {@code null} element "simulates" addition/replacement; it doesn't actually add or replace anything.
	 * @param replace {@code true} if the element is intended to replace an existing element. This enables checks to verify the replaced element actually exists first.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record AddOperation(@Nullable JsonElement elem, boolean replace) implements Operation {

		@Override
		public ElementContext apply(ElementContext context) {
			if (context instanceof ElementContext.Object obj) {
				if (elem != null) obj.parent().add(obj.name(), elem);
				return new ElementContext.Object(obj.context(), obj.parent(), obj.name(), elem);
			}

			if (context instanceof ElementContext.Array arr) {
				if (elem != null)
					if (replace())
						arr.parent().set(arr.index(), elem);
					else
						add(arr.parent(), arr.index(), elem);

				return new ElementContext.Array(arr.context(), arr.parent(), arr.index(), elem);
			}

			if (context instanceof ElementContext.Document doc)
				doc.doc().setRoot(elem);

			return context;
		}

		@Override
		public boolean allowsEndOfArrayRef() {
			return !replace;
		}

		@Override
		public boolean allowsOutOfBounds(ElementContext context) {
			return !replace && (!context.context().throwOnOobAdd() || (context instanceof ElementContext.Array a
					&& a.index() == a.parent().size()));
		}

		@Override
		public boolean strictHas() {
			return replace;
		}
	}
}