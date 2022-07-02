package net.enderturret.patched.exception;

/**
 * A generic exception thrown when something goes wrong during patch application or deserialization.
 * @author EnderTurret
 */
public class PatchingException extends RuntimeException {

	public PatchingException() {}

	public PatchingException(String message) {
        super(message);
    }

    public PatchingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatchingException(Throwable cause) {
        super(cause);
    }
}