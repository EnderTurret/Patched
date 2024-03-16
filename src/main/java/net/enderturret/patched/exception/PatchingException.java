package net.enderturret.patched.exception;

import org.jetbrains.annotations.Nullable;

/**
 * A generic exception thrown when something goes wrong during patch application or deserialization.
 * @author EnderTurret
 */
public class PatchingException extends RuntimeException {

	public PatchingException() {}

	public PatchingException(@Nullable String message) {
		super(message);
	}

	public PatchingException(@Nullable String message, @Nullable Throwable cause) {
		super(message, cause);
	}

	public PatchingException(@Nullable Throwable cause) {
		super(cause);
	}
}