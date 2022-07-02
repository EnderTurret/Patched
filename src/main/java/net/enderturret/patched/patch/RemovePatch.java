package net.enderturret.patched.patch;

import com.google.gson.JsonElement;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.exception.TraversalException;

/**
 * A patch that removes an element.
 * @see PatchUtil#remove(String)
 * @author EnderTurret
 */
public final class RemovePatch extends JsonPatch {

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#remove(String)} instead.
	 * @param path The path to the element to remove.
	 */
	protected RemovePatch(String path) {
		super(path);
	}

	@Override
	protected String operation() {
		return "remove";
	}

	@Override
	protected void patchJson(ElementContext elem, PatchContext context) {
		try {
			last.remove(elem, true);
		} catch (TraversalException e) {
			throw e.withPath(path + "/" + last);
		}
	}
}