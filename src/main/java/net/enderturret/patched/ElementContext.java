package net.enderturret.patched;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.enderturret.patched.exception.TraversalException;
import net.enderturret.patched.patch.PatchUtil.Operation;

/**
 * <p>Represents a {@link JsonElement} and its parent element.</p>
 * <p>This class acts as an abstraction layer to the different kinds of elements that can be parents.</p>
 * @author EnderTurret
 */
public interface ElementContext {

	public JsonElement parent();
	public JsonElement elem();
	public void apply(Operation op);

	public default ElementContext child(String name, JsonElement elem) {
		if (!(elem() instanceof JsonObject o))
			throw new TraversalException("Not an object!");
		return new Object(o, name, elem);
	}

	public default ElementContext child(int index, JsonElement elem) {
		if (!(elem() instanceof JsonArray a))
			throw new TraversalException("Not an array!");
		return new Array(a, index, elem);
	}

	/**
	 * <p>An element without a parent.</p>
	 * <p>Such an element cannot have operations applied to it.</p>
	 * @param elem The element.
	 * @author EnderTurret
	 */
	public static record NoParent(JsonElement elem) implements ElementContext {
		@Override
		public JsonElement parent() {
			return null;
		}
		@Override
		public void apply(Operation op) {
			// Just don't do anything. It'll be fine, probably.
		}
	}

	/**
	 * An element whose parent is a {@link JsonObject}.
	 * @param parent The parent.
	 * @param name The name of the child element.
	 * @param elem The child element.
	 * @author EnderTurret
	 */
	public static record Object(JsonObject parent, String name, JsonElement elem) implements ElementContext {

		@Override
		public JsonObject parent() {
			return parent;
		}

		@Override
		public void apply(Operation op) {
			op.apply(parent, name);
		}
	}

	/**
	 * An element whose parent is a {@link JsonArray}.
	 * @param parent The parent.
	 * @param index The index of the child element.
	 * @param elem The child element.
	 * @author EnderTurret
	 */
	public static record Array(JsonArray parent, int index, JsonElement elem) implements ElementContext {

		@Override
		public JsonArray parent() {
			return parent;
		}

		@Override
		public void apply(Operation op) {
			op.apply(parent, index);
		}
	}
}