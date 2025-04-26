package net.enderturret.patched.patch.context;

import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.IDataSource;
import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.patch.IncludePatch;
import net.enderturret.patched.patch.PastePatch;

/**
 * {@code PatchContext} contains various settings to control how patches are deserialized and applied.
 * Most notably, a few extensions to the Json patch format can optionally be enabled.
 * @author EnderTurret
 * @since 2.0.0
 * @see ImmutablePatchContext
 * @see MutablePatchContext
 */
public sealed interface PatchContext permits ConfigurablePatchContext {

	/**
	 * Returns whether or not extensions to the {@code test} operation should be enabled.
	 * These extensions add an "inverse" mode and also allows testing for the existence of values.
	 * @return {@code true} if {@code test} extensions are enabled.
	 * @since 2.0.0
	 */
	public boolean testExtensions();

	/**
	 * Returns whether or not extensions from this library should be enabled.
	 * This enables the "find" operation, which is a sort of fuzzy search operation for arrays or objects.
	 * @return {@code true} if Patched extensions are enabled.
	 * @since 2.0.0
	 */
	public boolean patchedExtensions();

	/**
	 * Returns whether or not the test operation should throw an exception if it fails.
	 * @return {@code true} if the test operation should throw exceptions.
	 * @since 2.0.0
	 */
	public boolean throwOnFailedTest();

	/**
	 * Returns whether or not to throw an exception when using an {@code add} patch to add an element at a positive out-of-bounds index.
	 * @return {@code true} if exceptions should be thrown when adding out-of-bounds.
	 * @since 2.0.0
	 */
	public boolean throwOnOobAdd();

	/**
	 * Returns an evaluator for custom tests used in {@code test} patches.
	 * @return The 'custom' test evaluator. May be {@code null}.
	 * @since 2.0.0
	 */
	public @Nullable ITestEvaluator testEvaluator();

	/**
	 * Returns the file access for {@linkplain IncludePatch include patches}.
	 * @return The file access. May be {@code null}.
	 * @since 2.0.0
	 */
	public @Nullable IFileAccess fileAccess();

	/**
	 * Returns the data source for {@linkplain PastePatch paste patches}.
	 * @return The data source. May be {@code null}.
	 * @since 2.0.0
	 */
	public @Nullable IDataSource dataSource();

	/**
	 * Returns an audit to record changes made by patches.
	 * @return The patch audit. May be {@code null}.
	 * @since 2.0.0
	 */
	public @Nullable PatchAudit audit();
}