package net.enderturret.patched;

import java.util.Objects;

import com.google.gson.JsonElement;

/**
 * A wrapper for {@link JsonElement}, to allow patches that change the root element to work.
 * @author EnderTurret
 */
public final class JsonDocument {

	private JsonElement root;

	public JsonDocument(JsonElement root) {
		this.root = Objects.requireNonNull(root);
	}

	public JsonElement getRoot() {
		return root;
	}

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