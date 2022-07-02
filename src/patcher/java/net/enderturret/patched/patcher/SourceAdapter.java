package net.enderturret.patched.patcher;

/**
 * Represents an adapter for a {@linkplain Source source folder}.
 * @author EnderTurret
 *
 * @param <P> The path type.
 */
public interface SourceAdapter<P> {
	public byte[] read(P path);
	public String namespace();
}