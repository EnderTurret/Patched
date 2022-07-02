package net.enderturret.patched.patcher;

/**
 * Dumps patched files requested from a {@link Patcher}.
 * @author EnderTurret
 *
 * @param <P> The path type.
 */
public interface Dumper<P> {
	public void dump(P path, byte[] data);
}