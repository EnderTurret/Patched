package net.enderturret.patched.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.patch.context.ElementContext;
import net.enderturret.patched.patch.context.PatchContext;

/**
 * A patch that adds an element to something, such as a Json object or array.
 * @see PatchUtil#add(String, JsonElement)
 * @author EnderTurret
 * @since 1.0.0
 */
public final class AddPatch extends JsonPatch {

	private final JsonElement value;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#add(String, JsonElement)} instead.
	 * @param path The location the element will be placed.
	 * @param value The element that will be added.
	 * @since 1.0.0
	 */
	protected AddPatch(String path, JsonElement value) {
		super(path);
		this.value = value;
	}

	@Override
	protected String operation() {
		return "add";
	}

	@Override
	protected void writeAdditional(JsonObject obj, JsonSerializationContext context) {
		obj.add("value", value);
	}

	@Override
	public void patch(ElementContext root, PatchContext context) {
		final ElementContext e = path.add(root, true, value);
		if (context.audit() != null) context.audit().recordAdd(root, path, e);
	}
}