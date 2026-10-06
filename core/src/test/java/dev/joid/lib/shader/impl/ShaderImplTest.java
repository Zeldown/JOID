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

	private static final class BrokenStream extends InputStream {

		@Override
		public int read() throws IOException {
			throw new IOException("broken");
		}

	}

}