package net.enderturret.patched.patch;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;

/**
 * Represents a patch made of multiple patches.
 * @see PatchUtil#compound(JsonPatch...)
 * @author EnderTurret
 * @since 1.0.0
 */
public class CompoundPatch extends ManualTraversalPatch {

	private final JsonPatch[] patches;

	/**
	 * Users: don't instantiate directly. Use {@link PatchUtil#compound(JsonPatch...)} instead.
	 * @param patches The patches that will be contained within this {@link CompoundPatch}.
	 * @since 1.0.0
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
	protected String operation() { return null; }

	@Override
	protected JsonArray write(JsonSerializationContext context, @Nullable String omitOperation) {
		final JsonArray arr = new JsonArray(patches.length);

		for (JsonPatch patch : patches)
			arr.add(context.serialize(patch));

		return arr;
	}
}