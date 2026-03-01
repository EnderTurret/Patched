package net.enderturret.patched.patch.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.JsonSelector;

/**
 * {@code ElementContexts} holds all of the {@link ElementContext} implementations.
 * @author EnderTurret
 * @since 2.0.0
 */
public final class ElementContexts {

	private ElementContexts() {}

	private static abstract class AbstractElementContext implements ElementContext {

		protected final PatchContext context;
		protected final JsonDocument doc;
		protected Map<String, JsonSelector> placeholders;

		AbstractElementContext(PatchContext context, JsonDocument doc, @Nullable Map<String, JsonSelector> placeholders) {
			this.context = Objects.requireNonNull(context, "context");
			this.doc = doc;
			this.placeholders = placeholders;
		}

		AbstractElementContext(ElementContext context) {
			this(context.context(), context.doc(), ((AbstractElementContext) context).placeholders);
		}

		@Override
		public PatchContext context() {
			return context;
		}

		@Override
		public JsonDocument doc() {
			return doc;
		}

		@Override
		@Nullable
		public JsonSelector getPlaceholder(String name) {
			return placeholders != null ? placeholders.get(name) : null;
		}

		@Override
		public void setPlaceholder(String name, @Nullable JsonSelector value) {
			if (placeholders == null)
				placeholders = new HashMap<>(4);

			if (value == null) placeholders.remove(name);
			else placeholders.put(name, value);
		}
	}

	/**
	 * <p>An element without a parent.</p>
	 * <p>Such an element cannot have operations applied to it.</p>
	 * @author EnderTurret
	 * @since 1.0.0
	 */
	public static final class NoParent extends AbstractElementContext {

		private final JsonElement elem;

		/**
		 * Constructs a new no-parent context.
		 * @param context The patch configuration.
		 * @param doc The root document.
		 * @param placeholders The current placeholders.
		 * @param elem The element in context.
		 * @since 2.0.0
		 */
		public NoParent(PatchContext context, JsonDocument doc, @Nullable Map<String, JsonSelector> placeholders, JsonElement elem) {
			super(context, doc, placeholders);
			this.elem = Objects.requireNonNull(elem, "elem");
		}

		/**
		 * Constructs a new no-parent context.
		 * @param derived An {@code ElementContext} to derive the {@code PatchContext}, document, and placeholders from.
		 * @param elem The element in context.
		 * @since 2.0.0
		 */
		public NoParent(ElementContext derived, JsonElement elem) {
			super(derived);
			this.elem = Objects.requireNonNull(elem, "elem");
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
	public static final class Document extends AbstractElementContext {

		/**
		 * Constructs a new root document context.
		 * @param context The patch configuration.
		 * @param placeholders The current placeholders.
		 * @param doc The document in context.
		 * @since 2.0.0
		 */
		public Document(PatchContext context, @Nullable Map<String, JsonSelector> placeholders, JsonDocument doc) {
			super(context, doc, placeholders);
		}

		/**
		 * Constructs a new root document context.
		 * @param derived An {@code ElementContext} to derive the {@code PatchContext} and placeholders from.
		 * @param doc The document in context.
		 * @since 1.3.0
		 */
		public Document(ElementContext derived, JsonDocument doc) {
			this(derived.context(), ((AbstractElementContext) derived).placeholders, doc);
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
	public static final class Object extends AbstractElementContext {

		private final JsonObject parent;
		private final String name;
		private final @Nullable JsonElement elem;

		/**
		 * Constructs a new object-parent context.
		 * @param context The patch configuration.
		 * @param doc The root document.
		 * @param placeholders The current placeholders.
		 * @param parent The parent object.
		 * @param name The name of the current element.
		 * @param elem The current element.
		 * @since 2.0.0
		 */
		public Object(PatchContext context, JsonDocument doc, @Nullable Map<String, JsonSelector> placeholders, JsonObject parent, String name, @Nullable JsonElement elem) {
			super(context, doc, placeholders);
			this.parent = Objects.requireNonNull(parent, "parent");
			this.name = Objects.requireNonNull(name, "name");
			this.elem = elem;
		}

		/**
		 * Constructs a new object-parent context.
		 * @param context The element context to inherit the patch context and placeholders from.
		 * @param parent The parent object.
		 * @param name The name of the current element.
		 * @param elem The current element.
		 * @since 2.0.0
		 */
		public Object(ElementContext context, JsonObject parent, String name, @Nullable JsonElement elem) {
			this(context.context(), ((AbstractElementContext) context).doc, ((AbstractElementContext) context).placeholders, parent, name, elem);
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
	public static final class Array extends AbstractElementContext {

		private final JsonArray parent;
		private final int index;
		private final @Nullable JsonElement elem;

		/**
		 * Constructs a new array-parent context.
		 * @param context The patch configuration.
		 * @param doc The root document.
		 * @param placeholders The current placeholders.
		 * @param parent The parent array.
		 * @param index The index of the current element.
		 * @param elem The current element.
		 * @since 2.0.0
		 */
		public Array(PatchContext context, JsonDocument doc, @Nullable Map<String, JsonSelector> placeholders, JsonArray parent, int index, @Nullable JsonElement elem) {
			super(context, doc, placeholders);
			this.parent = Objects.requireNonNull(parent, "parent");
			this.index = index;
			this.elem = elem;
		}

		/**
		 * Constructs a new array-parent context.
		 * @param context The element context to inherit the patch context and placeholders from.
		 * @param parent The parent array.
		 * @param index The index of the current element.
		 * @param elem The current element.
		 * @since 2.0.0
		 */
		public Array(ElementContext context, JsonArray parent, int index, @Nullable JsonElement elem) {
			this(context.context(), ((AbstractElementContext) context).doc, ((AbstractElementContext) context).placeholders, parent, index, elem);
		}

		/**
		 * Constructs a new array-parent context.
		 * @param context The patch configuration.
		 * @param doc The root document.
		 * @param parent The parent array.
		 * @param index The index of the current element.
		 * @param elem The current element.
		 * @since 2.0.0
		 */
		public Array(PatchContext context, JsonDocument doc, JsonArray parent, int index, @Nullable JsonElement elem) {
			this(context, doc, null, parent, index, elem);
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