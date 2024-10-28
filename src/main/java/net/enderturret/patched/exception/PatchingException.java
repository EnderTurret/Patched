package net.enderturret.patched.exception;

import org.jetbrains.annotations.Nullable;

/**
 * A generic exception thrown when something goes wrong during patch application or deserialization.
 * @author EnderTurret
 * @since 1.0.0
 */
public class PatchingException extends RuntimeException {

	/**
	 * Constructs a new {@code PatchingException} with {@code null} as its detail message.
	 */
	public PatchingException() {}

	/**
	 * Constructs a new {@code PatchingException} with the specified detail message.
	 * @param message The detail message.
	 */
	public PatchingException(@Nullable String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code PatchingException} with the specified detail message and cause.
	 * @param message The detail message.
	 * @param cause The cause.
	 */
	public PatchingException(@Nullable String message, @Nullable Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code PatchingException} with {@code null} as its detail message and with the specified cause.
	 * @param cause The cause.
	 */
	public PatchingException(@Nullable Throwable cause) {
		super(cause);
	}
}