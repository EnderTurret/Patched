package tests.util;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.enderturret.patched.IDataSource;
import net.enderturret.patched.exception.PatchingException;

public final class SimpleDataSource implements IDataSource {

	@Override
	@Nullable
	public JsonElement getData(String type, @Nullable JsonElement from, @Nullable JsonElement value) throws PatchingException {
		return switch (type) {
			case "test" -> new JsonPrimitive("test value");
			case "args" -> value;
			case "contrived-copy-paste" -> from;
			default -> null;
		};
	}
}