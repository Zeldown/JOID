package dev.joid.test.snapshot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.ConsoleCapture;
import lombok.NonNull;

public class SnapshotSuiteTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public final ConsoleCapture console = new ConsoleCapture();

	private Properties previous;

	@Before
	public void isolateTheProperties() {
		this.previous = System.getProperties();
		final Properties properties = new Properties();
		properties.putAll(this.previous);
		properties.remove("joid.snapshot.update");
		properties.remove("joid.snapshot.record");
		properties.setProperty("joid.snapshot.output", new File(this.folder.getRoot(), "renders").getPath());
		properties.setProperty("joid.snapshot.references", new File(this.folder.getRoot(), "references").getPath());
		System.setProperties(properties);
	}

	@After
	public void restoreTheProperties() {
		SnapshotSuite.stopRunner();
		System.setProperties(this.previous);
	}

	@Test
	public void failsOnAShotWithoutAReferenceForItsGpu() throws ReflectiveOperationException {
		final File references = new File(this.folder.getRoot(), "references");
		Assert.assertTrue(new File(references, "NVIDIA-GeForce-RTX-4070-Laptop-GPU").mkdirs());
		final String failure = this.verify("blank");
		Assert.assertNotNull(failure);
		Assert.assertTrue(failure, failure.contains("[JOID] No reference for the GPU headless in " + references + ", the shots of blank were not compared: record them on this GPU with updateSnapshots (references exist for NVIDIA-GeForce-RTX-4070-Laptop-GPU)"));
		Assert.assertFalse(new File(references, "headless").exists());
		Assert.assertTrue(new File(this.folder.getRoot(), "renders/blank.png").exists());
	}

	@Test
	public void failsOnAShotWithoutAnyReference() throws ReflectiveOperationException {
		final String failure = this.verify("blank");
		Assert.assertNotNull(failure);
		Assert.assertTrue(failure, failure.contains("record them on this GPU with updateSnapshots" + System.lineSeparator()));
		Assert.assertFalse(new File(this.folder.getRoot(), "references").exists());
	}

	@Test
	public void recordsTheMissingReferencesInRecordMode() throws ReflectiveOperationException {
		System.setProperty("joid.snapshot.record", "true");
		Assert.assertNull(this.verify("blank"));
		Assert.assertTrue(new File(this.folder.getRoot(), "references/headless/blank.png").exists());
		System.clearProperty("joid.snapshot.record");
		Assert.assertNull(this.verify("blank"));
	}

	@Test
	public void writesTheRendererNextToTheRenders() throws IOException, ReflectiveOperationException {
		this.verify("blank");
		Assert.assertEquals("headless", new String(Files.readAllBytes(new File(this.folder.getRoot(), "renders/renderer.txt").toPath()), StandardCharsets.UTF_8));
	}

	private String verify(final String scenario) throws ReflectiveOperationException {
		final Method verify = SnapshotSuite.class.getDeclaredMethod("verify", String.class);
		verify.setAccessible(true);
		try {
			verify.invoke(new HeadlessSuite(), scenario);
			return null;
		} catch (final InvocationTargetException e) {
			return ((AssertionError) e.getCause()).getMessage();
		}
	}

	private final class HeadlessSuite extends SnapshotSuite {

		@Override
		protected @NonNull ISnapshotBackend createBackend() {
			return new SnapshotRunnerTest.HeadlessBackend();
		}

	}

}