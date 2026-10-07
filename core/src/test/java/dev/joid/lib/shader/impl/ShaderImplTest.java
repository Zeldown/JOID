package dev.joid.lib.shader.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;

public class ShaderImplTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void loadsItsShaderFromTheRenderBridge() {
		final SourceShader shader = new SourceShader(ShaderImplTest.source(), ShaderImplTest.source());
		Assert.assertTrue(shader.getShader() instanceof RecordingShader);
		Assert.assertTrue(shader.isAvailable());
		shader.bind();
		Assert.assertSame(shader.getShader(), this.bridges.getRender().getShader());
		shader.unbind();
		Assert.assertNull(this.bridges.getRender().getShader());
	}

	@Test
	public void staysUnavailableWhenItsSourceCannotBeRead() {
		final SourceShader[] shader = new SourceShader[1];
		final String error = ShaderImplTest.capture(() -> shader[0] = new SourceShader(new BrokenStream(), ShaderImplTest.source()));
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
		ShaderImplTest.capture(() -> new SourceShader(new BrokenStream(), fragment));
		Assert.assertTrue(fragment.closed);
	}

	@Test
	public void warnsOnceInDevModeThatItIsUnavailable() {
		final SourceShader[] shader = new SourceShader[1];
		ShaderImplTest.capture(() -> shader[0] = new SourceShader(new BrokenStream(), ShaderImplTest.source()));
		JOID.inst().setDevMode(true);
		try {
			final String error = ShaderImplTest.capture(() -> {
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
		ShaderImplTest.capture(() -> shader[0] = new SourceShader(new BrokenStream(), ShaderImplTest.source()));
		Assert.assertEquals("", ShaderImplTest.capture(() -> shader[0].canDraw()));
	}

	@Test
	public void checksItsAvailabilitySilentlyInDevMode() {
		final SourceShader[] shader = new SourceShader[1];
		ShaderImplTest.capture(() -> shader[0] = new SourceShader(new BrokenStream(), ShaderImplTest.source()));
		JOID.inst().setDevMode(true);
		try {
			Assert.assertEquals("", ShaderImplTest.capture(() -> Assert.assertFalse(shader[0].isAvailable())));
			Assert.assertNotEquals("", ShaderImplTest.capture(() -> Assert.assertFalse(shader[0].canDraw())));
		} finally {
			JOID.inst().setDevMode(false);
		}
	}

	@Test
	public void staysSilentWhenItIsAvailable() {
		final SourceShader shader = new SourceShader(ShaderImplTest.source(), ShaderImplTest.source());
		JOID.inst().setDevMode(true);
		try {
			Assert.assertEquals("", ShaderImplTest.capture(() -> Assert.assertTrue(shader.canDraw())));
		} finally {
			JOID.inst().setDevMode(false);
		}
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingSource() {
		new SourceShader(null, ShaderImplTest.source());
	}

	@Test
	public void reportsAnUnreadableShaderAsAJoidWarning() {
		final String error = ShaderImplTest.capture(() -> new SourceShader(new BrokenStream(), new ByteArrayInputStream("void main() {}".getBytes(StandardCharsets.UTF_8))));
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

	private static final class SourceShader extends ShaderImpl {

		private SourceShader(final InputStream vertex, final InputStream fragment) {
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