package dev.joid.lib.draw.resource;

import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge.Capture;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.shader.impl.RoundedShader;

import lombok.Getter;
import lombok.NonNull;

public class DrawResourceTest {

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	@Test(expected = RuntimeException.class)
	public void refusesASecondInstance() {
		Assert.assertSame(DrawUtils.RESOURCE, DrawResource.getInstance());
		new DrawResource();
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingResource() {
		DrawUtils.RESOURCE.drawResource(0D, 0D, 10D, 10D, null);
	}

	@Test
	public void drawsAnImageAtItsNaturalSize() {
		final Resource resource = DrawResourceTest.image(64, 32);
		DrawUtils.RESOURCE.drawResource(10D, 20D, resource);
		final Capture capture = this.single();
		Assert.assertEquals(6, capture.getCount());
		Assert.assertTrue(capture.isTexture());
		Assert.assertEquals(10D, capture.getLeft(), 1E-3D);
		Assert.assertEquals(74D, capture.getRight(), 1E-3D);
		Assert.assertEquals(20D, capture.getTop(), 1E-3D);
		Assert.assertEquals(52D, capture.getBottom(), 1E-3D);
	}

	@Test
	public void mapsTheWholeTextureOnTheQuad() {
		DrawUtils.RESOURCE.drawResource(10D, 20D, 100D, 50D, DrawResourceTest.image(64, 32));
		final Capture capture = this.single();
		Assert.assertEquals(70F, capture.getY(0), 1E-3F);
		Assert.assertEquals(0F, capture.getU(0), 0F);
		Assert.assertEquals(1F, capture.getV(0), 0F);
		Assert.assertEquals(110F, capture.getX(2), 1E-3F);
		Assert.assertEquals(1F, capture.getU(2), 0F);
		Assert.assertEquals(0F, capture.getV(2), 0F);
		Assert.assertEquals(0F, capture.getU(5), 0F);
		Assert.assertEquals(0F, capture.getV(5), 0F);
	}

	@Test
	public void bindsTheTextureOfTheResource() {
		final Resource resource = DrawResourceTest.image(64, 32);
		DrawUtils.RESOURCE.drawResource(10D, 20D, 100D, 50D, resource);
		final Capture capture = this.single();
		Assert.assertSame(resource.getTexture(), capture.getState().getTexture());
		Assert.assertSame(TextureFilter.LINEAR, capture.getState().getTextureFilter());
		Assert.assertSame(TextureWrap.CLAMP_TO_EDGE, capture.getState().getTextureWrap());
		Assert.assertSame(BlendState.NORMAL, capture.getState().getBlend());
	}

	@Test
	public void releasesTheTextureAndTheBlendingAfterItsDraw() {
		DrawUtils.RESOURCE.drawResource(10D, 20D, 100D, 50D, DrawResourceTest.image(64, 32));
		Assert.assertNull(this.render.getState().getTexture());
		Assert.assertSame(BlendState.DISABLED, this.render.getState().getBlend());
	}

	@Test
	public void snapsTheCornersOfAnImage() {
		this.render.resize(1366, 768);
		this.render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		DrawUtils.RESOURCE.drawResource(10.3D, 20.6D, 100D, 50D, DrawResourceTest.image(64, 32));
		final Capture capture = this.single();
		Assert.assertEquals(Math.rint(10.3D * 1366D / 1920D), capture.getLeft() * 1366D / 1920D, 1E-3D);
		Assert.assertEquals(Math.rint(110.3D * 1366D / 1920D), capture.getRight() * 1366D / 1920D, 1E-3D);
		Assert.assertEquals(Math.rint(20.6D * 768D / 1080D), capture.getTop() * 768D / 1080D, 1E-3D);
		Assert.assertEquals(Math.rint(70.6D * 768D / 1080D), capture.getBottom() * 768D / 1080D, 1E-3D);
	}

	@Test
	public void keepsAtLeastOnePixel() {
		this.render.resize(1366, 768);
		this.render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		DrawUtils.RESOURCE.drawResource(10.3D, 20.6D, 0.2D, 0.2D, DrawResourceTest.image(64, 32));
		final Capture capture = this.single();
		Assert.assertEquals(1D, (capture.getRight() - capture.getLeft()) * 1366D / 1920D, 1E-3D);
		Assert.assertEquals(1D, (capture.getBottom() - capture.getTop()) * 768D / 1080D, 1E-3D);
	}

	@Test
	public void smoothsTheEdgesOfARotatedImageWithTheRoundedShader() {
		this.render.pushMatrix();
		try {
			this.render.rotate(30D, 0D, 0D, 1D);
			DrawUtils.RESOURCE.drawResource(10.3D, 20.6D, 100D, 50D, DrawResourceTest.image(64, 32));
		} finally {
			this.render.popMatrix();
		}

		final Capture capture = this.single();
		Assert.assertSame(RoundedShader.inst().getShader(), capture.getState().getShader());
		Assert.assertEquals(0, capture.getUniforms().get("u_Aligned"));
		Assert.assertArrayEquals(new float[] {10.8F, 21.1F, 109.8F, 70.1F}, (float[]) capture.getUniforms().get("u_InnerRect"), 1E-4F);
		Assert.assertEquals(9.3D, capture.getLeft(), 1E-4D);
		Assert.assertEquals(71.6D, capture.getBottom(), 1E-4D);
		Assert.assertEquals(-0.01F, capture.getU(0), 1E-6F);
		Assert.assertEquals(1.02F, capture.getV(0), 1E-6F);
		Assert.assertSame(TextureWrap.CLAMP_TO_EDGE, capture.getState().getTextureWrap());
		Assert.assertNull(this.render.getShader());
	}

	@Test
	public void smoothsTheEdgesOfARotatedImageUnderTheShaderOfTheCaller() {
		final RecordingShader shader = new RecordingShader();
		this.render.shader(shader);
		this.render.pushMatrix();
		try {
			this.render.rotate(30D, 0D, 0D, 1D);
			DrawUtils.RESOURCE.drawResource(10D, 20D, 100D, 50D, DrawResourceTest.image(64, 32));
		} finally {
			this.render.popMatrix();
		}

		final Capture capture = this.single();
		Assert.assertSame(shader, capture.getState().getShader());
		Assert.assertTrue(capture.isColor());
		Assert.assertEquals(30, capture.getCount());
		Assert.assertEquals(9.5D, capture.getLeft(), 1E-4D);
		Assert.assertEquals(110.5D, capture.getRight(), 1E-4D);
		Assert.assertEquals(0.005F, capture.getU(0), 1E-6F);
	}

	@Test
	public void samplesARegionOfTheResource() {
		DrawUtils.RESOURCE.drawResource(10D, 20D, 100D, 50D, 16D, 8D, 32D, 16D, DrawResourceTest.image(64, 32));
		final Capture capture = this.single();
		Assert.assertEquals(10D, capture.getLeft(), 1E-3D);
		Assert.assertEquals(110D, capture.getRight(), 1E-3D);
		Assert.assertEquals(20D, capture.getTop(), 1E-3D);
		Assert.assertEquals(70D, capture.getBottom(), 1E-3D);
		Assert.assertEquals(0.25F, capture.getU(0), 0F);
		Assert.assertEquals(0.75F, capture.getV(0), 0F);
		Assert.assertEquals(0.75F, capture.getU(2), 0F);
		Assert.assertEquals(0.25F, capture.getV(2), 0F);
	}

	@Test
	public void samplesTheSpriteOfTheResource() {
		final Resource resource = DrawResourceTest.image(64, 32).textureCoords(16D, 8D, 32D, 16D);
		DrawUtils.RESOURCE.drawResource(10D, 20D, resource);
		final Capture capture = this.single();
		Assert.assertEquals(10D, capture.getLeft(), 1E-3D);
		Assert.assertEquals(42D, capture.getRight(), 1E-3D);
		Assert.assertEquals(20D, capture.getTop(), 1E-3D);
		Assert.assertEquals(36D, capture.getBottom(), 1E-3D);
		Assert.assertEquals(0.25F, capture.getU(0), 0F);
		Assert.assertEquals(0.75F, capture.getV(0), 0F);
		Assert.assertEquals(0.75F, capture.getU(2), 0F);
		Assert.assertEquals(0.25F, capture.getV(2), 0F);
	}

	@Test
	public void requestsThePixelSizeOfTheDraw() {
		final SizedDecoder decoder = new SizedDecoder(true);
		this.render.resize(1366, 768);
		this.render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		DrawUtils.RESOURCE.drawResource(0D, 0D, 100D, 50D, DrawResourceTest.decoded(decoder, TextureFilter.LINEAR));
		Assert.assertEquals(71, decoder.getWidth());
		Assert.assertEquals(36, decoder.getHeight());
	}

	@Test
	public void requestsNothingForASprite() {
		final SizedDecoder decoder = new SizedDecoder(true);
		DrawUtils.RESOURCE.drawResource(0D, 0D, 100D, 50D, DrawResourceTest.decoded(decoder, TextureFilter.LINEAR).textureCoords(0D, 0D, 32D, 16D));
		Assert.assertEquals(0, decoder.getWidth());
	}

	@Test
	public void mipmapsAnImageDrawnSmallerThanItsTexture() {
		final Resource resource = DrawResourceTest.image(64, 32);
		DrawUtils.RESOURCE.drawResource(0D, 0D, 32D, 16D, resource);
		Assert.assertEquals(Boolean.TRUE, resource.getProperties().getMipmap());
		Assert.assertTrue(((RecordingTexture) resource.getTexture()).isMipmapped());
	}

	@Test
	public void keepsAnImageDrawnAtFullSizeWithoutMipmaps() {
		final Resource resource = DrawResourceTest.image(64, 32);
		DrawUtils.RESOURCE.drawResource(0D, 0D, 64D, 32D, resource);
		DrawUtils.RESOURCE.drawResource(0D, 0D, 128D, 64D, resource);
		Assert.assertNull(resource.getProperties().getMipmap());
		Assert.assertFalse(((RecordingTexture) resource.getTexture()).isMipmapped());
	}

	@Test
	public void skipsTheMipmapsOfNearestFlatOrDecidedImages() {
		final Resource nearest = DrawResourceTest.decoded(new SizedDecoder(true), TextureFilter.NEAREST);
		final Resource flat = DrawResourceTest.decoded(new SizedDecoder(false), TextureFilter.LINEAR);
		final Resource chosen = DrawResourceTest.decoded(new SizedDecoder(true), TextureFilter.LINEAR).mipmap(false);
		for (final Resource resource : new Resource[] {nearest, flat, chosen}) {
			DrawUtils.RESOURCE.drawResource(0D, 0D, 16D, 8D, resource);
			Assert.assertNotEquals(Boolean.TRUE, resource.getProperties().getMipmap());
		}
	}

	@Test
	public void stretchesTheSpriteToTheRequestedSize() {
		DrawUtils.RESOURCE.drawResource(10D, 20D, 128D, 64D, DrawResourceTest.image(64, 32).textureCoords(16D, 8D, 32D, 16D));
		final Capture capture = this.render.getLast();
		Assert.assertEquals(0.25F, capture.getU(0), 0F);
		Assert.assertEquals(0.75F, capture.getU(2), 0F);
		Assert.assertEquals(138D, capture.getRight(), 1E-3D);
		Assert.assertEquals(84D, capture.getBottom(), 1E-3D);
	}

	private Capture single() {
		Assert.assertEquals(1, this.render.getCaptures().size());
		return this.render.getCaptures().get(0);
	}

	private static Resource image(final int width, final int height) {
		final Resource resource = ResourceBuilder.create().cache(null).blocking().linear().of(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
		resource.prepareBind();
		return resource;
	}

	private static Resource decoded(final IResourceDecoder decoder, final TextureFilter filter) {
		final Resource resource = ResourceBuilder.create().cache(null).blocking().interpolation(filter).compute("decoded", () -> new ResourceData("decoded", decoder));
		resource.prepareBind();
		return resource;
	}

	private static final class SizedDecoder implements IResourceDecoder {

		private final boolean mipmappable;

		@Getter private int width;
		@Getter private int height;

		private SizedDecoder(final boolean mipmappable) {
			this.mipmappable = mipmappable;
		}

		@Override
		public void init(final @NonNull ResourceData resource) {}

		@Override
		public void clear(final @NonNull ResourceData resource) {}

		@Override
		public void decode(final @NonNull ResourceData resource) {
			resource.width(64).height(32);
		}

		@Override
		public void update(final @NonNull ResourceData resource) {}

		@Override
		public void upload(final @NonNull ResourceData resource) {}

		@Override
		public void prepare(final @NonNull ResourceData resource) {
			resource.texture(BridgeHandler.RENDER.get().createTexture().allocate(64, 32));
		}

		@Override
		public void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async) {
			this.width = width;
			this.height = height;
		}

		@Override
		public boolean isMipmappable() {
			return this.mipmappable;
		}

	}

}