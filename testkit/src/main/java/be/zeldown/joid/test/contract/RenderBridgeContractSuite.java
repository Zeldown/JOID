package be.zeldown.joid.test.contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderStage;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.bridge.render.vertex.VertexBuffer;
import be.zeldown.joid.test.shader.CoreShaders;
import be.zeldown.joid.test.snapshot.ISnapshotBackend;
import be.zeldown.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public abstract class RenderBridgeContractSuite {

	private static final int SIZE     = 64;
	private static final int ATLAS    = 256;
	private static final int MINIFIED = 7;

	private static final int RED   = 0xFFFF0000;
	private static final int BLUE  = 0xFF0000FF;
	private static final int BLACK = 0xFF000000;
	private static final int GREEN = 0xFF00FF00;
	private static final int WHITE = 0xFFFFFFFF;

	private static ISnapshotBackend backend;

	@Before
	public void startBackend() {
		if (RenderBridgeContractSuite.backend == null) {
			RenderBridgeContractSuite.backend = this.createBackend();
			RenderBridgeContractSuite.backend.create(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.loadIdentity();
		render.ortho(0D, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, 0D, 0D, 10000D);
		render.viewport(0, 0, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		render.frameBuffer(null);
		render.shader(null);
		render.resetTexture();
		render.blend(BlendState.NORMAL);
		render.depth(false, false);
		render.cull(false);
		render.color(1F, 1F, 1F, 1F);
		render.lineWidth(1F);
		render.lineSmooth(false);
	}

	@Test
	public void drawsVertexColors() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN)));
		RenderBridgeContractSuite.assertPixel(image, 32, 32, RenderBridgeContractSuite.GREEN);
	}

	@AfterClass
	public static void stopBackend() {
		if (RenderBridgeContractSuite.backend == null) {
			return;
		}

		RenderBridgeContractSuite.backend.destroy();
		RenderBridgeContractSuite.backend = null;
	}

	@Test
	public void compilesCoreShaders() {
		for (final String name : CoreShaders.getNames()) {
			final IShader shader = BridgeHandler.RENDER.get().createShader(CoreShaders.read(name, ShaderStage.VERTEX), CoreShaders.read(name, ShaderStage.FRAGMENT), BlendState.NORMAL);
			Assert.assertTrue("The " + name + " shader does not compile", shader.isActive());
		}
	}

	@Test
	public void exposesTheLineState() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.lineWidth(3F);
		render.lineSmooth(true);
		Assert.assertEquals(3F, render.getLineWidth(), 0F);
		Assert.assertTrue(render.isLineSmooth());
	}

	@Test
	public void uploadsArgbTextures() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final ITexture texture = render.createTexture().allocate(2, 2).upload(new int[] {RenderBridgeContractSuite.RED, RenderBridgeContractSuite.GREEN, RenderBridgeContractSuite.BLUE, RenderBridgeContractSuite.WHITE}, 2, 2);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.texture(texture, TextureFilter.NEAREST, TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		texture.delete();

		RenderBridgeContractSuite.assertPixel(image, 16, 16, RenderBridgeContractSuite.RED);
		RenderBridgeContractSuite.assertPixel(image, 48, 16, RenderBridgeContractSuite.GREEN);
		RenderBridgeContractSuite.assertPixel(image, 16, 48, RenderBridgeContractSuite.BLUE);
		RenderBridgeContractSuite.assertPixel(image, 48, 48, RenderBridgeContractSuite.WHITE);
	}

	@Test
	public void deletesTexturesTwice() {
		final ITexture texture = BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {RenderBridgeContractSuite.WHITE}, 1, 1);
		texture.delete();
		texture.delete();
	}

	@Test
	public void restoresTheStateOnPop() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IFrameBuffer frameBuffer = render.createFrameBuffer(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, TextureFilter.NEAREST);
		final IShader shader = render.createShader(CoreShaders.read("line", ShaderStage.VERTEX), CoreShaders.read("line", ShaderStage.FRAGMENT), BlendState.NORMAL);
		render.pushState();
		render.frameBuffer(frameBuffer);
		render.shader(shader);
		render.viewport(0, 0, 8, 8);
		render.lineWidth(4F);
		render.lineSmooth(true);
		render.popState();

		Assert.assertNull(render.getShader());
		Assert.assertFalse(render.isLineSmooth());
		Assert.assertEquals(1F, render.getLineWidth(), 0F);
		Assert.assertEquals(RenderBridgeContractSuite.SIZE, render.getViewportWidth());
		Assert.assertEquals(RenderBridgeContractSuite.SIZE, render.getViewportHeight());

		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.color(1F, 0F, 0F, 1F);
			bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
		});
		frameBuffer.delete();

		RenderBridgeContractSuite.assertPixel(image, 60, 60, RenderBridgeContractSuite.RED);
	}

	@Test
	public void minifiesThroughMipmaps() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final int[] pixels = RenderBridgeContractSuite.checkerboard();

		final ITexture plain = render.createTexture().allocate(RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS).upload(pixels, RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS);
		final ITexture mipmapped = render.createTexture().mipmap(true).allocate(RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS).upload(pixels, RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS);
		final ITexture late = render.createTexture().allocate(RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS).upload(pixels, RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS).mipmap(true);

		final double plainDeviation = RenderBridgeContractSuite.deviation(RenderBridgeContractSuite.minify(plain));
		final double mipmapDeviation = RenderBridgeContractSuite.deviation(RenderBridgeContractSuite.minify(mipmapped));
		final double lateDeviation = RenderBridgeContractSuite.deviation(RenderBridgeContractSuite.minify(late));

		plain.delete();
		mipmapped.delete();
		late.delete();

		Assert.assertFalse("A texture must not be mipmapped by default", plain.isMipmapped());
		Assert.assertTrue("A texture asked to mipmap must report it", mipmapped.isMipmapped());
		Assert.assertTrue("A mipmapped checkerboard must minify to its average (deviation " + mipmapDeviation + ")", mipmapDeviation < 16D);
		Assert.assertTrue("Mipmaps must reduce the minification error (plain " + plainDeviation + ", mipmapped " + mipmapDeviation + ")", mipmapDeviation <= plainDeviation);
		Assert.assertTrue("Mipmaps asked after the upload must keep the content and minify to its average (deviation " + lateDeviation + ")", lateDeviation < 16D);
	}

	@Test
	public void rendersIntoFrameBuffers() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IFrameBuffer frameBuffer = render.createFrameBuffer(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, TextureFilter.NEAREST);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.frameBuffer(frameBuffer);
			bridge.clear(0F, 1F, 0F, 1F);
			bridge.color(1F, 0F, 0F, 1F);
			bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, 0));
			bridge.frameBuffer(null);
			bridge.color(1F, 1F, 1F, 1F);
			bridge.texture(frameBuffer.getTexture(), TextureFilter.NEAREST, TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		frameBuffer.delete();

		RenderBridgeContractSuite.assertPixel(image, 16, 32, RenderBridgeContractSuite.RED);
		RenderBridgeContractSuite.assertPixel(image, 48, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void drawsWithTheCurrentColor() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.color(1F, 0F, 0F, 1F);
			bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
		});
		RenderBridgeContractSuite.assertPixel(image, 32, 32, RenderBridgeContractSuite.RED);
	}

	@Test
	public void followsTheOpenGlProjection() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.color(1F, 0F, 0F, 1F);
			bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE / 2, false, 0));
		});
		RenderBridgeContractSuite.assertPixel(image, 8, 8, RenderBridgeContractSuite.RED);
		RenderBridgeContractSuite.assertPixel(image, 56, 56, RenderBridgeContractSuite.BLACK);
	}

	@Test
	public void resetsToAnOpaqueWhiteTexture() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final ITexture texture = render.createTexture().allocate(1, 1).upload(new int[] {RenderBridgeContractSuite.RED}, 1, 1);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.texture(texture, TextureFilter.NEAREST, TextureWrap.CLAMP_TO_EDGE);
			bridge.resetTexture();
			bridge.color(0F, 0F, 1F, 1F);
			bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		texture.delete();

		RenderBridgeContractSuite.assertPixel(image, 32, 32, RenderBridgeContractSuite.BLUE);
	}

	protected abstract @NonNull ISnapshotBackend createBackend();

	private static int[] checkerboard() {
		final int[] pixels = new int[RenderBridgeContractSuite.ATLAS * RenderBridgeContractSuite.ATLAS];
		for (int y = 0; y < RenderBridgeContractSuite.ATLAS; y++) {
			for (int x = 0; x < RenderBridgeContractSuite.ATLAS; x++) {
				pixels[x + y * RenderBridgeContractSuite.ATLAS] = (x + y & 1) == 0 ? RenderBridgeContractSuite.WHITE : RenderBridgeContractSuite.BLACK;
			}
		}
		return pixels;
	}

	private static double deviation(final SnapshotImage image) {
		double sum = 0D;
		int count = 0;
		for (int y = 2; y < RenderBridgeContractSuite.MINIFIED - 1; y++) {
			for (int x = 2; x < RenderBridgeContractSuite.MINIFIED - 1; x++) {
				final int pixel = image.getPixels()[x + y * image.getWidth()];
				final double luma = ((pixel >> 16 & 255) + (pixel >> 8 & 255) + (pixel & 255)) / 3D;
				sum += Math.abs(luma - 127.5D);
				count++;
			}
		}
		return sum / count;
	}

	private static SnapshotImage minify(final ITexture texture) {
		return RenderBridgeContractSuite.render(bridge -> {
			bridge.texture(texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(DrawMode.TRIANGLES, RenderBridgeContractSuite.quad(0.5F, 0.5F, RenderBridgeContractSuite.MINIFIED, RenderBridgeContractSuite.MINIFIED, true, 0));
		});
	}

	private static SnapshotImage render(final Consumer<IRenderBridge> draw) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		RenderBridgeContractSuite.backend.frame(() -> {
			render.clear(0F, 0F, 0F, 1F);
			draw.accept(render);
		});

		final SnapshotImage image = RenderBridgeContractSuite.backend.capture(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		RenderBridgeContractSuite.backend.present();
		return image;
	}

	private static void assertPixel(final SnapshotImage image, final int x, final int y, final int expected) {
		final int actual = image.getPixels()[x + y * image.getWidth()];
		Assert.assertEquals("Pixel " + x + "," + y, String.format("#%08X", expected), String.format("#%08X", actual));
	}

	private static VertexBuffer quad(final float x, final float y, final float width, final float height, final boolean texture, final int color) {
		final float[][] corners = {{x, y, 0F, 0F}, {x + width, y, 1F, 0F}, {x + width, y + height, 1F, 1F}, {x, y, 0F, 0F}, {x + width, y + height, 1F, 1F}, {x, y + height, 0F, 1F}};
		final ByteBuffer buffer = ByteBuffer.allocateDirect(corners.length * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
		for (int i = 0; i < corners.length; i++) {
			final int offset = i * VertexBuffer.STRIDE;
			buffer.putFloat(offset + VertexBuffer.POSITION_OFFSET, corners[i][0]);
			buffer.putFloat(offset + VertexBuffer.POSITION_OFFSET + 4, corners[i][1]);
			buffer.putFloat(offset + VertexBuffer.TEXTURE_OFFSET, corners[i][2]);
			buffer.putFloat(offset + VertexBuffer.TEXTURE_OFFSET + 4, corners[i][3]);
			buffer.put(offset + VertexBuffer.COLOR_OFFSET, (byte) (color >> 16));
			buffer.put(offset + VertexBuffer.COLOR_OFFSET + 1, (byte) (color >> 8));
			buffer.put(offset + VertexBuffer.COLOR_OFFSET + 2, (byte) color);
			buffer.put(offset + VertexBuffer.COLOR_OFFSET + 3, (byte) (color >> 24));
		}
		return VertexBuffer.create(buffer, corners.length, texture, color != 0, false);
	}

}