package dev.joid.test.contract;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.CoreShader;
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
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.utils.geom.Point;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public abstract class RenderBridgeContractSuite {

	private static final int SIZE  = 64;
	private static final int ATLAS = 256;

	private static final int RED   = 0xFFFF0000;
	private static final int BLUE  = 0xFF0000FF;
	private static final int BLACK = 0xFF000000;
	private static final int GREEN = 0xFF00FF00;
	private static final int WHITE = 0xFFFFFFFF;

	private static ISnapshotBackend backend;

	protected abstract @NonNull ISnapshotBackend createBackend();

	@Before
	public void startBackend() {
		if (RenderBridgeContractSuite.backend == null) {
			RenderBridgeContractSuite.backend = this.createBackend();
			RenderBridgeContractSuite.backend.create(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		}

		ContractDraw.reset(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
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
	public void drawsVertexColors() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN)));
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void drawsEveryFrameBegunBeforeThePresent() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.beginFrame();
		try {
			render.clearColor(0F, 0F, 0F, 1F);
			render.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN));
		} finally {
			render.endFrame();
		}

		render.beginFrame();
		try {
			render.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(RenderBridgeContractSuite.SIZE / 2, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.RED));
		} finally {
			render.endFrame();
		}

		final SnapshotImage image = RenderBridgeContractSuite.backend.capture(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		RenderBridgeContractSuite.backend.present();
		ContractDraw.assertPixel(image, 16, 32, RenderBridgeContractSuite.GREEN);
		ContractDraw.assertPixel(image, 48, 32, RenderBridgeContractSuite.RED);
	}

	@Test
	public void refusesATextureLargerThanTheBackend() {
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> BridgeHandler.RENDER.get().createTexture().allocate(1 << 20, 1));
		Assert.assertTrue(exception.getMessage(), exception.getMessage().startsWith("A texture of 1048576x1 exceeds the maximum size "));
	}

	@Test
	public void compilesCoreShaders() {
		for (final CoreShader coreShader : CoreShader.values()) {
			final IShader shader = BridgeHandler.RENDER.get().createShader(coreShader.read(ShaderStage.VERTEX), coreShader.read(ShaderStage.FRAGMENT), BlendState.NORMAL);
			Assert.assertTrue("The " + coreShader.name() + " shader does not compile", shader.isActive());
		}
	}

	@Test
	public void skipsTheDrawsOfAShaderThatFailsToCompile() {
		final IShader shader = ContractDraw.shader("void main() {\n    fragColor = undeclaredColor;\n}");
		Assert.assertFalse("A shader that fails to compile must report isActive() == false", shader.isActive());

		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			shader.bind();
			try {
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN));
			} finally {
				shader.unbind();
			}

			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(RenderBridgeContractSuite.SIZE / 2, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN));
		});
		ContractDraw.assertPixel(image, 16, 32, RenderBridgeContractSuite.BLACK);
		ContractDraw.assertPixel(image, 48, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void appliesTheUniformsSetBeforeTheShaderIsBound() {
		final IShader shader = ContractDraw.shader("uniform vec4 tint;\n\nvoid main() {\n    fragColor = tint;\n}");
		shader.uniform("tint", 0F, 1F, 0F, 1F);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			shader.bind();
			try {
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
			} finally {
				shader.unbind();
			}
		});
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void appliesEveryUniformType() {
		final IShader shader = ContractDraw.shader("uniform float u_Float;\nuniform vec2 u_Vec2;\nuniform vec3 u_Vec3;\nuniform vec4 u_Vec4;\nuniform int u_Int;\nuniform bool u_Bool;\nuniform mat3 u_Matrix;\nuniform float u_Floats[3];\nuniform vec4 u_Colors[2];\n\nvoid main() {\n    bool valid = u_Float == 0.5 && u_Vec2 == vec2(1.0, 2.0) && u_Vec3 == vec3(3.0, 4.0, 5.0) && u_Vec4 == vec4(6.0, 7.0, 8.0, 9.0) && u_Int == 3 && u_Bool && u_Matrix[1][2] == 6.0 && u_Matrix[2][0] == 7.0 && u_Floats[2] == 12.0;\n    fragColor = valid ? u_Colors[1] : u_Colors[0];\n}");
		shader
		.uniform("u_Float", 0.5F)
		.uniform("u_Vec2", 1F, 2F)
		.uniform("u_Vec3", 3F, 4F, 5F)
		.uniform("u_Vec4", 6F, 7F, 8F, 9F)
		.uniform("u_Int", 3)
		.uniform("u_Bool", true)
		.uniform("u_Matrix", 1F, 2F, 3F, 4F, 5F, 6F, 7F, 8F, 9F)
		.uniform("u_Floats", 10F, 11F, 12F)
		.uniform("u_Colors", 1F, 0F, 0F, 1F, 0F, 1F, 0F, 1F);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			shader.bind();
			try {
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
			} finally {
				shader.unbind();
			}
		});
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void samplesTheTextureOfASampler() {
		final ITexture texture = BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {RenderBridgeContractSuite.GREEN});
		final IShader shader = ContractDraw.shader("uniform sampler2D u_Mask;\n\nvoid main() {\n    fragColor = texture(u_Mask, vec2(0.5, 0.5));\n}");
		shader.sampler("u_Mask", texture, TextureFilter.NEAREST, TextureWrap.CLAMP_TO_EDGE);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			shader.bind();
			try {
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
			} finally {
				shader.unbind();
			}
		});
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void samplesEveryTextureOfTheFirstDrawOfABackend() throws ReflectiveOperationException {
		final Field empty = RenderBridge.class.getDeclaredField("emptyTexture");
		empty.setAccessible(true);
		empty.set(BridgeHandler.RENDER.get(), null);
		final ITexture texture = BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {RenderBridgeContractSuite.GREEN});
		final IShader shader = ContractDraw.shader("uniform sampler2D u_First;\nuniform sampler2D u_Second;\n\nvoid main() {\n    fragColor = texture(u_First, vec2(0.5, 0.5)) * texture(u_Second, vec2(0.5, 0.5));\n}");
		shader.sampler("u_First", texture, TextureFilter.NEAREST, TextureWrap.CLAMP_TO_EDGE);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			shader.bind();
			try {
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
			} finally {
				shader.unbind();
			}
		});
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void readsTheLightingOfEachDraw() {
		final IShader shader = ContractDraw.shader("void main() {\n    fragColor = uLighting ? vec4(0.0, 1.0, 0.0, 1.0) : vec4(1.0, 0.0, 0.0, 1.0);\n}");
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			shader.bind();
			try {
				bridge.getState().lighting(true);
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, 0));
				bridge.getState().lighting(false);
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(RenderBridgeContractSuite.SIZE / 2, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, 0));
			} finally {
				bridge.getState().lighting(false);
				shader.unbind();
			}
		});
		ContractDraw.assertPixel(image, 16, 32, RenderBridgeContractSuite.GREEN);
		ContractDraw.assertPixel(image, 48, 32, RenderBridgeContractSuite.RED);
	}

	@Test
	public void litsAFaceTheSameAtAnyScale() {
		final SnapshotImage unit = RenderBridgeContractSuite.render(bridge -> RenderBridgeContractSuite.drawLitFace(bridge, 1D));
		final SnapshotImage scaled = RenderBridgeContractSuite.render(bridge -> RenderBridgeContractSuite.drawLitFace(bridge, 100D));
		final int lit = unit.getPixels()[32 + 32 * unit.getWidth()];
		Assert.assertTrue("A face turned to the light must be brighter than the ambient light", (lit & 255) > 0x80);
		ContractDraw.assertPixel(scaled, 32, 32, lit);
	}

	@Test
	public void disablesTheAlphaTestAtZero() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().blend(BlendState.PREMULTIPLIED).alphaCutoff(0.5F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, 0x0000FF00));
			bridge.getState().alphaCutoff(0F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(RenderBridgeContractSuite.SIZE / 2, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, 0x0000FF00));
		});
		ContractDraw.assertPixel(image, 16, 32, RenderBridgeContractSuite.BLACK);
		ContractDraw.assertPixel(image, 48, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void cullsTheBackFacesLikeOpenGl() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().cull(true);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN));
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(RenderBridgeContractSuite.SIZE, 0F, -RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN));
		});
		ContractDraw.assertPixel(image, 16, 32, RenderBridgeContractSuite.BLACK);
		ContractDraw.assertPixel(image, 48, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void cullsTheBackFacesInFrameBuffersLikeOpenGl() {
		final IFrameBuffer frameBuffer = BridgeHandler.RENDER.get().createFrameBuffer(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().frameBuffer(frameBuffer).cull(true);
			bridge.clearColor(0F, 0F, 0F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN));
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(RenderBridgeContractSuite.SIZE, 0F, -RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN));
			bridge.getState().frameBuffer(null).cull(false).color(1F, 1F, 1F, 1F);
			bridge.getState().texture(frameBuffer.getTexture()).textureFilter(TextureFilter.NEAREST).textureWrap(TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		frameBuffer.delete();

		ContractDraw.assertPixel(image, 16, 32, RenderBridgeContractSuite.BLACK);
		ContractDraw.assertPixel(image, 48, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void appliesTheStencilStateOfEachDraw() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.clearStencil();
			bridge.getState().colorWrite(false).stencil(StencilState.of(StencilFunction.ALWAYS, 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.REPLACE));
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.RED));
			bridge.getState().colorWrite(true).stencil(StencilState.of(StencilFunction.EQUAL, 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, RenderBridgeContractSuite.GREEN));
			bridge.getState().stencil(StencilState.DISABLED);
		});
		ContractDraw.assertPixel(image, 16, 32, RenderBridgeContractSuite.GREEN);
		ContractDraw.assertPixel(image, 48, 32, RenderBridgeContractSuite.BLACK);
	}

	@Test
	public void exposesTheLineState() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.getState().lineWidth(3F).lineSmooth(true);
		Assert.assertEquals(3F, render.getState().getLineWidth(), 0F);
		Assert.assertTrue(render.getState().isLineSmooth());
	}

	@Test
	public void drawsLinesOnePixelWide() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> bridge.draw(Primitive.LINES, RenderBridgeContractSuite.line(8F, 32.5F, 56F, 32.5F, RenderBridgeContractSuite.WHITE)));
		ContractDraw.assertPixel(image, 32, 31, RenderBridgeContractSuite.BLACK);
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.WHITE);
		ContractDraw.assertPixel(image, 32, 33, RenderBridgeContractSuite.BLACK);
	}

	@Test
	public void drawsAWideLineAsWideAsItsWidth() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().lineWidth(6F);
			DrawUtils.SHAPE.drawShape(DrawMode.LINES, Color.WHITE, Point.of(8D, 32D), Point.of(56D, 32D));
		});
		ContractDraw.assertPixel(image, 32, 27, RenderBridgeContractSuite.BLACK);
		ContractDraw.assertPixel(image, 32, 30, RenderBridgeContractSuite.WHITE);
		ContractDraw.assertPixel(image, 32, 33, RenderBridgeContractSuite.WHITE);
		ContractDraw.assertPixel(image, 32, 36, RenderBridgeContractSuite.BLACK);
	}

	@Test
	public void joinsTheSegmentsOfAWideLine() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().lineWidth(6F);
			DrawUtils.SHAPE.drawShape(DrawMode.LINE_STRIP, Color.WHITE, Point.of(8D, 32D), Point.of(32D, 32D), Point.of(32D, 56D));
		});
		ContractDraw.assertPixel(image, 32, 31, RenderBridgeContractSuite.WHITE);
		ContractDraw.assertPixel(image, 34, 29, RenderBridgeContractSuite.WHITE);
		ContractDraw.assertPixel(image, 36, 27, RenderBridgeContractSuite.BLACK);
	}

	@Test
	public void drawsAWideLineWithTheBoundShader() {
		final IShader shader = ContractDraw.shader("uniform vec4 tint;\n\nvoid main() {\n    fragColor = tint;\n}");
		shader.uniform("tint", 0F, 1F, 0F, 1F);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().lineWidth(6F);
			shader.bind();
			try {
				DrawUtils.SHAPE.drawShape(DrawMode.LINES, Color.WHITE, Point.of(8D, 32D), Point.of(56D, 32D));
			} finally {
				shader.unbind();
			}
		});
		ContractDraw.assertPixel(image, 32, 27, RenderBridgeContractSuite.BLACK);
		ContractDraw.assertPixel(image, 32, 30, RenderBridgeContractSuite.GREEN);
		ContractDraw.assertPixel(image, 32, 33, RenderBridgeContractSuite.GREEN);
		ContractDraw.assertPixel(image, 32, 36, RenderBridgeContractSuite.BLACK);
	}

	@Test
	public void uploadsArgbTextures() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final ITexture texture = render.createTexture().allocate(2, 2).upload(new int[] {RenderBridgeContractSuite.RED, RenderBridgeContractSuite.GREEN, RenderBridgeContractSuite.BLUE, RenderBridgeContractSuite.WHITE});
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().texture(texture).textureFilter(TextureFilter.NEAREST).textureWrap(TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		texture.delete();

		ContractDraw.assertPixel(image, 16, 16, RenderBridgeContractSuite.RED);
		ContractDraw.assertPixel(image, 48, 16, RenderBridgeContractSuite.GREEN);
		ContractDraw.assertPixel(image, 16, 48, RenderBridgeContractSuite.BLUE);
		ContractDraw.assertPixel(image, 48, 48, RenderBridgeContractSuite.WHITE);
	}

	@Test
	public void minifiesThroughMipmaps() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final int[] pixels = RenderBridgeContractSuite.checkerboard();

		final ITexture plain = render.createTexture().allocate(RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS).upload(pixels);
		final ITexture mipmapped = render.createTexture().mipmap(true).allocate(RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS).upload(pixels);
		final ITexture late = render.createTexture().allocate(RenderBridgeContractSuite.ATLAS, RenderBridgeContractSuite.ATLAS).upload(pixels).mipmap(true);

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
	public void samplesATransparentBorderBeyondTheTexture() {
		final int[] pixels = new int[16];
		Arrays.fill(pixels, RenderBridgeContractSuite.RED);
		final ITexture texture = BridgeHandler.RENDER.get().createTexture().allocate(4, 4).upload(pixels);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().blend(BlendState.DISABLED).texture(texture).textureFilter(TextureFilter.LINEAR).textureWrap(TextureWrap.CLAMP_TO_BORDER);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, -0.5F, 1.5F));
			bridge.getState().texture(null);
		});
		texture.delete();

		for (int x = 0; x < RenderBridgeContractSuite.SIZE; x++) {
			final double texel = (-0.5D + 2D * (x + 0.5D) / RenderBridgeContractSuite.SIZE) * 4D;
			final double coverage = Math.max(0D, Math.min(1D, Math.min(texel, 4D - texel) + 0.5D));
			final int red = image.getPixels()[x + 32 * image.getWidth()] >> 16 & 255;
			Assert.assertEquals("Pixel " + x + ",32", 255D * coverage, red, 2D);
		}
	}

	@Test
	public void deletesTexturesTwice() {
		final ITexture texture = BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {RenderBridgeContractSuite.WHITE});
		texture.delete();
		texture.delete();
	}

	@Test
	public void restoresTheStateOnPop() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IFrameBuffer frameBuffer = render.createFrameBuffer(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		final IShader shader = render.createShader(CoreShader.LINE.read(ShaderStage.VERTEX), CoreShader.LINE.read(ShaderStage.FRAGMENT), BlendState.NORMAL);
		render.pushState();
		try {
			render.getState().frameBuffer(frameBuffer).shader(shader).viewport(0, 0, 8, 8).lineWidth(4F).lineSmooth(true);
		} finally {
			render.popState();
		}

		Assert.assertNull(render.getState().getShader());
		Assert.assertFalse(render.getState().isLineSmooth());
		Assert.assertEquals(1F, render.getState().getLineWidth(), 0F);
		Assert.assertEquals(RenderBridgeContractSuite.SIZE, render.getState().getViewportWidth());
		Assert.assertEquals(RenderBridgeContractSuite.SIZE, render.getState().getViewportHeight());

		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().color(1F, 0F, 0F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
		});
		frameBuffer.delete();

		ContractDraw.assertPixel(image, 60, 60, RenderBridgeContractSuite.RED);
	}

	@Test
	public void rendersIntoFrameBuffers() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IFrameBuffer frameBuffer = render.createFrameBuffer(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().frameBuffer(frameBuffer);
			bridge.clearColor(0F, 1F, 0F, 1F);
			bridge.getState().color(1F, 0F, 0F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE, false, 0));
			bridge.getState().frameBuffer(null).color(1F, 1F, 1F, 1F);
			bridge.getState().texture(frameBuffer.getTexture()).textureFilter(TextureFilter.NEAREST).textureWrap(TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		frameBuffer.delete();

		ContractDraw.assertPixel(image, 16, 32, RenderBridgeContractSuite.RED);
		ContractDraw.assertPixel(image, 48, 32, RenderBridgeContractSuite.GREEN);
	}

	@Test
	public void rastersADrawingUprightAtItsPixelSize() {
		final int[] size = new int[2];
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> DrawUtils.RASTER.drawRaster(8D, 8D, 32D, 32D, (width, height) -> {
			size[0] = width;
			size[1] = height;
			bridge.getState().color(1F, 0F, 0F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, width, height / 2F, false, 0));
			bridge.getState().color(0F, 1F, 0F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, height / 2F, width, height / 2F, false, 0));
		}));
		Assert.assertEquals(32, size[0]);
		Assert.assertEquals(32, size[1]);
		ContractDraw.assertPixel(image, 24, 12, RenderBridgeContractSuite.RED);
		ContractDraw.assertPixel(image, 24, 36, RenderBridgeContractSuite.GREEN);
		ContractDraw.assertPixel(image, 4, 4, RenderBridgeContractSuite.BLACK);
		ContractDraw.assertPixel(image, 50, 50, RenderBridgeContractSuite.BLACK);
	}

	@Test
	public void hidesFarFacesBehindNearOnes() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> RenderBridgeContractSuite.drawInDepth(bridge, false));
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.RED);
	}

	@Test
	public void clearsTheDepth() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> RenderBridgeContractSuite.drawInDepth(bridge, true));
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.BLUE);
	}

	@Test
	public void testsTheDepthInFrameBuffers() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IFrameBuffer frameBuffer = render.createFrameBuffer(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().frameBuffer(frameBuffer);
			bridge.clearColor(0F, 0F, 0F, 1F);
			RenderBridgeContractSuite.drawInDepth(bridge, false);
			bridge.getState().frameBuffer(null).color(1F, 1F, 1F, 1F);
			bridge.getState().texture(frameBuffer.getTexture()).textureFilter(TextureFilter.NEAREST).textureWrap(TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		frameBuffer.delete();

		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.RED);
	}

	@Test
	public void keepsTheFrameBufferDepthAcrossTargets() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IFrameBuffer frameBuffer = render.createFrameBuffer(RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE);
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getModelView().push();
			try {
				bridge.getState().frameBuffer(frameBuffer).depthTest(true).depthWrite(true);
				bridge.clearColor(0F, 0F, 0F, 1F);
				bridge.clearDepth();
				bridge.getModelView().translate(0D, 0D, -10D);
				bridge.getState().color(1F, 0F, 0F, 1F);
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
				bridge.getState().frameBuffer(null);
				bridge.clearColor(0F, 0F, 0F, 1F);
				bridge.getState().frameBuffer(frameBuffer);
				bridge.getModelView().translate(0D, 0D, -10D);
				bridge.getState().color(0F, 0F, 1F, 1F);
				bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
			} finally {
				bridge.getState().depthTest(false).depthWrite(false);
				bridge.getModelView().pop();
			}

			bridge.getState().frameBuffer(null).color(1F, 1F, 1F, 1F);
			bridge.getState().texture(frameBuffer.getTexture()).textureFilter(TextureFilter.NEAREST).textureWrap(TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		frameBuffer.delete();

		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.RED);
	}

	@Test
	public void drawsWithTheCurrentColor() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().color(1F, 0F, 0F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
		});
		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.RED);
	}

	@Test
	public void followsTheOpenGlProjection() {
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().color(1F, 0F, 0F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE / 2, RenderBridgeContractSuite.SIZE / 2, false, 0));
		});
		ContractDraw.assertPixel(image, 8, 8, RenderBridgeContractSuite.RED);
		ContractDraw.assertPixel(image, 56, 56, RenderBridgeContractSuite.BLACK);
	}

	@Test
	public void resetsToAnOpaqueWhiteTexture() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final ITexture texture = render.createTexture().allocate(1, 1).upload(new int[] {RenderBridgeContractSuite.RED});
		final SnapshotImage image = RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().texture(texture).textureFilter(TextureFilter.NEAREST).textureWrap(TextureWrap.CLAMP_TO_EDGE).texture(null);
			bridge.getState().color(0F, 0F, 1F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, true, 0));
		});
		texture.delete();

		ContractDraw.assertPixel(image, 32, 32, RenderBridgeContractSuite.BLUE);
	}

	@Test
	public void keepsTranslationsExact() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.getModelView().with(matrix -> {
			matrix.scale(0.75D, 0.75D, 1D);
			matrix.translate(10.3D, 0D, 0D);
			Assert.assertEquals(7.725D, render.getPixelGrid().toScreenX(0D), 1E-4D);
		});
	}

	@Test
	public void quantizesAMotionToWholePixels() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.getModelView().with(matrix -> {
			matrix.scale(0.75D, 0.75D, 1D);
			matrix.translate(10.3D, 0D, 0D);
			render.quantize(10.3D, -4.1D);
			final PixelGrid grid = render.getPixelGrid();
			Assert.assertEquals(8D, grid.toScreenX(0D), 1E-4D);
			Assert.assertEquals(Math.rint(grid.toScreenY(-4.1D)), grid.toScreenY(-4.1D), 1E-4D);
		});
	}

	@Test
	public void restoresTheMatrixAfterATransformation() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final double before = render.getPixelGrid().toScreenX(0D);
		final Transformation transformation = Transformation.create().translate(Vector.create(10.3D, 4.6D)).scale(Scale.create(1.5D, 1.5D, 1D), Vector.create(40D, 40D));
		transformation.apply();
		final double during;
		try {
			during = render.getPixelGrid().toScreenX(0D);
		} finally {
			transformation.reset();
		}
		Assert.assertNotEquals(before, during, 1E-4D);
		Assert.assertEquals(before, render.getPixelGrid().toScreenX(0D), 0D);
	}

	private static SnapshotImage render(final Consumer<IRenderBridge> draw) {
		return ContractDraw.frame(RenderBridgeContractSuite.backend, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, draw);
	}

	private static void drawLitFace(final IRenderBridge bridge, final double scale) {
		bridge.getModelView().push();
		try {
			bridge.getModelView().translate(12D, 12D, -100D);
			bridge.getModelView().scale(scale, scale, scale);
			bridge.getState().lighting(true);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, (float) (40D / scale), (float) (40D / scale), false, 0xFF808080, true));
		} finally {
			bridge.getState().lighting(false);
			bridge.getModelView().pop();
		}
	}

	private static void drawInDepth(final IRenderBridge bridge, final boolean clearBetween) {
		bridge.getModelView().push();
		try {
			bridge.getState().depthTest(true).depthWrite(true);
			bridge.clearDepth();
			bridge.getModelView().translate(0D, 0D, -10D);
			bridge.getState().color(1F, 0F, 0F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
			if (clearBetween) {
				bridge.clearDepth();
			}

			bridge.getModelView().translate(0D, 0D, -10D);
			bridge.getState().color(0F, 0F, 1F, 1F);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0F, 0F, RenderBridgeContractSuite.SIZE, RenderBridgeContractSuite.SIZE, false, 0));
		} finally {
			bridge.getState().depthTest(false).depthWrite(false);
			bridge.getModelView().pop();
		}
	}

	private static int[] checkerboard() {
		final int[] pixels = new int[RenderBridgeContractSuite.ATLAS * RenderBridgeContractSuite.ATLAS];
		for (int y = 0; y < RenderBridgeContractSuite.ATLAS; y++) {
			for (int x = 0; x < RenderBridgeContractSuite.ATLAS; x++) {
				pixels[x + y * RenderBridgeContractSuite.ATLAS] = (x + y & 1) == 0 ? RenderBridgeContractSuite.WHITE : RenderBridgeContractSuite.BLACK;
			}
		}
		return pixels;
	}

	private static SnapshotImage minify(final ITexture texture) {
		return RenderBridgeContractSuite.render(bridge -> {
			bridge.getState().texture(texture).textureFilter(TextureFilter.LINEAR).textureWrap(TextureWrap.CLAMP_TO_EDGE);
			bridge.draw(Primitive.TRIANGLES, RenderBridgeContractSuite.quad(0.5F, 0.5F, 7, 7, true, 0));
		});
	}

	private static double deviation(final SnapshotImage image) {
		double sum = 0D;
		int count = 0;
		for (int y = 2; y < 7 - 1; y++) {
			for (int x = 2; x < 7 - 1; x++) {
				final int pixel = image.getPixels()[x + y * image.getWidth()];
				final double luma = ((pixel >> 16 & 255) + (pixel >> 8 & 255) + (pixel & 255)) / 3D;
				sum += Math.abs(luma - 127.5D);
				count++;
			}
		}
		return sum / count;
	}

	private static VertexBuffer quad(final float x, final float y, final float width, final float height, final float start, final float end) {
		return ContractDraw.quad(x, y, width, height, 0F, true, 0, false, start, end);
	}

	private static VertexBuffer quad(final float x, final float y, final float width, final float height, final boolean texture, final int color) {
		return RenderBridgeContractSuite.quad(x, y, width, height, texture, color, false);
	}

	private static VertexBuffer quad(final float x, final float y, final float width, final float height, final boolean texture, final int color, final boolean normal) {
		return ContractDraw.quad(x, y, width, height, 0F, texture, color, normal, 0F, 1F);
	}

	private static VertexBuffer line(final float startX, final float startY, final float endX, final float endY, final int color) {
		final float[][] points = {{startX, startY}, {endX, endY}};
		final ByteBuffer buffer = ByteBuffer.allocateDirect(points.length * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
		for (int i = 0; i < points.length; i++) {
			final int offset = i * VertexBuffer.STRIDE;
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset(), points[i][0]);
			buffer.putFloat(offset + VertexAttribute.POSITION.getOffset() + 4, points[i][1]);
			buffer.put(offset + VertexAttribute.COLOR.getOffset(), (byte) (color >> 16));
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 1, (byte) (color >> 8));
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 2, (byte) color);
			buffer.put(offset + VertexAttribute.COLOR.getOffset() + 3, (byte) (color >> 24));
		}
		return VertexBuffer.of(buffer, points.length, false, true, false);
	}

}