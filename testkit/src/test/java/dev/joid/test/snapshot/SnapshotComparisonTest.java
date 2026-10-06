package dev.joid.test.snapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Permission;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SnapshotComparisonTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void acceptsCandidatesWithinOneLevelPerChannel() throws IOException {
		final File vulkan = this.shots("vulkan", 0xFF808080, 0xFF101010);
		SnapshotComparisonTest.image(0xFF000000).write(new File(vulkan, "extra.png"));
		final Run run = this.compare(this.shots("lwjgl3", 0xFF808080, 0xFF101010), vulkan, this.shots("glfw", 0xFF818080, 0xFF10100F));
		Assert.assertNull(run.getStatus());
		Assert.assertEquals("", run.getErrors());
		Assert.assertTrue(run.getOutput(), run.getOutput().contains("vulkan: 2/2 snapshots identical to lwjgl3"));
		Assert.assertTrue(run.getOutput(), run.getOutput().contains("glfw: 2/2 snapshots identical to lwjgl3"));

		final String report = SnapshotComparisonTest.text(new File(this.folder.getRoot(), "report.html"));
		Assert.assertTrue(report.contains("<title>JOID cross-backend comparison</title>"));
		Assert.assertTrue(report.contains("{\"name\":\"glfw/a.png\",\"status\":\"identical\",\"pixels\":0}"));
		Assert.assertFalse(report.contains("extra.png"));
	}

	@Test
	public void failsOnAShotBeyondTheTolerance() throws IOException {
		final Run run = this.compare(this.shots("lwjgl3", 0xFF808080, 0xFF101010), this.shots("vulkan", 0xFF808080, 0xFF101210));
		Assert.assertEquals(Integer.valueOf(1), run.getStatus());
		Assert.assertTrue(run.getOutput(), run.getOutput().contains("vulkan: 1/2 snapshots identical to lwjgl3"));
		Assert.assertTrue(run.getErrors(), run.getErrors().contains("vulkan/b.png: 1 pixels differ from lwjgl3, maximum channel delta 2"));
		Assert.assertTrue(run.getErrors(), run.getErrors().contains("Report: " + new File(this.folder.getRoot(), "report.html").toPath().toUri().toASCIIString()));
		Assert.assertTrue(SnapshotComparisonTest.text(new File(this.folder.getRoot(), "report.html")).contains("{\"name\":\"vulkan/b.png\",\"status\":\"different\",\"pixels\":1}"));
	}

	@Test
	public void failsOnAShotThatWasNotRendered() {
		final Run run = this.compare(this.shots("lwjgl3", 0xFF808080, 0xFF101010), this.shots("vulkan", 0xFF808080));
		Assert.assertEquals(Integer.valueOf(1), run.getStatus());
		Assert.assertTrue(run.getOutput(), run.getOutput().contains("vulkan: 1/2 snapshots identical to lwjgl3"));
		Assert.assertTrue(run.getErrors(), run.getErrors().contains("vulkan/b.png: not rendered"));
	}

	@Test
	public void failsWithoutAnyReferenceShot() throws IOException {
		final File empty = this.folder.newFolder("lwjgl3");
		Assert.assertTrue(new File(empty, "notes.txt").createNewFile());
		final Run run = this.compare(empty, this.shots("vulkan", 0xFF808080));
		Assert.assertEquals(Integer.valueOf(1), run.getStatus());
		Assert.assertTrue(run.getErrors(), run.getErrors().contains("No snapshot rendered in " + empty));
		Assert.assertFalse(new File(this.folder.getRoot(), "report.html").exists());
		Assert.assertEquals(Integer.valueOf(1), this.compare(new File(this.folder.getRoot(), "missing")).getStatus());
	}

	private File shots(final String backend, final int... colors) {
		final File directory = new File(this.folder.getRoot(), backend);
		for (int i = 0; i < colors.length; i++) {
			SnapshotComparisonTest.image(colors[i]).write(new File(directory, (char) ('a' + i) + ".png"));
		}
		return directory;
	}

	private Run compare(final File reference, final File... candidates) {
		return SnapshotComparisonTest.run(Stream.concat(Stream.of(this.folder.getRoot(), reference), Arrays.stream(candidates)).map(File::getPath).toArray(String[]::new));
	}

	private static SnapshotImage image(final int color) {
		return SnapshotImage.fromBytes(ByteBuffer.wrap(new byte[] {(byte) (color >> 16), (byte) (color >> 8), (byte) color, (byte) 255}), 1, 1, false, false);
	}

	private static String text(final File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

	private static Run run(final String... arguments) {
		final PrintStream out = System.out;
		final PrintStream err = System.err;
		final SecurityManager security = System.getSecurityManager();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final ByteArrayOutputStream errors = new ByteArrayOutputStream();
		Integer status = null;
		try {
			System.setOut(new PrintStream(output, true));
			System.setErr(new PrintStream(errors, true));
			System.setSecurityManager(new ExitTrap());
			SnapshotComparison.main(arguments);
		} catch (final ExitException exit) {
			status = exit.getStatus();
		} finally {
			System.setSecurityManager(security);
			System.setOut(out);
			System.setErr(err);
		}
		return new Run(status, new String(output.toByteArray(), StandardCharsets.UTF_8), new String(errors.toByteArray(), StandardCharsets.UTF_8));
	}

	@Getter
	@AllArgsConstructor
	private static final class Run {

		private final Integer status;
		private final String  output;
		private final String  errors;

	}

	@Getter
	private static final class ExitException extends SecurityException {

		private final int status;

		private ExitException(final int status) {
			super("System.exit(" + status + ")");
			this.status = status;
		}

	}

	private static final class ExitTrap extends SecurityManager {

		@Override
		public void checkPermission(final Permission permission) {}

		@Override
		public void checkExit(final int status) {
			throw new ExitException(status);
		}

	}

}