package net.enderturret.patched.patch;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.context.ElementContext;
import net.enderturret.patched.patch.context.ElementContexts;

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
	 * @param placeholder The placeholder associated with this patch. May be {@code null}.
	 * @param multi Whether to continue searching for matching elements after the first one is found.
	 * @return A new {@code FindPatch}.
	 * @since 2.0.0
	 */
	public static FindPatch find(String path, List<TestPatch> tests, JsonPatch then, @Nullable String placeholder, boolean multi) {
		return new FindPatch(path, tests, then, placeholder, multi);
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
		return new FindPatch(path, tests, then, null, multi);
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

	public static enum TraversalMode {

		NORMAL,
		ADD;

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
		 * @since 2.0.0
		 */
		public boolean allowsEndOfArrayRef() {
			return this == ADD;
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
		 * @param context The parent element.
		 * @param index The index.
		 * @return {@code true} if out-of-bounds indices are allowed.
		 * @since 2.0.0
		 */
		public boolean allowsOutOfBounds(ElementContext context, int index) {
			// Non-add patches cannot access out-of-bounds.
			if (this != ADD) return false;

			// Patches operating in a context that allows access out-of-bounds can access out-of-bounds.
			if (!context.context().throwOnOobAdd()) return true;

			// Add patches need to be able to reference a non-existing element as a place to add to.
			return context.elem() instanceof JsonArray a && index == a.size();
		}

		/**
		 * <p>Whether a path must point to an existing element to be valid.</p>
		 * <p>This is by default {@code true} for the {@code replace} operation,
		 * as it is expected that you will be replacing something.</p>
		 * @return {@code true} if an element is required exist at a path.
		 * @since 2.0.0
		 */
		public boolean strictHas() {
			return this != ADD;
		}
	}

	public static void applyRemove(ElementContext context) {
		if (context instanceof ElementContexts.Object obj)
			obj.parent().remove(obj.name());

		else if (context instanceof ElementContexts.Array arr)
			arr.parent().remove(arr.index());

		else if (context instanceof ElementContexts.Document)
			throw new PatchingException("Attempted to remove root element!");
	}

	public static void applyAdd(ElementContext context, JsonElement elem, boolean replace) {
		// Make sure we actually copy the element. Not important for primitives (numbers, strings) but required for objects and arrays.
		// Avoids leaking a patch's element reference into the document.
		if (elem != null) elem = elem.deepCopy();

		if (context instanceof ElementContexts.Object obj)
			obj.parent().add(obj.name(), elem);

		else if (context instanceof ElementContexts.Array arr) {
			if (replace)
				arr.parent().set(arr.index(), elem);
			else
				add(arr.parent(), arr.index(), elem);
		}

		else if (context instanceof ElementContexts.Document doc)
			doc.doc().setRoot(elem);
	}
}