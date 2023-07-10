package net.enderturret.patched.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.exception.TraversalException;

/**
 * A patch that moves an element from one location to another.
 * @see PatchUtil#move(String, String)
 * @author EnderTurret
 */
public final class MovePatch extends JsonPatch {

	protected final JsonSelector from;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#move(String, String)} instead.
	 * @param path The location the element will be moved to.
	 * @param from The path to the element to move.
	 */
	protected MovePatch(String path, String from) {
		super(path);
		this.from = JsonSelector.of(from);
	}

	@Override
	protected String operation() {
		return "move";
	}

	@Override
	protected void writeAdditional(JsonObject obj, JsonSerializationContext context) {
		obj.addProperty("from", from.toString());
	}

	@Override
	public void patch(ElementContext root, PatchContext context) {
		final ElementContext removed = from.select(root, true);

		try {
			ElementContext added = path.select(root, true);
			added = last.add(added, true, null);

			PatchUtil.Operations.REMOVE.apply(removed);
			new PatchUtil.AddOperation(removed.elem(), false).apply(added);

			if (context.audit() != null) context.audit().recordMove(path.toString(), last.toString(), from.toString(), added);
		} catch (TraversalException e) {
			throw e.withPath(path + "/" + last);
		}
	}

	@Override
	protected void patchJson(ElementContext elem, PatchContext context) {}
}