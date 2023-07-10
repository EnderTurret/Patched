package net.enderturret.patched.patch;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.JsonDocument;

/**
 * Various utilities used in the patching backend.
 * @author EnderTurret
 */
public final class PatchUtil {

	private PatchUtil() {}

	/**
	 * <p>Adds the given value to the specified array at the given index.</p>
	 * <p>This is primarily a hack caused by limitations of {@link JsonArray}.</p>
	 * @param array The array to add the element to.
	 * @param index The index to insert it at.
	 * @param value The value to add.
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
	 * @param path The location the element will be placed.
	 * @param value The element that will be added.
	 * @return A new {@link AddPatch}.
	 */
	public static AddPatch add(String path, JsonElement value) {
		return new AddPatch(path, value);
	}

	/**
	 * @param path The location the element will be copied to.
	 * @param from The path to the element to copy.
	 * @return A new {@link CopyPatch}.
	 */
	public static CopyPatch copy(String path, String from) {
		return new CopyPatch(path, from);
	}

	/**
	 * @param path The location the element will be moved to.
	 * @param from The path to the element to move.
	 * @return A new {@link MovePatch}.
	 */
	public static MovePatch move(String path, String from) {
		return new MovePatch(path, from);
	}

	/**
	 * @param path The path to the element to remove.
	 * @return A new {@link RemovePatch}.
	 */
	public static RemovePatch remove(String path) {
		return new RemovePatch(path);
	}

	/**
	 * @param path The path to the element to replace.
	 * @param value The value to replace the element with.
	 * @return A new {@link ReplacePatch}.
	 */
	public static ReplacePatch replace(String path, JsonElement value) {
		return new ReplacePatch(path, value);
	}

	/**
	 * @param type A custom type for {@link ITestEvaluator}.
	 * @param path The path to the element to test. May be {@code null}.
	 * @param test The test element. May be {@code null}.
	 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
	 * @return A new {@link TestPatch}.
	 */
	public static TestPatch test(String type, String path, JsonElement test, boolean inverse) {
		return new TestPatch(type, path, test, inverse);
	}

	/**
	 * @param path The path to the element to test.
	 * @param test The test element. May be {@code null}.
	 * @param inverse Whether the check is inverted, i.e checking to see if something doesn't exist.
	 * @return A new {@link TestPatch}.
	 */
	public static TestPatch test(String path, JsonElement test, boolean inverse) {
		return new TestPatch(null, path, test, inverse);
	}

	/**
	 * @param path The path to the element to find things in.
	 * @param tests A list of tests that an element must pass to have {@code then} applied to it.
	 * @param then A patch to apply to elements passing the tests.
	 * @param multi Whether to continue searching for matching elements after the first one is found.
	 * @return A new {@link FindPatch}.
	 */
	public static FindPatch find(String path, List<TestPatch> tests, JsonPatch then, boolean multi) {
		return new FindPatch(path, tests, then, multi);
	}

	/**
	 * @param patches The patches that will be contained within the {@link CompoundPatch}.
	 * @return A new {@link CompoundPatch}.
	 */
	public static CompoundPatch compound(JsonPatch... patches) {
		return new CompoundPatch(patches);
	}

	/**
	 * Represents an operation that can be applied to a specific {@link JsonElement}.
	 * @author EnderTurret
	 */
	public static interface Operation {

		/**
		 * Applies this operation to the element represented by the given context.
		 * @param context The element.
		 * @return An {@link ElementContext} containing the child element and related information.
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
		 * @param obj The object to find the element in.
		 * @param name The name of the element to apply on.
		 * @return An {@link ElementContext} containing the child element and related information.
		 */
		@Deprecated(forRemoval = true)
		public default ElementContext apply(JsonObject obj, String name) {
			return apply(new ElementContext.Object(obj, name, JsonNull.INSTANCE));
		}

		/**
		 * Applies this operation to the element at the given index inside the given array.
		 * @param arr The array to find the element in.
		 * @param idx The index of the element to apply on.
		 * @return An {@link ElementContext} containing the child element and related information.
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
		 */
		public default boolean allowsOutOfBounds(ElementContext context) {
			return false;
		}

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
		 */
		public default boolean allowsEndOfArrayRef() {
			return false;
		}

		/**
		 * <p>Whether a path must point to an existing element to be valid.</p>
		 * <p>This is by default {@code true} for the {@code replace} operation,
		 * as it is expected that you will be replacing something.</p>
		 * @return {@code true} if an element is required exist at a path.
		 */
		public default boolean strictHas() {
			return true;
		}
	}

	/**
	 * Implements a few of the operations.
	 * @author EnderTurret
	 */
	public static enum Operations implements Operation {

		/**
		 * Does absolutely nothing. You'd be surprised.
		 */
		NOOP,

		/**
		 * Implements the {@code remove} operation.
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

			return context;
		}
	}

	/**
	 * Implements the {@code add} and {@code replace} operations.
	 * @param elem The element to add or replace with. A {@code null} element "simulates" addition/replacement; it doesn't actually add or replace anything.
	 * @param replace {@code true} if the element is intended to replace an existing element. This enables checks to verify the replaced element actually exists first.
	 * @author EnderTurret
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