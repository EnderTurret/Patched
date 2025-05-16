package net.enderturret.patched.patch.context;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.JsonDocument;
import net.enderturret.patched.JsonSelector;
import net.enderturret.patched.exception.TraversalException;

/**
 * <p>Represents a {@link JsonElement} and its parent element.</p>
 * <p>This class acts as an abstraction layer to the different kinds of elements that can be parents.</p>
 * @author EnderTurret
 * @since 1.0.0
 */
public interface ElementContext {

	/**
	 * @return The patch context.
	 * @since 1.3.0
	 */
	public PatchContext context();

	@Nullable
	public JsonSelector getPlaceholder(String name);
	public void setPlaceholder(String name, @Nullable JsonSelector value);

	/**
	 * Returns the root document, for use with absolute paths.
	 * @return The root document.
	 * @since 2.0.0
	 */
	public JsonDocument doc();

	/**
	 * Returns the parent element.
	 * @return The parent element.
	 * @since 1.0.0
	 */
	@Nullable
	public JsonElement parent();

	/**
	 * Returns the current element.
	 * @return The element.
	 * @since 1.0.0
	 */
	@Nullable
	public JsonElement elem();

	/**
	 * Creates a new {@link ElementContext} with this set as its parent and the given values as its name and element.
	 * @param name The name of {@code elem}.
	 * @param elem The element wrapped by the {@link ElementContext}.
	 * @return The new {@link ElementContext}.
	 * @throws TraversalException If this element is not a {@link JsonObject}.
	 * @since 1.0.0
	 */
	public default ElementContext child(String name, JsonElement elem) {
		if (!(elem() instanceof JsonObject o))
			throw new TraversalException("Not an object!");
		return new ElementContexts.Object(this, o, name, elem);
	}

	/**
	 * Creates a new {@link ElementContext} with this set as its parent and the given values as its index and element.
	 * @param index The index of {@code elem}.
	 * @param elem The element wrapped by the {@link ElementContext}.
	 * @return The new {@link ElementContext}.
	 * @throws TraversalException If this element is not a {@link JsonArray}.
	 * @since 1.0.0
	 */
	public default ElementContext child(int index, JsonElement elem) {
		if (!(elem() instanceof JsonArray a))
			throw new TraversalException("Not an array!");
		return new ElementContexts.Array(this, a, index, elem);
	}
}