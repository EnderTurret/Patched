package net.enderturret.patched.patch.context;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.IDataSource;
import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.audit.PatchAudit;
import net.enderturret.patched.patch.IncludePatch;
import net.enderturret.patched.patch.PastePatch;

/**
 * <p>
 * {@code ImmutablePatchContext} is the immutable version of {@link PatchContext}.
 * For the mutable version, see {@link MutablePatchContext}.
 * </p>
 * <p>
 * To obtain an {@code ImmutablePatchContext}, use {@link #newContext()} or {@link #newContext(PatchContext)}.
 * </p>
 * @param testExtensions Whether extensions to the {@code test} operation should be enabled. These extensions add an "inverse" mode and also allows testing for the existence of values.
 * @param patchedExtensions Whether extensions from this library should be enabled. This enables the "find" operation, which is a sort of fuzzy search operation for arrays or objects.
 * @param throwOnFailedTest Whether the test operation should throw an exception if it fails.
 * @param throwOnOobAdd Whether to throw an exception when using an {@code add} patch to add an element at a positive out-of-bounds index.
 * @param testEvaluator An evaluator for custom tests used in {@code test} patches. May be {@code null}.
 * @param fileAccess File access for {@linkplain IncludePatch include patches}. May be {@code null}.
 * @param dataSource A data source for {@linkplain PastePatch paste patches}. May be {@code null}.
 * @param audit An audit to record changes made by patches. May be {@code null}.
 * @author EnderTurret
 * @since 2.0.0
 */
public record ImmutablePatchContext(
		boolean testExtensions, boolean patchedExtensions,
		boolean throwOnFailedTest, boolean throwOnOobAdd,
		@Nullable ITestEvaluator testEvaluator,
		@Nullable IFileAccess fileAccess,
		@Nullable IDataSource dataSource,
		@Nullable PatchAudit audit) implements ConfigurablePatchContext {

	/**
	 * <p>Constructs a new {@code ImmutablePatchContext} with the specified values.</p>
	 * <p>
	 * <b>Note:</b> this constructor may change in the future if and when more fields are added to {@link PatchContext}.
	 * It is heavily recommended to use {@link #newContext()} instead of this constructor so that one does not need to adapt to these changes.
	 * </p>
	 * @param testExtensions Whether extensions to the {@code test} operation should be enabled. These extensions add an "inverse" mode and also allows testing for the existence of values.
	 * @param patchedExtensions Whether extensions from this library should be enabled. This enables the "find" operation, which is a sort of fuzzy search operation for arrays or objects.
	 * @param throwOnFailedTest Whether the test operation should throw an exception if it fails.
	 * @param throwOnOobAdd Whether to throw an exception when using an {@code add} patch to add an element at a positive out-of-bounds index.
	 * @param testEvaluator An evaluator for custom tests used in {@code test} patches. May be {@code null}.
	 * @param fileAccess File access for {@linkplain IncludePatch include patches}. May be {@code null}.
	 * @param dataSource A data source for {@linkplain PastePatch paste patches}. May be {@code null}.
	 * @param audit An audit to record changes made by patches. May be {@code null}.
	 * @since 2.0.0
	 */
	@Internal
	@Experimental
	public ImmutablePatchContext {}

	/**
	 * Returns a new {@code ImmutablePatchContext} initialized with default values.
	 * @return A new {@code ImmutablePatchContext}.
	 * @since 2.0.0
	 */
	public static ImmutablePatchContext newContext() {
		return new ImmutablePatchContext(false, false, false, false, null, null, null, null);
	}

	/**
	 * Returns a new {@code ImmutablePatchContext} initialized with the values from the specified {@code PatchContext}.
	 * @param from The {@code PatchContext} to inherit values from.
	 * @return A new {@code ImmutablePatchContext}.
	 * @since 2.0.0
	 */
	public static ImmutablePatchContext newContext(PatchContext from) {
		return from instanceof ImmutablePatchContext i ? i : new ImmutablePatchContext(
				from.testExtensions(), from.patchedExtensions(), from.throwOnFailedTest(),
				from.throwOnOobAdd(), from.testEvaluator(), from.fileAccess(), from.dataSource(),
				from.audit()
				);
	}

	@Override
	public ImmutablePatchContext testExtensions(boolean value) {
		return new ImmutablePatchContext(value, patchedExtensions, throwOnFailedTest, throwOnOobAdd, testEvaluator, fileAccess, dataSource, audit);
	}

	@Override
	public ImmutablePatchContext patchedExtensions(boolean value) {
		return new ImmutablePatchContext(testExtensions, value, throwOnFailedTest, throwOnOobAdd, testEvaluator, fileAccess, dataSource, audit);
	}

	@Override
	public ImmutablePatchContext throwOnFailedTest(boolean value) {
		return new ImmutablePatchContext(testExtensions, patchedExtensions, value, throwOnOobAdd, testEvaluator, fileAccess, dataSource, audit);
	}

	@Override
	public ImmutablePatchContext throwOnOobAdd(boolean value) {
		return new ImmutablePatchContext(testExtensions, patchedExtensions, throwOnFailedTest, value, testEvaluator, fileAccess, dataSource, audit);
	}

	@Override
	public ImmutablePatchContext testEvaluator(@Nullable ITestEvaluator value) {
		return new ImmutablePatchContext(testExtensions, patchedExtensions, throwOnFailedTest, throwOnOobAdd, value, fileAccess, dataSource, audit);
	}

	@Override
	public ImmutablePatchContext fileAccess(@Nullable IFileAccess value) {
		return new ImmutablePatchContext(testExtensions, patchedExtensions, throwOnFailedTest, throwOnOobAdd, testEvaluator, value, dataSource, audit);
	}

	@Override
	public ImmutablePatchContext dataSource(@Nullable IDataSource value) {
		return new ImmutablePatchContext(testExtensions, patchedExtensions, throwOnFailedTest, throwOnOobAdd, testEvaluator, fileAccess, value, audit);
	}

	@Override
	public ImmutablePatchContext audit(@Nullable PatchAudit value) {
		return new ImmutablePatchContext(testExtensions, patchedExtensions, throwOnFailedTest, throwOnOobAdd, testEvaluator, fileAccess, dataSource, value);
	}
}