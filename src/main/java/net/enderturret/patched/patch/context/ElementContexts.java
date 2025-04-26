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
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static final class NoParent implements ElementContext {

		private final PatchContext context;
		private final JsonElement elem;

		/**
		 * Constructs a new no-parent context.
		 * @param context The patch configuration.
		 * @param elem The element in context.
		 * @since 1.3.0
		 */
		public NoParent(PatchContext context, JsonElement elem) {
			this.context = Objects.requireNonNull(context, "context");
			this.elem = Objects.requireNonNull(elem, "elem");
		}

		@Override
		public PatchContext context() {
			return context;
		}

		@Override
		public JsonElement elem() {
			return elem;
		}

		@Override
		public JsonElement parent() {
			return null;
		}
	}

	/**
	 * An element whose parent is the json document -- allows swapping out this element without needing to change 300 {@code void}s to {@code JsonElement}s.
	 * @author EnderTurret
	 * @since 1.3.0
	 */
	public static final class Document implements ElementContext {

		private final PatchContext context;
		private final JsonDocument doc;

		/**
		 * Constructs a new root document context.
		 * @param context The patch configuration.
		 * @param doc The document in context.
		 * @since 1.3.0
		 */
		public Document(PatchContext context, JsonDocument doc) {
			this.context = Objects.requireNonNull(context, "context");
			this.doc = Objects.requireNonNull(doc, "doc");
		}

		/**
		 * Returns the root document.
		 * @return The document.
		 * @since 1.3.0
		 */
		public JsonDocument doc() {
			return doc;
		}

		@Override
		public PatchContext context() {
			return context;
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
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static final class Object implements ElementContext {

		private final PatchContext context;
		private final JsonObject parent;
		private final String name;
		private final @Nullable JsonElement elem;

		/**
		 * Constructs a new object-parent context.
		 * @param context The patch configuration.
		 * @param parent The parent object.
		 * @param name The name of the current element.
		 * @param elem The current element.
		 * @since 1.3.0
		 */
		public Object(PatchContext context, JsonObject parent, String name, @Nullable JsonElement elem) {
			this.context = Objects.requireNonNull(context, "context");
			this.parent = Objects.requireNonNull(parent, "parent");
			this.name = Objects.requireNonNull(name, "name");
			this.elem = elem;
		}

		/**
		 * Returns the name.
		 * @return The name.
		 * @since 1.0.0
		 */
		public String name() {
			return name;
		}

		@Override
		public PatchContext context() {
			return context;
		}

		@Override
		@Nullable
		public JsonElement elem() {
			return elem;
		}

		@Override
		public JsonObject parent() {
			return parent;
		}
	}

	/**
	 * An element whose parent is a {@link JsonArray}.
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static final class Array implements ElementContext {

		private final PatchContext context;
		private final JsonArray parent;
		private final int index;
		private final @Nullable JsonElement elem;

		/**
		 * Constructs a new array-parent context.
		 * @param context The patch configuration.
		 * @param parent The parent array.
		 * @param index The index of the current element.
		 * @param elem The current element.
		 * @since 1.3.0
		 */
		public Array(PatchContext context, JsonArray parent, int index, @Nullable JsonElement elem) {
			this.context = Objects.requireNonNull(context, "context");
			this.parent = Objects.requireNonNull(parent, "parent");
			this.index = index;
			this.elem = elem;
		}

		/**
		 * Returns the index.
		 * @return The index.
		 * @since 1.0.0
		 */
		public int index() {
			return index;
		}

		@Override
		public PatchContext context() {
			return context;
		}

		@Override
		@Nullable
		public JsonElement elem() {
			return elem;
		}

		@Override
		public JsonArray parent() {
			return parent;
		}
	}
}