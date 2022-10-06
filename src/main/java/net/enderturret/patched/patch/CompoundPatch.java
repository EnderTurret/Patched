package net.enderturret.patched.patch;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;

/**
 * Represents a patch made of multiple patches.
 * @see PatchUtil#compound(JsonPatch...)
 * @author EnderTurret
 */
public class CompoundPatch extends JsonPatch {

	private final JsonPatch[] patches;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#compound(JsonPatch...)} instead.
	 * @param patches The patches that will be contained within this {@link CompoundPatch}.
	 */
	protected CompoundPatch(JsonPatch[] patches) {
		super(null);
		this.patches = patches;
	}

	@Override
	public void patch(ElementContext root, PatchContext context) {
		for (JsonPatch patch : patches) {
			if (patch instanceof TestPatch tp && !tp.test(root.elem(), context))
				return;
			patch.patch(root, context);
		}
	}

	@Override
	protected void patchJson(ElementContext elem, PatchContext context) {}

	@Override
	protected String operation() { return null; }

	@Override
	protected JsonArray write(JsonSerializationContext context, String omitOperation) {
		final JsonArray arr = new JsonArray(patches.length);

		for (JsonPatch patch : patches)
			arr.add(context.serialize(patch));

		return arr;
	}
}