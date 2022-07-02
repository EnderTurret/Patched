package net.enderturret.patched.exception;

/**
 * An exception thrown if a path could not be resolved during patching.
 * @author EnderTurret
 */
public class TraversalException extends PatchingException {

	private String path;

	public TraversalException() {}

	public TraversalException(String message) {
		super(message);
	}

	public TraversalException(String message, Throwable cause) {
		super(message, cause);
	}

	public TraversalException(Throwable cause) {
		super(cause);
	}

	/**
	 * Sets the path that caused this exception to be thrown.
	 * @param path The path that caused this {@link TraversalException}.
	 * @return {@code this}.
	 */
	public TraversalException withPath(String path) {
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