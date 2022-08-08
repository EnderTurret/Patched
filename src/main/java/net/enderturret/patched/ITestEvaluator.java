package net.enderturret.patched;

import com.google.gson.JsonElement;

import net.enderturret.patched.patch.PatchContext;

/**
 * An evaluator for custom tests in {@code test} patches.
 * This can be used to define and handle the {@code type} field in these patches.
 * @author EnderTurret
 */
@FunctionalInterface
public interface ITestEvaluator {

	/**
	 * For custom tests in {@code test} patches, determines whether a given test succeeds.
	 * @param root The root element.
	 * @param type The test type, as specified in the {@code type} field.
	 * @param target The target element. In a normal test patch, this is the element tested for equality. May be {@code null}.
	 * @param value The value. In a normal test patch, this is the element that {@code target} is being tested against. May be {@code null}.
	 * @param context The patch context.
	 * @return {@code true} if the test succeeds.
	 */
	public boolean test(JsonElement root, String type, JsonElement target, JsonElement value, PatchContext context);
}