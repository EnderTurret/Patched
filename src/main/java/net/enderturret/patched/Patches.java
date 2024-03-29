package net.enderturret.patched;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.enderturret.patched.exception.PatchingException;
import net.enderturret.patched.patch.JsonPatch;
import net.enderturret.patched.patch.PatchContext;

/**
 * Various utilities for reading and writing patches.
 * @author EnderTurret
 * @since 1.0.0
 */
public final class Patches {

	private Patches() {}

	/**
	 * Creates a {@link GsonBuilder} configured to serialize and deserialize patches with the given extensions enabled or disabled.
	 * @param testExtensions Whether extensions to the {@code test} operation are enabled. See {@link PatchContext}.
	 * @param patchedExtensions Whether extensions from this library are enabled. See {@link PatchContext}.
	 * @return The created {@code GsonBuilder}.
	 * @since 1.0.0
	 */
	public static GsonBuilder patchGson(boolean testExtensions, boolean patchedExtensions) {
		return new GsonBuilder()
				.registerTypeHierarchyAdapter(JsonPatch.class, new JsonPatch.Serializer(testExtensions, patchedExtensions))
				.serializeNulls();
	}

	/**
	 * {@link PatchContext} version of {@link #patchGson(boolean, boolean)}.
	 * @param context The {@code PatchContext}.
	 * @return The created {@code GsonBuilder}.
	 * @since 1.3.0
	 */
	public static GsonBuilder patchGson(PatchContext context) {
		return patchGson(context.testExtensions(), context.patchedExtensions());
	}

	/**
	 * Conveniently reads a {@link JsonPatch} using the given {@link Gson}.
	 * @param gson The {@link Gson} to use to read the patch. See {@link #patchGson(boolean, boolean)}.
	 * @param json The Json to parse a patch from.
	 * @return The patch.
	 * @throws PatchingException If an exception occurs reading the patch.
	 * @since 1.0.0
	 */
	public static JsonPatch readPatch(Gson gson, String json) throws PatchingException {
		return gson.fromJson(json, JsonPatch.class);
	}

	/**
	 * Conveniently reads a {@link JsonPatch} using the given {@link Gson}.
	 * @param gson The {@link Gson} to use to read the patch. See {@link #patchGson(boolean, boolean)}.
	 * @param reader The reader to read the patch Json from.
	 * @return The patch.
	 * @throws PatchingException If an exception occurs reading the patch.
	 * @since 1.0.0
	 */
	public static JsonPatch readPatch(Gson gson, Reader reader) throws PatchingException {
		return gson.fromJson(reader, JsonPatch.class);
	}

	/**
	 * Conveniently reads a {@link JsonPatch} using the given {@link Gson}.
	 * @param gson The {@link Gson} to use to read the patch. See {@link #patchGson(boolean, boolean)}.
	 * @param elem The Json to parse a patch from.
	 * @return The patch.
	 * @throws PatchingException If an exception occurs reading the patch.
	 * @since 1.0.0
	 */
	public static JsonPatch readPatch(Gson gson, JsonElement elem) throws PatchingException {
		return gson.fromJson(elem, JsonPatch.class);
	}
}