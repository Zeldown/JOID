package dev.joid.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class JOIDLogTest {

	@After
	public void forgetTheWarnings() {
		JOIDWarnings.forget();
	}

	@Test
	public void writesItsInformationOnTheStandardOutput() {
		final PrintStream previous = System.out;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(output, true));
			JOIDLog.info("Font Inter loaded in 1.23ms");
		} finally {
			System.setOut(previous);
		}
		Assert.assertEquals("[JOID] Font Inter loaded in 1.23ms" + System.lineSeparator(), new String(output.toByteArray(), StandardCharsets.UTF_8));
	}

	@Test
	public void prefixesItsWarnings() {
		Assert.assertEquals("[JOID] The resource photo.heic cannot be read" + System.lineSeparator(), TestUtils.captureErr(() -> JOIDLog.warn("The resource photo.heic cannot be read")));
	}

	@Test
	public void printsTheStackOfTheCauseAfterTheWarning() {
		final String output = TestUtils.captureErr(() -> JOIDLog.warn("The listener failed", new IllegalStateException("broken")));
		final String[] lines = output.split(System.lineSeparator());
		Assert.assertEquals("[JOID] The listener failed", lines[0]);
		Assert.assertEquals("java.lang.IllegalStateException: broken", lines[1]);
		Assert.assertTrue(lines[2], lines[2].startsWith("\tat dev.joid.internal.JOIDLogTest."));
	}

	@Test
	public void warnsOnceForEachKey() {
		final String output = TestUtils.captureErr(() -> {
			JOIDLog.warnOnce("JOIDLogTest.once.first", "first");
			JOIDLog.warnOnce("JOIDLogTest.once.first", "first again");
			JOIDLog.warnOnce("JOIDLogTest.once.second", "second");
		});
		Assert.assertEquals("[JOID] first" + System.lineSeparator() + "[JOID] second" + System.lineSeparator(), output);
	}

	@Test
	public void printsTheStackOnceForEachKey() {
		final String output = TestUtils.captureErr(() -> {
			for (int i = 0; i < 3; i++) {
				JOIDLog.warnOnce("JOIDLogTest.stack", "The pull failed", new IllegalArgumentException("frame " + i));
			}
		});
		Assert.assertTrue(output, output.startsWith("[JOID] The pull failed" + System.lineSeparator() + "java.lang.IllegalArgumentException: frame 0" + System.lineSeparator()));
		Assert.assertEquals(1, output.split("\\[JOID\\]", -1).length - 1);
		Assert.assertEquals(1, output.split("java.lang.IllegalArgumentException", -1).length - 1);
	}

	@Test
	public void warnsInDevModeOnly() {
		final boolean devMode = JOIDEnv.isDevMode();
		final AtomicInteger built = new AtomicInteger();
		try {
			JOIDEnv.devMode(false);
			final String production = TestUtils.captureErr(() -> JOIDLog.devWarnOnce("JOIDLogTest.dev", () -> "dev " + built.incrementAndGet()));
			JOIDEnv.devMode(true);
			final String development = TestUtils.captureErr(() -> {
				JOIDLog.devWarnOnce("JOIDLogTest.dev", () -> "dev " + built.incrementAndGet());
				JOIDLog.devWarnOnce("JOIDLogTest.dev", () -> "dev " + built.incrementAndGet());
			});
			Assert.assertEquals("", production);
			Assert.assertEquals("[JOID] dev 1" + System.lineSeparator(), development);
			Assert.assertEquals(1, built.get());
		} finally {
			JOIDEnv.devMode(devMode);
		}
	}

}