package net.enderturret.patched;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import net.enderturret.patched.exception.PatchingException;

/**
 * Provides a source of data for {@code paste} patches.
 * @author EnderTurret
 * @since 1.5.0
 */
public interface IDataSource {

	/**
	 * Retrieves the data associated with the specified type, optionally using the patch-provided {@code value} argument.
	 * If the specified type is unknown, this method returns {@code null}.
	 * @param type The {@code type} field specified in the patch. This determines which data is to be retrieved.
	 * @param from The element pointed to by the {@code from} path in the patch. May be {@code null} if the patch did not specify one.
	 * @param value The {@code value} field specified in the patch. May be {@code null} if the patch did not specify one.
	 * @return The corresponding data, or {@code null} if there is not any.
	 * @throws PatchingException If an error occurs retrieving the data (such as missing or invalid context).
	 */
	@Nullable
	public JsonElement getData(String type, @Nullable JsonElement from, @Nullable JsonElement value) throws PatchingException;
}