package net.enderturret.patched.patch;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.IDataSource;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;

/**
 * A patch that inserts or "pastes" something from a {@linkplain IDataSource data source}.
 * @see PatchUtil#paste(String, String, String, JsonElement)
 * @author EnderTurret
 * @since 1.5.0
 */
public final class PastePatch extends ManualTraversalPatch {

	private final String type;
	@Nullable
	private final JsonSelector from;
	@Nullable
	private final JsonElement value;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#paste(String, String, String, JsonElement)} instead.
	 * @param path The location the element will be placed.
	 * @param type The type to pass to the data source.
	 * @param from A path to the input element to pass to the data source. May be {@code null}.
	 * @param value Extra context to pass to the data source. May be {@code null}.
	 * @since 1.5.0
	 */
	protected PastePatch(String path, String type, @Nullable String from, @Nullable JsonElement value) {
		super(path);
		this.type = type;
		this.from = from == null ? null : JsonSelector.of(from);
		this.value = value;
	}

	@Override
	protected String operation() {
		return "paste";
	}

	@Override
	protected JsonElement write(JsonSerializationContext context, @Nullable String omitOperation) {
		final JsonObject obj = new JsonObject();

		if (!operation().equals(omitOperation))
			obj.addProperty("op", operation());

		obj.addProperty("type", type);

		if (from != null)
			obj.addProperty("from", from.toString());

		obj.addProperty("path", path + (last.isEmpty() ? "" : "/" + last));

		if (value != null)
			obj.add("value", value);

		return obj;
	}

	@Override
	public void patch(ElementContext root, PatchContext context) throws PatchingException, TraversalException {
		if (!context.patchedExtensions())
			throw new PatchingException("Cannot paste: Patched extensions are not enabled!");

		if (context.dataSource() == null)
			throw new PatchingException("Cannot paste: no data source available!");

		final ElementContext parent = path.select(root, true);
		final JsonElement from = this.from != null ? this.from.select(root, true).elem() : null;

		final JsonElement pasted = context.dataSource().getData(type, from, value);
		if (pasted == null)
			throw new PatchingException("Unknown paste data source type: '" + type + "'");

		try {
			final ElementContext pastedContext = last.add(parent, true, pasted);
			if (context.audit() != null) context.audit().recordAdd(path.toString(), last.toString(), pastedContext);
		} catch (TraversalException e) {
			throw e.withPath(path + "/" + last);
		}
	}
}