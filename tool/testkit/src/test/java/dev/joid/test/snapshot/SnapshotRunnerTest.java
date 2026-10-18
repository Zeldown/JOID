package dev.joid.test.snapshot;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.asset.impl.UrlAsset;
import dev.joid.lib.asset.locator.AssetLocator;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.clock.IClockBridge;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.RecordingRenderBridge;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.utils.image.PixelLayout;
import lombok.NonNull;

public class SnapshotRunnerTest {

	@Test
	public void restoresTheBridgesItReplacedOnStop() {
		final IClockBridge clock = BridgeHandler.CLOCK.get();
		final List<IUIBridge> uis = new ArrayList<>(BridgeHandler.UI.getBridgeList());
		final List<IAudioBridge> audios = new ArrayList<>(BridgeHandler.AUDIO.getBridgeList());
		final List<IWindowBridge> windows = new ArrayList<>(BridgeHandler.WINDOW.getBridgeList());
		final List<IRenderBridge> renders = new ArrayList<>(BridgeHandler.RENDER.getBridgeList());
		final SnapshotRunner runner = SnapshotRunner.start(new HeadlessBackend());
		Assert.assertNotSame(clock, BridgeHandler.CLOCK.get());
		runner.stop();
		Assert.assertSame(clock, BridgeHandler.CLOCK.get());
		Assert.assertEquals(uis, BridgeHandler.UI.getBridgeList());
		Assert.assertEquals(audios, BridgeHandler.AUDIO.getBridgeList());
		Assert.assertEquals(windows, BridgeHandler.WINDOW.getBridgeList());
		Assert.assertEquals(renders, BridgeHandler.RENDER.getBridgeList());
	}

	@Test
	public void leavesNoBridgeWhenItsBackendCannotStart() {
		final List<IUIBridge> uis = new ArrayList<>(BridgeHandler.UI.getBridgeList());
		final List<IRenderBridge> renders = new ArrayList<>(BridgeHandler.RENDER.getBridgeList());
		try {
			SnapshotRunner.start(new FailingBackend());
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("No window", exception.getMessage());
		}
		Assert.assertEquals(uis, BridgeHandler.UI.getBridgeList());
		Assert.assertEquals(renders, BridgeHandler.RENDER.getBridgeList());
	}

	@Test
	public void restoresTheAssetLocatorsAndTheDevModeOnStop() {
		final boolean devMode = JOIDEnv.isDevMode();
		SnapshotRunner.start(new HeadlessBackend()).stop();
		Assert.assertTrue(AssetLocator.locate("https://example.invalid/image.png") instanceof UrlAsset);
		Assert.assertEquals(devMode, JOIDEnv.isDevMode());
	}

	public static final class HeadlessBackend implements ISnapshotBackend {

		@Override
		public void destroy() {}

		@Override
		public void create(final int width, final int height) {
			BridgeHandler.RENDER.register(new RecordingRenderBridge());
		}

		@Override
		public void present() {}

		@Override
		public @NonNull SnapshotImage capture(final int width, final int height) {
			return SnapshotImage.from(ByteBuffer.allocate(width * height * 4), width, height, false, PixelLayout.RGBA8);
		}

		@Override
		public @NonNull String getRenderer() {
			return "headless";
		}

	}

	public static final class FailingBackend implements ISnapshotBackend {

		@Override
		public void destroy() {}

		@Override
		public void create(final int width, final int height) {
			BridgeHandler.RENDER.register(new RecordingRenderBridge());
			throw new IllegalStateException("No window");
		}

		@Override
		public void present() {}

		@Override
		public @NonNull SnapshotImage capture(final int width, final int height) {
			throw new IllegalStateException("No window");
		}

		@Override
		public @NonNull String getRenderer() {
			return "failing";
		}

	}

}