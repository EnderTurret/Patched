package net.enderturret.patched.patch;

/**
 * Passed to patches to customize their behavior.
 * @param sbExtensions Whether extensions from the game Starbound should be enabled. These extensions add an "inverse" mode to the test operation and also allow it to test for the existence of values.
 * @param patchedExtensions Whether extensions from this library should be enabled. This enables the "find" operation, which is a sort of fuzzy search operation for arrays or objects.
 * @param throwOnFailedTest Whether the test operation should throw an exception if it fails.
 * @author EnderTurret
 */
public record PatchContext(boolean sbExtensions, boolean patchedExtensions, boolean throwOnFailedTest) {

	/**
	 * @deprecated Use {@link #newContext()} where possible to avoid new fields causing binary and source compatibility breaks.
	 * @param sbExtensions Whether extensions from the game Starbound should be enabled.
	 * @param patchedExtensions Whether extensions from this library should be enabled.
	 * @param throwOnFailedTest Whether the test operation should throw an exception if it fails.
	 */
	@Deprecated
	public PatchContext {}

	/**
	 * @return A new {@link PatchContext} with default values for all fields.
	 */
	public static PatchContext newContext() {
		return new PatchContext(false, false, false);
	}

	/**
	 * Returns a new {@link PatchContext} based on this one but with {@link #sbExtensions} set to the given value.
	 * @param value Whether Starbound's patching extensions are enabled.
	 * @return The new {@link PatchContext}.
	 */
	public PatchContext sbExtensions(boolean value) {
		return new PatchContext(value, patchedExtensions, throwOnFailedTest);
	}

	/**
	 * Returns a new {@link PatchContext} based on this one but with {@link #patchedExtensions} set to the given value.
	 * @param value Whether this library's patching extensions are enabled.
	 * @return The new {@link PatchContext}.
	 */
	public PatchContext patchedExtensions(boolean value) {
		return new PatchContext(sbExtensions, value, throwOnFailedTest);
	}

	/**
	 * Returns a new {@link PatchContext} based on this one but with {@link #throwOnFailedTest} set to the given value.
	 * @param value Whether the test operation should throw an exception if it fails.
	 * @return The new {@link PatchContext}.
	 */
	public PatchContext throwOnFailedTest(boolean value) {
		return new PatchContext(sbExtensions, patchedExtensions, value);
	}
}