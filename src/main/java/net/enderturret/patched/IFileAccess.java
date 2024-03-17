package net.enderturret.patched;

import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.patch.IncludePatch;
import net.enderturret.patched.patch.JsonPatch;

/**
 * Provides access to files (which need not be actual files on the filesystem) for {@linkplain IncludePatch include patches},
 * so that they can include other patches.
 * @author EnderTurret
 */
public interface IFileAccess {

	/**
	 * <p>
	 * Reads and returns the contents of an {@linkplain IncludePatch included patch}.
	 * </p>
	 * <p>
	 * One of the use cases of include patches is to perform the same series of patches on different files,
	 * and because of that it's recommended for the implementor of this method to cache the returned result
	 * to minimize the performance impact of duplicate queries.
	 * </p>
	 * @param path The path to the patch, specified by the include patch.
	 * @return The contents of the patch. May be {@code null} if no such patch exists.
	 */
	@Nullable
	public JsonPatch readIncludedPatch(String path);
}