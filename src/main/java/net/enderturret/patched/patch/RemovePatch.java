package net.enderturret.patched.patch;

import net.enderturret.patched.patch.context.ElementContext;
import net.enderturret.patched.patch.context.PatchContext;

/**
 * A patch that removes an element.
 * @see PatchUtil#remove(String)
 * @author EnderTurret
 * @since 1.0.0
 */
public final class RemovePatch extends JsonPatch {

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#remove(String)} instead.
	 * @param path The path to the element to remove.
	 * @since 1.0.0
	 */
	protected RemovePatch(String path) {
		super(path);
	}

	@Override
	protected String operation() {
		return "remove";
	}

	@Override
	public void patch(ElementContext root, PatchContext context) {
		final ElementContext after = path.remove(root, true);
		if (context.audit() != null) context.audit().recordRemove(root, path, after.elem());
	}
}