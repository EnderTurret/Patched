package net.enderturret.patched.patcher;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an adapter for a {@linkplain Source source folder}.
 * @author EnderTurret
 *
 * @param <P> The path type.
 */
public interface SourceAdapter<P> {
	@Nullable
	public byte[] read(P path);
	public String namespace();
}