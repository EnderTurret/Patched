package net.enderturret.patched.patcher.path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.enderturret.patched.patcher.Dumper;

/**
 * A {@link Dumper} for {@link Path Paths}.
 * @param to The output directory.
 * @author EnderTurret
 */
public record PathDumper(Path to) implements Dumper<Path> {

	public PathDumper {
		try {
			if (!Files.exists(to))
				Files.createDirectories(to);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dump(Path path, byte[] data) {
		try {
			final Path to = this.to.resolve(path);
			Files.createDirectories(to.getParent());
			Files.write(to, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}