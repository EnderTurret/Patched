package tests;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.patch.PatchContext;

public class SimpleTestEvaluator implements ITestEvaluator {

	private final Set<String> values;

	public SimpleTestEvaluator(Set<String> values) {
		this.values = Set.copyOf(values);
	}

	public SimpleTestEvaluator(JsonElement elem) {
		final Set<String> vals = new HashSet<>();

		if (elem.isJsonObject())
			for (Map.Entry<String, JsonElement> entry : elem.getAsJsonObject().entrySet())
				if (entry.getValue().getAsBoolean())
					vals.add(entry.getKey());

		values = Set.copyOf(vals);
	}

	@Override
	public boolean test(JsonElement root, String type, JsonElement target, JsonElement value, PatchContext context) {
		return values.contains(type);
	}
}