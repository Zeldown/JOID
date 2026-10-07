package dev.joid.showcase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.Configuration;

import dev.joid.impl.vulkan.snapshot.SnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import dev.joid.test.snapshot.SnapshotRunner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowcaseRender {

	public static void main(final String[] args) throws IOException {
		Configuration.STACK_SIZE.set(1024);
		final List<String> commands = Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8);
		final File output = new File(args[1]);
		output.mkdirs();

		final SnapshotRunner runner = SnapshotRunner.start(new SnapshotBackend());
		try {
			for (final Map.Entry<String, SnapshotImage> entry : runner.execute(commands.toArray(new String[0])).entrySet()) {
				entry.getValue().write(new File(output, entry.getKey() + ".png"));
			}
		} finally {
			runner.stop();
		}
		System.exit(0);
	}

}