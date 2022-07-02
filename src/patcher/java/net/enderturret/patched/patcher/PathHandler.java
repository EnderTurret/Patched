package net.enderturret.patched.patcher;

public interface PathHandler<P> {
	public P resolvePatch(P path);
	public String extension(P path);
	public void onError(Exception e);
}