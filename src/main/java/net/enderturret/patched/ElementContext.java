package net.enderturret.patched;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.PatchContext;
import net.enderturret.patched.patch.PatchUtil.Operation;

/**
 * <p>Represents a {@link JsonElement} and its parent element.</p>
 * <p>This class acts as an abstraction layer to the different kinds of elements that can be parents.</p>
 * @author EnderTurret
 * @since 1.0.0
 */
public interface ElementContext {

	/**
	 * @return The patch context.
	 * @since 1.3.0
	 */
	@Nullable
	public default PatchContext context() {
		return null;
	}

	/**
	 * @return The parent element.
	 * @since 1.0.0
	 */
	@Nullable
	public JsonElement parent();

	/**
	 * @return The element.
	 * @since 1.0.0
	 */
	@Nullable
	public JsonElement elem();

	/**
	 * Applies the given operation to this element.
	 * @deprecated Use {@link Operation#apply(ElementContext)} instead.
	 * @param op The operation to apply.
	 * @since 1.0.0
	 */
	@Deprecated(forRemoval = true)
	public default void apply(Operation op) {
		op.apply(this);
	}

	/**
	 * Creates a new {@link ElementContext} with this set as its parent and the given values as its name and element.
	 * @param name The name of {@code elem}.
	 * @param elem The element wrapped by the {@link ElementContext}.
	 * @return The new {@link ElementContext}.
	 * @throws TraversalException If this element is not a {@link JsonObject}.
	 * @since 1.0.0
	 */
	public default ElementContext child(String name, JsonElement elem) {
		if (!(elem() instanceof JsonObject o))
			throw new TraversalException("Not an object!");
		return new Object(context(), o, name, elem);
	}

	/**
	 * Creates a new {@link ElementContext} with this set as its parent and the given values as its index and element.
	 * @param index The index of {@code elem}.
	 * @param elem The element wrapped by the {@link ElementContext}.
	 * @return The new {@link ElementContext}.
	 * @throws TraversalException If this element is not a {@link JsonArray}.
	 * @since 1.0.0
	 */
	public default ElementContext child(int index, JsonElement elem) {
		if (!(elem() instanceof JsonArray a))
			throw new TraversalException("Not an array!");
		return new Array(context(), a, index, elem);
	}

	/**
	 * <p>An element without a parent.</p>
	 * <p>Such an element cannot have operations applied to it.</p>
	 * @param context The patch context. May be {@code null} in circumstances involving old code.
	 * @param elem The element.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record NoParent(@Nullable PatchContext context, JsonElement elem) implements ElementContext {

		/**
		 * Constructs a new no-parent context.
		 * @param context The patch configuration.
		 * @param elem The element in context.
		 */
		public NoParent {}

		/**
		 * Constructs a new no-parent context.
		 * @deprecated Use {@link NoParent#NoParent(PatchContext, JsonElement)} instead.
		 * @param elem The element in context.
		 */
		@Deprecated(forRemoval = true)
		public NoParent(JsonElement elem) { this(null, elem); }

		@Override
		public JsonElement parent() {
			return null;
		}
	}

	/**
	 * An element whose parent is the json document -- allows swapping out this element without needing to change 300 {@code void}s to {@code JsonElement}s.
	 * @param context The patch context. May be {@code null} in circumstances involving old code.
	 * @param doc The document.
	 * @author EnderTurret
	 * @since 1.3.0
	 */
	public static record Document(@Nullable PatchContext context, JsonDocument doc) implements ElementContext {

		/**
		 * Constructs a new root document context.
		 * @param context The patch configuration.
		 * @param doc The document in context.
		 */
		public Document {}

		/**
		 * Constructs a new root document context.
		 * @deprecated Use {@link Document#Document(PatchContext, JsonDocument)} instead.
		 * @param doc The document in context.
		 */
		@Deprecated(forRemoval = true)
		public Document(JsonDocument doc) { this(null, doc); }

		@Override
		public JsonElement parent() {
			return null;
		}

		@Override
		public JsonElement elem() {
			return doc.getRoot();
		}
	}

	/**
	 * An element whose parent is a {@link JsonObject}.
	 * @param context The patch context. May be {@code null} in circumstances involving old code.
	 * @param parent The parent object.
	 * @param name The name of the current element.
	 * @param elem The current element.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record Object(@Nullable PatchContext context, JsonObject parent, String name, @Nullable JsonElement elem) implements ElementContext {

		/**
		 * Constructs a new object-parent context.
		 * @param context The patch configuration.
		 * @param parent The parent object.
		 * @param name The name of the current element.
		 * @param elem The current element.
		 */
		public Object {}

		/**
		 * Constructs a new object-parent context.
		 * @deprecated Use {@link Object#Object(PatchContext, JsonObject, String, JsonElement)} instead.
		 * @param parent The parent object.
		 * @param name The name of the current element.
		 * @param elem The current element.
		 */
		@Deprecated(forRemoval = true)
		public Object(JsonObject parent, String name, @Nullable JsonElement elem) { this(null, parent, name, elem); }

		@Override
		public JsonObject parent() {
			return parent;
		}
	}

	/**
	 * An element whose parent is a {@link JsonArray}.
	 * @param context The patch context. May be {@code null} in circumstances involving old code.
	 * @param parent The parent array.
	 * @param index The index of the current element.
	 * @param elem The current element.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record Array(@Nullable PatchContext context, JsonArray parent, int index, @Nullable JsonElement elem) implements ElementContext {

		/**
		 * Constructs a new array-parent context.
		 * @param context The patch configuration.
		 * @param parent The parent array.
		 * @param index The index of the current element.
		 * @param elem The current element.
		 */
		public Array {}

		/**
		 * Constructs a new array-parent context.
		 * @deprecated Use {@link Array#Array(PatchContext, JsonArray, int, JsonElement)} instead.
		 * @param parent The parent array.
		 * @param index The index of the current element.
		 * @param elem The current element.
		 */
		@Deprecated(forRemoval = true)
		public Array(JsonArray parent, int index, @Nullable JsonElement elem) { this(null, parent, index, elem); }

		@Override
		public JsonArray parent() {
			return parent;
		}
	}
}