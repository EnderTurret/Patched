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

	public TraversalException() {}

	public TraversalException(@Nullable String message) {
		super(message);
	}

	public TraversalException(@Nullable String message, @Nullable Throwable cause) {
		super(message, cause);
	}

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