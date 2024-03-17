package net.enderturret.patched.patch;

import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.audit.PatchAudit;

/**
 * {@code PatchContext} contains various settings to control how patches are deserialized and applied.
 * Most notably, a few extensions to the Json patch format can optionally be enabled.
 * @param testExtensions Whether extensions to the {@code test} operation should be enabled. These extensions add an "inverse" mode and also allows testing for the existence of values.
 * @param patchedExtensions Whether extensions from this library should be enabled. This enables the "find" operation, which is a sort of fuzzy search operation for arrays or objects.
 * @param throwOnFailedTest Whether the test operation should throw an exception if it fails.
 * @param throwOnOobAdd Whether to throw an exception when using an {@code add} patch to add an element at a positive out-of-bounds index.
 * @param testEvaluator An evaluator for custom tests used in {@code test} patches. May be {@code null}.
 * @param fileAccess File access for {@linkplain IncludePatch include patches}. May be {@code null}.
 * @param audit An audit to record changes made by patches. May be {@code null}.
 * @author EnderTurret
 * @since 1.0.0
 */
public record PatchContext(boolean testExtensions, boolean patchedExtensions, boolean throwOnFailedTest, boolean throwOnOobAdd, @Nullable ITestEvaluator testEvaluator, @Nullable IFileAccess fileAccess, @Nullable PatchAudit audit) {

	/**
	 * Constructs a new {@code PatchContext}.
	 * @deprecated To avoid breaking changes to the contents of this record, use {@link #newContext()} instead of calling this directly.
	 * @param testExtensions Whether extensions to the {@code test} operation should be enabled.
	 * @param patchedExtensions Whether extensions from this library should be enabled.
	 * @param throwOnFailedTest Whether the test operation should throw an exception if it fails.
	 * @param throwOnOobAdd Whether to throw an exception when using an {@code add} patch to add an element at a positive out-of-bounds index.
	 * @param testEvaluator An evaluator for custom tests in the {@code test} operation. May be {@code null}.
	 * @param fileAccess File access for {@linkplain IncludePatch include patches}. May be {@code null}.
	 * @param audit An audit to record changes made by patches. May be {@code null}.
	 */
	@Deprecated
	public PatchContext {}

	/**
	 * Returns whether or not {@code testExtensions} is enabled.
	 * @deprecated Use {@link #testExtensions()} instead.
	 * @return {@code true} if {@code testExtensions} is enabled.
	 * @since 1.0.0
	 */
	@Deprecated(forRemoval = true)
	public boolean sbExtensions() {
		return testExtensions;
	}

	/**
	 * Returns a new {@code PatchContext} initialized with default values.
	 * @return A new {@code PatchContext}.
	 * @since 1.0.0
	 */
	public static PatchContext newContext() {
		return new PatchContext(false, false, false, false, null, null, null);
	}

	/**
	 * Returns a copy of this {@code PatchContext} with {@link #testExtensions} set to the specified value.
	 * @deprecated Use {@link #testExtensions(boolean)} instead.
	 * @param value Whether extensions to the {@code test} operation should be enabled.
	 * @return The new {@code PatchContext}.
	 * @since 1.0.0
	 */
	@Deprecated(forRemoval = true)
	public PatchContext sbExtensions(boolean value) {
		return new PatchContext(value, patchedExtensions, throwOnFailedTest, throwOnOobAdd, testEvaluator, fileAccess, audit);
	}

	/**
	 * Returns a copy of this {@code PatchContext} with {@link #testExtensions} set to the given value.
	 * @param value Whether extensions to the {@code test} operation should be enabled.
	 * @return The new {@code PatchContext}.
	 * @since 1.3.0
	 */
	public PatchContext testExtensions(boolean value) {
		return new PatchContext(value, patchedExtensions, throwOnFailedTest, throwOnOobAdd, testEvaluator, fileAccess, audit);
	}

	/**
	 * Returns a copy of this {@code PatchContext} with {@link #patchedExtensions} set to the given value.
	 * @param value Whether this library's patching extensions are enabled.
	 * @return The new {@code PatchContext}.
	 * @since 1.0.0
	 */
	public PatchContext patchedExtensions(boolean value) {
		return new PatchContext(testExtensions, value, throwOnFailedTest, throwOnOobAdd, testEvaluator, fileAccess, audit);
	}

	/**
	 * Returns a copy of this {@code PatchContext} with {@link #throwOnFailedTest} set to the given value.
	 * @param value Whether the test operation should throw an exception if it fails.
	 * @return The new {@code PatchContext}.
	 * @since 1.0.0
	 */
	public PatchContext throwOnFailedTest(boolean value) {
		return new PatchContext(testExtensions, patchedExtensions, value, throwOnOobAdd, testEvaluator, fileAccess, audit);
	}

	/**
	 * Returns a copy of this {@code PatchContext} with {@link #throwOnOobAdd} set to the given value.
	 * @param value Whether to throw an exception when using an {@code add} patch to add an element at a positive out-of-bounds index.
	 * @return The new {@code PatchContext}.
	 * @since 1.3.0
	 */
	public PatchContext throwOnOobAdd(boolean value) {
		return new PatchContext(testExtensions, patchedExtensions, throwOnFailedTest, value, testEvaluator, fileAccess, audit);
	}

	/**
	 * Returns a copy of this {@code PatchContext} with {@link #testEvaluator} set to the given value.
	 * @param value An evaluator for custom tests in the {@code test} operation. May be {@code null}.
	 * @return The new {@code PatchContext}.
	 * @since 1.1.0
	 */
	public PatchContext testEvaluator(@Nullable ITestEvaluator value) {
		return new PatchContext(testExtensions, patchedExtensions, throwOnFailedTest, throwOnOobAdd, value, fileAccess, audit);
	}

	/**
	 * Returns a copy of this {@code PatchContext} with {@link #fileAccess} set to the given value.
	 * @param value File access for {@linkplain IncludePatch include patches}. May be {@code null}.
	 * @return The new {@code PatchContext}.
	 */
	public PatchContext fileAccess(@Nullable IFileAccess value) {
		return new PatchContext(testExtensions, patchedExtensions, throwOnFailedTest, throwOnOobAdd, testEvaluator, value, audit);
	}

	/**
	 * Returns a copy of this {@code PatchContext} with {@link #audit} set to the given value.
	 * @param value An audit to record changes made by patches. May be {@code null}.
	 * @return The new {@code PatchContext}.
	 * @since 1.2.0
	 */
	public PatchContext audit(@Nullable PatchAudit value) {
		return new PatchContext(testExtensions, patchedExtensions, throwOnFailedTest, throwOnOobAdd, testEvaluator, fileAccess, value);
	}
}