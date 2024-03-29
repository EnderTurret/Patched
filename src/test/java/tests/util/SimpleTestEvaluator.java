package tests.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.patch.PatchContext;

import tests.PatchingTests;

/**
 * A custom {@link ITestEvaluator} for {@linkplain PatchingTests tests}.
 * @author EnderTurret
 */
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
	public boolean test(JsonElement root, String type, @Nullable JsonElement target, @Nullable JsonElement value, PatchContext context) {
		return values.contains(type);
	}
}