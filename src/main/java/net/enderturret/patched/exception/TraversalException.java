package net.enderturret.patched.exception;

import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown if a path could not be resolved during patching.
 * @author EnderTurret
 * @since 1.0.0
 */
public class TraversalException extends PatchingException {

	@Nullable
	private String path;

	/**
	 * Constructs a new {@code TraversalException} with {@code null} as its detail message.
	 */
	public TraversalException() {}

	/**
	 * Constructs a new {@code TraversalException} with the specified detail message.
	 * @param message The detail message.
	 */
	public TraversalException(@Nullable String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code TraversalException} with the specified detail message and cause.
	 * @param message The detail message.
	 * @param cause The cause.
	 */
	public TraversalException(@Nullable String message, @Nullable Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code TraversalException} with {@code null} as its detail message and with the specified cause.
	 * @param cause The cause.
	 */
	public TraversalException(@Nullable Throwable cause) {
		super(cause);
	}

	/**
	 * Sets the path that caused this exception to be thrown.
	 * @param path The path that caused this {@link TraversalException}.
	 * @return {@code this}.
	 * @since 1.0.0
	 */
	public TraversalException withPath(@Nullable String path) {
		this.path = path;
		return this;
	}

	@Override
	public String getMessage() {
		if (path != null)
			return path + ": " + super.getMessage();

		return super.getMessage();
	}
}