package net.enderturret.patched.patch;

import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.audit.PatchAudit;

/**
 * Passed to patches to customize their behavior.
 * @param testExtensions Whether extensions to the {@code test} operation should be enabled. These extensions add an "inverse" mode and also allows testing for the existence of values.
 * @param patchedExtensions Whether extensions from this library should be enabled. This enables the "find" operation, which is a sort of fuzzy search operation for arrays or objects.
 * @param throwOnFailedTest Whether the test operation should throw an exception if it fails.
 * @param testEvaluator An evaluator for custom tests in the {@code test} operation. May be {@code null}.
 * @param audit An audit to record changes made by patches. May be {@code null}.
 * @author EnderTurret
 */
public record PatchContext(boolean testExtensions, boolean patchedExtensions, boolean throwOnFailedTest, ITestEvaluator testEvaluator, PatchAudit audit) {

	/**
	 * @deprecated Use {@link #newContext()} where possible to avoid new fields causing binary and source compatibility breaks.
	 * @param testExtensions Whether extensions to the {@code test} operation should be enabled.
	 * @param patchedExtensions Whether extensions from this library should be enabled.
	 * @param throwOnFailedTest Whether the test operation should throw an exception if it fails.
	 * @param testEvaluator An evaluator for custom tests in the {@code test} operation. May be {@code null}.
	 * @param audit An audit to record changes made by patches. May be {@code null}.
	 */
	@Deprecated
	public PatchContext {}

	/**
	 * @deprecated Use {@link #testExtensions()} instead.
	 * @return {@code true} if {@code testExtensions} is enabled.
	 */
	@Deprecated(forRemoval = true)
	public boolean sbExtensions() {
		return testExtensions;
	}

	/**
	 * @return A new {@link PatchContext} with default values for all fields.
	 */
	public static PatchContext newContext() {
		return new PatchContext(false, false, false, null, null);
	}

	/**
	 * @deprecated Use {@link #testExtensions(boolean)} instead.
	 * Returns a new {@link PatchContext} based on this one but with {@link #testExtensions} set to the given value.
	 * @param value Whether extensions to the {@code test} operation should be enabled.
	 * @return The new {@link PatchContext}.
	 */
	@Deprecated(forRemoval = true)
	public PatchContext sbExtensions(boolean value) {
		return new PatchContext(value, patchedExtensions, throwOnFailedTest, testEvaluator, audit);
	}

	/**
	 * Returns a new {@link PatchContext} based on this one but with {@link #testExtensions} set to the given value.
	 * @param value Whether extensions to the {@code test} operation should be enabled.
	 * @return The new {@link PatchContext}.
	 */
	public PatchContext testExtensions(boolean value) {
		return new PatchContext(value, patchedExtensions, throwOnFailedTest, testEvaluator, audit);
	}

	/**
	 * Returns a new {@link PatchContext} based on this one but with {@link #patchedExtensions} set to the given value.
	 * @param value Whether this library's patching extensions are enabled.
	 * @return The new {@link PatchContext}.
	 */
	public PatchContext patchedExtensions(boolean value) {
		return new PatchContext(testExtensions, value, throwOnFailedTest, testEvaluator, audit);
	}

	/**
	 * Returns a new {@link PatchContext} based on this one but with {@link #throwOnFailedTest} set to the given value.
	 * @param value Whether the test operation should throw an exception if it fails.
	 * @return The new {@link PatchContext}.
	 */
	public PatchContext throwOnFailedTest(boolean value) {
		return new PatchContext(testExtensions, patchedExtensions, value, testEvaluator, audit);
	}

	/**
	 * Returns a new {@link PatchContext} based on this one but with {@link #testEvaluator} set to the given value.
	 * @param value An evaluator for custom tests in the {@code test} operation. May be {@code null}.
	 * @return The new {@link PatchContext}.
	 */
	public PatchContext testEvaluator(ITestEvaluator value) {
		return new PatchContext(testExtensions, patchedExtensions, throwOnFailedTest, value, audit);
	}

	/**
	 * Returns a new {@link PatchContext} based on this one but with {@link #audit} set to the given value.
	 * @param value An audit to record changes made by patches. May be {@code null}.
	 * @return The new {@link PatchContext}.
	 */
	public PatchContext audit(PatchAudit value) {
		return new PatchContext(testExtensions, patchedExtensions, throwOnFailedTest, testEvaluator, value);
	}
}