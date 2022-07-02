package net.enderturret.patched.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.exception.TraversalException;

/**
 * A patch that adds an element to something.
 * @see PatchUtil#add(String, JsonElement)
 * @author EnderTurret
 */
public final class AddPatch extends JsonPatch {

	private final JsonElement value;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#add(String, JsonElement)} instead.
	 * @param path The location the element will be placed.
	 * @param value The element that will be added.
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
	protected void patchJson(ElementContext elem, PatchContext context) {
		try {
			last.add(elem, true, value);
		} catch (TraversalException e) {
			throw e.withPath(path + "/" + last);
		}
	}
}