package net.enderturret.patched.patch.context;

import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.IDataSource;
import net.enderturret.patched.IFileAccess;
import net.enderturret.patched.ITestEvaluator;
import net.enderturret.patched.audit.PatchAudit;

/**
 * {@code MutablePatchContext} is the mutable version of {@link PatchContext}.
 * For the immutable version, see {@link ImmutablePatchContext}.
 * @author EnderTurret
 * @since 2.0.0
 */
public final class MutablePatchContext implements ConfigurablePatchContext {

	private boolean testExtensions;
	private boolean patchedExtensions;
	private boolean throwOnFailedTest;
	private boolean throwOnOobAdd;
	private @Nullable ITestEvaluator testEvaluator;
	private @Nullable IFileAccess fileAccess;
	private @Nullable IDataSource dataSource;
	private @Nullable PatchAudit audit;

	public MutablePatchContext() {}

	public MutablePatchContext(PatchContext from) {
		testExtensions = from.testExtensions();
		patchedExtensions = from.patchedExtensions();
		throwOnFailedTest = from.throwOnFailedTest();
		throwOnOobAdd = from.throwOnOobAdd();
		testEvaluator = from.testEvaluator();
		fileAccess = from.fileAccess();
		dataSource = from.dataSource();
		audit = from.audit();
	}

	@Override
	public boolean testExtensions() {
		return testExtensions;
	}

	@Override
	public boolean patchedExtensions() {
		return patchedExtensions;
	}

	@Override
	public boolean throwOnFailedTest() {
		return throwOnFailedTest;
	}

	@Override
	public boolean throwOnOobAdd() {
		return throwOnOobAdd;
	}

	@Override
	@Nullable
	public ITestEvaluator testEvaluator() {
		return testEvaluator;
	}

	@Override
	@Nullable
	public IFileAccess fileAccess() {
		return fileAccess;
	}

	@Override
	@Nullable
	public IDataSource dataSource() {
		return dataSource;
	}

	@Override
	@Nullable
	public PatchAudit audit() {
		return audit;
	}

	@Override
	public MutablePatchContext testExtensions(boolean value) {
		testExtensions = value;
		return this;
	}

	@Override
	public MutablePatchContext patchedExtensions(boolean value) {
		patchedExtensions = value;
		return this;
	}

	@Override
	public MutablePatchContext throwOnFailedTest(boolean value) {
		throwOnFailedTest = value;
		return this;
	}

	@Override
	public MutablePatchContext throwOnOobAdd(boolean value) {
		throwOnOobAdd = value;
		return this;
	}

	@Override
	public MutablePatchContext testEvaluator(@Nullable ITestEvaluator value) {
		testEvaluator = value;
		return this;
	}

	@Override
	public MutablePatchContext fileAccess(@Nullable IFileAccess value) {
		fileAccess = value;
		return this;
	}

	@Override
	public MutablePatchContext dataSource(@Nullable IDataSource value) {
		dataSource = value;
		return this;
	}

	@Override
	public MutablePatchContext audit(@Nullable PatchAudit value) {
		audit = value;
		return this;
	}
}