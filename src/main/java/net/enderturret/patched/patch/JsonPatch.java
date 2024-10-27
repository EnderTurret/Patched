package net.enderturret.patched.patch;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.JsonSelector.CompoundSelector;
import net.enderturret.patched.Patches;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;

/**
 * <p>A patch that can be used to modify Json data.</p>
 * <p>This is the base class that all of the other patches extend from.<p>
 * <p>You can obtain one of these patches using {@link Patches}.</p>
 * @author EnderTurret
 * @since 1.0.0
 */
public abstract class JsonPatch {

	@Nullable
	protected final JsonSelector path;
	@Nullable
	protected JsonSelector last;

	/**
	 * Constructs a new {@code JsonPatch}.
	 * @param path The path that will be followed to find the element passed to {@link #patchJson(ElementContext, PatchContext)}, or {@code null} to handle this yourself.
	 * @since 1.0.0
	 */
	protected JsonPatch(@Nullable String path) {
		if (path != null) {
			final CompoundSelector selector = JsonSelector.of(path);
			if (selector.isEmpty()) {
				this.path = last = new JsonSelector.EmptySelector();
			} else {
				final CompoundSelector tempPath = new CompoundSelector(selector.path(0, selector.size() - 1));

				this.path = tempPath.size() == 0 ? new JsonSelector.EmptySelector() : tempPath;

				last = selector.path(selector.size() - 1);
			}
		} else {
			this.path = null;
			last = null;
		}
	}

	/**
	 * <p>Applies this patch to the given root element. The element need not be a root element, but usually is. In cases where the element is <i>not</i> a root element, use the corresponding {@link ElementContext}.</p>
	 * @param root The root element to apply the patch to.
	 * @param context The {@link PatchContext}. This customizes what features are available, among other things.
	 * @throws PatchingException If the patch could not be applied for some reason.
	 * @throws TraversalException If a path in the patch could not be traversed.
	 * @throws UnsupportedOperationException If someone passes {@code null} into {@link #JsonPatch(String)} and does not override this method.
	 * @since 1.0.0
	 */
	public void patch(ElementContext root, PatchContext context) throws PatchingException, TraversalException {
		if (path == null) throw new UnsupportedOperationException("Patch was not implemented correctly! (op = " + operation() + ")");
		patchJson(path.select(root, true), context);
	}

	/**
	 * @deprecated Use {@link #patch(JsonDocument, PatchContext)}, as that allows patches to change the root element.
	 * {@link JsonElement} version of {@link #patch(ElementContext, PatchContext)}.
	 * @param root The root element to apply the patch to.
	 * @param context The {@link PatchContext}. This customizes what features are available, among other things.
	 * @throws PatchingException If the patch could not be applied for some reason.
	 * @throws TraversalException If a path in the patch could not be traversed.
	 * @since 1.0.0
	 */
	@Deprecated(forRemoval = true)
	public final void patch(JsonElement root, PatchContext context) throws PatchingException, TraversalException {
		patch(new ElementContext.NoParent(root), context);
	}

	/**
	 * {@link JsonDocument} version of {@link #patch(ElementContext, PatchContext)}.
	 * @param root The document to apply the patch to.
	 * @param context The {@link PatchContext}. This customizes what features are available, among other things.
	 * @throws PatchingException If the patch could not be applied for some reason.
	 * @throws TraversalException If a path in the patch could not be traversed.
	 * @since 1.3.0
	 */
	public final void patch(JsonDocument root, PatchContext context) throws PatchingException, TraversalException {
		patch(new ElementContext.Document(context, root), context);
	}

	/**
	 * Applies this patch to the given element. The path will have already been followed, so this is the element being patched.
	 * @param elem The context representing the element to patch.
	 * @param context The {@link PatchContext}. This customizes what features are available, among other things.
	 * @throws PatchingException If the patch could not be applied for some reason.
	 * @throws TraversalException If a path in the patch could not be traversed.
	 * @see #patch(ElementContext, PatchContext)
	 * @since 1.0.0
	 */
	protected abstract void patchJson(ElementContext elem, PatchContext context) throws PatchingException, TraversalException;

	/**
	 * @return The operation this patch applies.
	 * @see #write(JsonSerializationContext, String)
	 * @see Serializer#serialize(JsonPatch, Type, JsonSerializationContext)
	 * @since 1.0.0
	 */
	protected abstract String operation();

	/**
	 * Converts this {@link JsonPatch} into a {@link JsonElement} and returns it.
	 * @param context The context to use for serialization. See {@link JsonSerializer}.
	 * @param omitOperation If non-{@code null}, specifies an operation that can be omitted from the output. This is useful for nicer output in places where the operation is limited or defaulted.
	 * @return The {@link JsonElement}.
	 * @since 1.0.0
	 */
	protected JsonElement write(JsonSerializationContext context, @Nullable String omitOperation) {
		final JsonObject obj = new JsonObject();

		if (!operation().equals(omitOperation))
			obj.addProperty("op", operation());

		if (path != null)
			obj.addProperty("path", path + (last.isEmpty() ? "" : "/" + last));

		writeAdditional(obj, context);

		return obj;
	}

	/**
	 * <p>Adds any additional information the patch contains to the given object.</p>
	 * <p>This is required for patches like {@link AddPatch} to serialize correctly.</p>
	 * @param obj The object to add information to.
	 * @param context The context to use for serialization. See {@link JsonSerializer}.
	 * @since 1.0.0
	 */
	protected void writeAdditional(JsonObject obj, JsonSerializationContext context) {}

	/**
	 * The (de)serializer for Json patches.
	 * @author EnderTurret
	 * @see Patches#patchGson(boolean, boolean)
	 * @since 1.0.0
	 */
	public static class Serializer implements JsonDeserializer<JsonPatch>, JsonSerializer<JsonPatch> {

		private final String defaultOp;
		private final boolean enforceOp;

		private final boolean testExtensions;
		private final boolean patchedExtensions;

		/**
		 * Constructs a new {@code Serializer}.
		 * @param defaultOp The default operation. This is the operation used if one isn't provided in the patch.
		 * @param enforceOp Whether to enforce the default operation. This can be used to force all read patches to be a specific kind.
		 * @param testExtensions Whether to enable deserializing patches using the test extensions -- see {@link PatchContext}.
		 * @param patchedExtensions Whether to enable deserializing patches using the "find" operation -- see {@link PatchContext}.
		 * @since 1.0.0
		 */
		public Serializer(@Nullable String defaultOp, boolean enforceOp, boolean testExtensions, boolean patchedExtensions) {
			this.defaultOp = defaultOp;
			this.enforceOp = enforceOp;
			this.testExtensions = testExtensions;
			this.patchedExtensions = patchedExtensions;
			if (enforceOp && defaultOp == null)
				// What does a null operation even mean?
				throw new IllegalArgumentException("Cannot enforce null operation!");
		}

		/**
		 * Equivalent to {@link #Serializer(String, boolean, boolean, boolean)} with no default operation.
		 * @param testExtensions Whether to enable deserializing patches using the test extensions -- see {@link PatchContext}.
		 * @param patchedExtensions Whether to enable deserializing patches using the "find" operation -- see {@link PatchContext}.
		 * @since 1.0.0
		 */
		public Serializer(boolean testExtensions, boolean patchedExtensions) {
			this(null, false, testExtensions, patchedExtensions);
		}

		/**
		 * Equivalent to {@link #Serializer(boolean, boolean)} with all extensions enabled.
		 * @since 1.0.0
		 */
		public Serializer() {
			this(null, false, true, true);
		}

		/**
		 * Returns the element with the given name from the given object, and asserts that it exists.
		 * @param obj The object to get the element from.
		 * @param path The name of the element.
		 * @return The element.
		 * @throws PatchingException If the element does not exist.
		 * @since 1.0.0
		 */
		private static JsonElement get(JsonObject obj, String path) throws PatchingException {
			final JsonElement elem = obj.get(path);

			if (elem == null)
				throw new PatchingException("'" + path + "' is missing!");

			return elem;
		}

		/**
		 * Returns the string with the given name from the given object.
		 * @param obj The object to get the string from.
		 * @param path The name of the string.
		 * @return The string.
		 * @throws PatchingException If the string does not exist or is not a string.
		 * @see #get(JsonObject, String)
		 * @since 1.0.0
		 */
		private static String getString(JsonObject obj, String path) throws PatchingException {
			final JsonElement elem = get(obj, path);

			if (!elem.isJsonPrimitive() || !elem.getAsJsonPrimitive().isString())
				throw new PatchingException("'" + path + "' must be a string (was: " + elem + ")!");

			return elem.getAsString();
		}

		@Override
		public JsonElement serialize(JsonPatch src, Type typeOfSrc, JsonSerializationContext context) {
			return src.write(context, defaultOp);
		}

		@Override
		public JsonPatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonArray())
				// This should be safe, as this serializer will be called on each child, and not the array as a whole.
				return new CompoundPatch(context.deserialize(json, JsonPatch[].class));

			return deserialize(defaultOp, enforceOp, json, typeOfT, context);
		}

		@SuppressWarnings("cast")
		protected <T extends JsonPatch> T deserialize(String defaultOp, boolean enforceOp, JsonElement json, Class<T> typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return (T) deserialize(defaultOp, enforceOp, json, (Type) typeOfT, context);
		}

		protected JsonPatch deserialize(String defaultOp, boolean enforceOp, JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			final JsonObject obj = json.getAsJsonObject();

			String op = !obj.has("op") ? null : obj.get("op").getAsString();

			if (op == null && defaultOp != null)
				op = defaultOp;

			if (op == null)
				throw new PatchingException("Missing operation!");

			if (enforceOp && !defaultOp.equals(op))
				throw new PatchingException("Unexpected operation \"" + op + "\": only " + defaultOp + " is allowed here.");

			return switch (op) {
				case "test" -> {
					final String type = patchedExtensions && obj.has("type") ? obj.get("type").getAsString() : null;
					final String path = type != null && !obj.has("path") ? null : getString(obj, "path");
					final JsonElement value = testExtensions || type != null ? obj.get("value") : get(obj, "value");
					final boolean inverse = testExtensions && obj.has("inverse") && obj.get("inverse").getAsBoolean();

					yield new TestPatch(type, path, value, inverse);
				}
				case "add" -> new AddPatch(getString(obj, "path"), get(obj, "value"));
				case "remove" -> new RemovePatch(getString(obj, "path"));
				case "copy" -> new CopyPatch(getString(obj, "path"), getString(obj, "from"));
				case "move" -> new MovePatch(getString(obj, "path"), getString(obj, "from"));
				case "replace" -> new ReplacePatch(getString(obj, "path"), get(obj, "value"));

				case "find" -> {
					if (!patchedExtensions)
						throw new PatchingException("Unsupported operation 'find': Patched extensions are not enabled.");

					final List<TestPatch> tests;

					if (obj.get("test") instanceof JsonArray testArray) {
						final List<TestPatch> list = new ArrayList<>();

						for (JsonElement elem : testArray)
							list.add(deserialize("test", true, elem, TestPatch.class, context));

						tests = List.copyOf(list);
					} else
						tests = List.of(deserialize("test", true, obj.get("test"), TestPatch.class, context));

					yield new FindPatch(
							getString(obj, "path"),
							tests,
							context.deserialize(obj.get("then"), JsonPatch.class),
							obj.has("multi") && obj.get("multi").getAsBoolean());
				}
				case "include" -> {
					if (!patchedExtensions)
						throw new PatchingException("Unsupported operation 'include': Patched extensions are not enabled.");

					yield new IncludePatch(getString(obj, "path"));
				}
				case "paste" -> {
					if (!patchedExtensions)
						throw new PatchingException("Unsupported operation 'paste': Patched extensions are not enabled.");

					yield new PastePatch(
							getString(obj, "path"),
							getString(obj, "type"),
							obj.has("from") ? getString(obj, "from") : null,
							obj.get("value"));
				}

				default -> throw new PatchingException("Unknown operation '" + op + "'");
			};
		}
	}
}