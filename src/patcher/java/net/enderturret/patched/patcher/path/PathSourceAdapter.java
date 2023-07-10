package net.enderturret.patched.patcher.path;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import net.enderturret.patched.patcher.SourceAdapter;

/**
 * An implementation of {@link SourceAdapter} for {@link Path Paths}.
 * @author EnderTurret
 *
 * @param root The root directory.
 * @param namespace The namespace of the source folder. This must be a unique value.
 */
public record PathSourceAdapter(Path root, String namespace) implements SourceAdapter<Path> {

	public PathSourceAdapter {}

	public PathSourceAdapter(Path root) {
		this(root, root.getFileName().toString());
	}

	@Override
	@Nullable
	public byte[] read(Path path) {
		final Path realPath = root.resolve(path);

		if (!Files.exists(realPath))
			return null;

		try {
			return Files.readAllBytes(realPath);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}