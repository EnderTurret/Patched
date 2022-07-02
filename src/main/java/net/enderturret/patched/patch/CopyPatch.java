package net.enderturret.patched.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.exception.TraversalException;

/**
 * A patch that copies an element from one location to another.
 * @see PatchUtil#copy(String, String)
 * @author EnderTurret
 */
public final class CopyPatch extends JsonPatch {

	protected final JsonSelector from;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#copy(String, String)} instead.
	 * @param path The location the element will be copied to.
	 * @param from The path to the element to copy.
	 */
	protected CopyPatch(String path, String from) {
		super(path);
		this.from = JsonSelector.of(from);
	}

	@Override
	protected String operation() {
		return "copy";
	}

	@Override
	protected void writeAdditional(JsonObject obj, JsonSerializationContext context) {
		obj.addProperty("from", from.toString());
	}

	@Override
	public void patch(JsonElement root, PatchContext context) {
		final JsonElement copied = from.select(root, true).elem();

		try {
			last.add(path.select(root, true), true, copied);
		} catch (TraversalException e) {
			throw e.withPath(path + "/" + last);
		}
	}

	@Override
	protected void patchJson(ElementContext elem, PatchContext context) {}
}