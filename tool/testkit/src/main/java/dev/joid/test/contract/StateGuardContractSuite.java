package dev.joid.test.contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.StencilFunction;
import dev.joid.lib.bridge.render.state.StencilOperation;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public abstract class StateGuardContractSuite {

	private static final int SIZE = 64;

	private IStateGuardBackend backend;

	protected abstract @NonNull IStateGuardBackend createBackend();

	@Before
	public void startBackend() {
		this.backend = this.createBackend();
		this.backend.create(StateGuardContractSuite.SIZE, StateGuardContractSuite.SIZE);
	}

	@After
	public void stopBackend() {
		this.backend.destroy();
	}

	@Test
	public void leavesACleanStateAsItWas() {
		this.check(null);
	}

	@Test
	public void restoresTheBlendingSetBeforeJoid() {
		this.check(StateTrap.BLEND);
	}

	@Test
	public void drawsThroughTheScissorSetBeforeJoid() {
		this.check(StateTrap.SCISSOR);
	}

	@Test
	public void drawsThroughTheLogicOperationSetBeforeJoid() {
		this.check(StateTrap.LOGIC_OP);
	}

	@Test
	public void cullsWhateverTheFrontFaceSetBeforeJoid() {
		this.check(StateTrap.FRONT_FACE);
	}

	@Test
	public void drawsThroughTheAlphaTestSetBeforeJoid() {
		this.check(StateTrap.ALPHA_TEST);
	}

	@Test
	public void uploadsWhateverThePixelStoreSetBeforeJoid() {
		this.check(StateTrap.PIXEL_STORE);
	}

	@Test
	public void drawsWhateverTheFrameBufferSetBeforeJoid() {
		this.check(StateTrap.FRAMEBUFFER);
	}

	@Test
	public void uploadsWhateverThePixelBufferSetBeforeJoid() {
		this.check(StateTrap.PIXEL_BUFFER);
	}

	@Test
	public void fillsWhateverThePolygonModeSetBeforeJoid() {
		this.check(StateTrap.POLYGON_MODE);
	}

	@Test
	public void masksWhateverTheStencilMaskSetBeforeJoid() {
		this.check(StateTrap.STENCIL_MASK);
	}

	@Test
	public void keepsTheVertexArraySetBeforeJoid() {
		this.check(StateTrap.VERTEX_ARRAY);
	}

	@Test
	public void keepsTheClientArraysSetBeforeJoid() {
		this.check(StateTrap.CLIENT_ARRAYS);
	}

	@Test
	public void keepsTheMaterialSetBeforeJoid() {
		this.check(StateTrap.COLOR_MATERIAL);
	}

	@Test
	public void testsTheDepthWhateverTheDepthFunctionSetBeforeJoid() {
		this.check(StateTrap.DEPTH_FUNCTION);
	}

	@Test
	public void samplesWhateverTheSamplerObjectsSetBeforeJoid() {
		this.check(StateTrap.SAMPLER_OBJECTS);
	}

	@Test
	public void keepsTheTextureParametersSetBeforeJoid() {
		this.check(StateTrap.TEXTURE_PARAMETERS);
	}

	@Test
	public void survivesEveryTrapAtOnce() {
		this.check(StateTrap.EVERYTHING);
	}

	@Test
	public void rastersANativeDrawingUprightAtItsPixelSize() {
		final int[] size = new int[2];
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.beginFrame();
		try {
			render.loadIdentity();
			render.ortho(0D, StateGuardContractSuite.SIZE, StateGuardContractSuite.SIZE, 0D, 0D, 10000D);
			render.viewport(0, 0, StateGuardContractSuite.SIZE, StateGuardContractSuite.SIZE);
			render.frameBuffer(null);
			render.clearColor(0F, 0F, 0F, 1F);
			DrawUtils.RASTER.drawRaster(8D, 8D, 32D, 32D, (width, height) -> {
				size[0] = width;
				size[1] = height;
				this.backend.fill(0, height / 2, width, height / 2, 0xFFFF0000);
				this.backend.fill(0, 0, width, height / 2, 0xFF00FF00);
			});
		} finally {
			render.endFrame();
		}

		final SnapshotImage image = this.backend.capture(StateGuardContractSuite.SIZE, StateGuardContractSuite.SIZE);
		this.backend.present();
		Assert.assertEquals(32, size[0]);
		Assert.assertEquals(32, size[1]);
		StateGuardContractSuite.assertPixel(image, 24, 12, 0xFFFF0000);
		StateGuardContractSuite.assertPixel(image, 24, 36, 0xFF00FF00);
		StateGuardContractSuite.assertPixel(image, 4, 4, 0xFF000000);
		StateGuardContractSuite.assertPixel(image, 50, 50, 0xFF000000);
	}

	private void check(final StateTrap trap) {
		Assume.assumeTrue("This context has no " + trap, trap == null || this.backend.supports(trap));
		final SnapshotImage expected = this.frame();
		if (trap != null) {
			this.backend.inject(trap);
		}

		final SnapshotImage actual = this.frame();
		for (int i = 0; i < expected.getPixels().length; i++) {
			if (expected.getPixels()[i] != actual.getPixels()[i]) {
				Assert.fail("With the " + trap + " of the host, the pixel " + i % expected.getWidth() + "," + i / expected.getWidth() + " is " + String.format("#%08X", actual.getPixels()[i]) + " instead of " + String.format("#%08X", expected.getPixels()[i]));
			}
		}
	}

	private SnapshotImage frame() {
		final Map<String, String> before = this.backend.readState();
		final AtomicReference<Map<String, String>> suspended = new AtomicReference<>();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.beginFrame();
		try {
			render.loadIdentity();
			render.ortho(0D, StateGuardContractSuite.SIZE, StateGuardContractSuite.SIZE, 0D, 0D, 10000D);
			render.viewport(0, 0, StateGuardContractSuite.SIZE, StateGuardContractSuite.SIZE);
			render.frameBuffer(null);
			render.shader(null);
			render.resetTexture();
			render.blend(BlendState.NORMAL);
			render.depthTest(false);
			render.depthWrite(false);
			render.cull(false);
			render.color(1F, 1F, 1F, 1F);
			render.alphaCutoff(0F);
			render.clearColor(0F, 0F, 0F, 1F);
			StateGuardContractSuite.drawTexture(render);
			StateGuardContractSuite.drawTranslucent(render);
			StateGuardContractSuite.drawInDepth(render);
			render.suspend(() -> {
				StateGuardContractSuite.assertState("JOID must give the host its state back before a nested host draw", before, this.backend.readState());
				this.backend.drawExternal();
				suspended.set(this.backend.readState());
			});
			StateGuardContractSuite.drawCulled(render);
			StateGuardContractSuite.drawMasked(render);
			StateGuardContractSuite.drawFrameBuffer(render);
			StateGuardContractSuite.drawSampler(render);
			StateGuardContractSuite.drawMipmaps(render);
		} finally {
			render.endFrame();
		}

		StateGuardContractSuite.assertState("JOID must leave the state of the host as the nested host draw left it", suspended.get(), this.backend.readState());
		final SnapshotImage image = this.backend.capture(StateGuardContractSuite.SIZE, StateGuardContractSuite.SIZE);
		StateGuardContractSuite.assertState("Reading the pixels must leave the state of the host", suspended.get(), this.backend.readState());
		this.backend.present();
		return image;
	}

	private static void drawTexture(final IRenderBridge render) {
		final ITexture texture = render.createTexture().allocate(2, 2).upload(new int[] {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFFFF}, 2, 2);
		render.texture(texture, TextureFilter.NEAREST, TextureWrap.CLAMP_TO_EDGE);
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(0F, 0F, 16F, 16F, -1F, true, 0));
		render.resetTexture();
		texture.delete();
	}

	private static void drawTranslucent(final IRenderBridge render) {
		render.color(0F, 1F, 0F, 0.5F);
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(16F, 0F, 16F, 16F, -1F, false, 0));
		render.color(1F, 1F, 1F, 1F);
	}

	private static void drawInDepth(final IRenderBridge render) {
		render.depthTest(true);
		render.depthWrite(true);
		render.clearDepth();
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(32F, 0F, 16F, 16F, -10F, false, 0xFFFF0000));
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(32F, 0F, 16F, 16F, -20F, false, 0xFF0000FF));
		render.depthTest(false);
		render.depthWrite(false);
	}

	private static void drawCulled(final IRenderBridge render) {
		render.cull(true);
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(48F, 0F, 16F, 16F, -1F, false, 0xFFFFFF00));
		render.cull(false);
	}

	private static void drawMasked(final IRenderBridge render) {
		render.clearStencil();
		render.colorWrite(false);
		render.stencil(StencilState.create(StencilFunction.ALWAYS, 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.REPLACE));
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(0F, 16F, 8F, 16F, -1F, false, 0xFFFFFFFF));
		render.colorWrite(true);
		render.stencil(StencilState.create(StencilFunction.EQUAL, 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(0F, 16F, 16F, 16F, -1F, false, 0xFF00FFFF));
		render.stencil(StencilState.DISABLED);
	}

	private static void drawFrameBuffer(final IRenderBridge render) {
		final IFrameBuffer frameBuffer = render.createFrameBuffer(StateGuardContractSuite.SIZE, StateGuardContractSuite.SIZE);
		render.frameBuffer(frameBuffer);
		render.clearColor(1F, 0F, 1F, 1F);
		render.frameBuffer(null);
		render.texture(frameBuffer.getTexture(), TextureFilter.NEAREST, TextureWrap.CLAMP_TO_EDGE);
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(16F, 16F, 16F, 16F, -1F, true, 0));
		render.resetTexture();
		frameBuffer.delete();
	}

	private static void drawSampler(final IRenderBridge render) {
		final ITexture texture = render.createTexture().allocate(1, 1).upload(new int[] {0xFF808000}, 1, 1);
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, "void main() {\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}");
		final IShader shader = render.createShader(vertex, ShaderSource.parse(ShaderStage.FRAGMENT, "uniform sampler2D u_Mask;\n\nvoid main() {\n    fragColor = texture(u_Mask, vec2(0.5, 0.5));\n}"), BlendState.NORMAL);
		shader.sampler("u_Mask", texture, TextureFilter.LINEAR, TextureWrap.REPEAT);
		shader.bind();
		try {
			render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(32F, 16F, 16F, 16F, -1F, false, 0));
		} finally {
			shader.unbind();
			texture.delete();
		}
	}

	private static void drawMipmaps(final IRenderBridge render) {
		final int[] pixels = new int[64 * 64];
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = (i % 64 + i / 64 & 1) == 0 ? 0xFFFFFFFF : 0xFF000000;
		}

		final ITexture texture = render.createTexture().mipmap(true).allocate(64, 64).upload(pixels, 64, 64);
		render.texture(texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE);
		render.draw(Primitive.TRIANGLES, StateGuardContractSuite.quad(48F, 16F, 8F, 8F, -1F, true, 0));
		render.resetTexture();
		texture.delete();
	}

	private static void assertState(final String message, final Map<String, String> expected, final Map<String, String> actual) {
		final StringBuilder differences = new StringBuilder();
		for (final String name : new TreeSet<>(expected.keySet())) {
			if (!expected.get(name).equals(actual.get(name))) {
				differences.append("\n  ").append(name).append(": ").append(expected.get(name)).append(" -> ").append(actual.get(name));
			}
		}
		if (differences.length() > 0) {
			Assert.fail(message + ":" + differences);
		}
	}

	private static void assertPixel(final SnapshotImage image, final int x, final int y, final int expected) {
		final int actual = image.getPixels()[x + y * image.getWidth()];
		Assert.assertEquals("Pixel " + x + "," + y, String.format("#%08X", expected), String.format("#%08X", actual));
	}

	private static VertexBuffer quad(final float x, final float y, final float width, final float height, final float z, final boolean texture, final int color) {
		final float[][] corners = {{x, y, 0F, 0F}, {x + width, y, 1F, 0F}, {x + width, y + height, 1F, 1F}, {x, y, 0F, 0F}, {x + width, y + height, 1F, 1F}, {x, y + height, 0F, 1F}};
		final ByteBuffer buffer = ByteBuffer.allocateDirect(corners.length * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
		for (int i = 0; i < corners.length; i++) {
			final int offset = i * VertexBuffer.STRIDE;
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset(), corners[i][0]);
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset() + 4, corners[i][1]);
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset() + 8, z);
			buffer.putFloat(offset + VertexAttribute.TEXTURE_COORDINATE.getOffset(), corners[i][2]);
			buffer.putFloat(offset + VertexAttribute.TEXTURE_COORDINATE.getOffset() + 4, corners[i][3]);
			buffer.put(offset + VertexAttribute.COLOR.getOffset(), (byte) (color >> 16));
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 1, (byte) (color >> 8));
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 2, (byte) color);
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 3, (byte) (color >> 24));
			buffer.put(offset + VertexAttribute.NORMAL.getOffset() + 2, (byte) 127);
		}
		return VertexBuffer.create(buffer, corners.length, texture, color != 0, false);
	}

}