package net.enderturret.patched.patch;

import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;

/**
 * An intermediate subclass of {@link JsonPatch} that makes {@link #patch(ElementContext, PatchContext)} abstract instead of {@link #patchJson(ElementContext, PatchContext)}.
 * @author EnderTurret
 */
abstract class ManualTraversalPatch extends JsonPatch {

	protected ManualTraversalPatch(@Nullable String path) {
		super(path);
	}

	@Override
	protected void patchJson(ElementContext elem, PatchContext context) throws PatchingException, TraversalException {}

	@Override
	public abstract void patch(ElementContext root, PatchContext context) throws PatchingException, TraversalException;
}