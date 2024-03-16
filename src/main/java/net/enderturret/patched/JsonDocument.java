package net.enderturret.patched;

import java.util.Objects;

import com.google.gson.JsonElement;

/**
 * A wrapper for {@link JsonElement}, to allow patches that change the root element to work.
 * @author EnderTurret
 */
public final class JsonDocument {

	private JsonElement root;

	/**
	 * Constructs a new {@code JsonDocument} with the specified root element.
	 * @param root The root element.
	 * @throws NullPointerException If {@code root} is {@code null}.
	 */
	public JsonDocument(JsonElement root) {
		this.root = Objects.requireNonNull(root);
	}

	/**
	 * Returns the root element.
	 * @return The root element.
	 */
	public JsonElement getRoot() {
		return root;
	}

	/**
	 * Sets the root element of this {@code JsonDocument} to the specified new value.
	 * @param value The new root element.
	 * @throws NullPointerException If {@code value} is {@code null}.
	 */
	public void setRoot(JsonElement value) {
		root = Objects.requireNonNull(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof JsonDocument doc)) return false;
		return root.equals(doc.root);
	}

	@Override
	public int hashCode() {
		return root.hashCode();
	}

	@Override
	public String toString() {
		return root.toString();
	}
}