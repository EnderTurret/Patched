package net.enderturret.patched.patcher.path;

import java.nio.file.Path;
import java.util.function.Consumer;

import net.enderturret.patched.patcher.PathHandler;

/**
 * A {@link PathHandler} for {@link Path Paths}.
 * @param errorCallback A handler for errors. May be {@code null}.
 * @author EnderTurret
 */
public record PathPathHandler(Consumer<Exception> errorCallback) implements PathHandler<Path> {

	@Override
	public Path resolvePatch(Path path) {
		return path.resolveSibling(path.getFileName().toString() + ".patch");
	}

	@Override
	public String extension(Path path) {
		final String name = path.getFileName().toString();
		return name.substring(name.lastIndexOf('.') + 1);
	}

	@Override
	public void onError(Exception e) {
		if (errorCallback != null)
			errorCallback.accept(e);
	}
}