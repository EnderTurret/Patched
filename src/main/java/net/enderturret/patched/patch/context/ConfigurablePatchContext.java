package net.enderturret.patched.patch.context;

import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.IDataSource;
import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.patch.IncludePatch;
import net.enderturret.patched.patch.PastePatch;

/**
 * {@code ConfigurablePatchContext} represents a {@code PatchContext} that can be (re-)configured.
 * Every method that could potentially mutate a {@code PatchContext} resides here.
 * @author EnderTurret
 * @since 2.0.0
 * @see ImmutablePatchContext
 * @see MutablePatchContext
 */
public sealed interface ConfigurablePatchContext extends PatchContext permits MutablePatchContext, ImmutablePatchContext {

	/**
	 * Returns a {@code ConfigurablePatchContext} with {@link #testExtensions} set to the given value.
	 * @param value Whether extensions to the {@code test} operation should be enabled.
	 * @return A new {@code ConfigurablePatchContext} or {@code this}, depending on implementation.
	 * @since 2.0.0
	 */
	public ConfigurablePatchContext testExtensions(boolean value);

	/**
	 * Returns a {@code ConfigurablePatchContext} with {@link #patchedExtensions} set to the given value.
	 * @param value Whether this library's patching extensions are enabled.
	 * @return A new {@code ConfigurablePatchContext} or {@code this}, depending on implementation.
	 * @since 2.0.0
	 */
	public ConfigurablePatchContext patchedExtensions(boolean value);

	/**
	 * Returns a {@code ConfigurablePatchContext} with {@link #throwOnFailedTest} set to the given value.
	 * @param value Whether the test operation should throw an exception if it fails.
	 * @return A new {@code ConfigurablePatchContext} or {@code this}, depending on implementation.
	 * @since 2.0.0
	 */
	public ConfigurablePatchContext throwOnFailedTest(boolean value);

	/**
	 * Returns a {@code ConfigurablePatchContext} with {@link #throwOnOobAdd} set to the given value.
	 * @param value Whether to throw an exception when using an {@code add} patch to add an element at a positive out-of-bounds index.
	 * @return A new {@code ConfigurablePatchContext} or {@code this}, depending on implementation.
	 * @since 2.0.0
	 */
	public ConfigurablePatchContext throwOnOobAdd(boolean value);

	/**
	 * Returns a {@code ConfigurablePatchContext} with {@link #testEvaluator} set to the given value.
	 * @param value An evaluator for custom tests in the {@code test} operation. May be {@code null}.
	 * @return A new {@code ConfigurablePatchContext} or {@code this}, depending on implementation.
	 * @since 2.0.0
	 */
	public ConfigurablePatchContext testEvaluator(@Nullable ITestEvaluator value);

	/**
	 * Returns a {@code ConfigurablePatchContext} with {@link #fileAccess} set to the given value.
	 * @param value File access for {@linkplain IncludePatch include patches}. May be {@code null}.
	 * @return A new {@code ConfigurablePatchContext} or {@code this}, depending on implementation.
	 * @since 2.0.0
	 */
	public ConfigurablePatchContext fileAccess(@Nullable IFileAccess value);

	/**
	 * Returns a {@code ConfigurablePatchContext} with {@link #dataSource} set to the given value.
	 * @param value A data source for {@linkplain PastePatch paste patches}. May be {@code null}.
	 * @return A new {@code ConfigurablePatchContext} or {@code this}, depending on implementation.
	 * @since 2.0.0
	 */
	public ConfigurablePatchContext dataSource(@Nullable IDataSource value);

	/**
	 * Returns a {@code ConfigurablePatchContext} with {@link #audit} set to the given value.
	 * @param value An audit to record changes made by patches. May be {@code null}.
	 * @return A new {@code ConfigurablePatchContext} or {@code this}, depending on implementation.
	 * @since 2.0.0
	 */
	public ConfigurablePatchContext audit(@Nullable PatchAudit value);

	/**
	 * Returns an {@code ImmutablePatchContext} based on this one.
	 * If this {@code ConfigurablePatchContext} is already an {@code ImmutablePatchContext}, {@code this} is returned.
	 * @return A new {@code ImmutablePatchContext}, or {@code this}.
	 */
	public default ImmutablePatchContext asImmutableContext() {
		return ImmutablePatchContext.newContext(this);
	}
}