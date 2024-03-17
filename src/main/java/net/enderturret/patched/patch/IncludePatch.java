package net.enderturret.patched.patch;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.enderturret.patched.ElementContext;
import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.exception.TraversalException;

/**
 * <p>
 * An include patch allows including some other patch in another patch, which can simplify bulk patching
 * of multiple files where all of the patches are identical.
 * </p>
 * <p>
 * Include patches can only be used if {@linkplain PatchContext#patchedExtensions() Patched extensions} are enabled <i>and</i>
 * a {@linkplain PatchContext#fileAccess() file access} is installed in the {@link PatchContext}.
 * </p>
 * @author EnderTurret
 */
public final class IncludePatch extends ManualTraversalPatch {

	private final String path;

	/**
	 * Constructs a new {@code IncludePatch}.
	 * @param path The path to the patch file to include.
	 */
	protected IncludePatch(String path) {
		super(null);
		this.path = path;
	}

	@Override
	public void patch(ElementContext root, PatchContext context) throws PatchingException, TraversalException {
		if (!context.patchedExtensions())
			throw new PatchingException("Attempted to include a patch, but Patched extensions are not enabled!");

		if (context.fileAccess() == null)
			throw new PatchingException("Attempted to include a patch, but no file access has been installed!");

		final JsonPatch patch = context.fileAccess().readIncludedPatch(path);
		if (patch == null) throw new PatchingException("Attempted to include a patch that doesn't exist: " + path);

		patch.patch(root, context);
	}

	@Override
	protected String operation() {
		return "include";
	}

	@Override
	protected void writeAdditional(JsonObject obj, JsonSerializationContext context) {
		obj.addProperty("path", path);
	}
}