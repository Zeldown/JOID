package dev.joid.lib.shader.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;

public class ShaderProgramTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void loadsItsShaderFromTheRenderBridge() {
		final SourceShader shader = new SourceShader(ShaderProgramTest.source(), ShaderProgramTest.source());
		Assert.assertTrue(shader.getShader() instanceof RecordingShader);
		Assert.assertTrue(shader.isAvailable());
		shader.bind();
		Assert.assertSame(shader.getShader(), this.bridges.getRender().getShader());
		shader.unbind();
		Assert.assertNull(this.bridges.getRender().getShader());
	}

	@Test
	public void createsItsShaderOnFirstUse() {
		BridgeHandler.RENDER.unregister(this.bridges.getRender());
		final SourceShader shader;
		try {
			shader = new SourceShader(ShaderProgramTest.source(), ShaderProgramTest.source());
		} finally {
			BridgeHandler.RENDER.register(this.bridges.getRender());
		}

		Assert.assertTrue(shader.isAvailable());
		Assert.assertTrue(shader.getShader() instanceof RecordingShader);
		Assert.assertSame(shader.getShader(), shader.getShader());
	}

	@Test
	public void loadsItsSourcesFromAnyAssetHandle() throws IOException {
		final File vertex = this.folder.newFile("wave.vsh");
		final File fragment = this.folder.newFile("wave.fsh");
		Files.write(vertex.toPath(), "void main() {}".getBytes(StandardCharsets.UTF_8));
		Files.write(fragment.toPath(), "void main() {}".getBytes(StandardCharsets.UTF_8));
		Assert.assertTrue(new SourceShader(vertex, fragment.toURI().toString()).isAvailable());
	}

	@Test
	public void staysUnavailableWhenItsSourceCannotBeRead() {
		final SourceShader[] shader = new SourceShader[1];
		final String error = ShaderProgramTest.capture(() -> shader[0] = new SourceShader(new BrokenStream(), ShaderProgramTest.source()));
		Assert.assertNull(shader[0].getShader());
		Assert.assertFalse(shader[0].isAvailable());
		Assert.assertTrue(error, error.contains("broken"));
		shader[0].bind();
		shader[0].unbind();
		Assert.assertNull(this.bridges.getRender().getShader());
	}

	@Test
	public void closesItsSources() {
		final ClosingStream vertex = new ClosingStream();
		final ClosingStream fragment = new ClosingStream();
		new SourceShader(vertex, fragment);
		Assert.assertTrue(vertex.closed);
		Assert.assertTrue(fragment.closed);
	}

	@Test
	public void closesItsSourcesWhenOneCannotBeRead() {
		final ClosingStream fragment = new ClosingStream();
		ShaderProgramTest.capture(() -> new SourceShader(new BrokenStream(), fragment));
		Assert.assertTrue(fragment.closed);
	}

	@Test
	public void warnsOnceInDevModeThatItIsUnavailable() {
		final SourceShader[] shader = new SourceShader[1];
		ShaderProgramTest.capture(() -> shader[0] = new SourceShader(new BrokenStream(), ShaderProgramTest.source()));
		JOID.inst().setDevMode(true);
		try {
			final String error = ShaderProgramTest.capture(() -> {
				shader[0].canDraw();
				shader[0].canDraw();
			});
			Assert.assertEquals("[JOID] The shader SourceShader is unavailable, what it draws is skipped" + System.lineSeparator(), error);
		} finally {
			JOID.inst().setDevMode(false);
		}
	}

	@Test
	public void staysSilentOutOfDevModeWhenItIsUnavailable() {
		final SourceShader[] shader = new SourceShader[1];
		ShaderProgramTest.capture(() -> shader[0] = new SourceShader(new BrokenStream(), ShaderProgramTest.source()));
		Assert.assertEquals("", ShaderProgramTest.capture(() -> shader[0].canDraw()));
	}

	@Test
	public void checksItsAvailabilitySilentlyInDevMode() {
		final SourceShader[] shader = new SourceShader[1];
		ShaderProgramTest.capture(() -> shader[0] = new SourceShader(new BrokenStream(), ShaderProgramTest.source()));
		JOID.inst().setDevMode(true);
		try {
			Assert.assertEquals("", ShaderProgramTest.capture(() -> Assert.assertFalse(shader[0].isAvailable())));
			Assert.assertNotEquals("", ShaderProgramTest.capture(() -> Assert.assertFalse(shader[0].canDraw())));
		} finally {
			JOID.inst().setDevMode(false);
		}
	}

	@Test
	public void staysSilentWhenItIsAvailable() {
		final SourceShader shader = new SourceShader(ShaderProgramTest.source(), ShaderProgramTest.source());
		JOID.inst().setDevMode(true);
		try {
			Assert.assertEquals("", ShaderProgramTest.capture(() -> Assert.assertTrue(shader.canDraw())));
		} finally {
			JOID.inst().setDevMode(false);
		}
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingSource() {
		new SourceShader(null, ShaderProgramTest.source());
	}

	@Test
	public void reportsAnUnreadableShaderAsAJoidWarning() {
		final String error = ShaderProgramTest.capture(() -> new SourceShader(new BrokenStream(), new ByteArrayInputStream("void main() {}".getBytes(StandardCharsets.UTF_8))));
		Assert.assertTrue(error, error.startsWith("[JOID] "));
	}

	private static InputStream source() {
		return new ByteArrayInputStream("void main() {}".getBytes(StandardCharsets.UTF_8));
	}

	private static String capture(final Runnable runnable) {
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(error);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	private static final class SourceShader extends ShaderProgram {

		private SourceShader(final Object vertex, final Object fragment) {
			super.load(vertex, fragment);
		}

	}

	private static final class ClosingStream extends ByteArrayInputStream {

		private boolean closed;

		private ClosingStream() {
			super("void main() {}".getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public void close() throws IOException {
			this.closed = true;
			super.close();
		}

	}

	private static final class BrokenStream extends InputStream {

		@Override
		public int read() throws IOException {
			throw new IOException("broken");
		}

	}

}