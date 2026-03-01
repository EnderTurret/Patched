package net.enderturret.patched;

import com.google.gson.JsonArray;

import net.enderturret.patched.patch.context.ElementContext;
import net.enderturret.patched.patch.context.PatchContext;

/**
 * {@code TraversalMode} determines some aspects of the behavior involved in traversing {@link JsonSelector}s.
 * In particular, whether the final element must exist, whether end-of-array path elements are allowed, and whether out-of-bounds indices are allowed.
 * @author EnderTurret
 * @since 2.0.0
 */
public enum TraversalMode {

	/**
	 * The normal, strict traversal mode, used for traversal that shouldn't include {@code add} semantics.
	 * Specifically:
	 * <ul>
	 * <li>{@linkplain #allowsEndOfArrayRef() End-of-array references} are not allowed</li>
	 * <li>Elements must {@linkplain #strictHas() exist}</li>
	 * <li>{@linkplain #allowsOutOfBounds(ElementContext, int) Out-of-bounds array indices} are not allowed</li>
	 * </ul>
	 * @since 2.0.0
	 */
	NORMAL,

	/**
	 * A lenient traversal mode with {@code add} semantics.
	 * Specifically:
	 * <ul>
	 * <li>{@linkplain #allowsEndOfArrayRef() End-of-array references} are allowed</li>
	 * <li>Elements need not {@linkplain #strictHas() exist}</li>
	 * <li>{@linkplain #allowsOutOfBounds(ElementContext, int) Out-of-bounds array indices} are allowed when {@link PatchContext#throwOnOobAdd()} is {@code false}</li>
	 * </ul>
	 * @since 2.0.0
	 */
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