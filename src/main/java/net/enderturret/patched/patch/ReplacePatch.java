package net.enderturret.patched.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.exception.TraversalException;

/**
 * A patch that replaces an element with something else.
 * @see PatchUtil#replace(String, JsonElement)
 * @author EnderTurret
 */
public final class ReplacePatch extends JsonPatch {

	private final JsonElement value;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#replace(String, JsonElement)} instead.
	 * @param path The path to the element to replace.
	 * @param value The value to replace the element with.
	 */
	protected ReplacePatch(String path, JsonElement value) {
		super(path);
		this.value = value;
	}

	@Override
	protected String operation() {
		return "replace";
	}

	@Override
	protected void writeAdditional(JsonObject obj, JsonSerializationContext context) {
		obj.add("value", value);
	}

	@Override
	protected void patchJson(ElementContext elem, PatchContext context) {
		try {
			last.replace(elem, true, value);
			if (context.audit() != null) context.audit().recordReplace(path.toString(), last.toString());
		} catch (TraversalException e) {
			throw e.withPath(path + "/" + last);
		}
	}
}