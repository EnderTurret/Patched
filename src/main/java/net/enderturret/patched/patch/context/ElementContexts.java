package net.enderturret.patched.patch.context;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.JsonDocument;

public final class ElementContexts {

	private ElementContexts() {}

	/**
	 * <p>An element without a parent.</p>
	 * <p>Such an element cannot have operations applied to it.</p>
	 * @param context The patch context.
	 * @param elem The element.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record NoParent(PatchContext context, JsonElement elem) implements ElementContext {

		/**
		 * Constructs a new no-parent context.
		 * @param context The patch configuration.
		 * @param elem The element in context.
		 * @since 1.3.0
		 */
		public NoParent {
			Objects.requireNonNull(context, "context");
			Objects.requireNonNull(elem, "elem");
		}

		@Override
		public JsonElement parent() {
			return null;
		}
	}

	/**
	 * An element whose parent is the json document -- allows swapping out this element without needing to change 300 {@code void}s to {@code JsonElement}s.
	 * @param context The patch context.
	 * @param doc The document.
	 * @author EnderTurret
	 * @since 1.3.0
	 */
	public static record Document(PatchContext context, JsonDocument doc) implements ElementContext {

		/**
		 * Constructs a new root document context.
		 * @param context The patch configuration.
		 * @param doc The document in context.
		 * @since 1.3.0
		 */
		public Document {
			Objects.requireNonNull(context, "context");
			Objects.requireNonNull(doc, "doc");
		}

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
	 * @param context The patch context.
	 * @param parent The parent object.
	 * @param name The name of the current element.
	 * @param elem The current element.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record Object(PatchContext context, JsonObject parent, String name, @Nullable JsonElement elem) implements ElementContext {

		/**
		 * Constructs a new object-parent context.
		 * @param context The patch configuration.
		 * @param parent The parent object.
		 * @param name The name of the current element.
		 * @param elem The current element.
		 * @since 1.3.0
		 */
		public Object {
			Objects.requireNonNull(context, "context");
			Objects.requireNonNull(parent, "parent");
			Objects.requireNonNull(name, "name");
		}

		@Override
		public JsonObject parent() {
			return parent;
		}
	}

	/**
	 * An element whose parent is a {@link JsonArray}.
	 * @param context The patch context.
	 * @param parent The parent array.
	 * @param index The index of the current element.
	 * @param elem The current element.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static record Array(PatchContext context, JsonArray parent, int index, @Nullable JsonElement elem) implements ElementContext {

		/**
		 * Constructs a new array-parent context.
		 * @param context The patch configuration.
		 * @param parent The parent array.
		 * @param index The index of the current element.
		 * @param elem The current element.
		 * @since 1.3.0
		 */
		public Array {
			Objects.requireNonNull(context, "context");
			Objects.requireNonNull(parent, "parent");
		}

		@Override
		public JsonArray parent() {
			return parent;
		}
	}
}