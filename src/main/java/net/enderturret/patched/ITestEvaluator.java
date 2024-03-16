package net.enderturret.patched;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import net.enderturret.patched.patch.PatchContext;

/**
 * <p>
 * An evaluator for custom conditions in {@code test} patches.
 * These evaluators can be used to supplement the existing {@code test} patch functionality with new conditions beyond
 * equality and existence checks.
 * </p>
 * <p>
 * If an evaluator is installed in a {@link PatchContext}, and a {@code test} patch that has a {@code type} field is processed,
 * the {@code test} patch will be passed to the evaluator to determine whether or not the condition succeeds.
 * </p>
 * @author EnderTurret
 * @since 1.1.0
 */
@FunctionalInterface
public interface ITestEvaluator {

	/**
	 * For custom conditions in {@code test} patches, determines whether a given condition succeeds.
	 * @param root The root element.
	 * @param type The {@code test} patch's type, as specified in the {@code type} field.
	 * @param target The target element. In a normal {@code test} patch, this is the element tested for equality. May be {@code null}.
	 * @param value The value. In a normal {@code test} patch, this is the element that {@code target} is being compared against. May be {@code null}.
	 * @param context The patch context.
	 * @return {@code true} if the test succeeds, {@code false} otherwise.
	 * @since 1.1.0
	 */
	public boolean test(JsonElement root, String type, @Nullable JsonElement target, @Nullable JsonElement value, PatchContext context);
}