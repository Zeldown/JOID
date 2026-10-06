package dev.joid.lib.resource.dto.decoder.impl;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.animation.IResourceAnimationReader;
import dev.joid.lib.resource.dto.animation.impl.ApngResourceAnimationReader;
import dev.joid.lib.resource.dto.animation.impl.GifResourceAnimationReader;
import lombok.NonNull;

public class AnimatedResourceDecoderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void preparesATransparentPlaceholder() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = new ResourceData("blink.png", decoder);
		decoder.prepare(data);
		final RecordingTexture texture = (RecordingTexture) decoder.getTexture();
		Assert.assertSame(texture, data.getTextures()[0]);
		Assert.assertEquals(1, texture.getWidth());
		Assert.assertArrayEquals(new int[] {0}, texture.getPixels());
	}

	@Test
	public void decodesTheFirstFrame() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = new ResourceData("blink.png", decoder);
		decoder.decode(data);
		Assert.assertEquals(8, data.getWidth());
		Assert.assertEquals(8, data.getHeight());
		Assert.assertSame(decoder.getAnimation().getFrames().get(0).getPixels(), data.getData()[0]);
		Assert.assertEquals(0.24D, decoder.getDuration(), 0D);
	}

	@Test
	public void reportsAnAnimationThatCannotOpen() {
		final AnimatedResourceDecoder decoder = new AnimatedResourceDecoder(new OfflineAsset(), new GifResourceAnimationReader());
		try {
			decoder.decode(new ResourceData("offline.gif", decoder));
			Assert.fail("The animation must not decode");
		} catch (final RuntimeException expected) {
			Assert.assertEquals("Unable to read the animation of offline.gif", expected.getMessage());
			Assert.assertTrue(expected.getCause() instanceof IOException);
		}
	}

	@Test
	public void startsOnUploadByDefault() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		AnimatedResourceDecoderTest.load(decoder);
		final RecordingTexture texture = (RecordingTexture) decoder.getTexture();
		Assert.assertTrue(decoder.isAutoplay());
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(0, decoder.getDisplayed());
		Assert.assertEquals(8, texture.getWidth());
		Assert.assertArrayEquals(decoder.getAnimation().getFrames().get(0).getPixels(), texture.getPixels());
	}

	@Test
	public void waitsWithoutAutoplay() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng().autoplay(false);
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.bridges.getClock().advance(100L);
		decoder.update(data);
		Assert.assertFalse(decoder.isAutoplay());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertEquals(0, decoder.getDisplayed());
		Assert.assertEquals(0D, decoder.getCurrentTime(), 0D);
	}

	@Test
	public void showsEachFrameOnTheClock() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 39L);
		Assert.assertEquals(0, decoder.getDisplayed());
		this.advance(decoder, data, 1L);
		Assert.assertEquals(1, decoder.getDisplayed());
		Assert.assertArrayEquals(decoder.getAnimation().getFrames().get(1).getPixels(), ((RecordingTexture) decoder.getTexture()).getPixels());
		this.advance(decoder, data, 80L);
		Assert.assertEquals(2, decoder.getDisplayed());
		this.advance(decoder, data, 120L);
		Assert.assertEquals(0, decoder.getDisplayed());
	}

	@Test
	public void measuresItsPosition() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 60L);
		Assert.assertEquals(0.06D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertEquals(0.25D, decoder.getProgress(), 1E-9D);
		this.advance(decoder, data, 250L);
		Assert.assertEquals(0.07D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void playsAsManyTimesAsItsFileSays() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		Assert.assertFalse(decoder.isLoop());
		this.advance(decoder, data, 470L);
		Assert.assertTrue(decoder.isPlaying());
		this.advance(decoder, data, 10L);
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertEquals(2, decoder.getDisplayed());
		Assert.assertEquals(0.239D, decoder.getCurrentTime(), 1E-9D);
		this.advance(decoder, data, 1000L);
		Assert.assertEquals(0.239D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void loopsAnEndlessGifForever() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.gif();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		Assert.assertTrue(decoder.isLoop());
		this.advance(decoder, data, 10000L);
		Assert.assertTrue(decoder.isPlaying());
	}

	@Test
	public void overridesTheLoopOfItsFile() {
		final AnimatedResourceDecoder apng = AnimatedResourceDecoderTest.apng().loop(true);
		final AnimatedResourceDecoder gif = AnimatedResourceDecoderTest.gif().loop(false);
		final ResourceData apngData = AnimatedResourceDecoderTest.load(apng);
		final ResourceData gifData = AnimatedResourceDecoderTest.load(gif);
		Assert.assertTrue(apng.isLoop());
		Assert.assertFalse(gif.isLoop());
		this.bridges.getClock().advance(2000L);
		apng.update(apngData);
		gif.update(gifData);
		Assert.assertTrue(apng.isPlaying());
		Assert.assertFalse(gif.isPlaying());
		Assert.assertEquals(2, gif.getDisplayed());
	}

	@Test
	public void stopsOnTheCurrentFrame() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 100L);
		Assert.assertSame(decoder, decoder.stop());
		this.advance(decoder, data, 500L);
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertFalse(decoder.isPaused());
		Assert.assertEquals(1, decoder.getDisplayed());
		Assert.assertEquals(0.1D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void playsFromTheBeginning() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 100L);
		decoder.stop();
		Assert.assertSame(decoder, decoder.play());
		this.advance(decoder, data, 10L);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(0, decoder.getDisplayed());
		Assert.assertEquals(0.01D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void keepsPlayingOnPlay() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 100L);
		Assert.assertSame(decoder, decoder.play());
		this.advance(decoder, data, 10L);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(0.11D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void resumesAPausedPlaybackOnPlay() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 50L);
		decoder.pause();
		this.advance(decoder, data, 100L);
		Assert.assertSame(decoder, decoder.play());
		this.advance(decoder, data, 10L);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(0.06D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void restartsFromTheBeginning() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 100L);
		Assert.assertSame(decoder, decoder.restart());
		this.advance(decoder, data, 10L);
		Assert.assertTrue(decoder.isPlaying());
		Assert.assertEquals(0, decoder.getDisplayed());
		Assert.assertEquals(0.01D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void freezesWhilePaused() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 50L);
		Assert.assertSame(decoder, decoder.pause());
		this.advance(decoder, data, 1000L);
		Assert.assertTrue(decoder.isPaused());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertEquals(0.05D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertSame(decoder, decoder.resume());
		this.advance(decoder, data, 30L);
		Assert.assertFalse(decoder.isPaused());
		Assert.assertEquals(0.08D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void keepsTheFirstPause() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 50L);
		decoder.pause();
		this.advance(decoder, data, 100L);
		decoder.pause().resume();
		this.advance(decoder, data, 10L);
		Assert.assertEquals(0.06D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void pausesOnlyARunningPlayback() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng().autoplay(false);
		AnimatedResourceDecoderTest.load(decoder);
		decoder.pause();
		Assert.assertFalse(decoder.isPaused());
	}

	@Test
	public void resumesOnlyAPausedPlayback() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 50L);
		decoder.resume();
		this.advance(decoder, data, 10L);
		Assert.assertEquals(0.06D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void seeksWhilePlaying() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		Assert.assertSame(decoder, decoder.seek(0.13D));
		decoder.update(data);
		Assert.assertEquals(0.13D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertEquals(2, decoder.getDisplayed());
		decoder.seek(-1D);
		Assert.assertEquals(0D, decoder.getCurrentTime(), 0D);
	}

	@Test
	public void seeksWhilePaused() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		this.advance(decoder, data, 20L);
		decoder.pause().seek(0.15D);
		this.advance(decoder, data, 100L);
		Assert.assertEquals(0.15D, decoder.getCurrentTime(), 1E-9D);
		decoder.resume();
		this.advance(decoder, data, 10L);
		Assert.assertEquals(0.16D, decoder.getCurrentTime(), 1E-9D);
	}

	@Test
	public void seeksWhileStopped() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		decoder.stop().seek(0.2D);
		this.advance(decoder, data, 100L);
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertEquals(0.2D, decoder.getCurrentTime(), 1E-9D);
		Assert.assertEquals(2, decoder.getDisplayed());
	}

	@Test
	public void hasNoTimeBeforeItsDecode() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		Assert.assertEquals(0D, decoder.getDuration(), 0D);
		Assert.assertEquals(0D, decoder.getProgress(), 0D);
		Assert.assertEquals(0D, decoder.getCurrentTime(), 0D);
		Assert.assertNull(decoder.getPlays());
		Assert.assertTrue(decoder.isLoop());
	}

	@Test
	public void ignoresUpdatesBeforeItsUpload() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = new ResourceData("blink.png", decoder);
		decoder.update(data);
		decoder.prepare(data);
		decoder.decode(data);
		this.advance(decoder, data, 50L);
		Assert.assertEquals(-1, decoder.getDisplayed());
		Assert.assertArrayEquals(new int[] {0}, ((RecordingTexture) decoder.getTexture()).getPixels());
	}

	@Test
	public void forgetsItsAnimationOnClear() {
		final AnimatedResourceDecoder decoder = AnimatedResourceDecoderTest.apng();
		final ResourceData data = AnimatedResourceDecoderTest.load(decoder);
		decoder.clear(data);
		this.advance(decoder, data, 50L);
		Assert.assertNull(decoder.getAnimation());
		Assert.assertEquals(-1, decoder.getDisplayed());
		Assert.assertFalse(decoder.isPlaying());
		Assert.assertEquals(0D, decoder.getDuration(), 0D);
	}

	@Test
	public void keepsItsSourceAndReader() {
		final Asset asset = Asset.of(AnimatedResourceDecoderTest.class.getResourceAsStream("/animation/blink.gif"));
		final IResourceAnimationReader reader = new GifResourceAnimationReader();
		final AnimatedResourceDecoder decoder = new AnimatedResourceDecoder(asset, reader);
		decoder.init(new ResourceData("blink.gif", null));
		Assert.assertSame(asset, decoder.getAsset());
		Assert.assertSame(reader, decoder.getReader());
		Assert.assertNull(decoder.getAnimation());
	}

	private void advance(final AnimatedResourceDecoder decoder, final ResourceData data, final long milliseconds) {
		this.bridges.getClock().advance(milliseconds);
		decoder.update(data);
	}

	private static AnimatedResourceDecoder apng() {
		return new AnimatedResourceDecoder(Asset.of(AnimatedResourceDecoderTest.class.getResourceAsStream("/animation/blink.png")), new ApngResourceAnimationReader());
	}

	private static AnimatedResourceDecoder gif() {
		return new AnimatedResourceDecoder(Asset.of(AnimatedResourceDecoderTest.class.getResourceAsStream("/animation/blink.gif")), new GifResourceAnimationReader());
	}

	private static ResourceData load(final AnimatedResourceDecoder decoder) {
		final ResourceData data = new ResourceData("animation", decoder);
		decoder.prepare(data);
		decoder.decode(data);
		decoder.upload(data);
		return data;
	}

	private static final class OfflineAsset extends Asset {

		private OfflineAsset() {
			super("offline.gif");
		}

		@Override
		public @NonNull InputStream open() throws IOException {
			throw new IOException("offline");
		}

	}

}