package dev.joid.lib.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.video.VideoNativesException;

import lombok.NonNull;

public class ResourceFailureTest {

	private static final byte[] HEIC = {0, 0, 0, 24, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c', 0, 0, 0, 0, 'm', 'i', 'f', '1', 'h', 'e', 'i', 'c'};

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private boolean devMode;

	@Before
	public void leaveTheDevMode() {
		this.devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(false);
	}

	@After
	public void restoreTheDevMode() {
		JOIDEnv.devMode(this.devMode);
	}

	@Test
	public void failsAHeifImageInsteadOfThrowing() {
		final List<Throwable> errors = new ArrayList<>();
		final Resource resource = ResourceBuilder.create().cache(null).of(new BytesAsset("photo.heic", false, ResourceFailureTest.HEIC)).onError((failed, error) -> errors.add(error));
		Assert.assertTrue(resource.isFailed());
		Assert.assertFalse(resource.isLoaded());
		Assert.assertNull(resource.getDecoder());
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals("photo.heic is a HEIF or AVIF image, which JOID cannot decode", errors.get(0).getMessage());
	}

	@Test
	public void failsAnImageThatCannotBeDecoded() {
		final List<Resource> failed = new ArrayList<>();
		final Resource resource = ResourceBuilder.create().cache(null).of(new BytesAsset("broken.png", false, "not an image".getBytes(StandardCharsets.UTF_8))).blocking().onError((received, error) -> failed.add(received));
		Assert.assertFalse(resource.isFailed());
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
		Assert.assertFalse(resource.isLoaded());
		Assert.assertEquals(Collections.singletonList(resource), failed);
		resource.prepareBind();
		Assert.assertEquals(1, failed.size());
	}

	@Test
	public void failsAMissingFile() {
		final Resource resource = ResourceBuilder.create().cache(null).of(new File(this.folder.getRoot(), "missing.png")).blocking();
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
	}

	@Test
	public void failsAMissingPath() {
		final List<Throwable> errors = new ArrayList<>();
		final Resource resource = ResourceBuilder.create().cache(null).of("joid-missing/missing.png").blocking();
		resource.getResourceData().onError(errors::add);
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
		Assert.assertEquals(1, errors.size());
	}

	@Test
	public void failsARemoteImageOnItsTaskThread() {
		final Resource resource = ResourceBuilder.create().cache(null).of(new BytesAsset("https://joid.invalid/photo.heic", true, ResourceFailureTest.HEIC));
		resource.getResourceData().await();
		Assert.assertTrue(resource.isFailed());
	}

	@Test
	public void warnsInDevModeWithTheReasonAndTheAdvice() {
		JOIDEnv.devMode(true);
		try {
			final String heif = ResourceFailureTest.capture(() -> ResourceBuilder.create().cache(null).of(new BytesAsset("photo.heic", false, ResourceFailureTest.HEIC)));
			Assert.assertEquals("[JOID] The resource photo.heic cannot be read: photo.heic is a HEIF or AVIF image, which JOID cannot decode, convert it to PNG, JPEG or WebP. It is drawn as a magenta and black checkerboard in dev mode, and nothing is drawn in production." + System.lineSeparator(), heif);
			final String missing = ResourceFailureTest.capture(() -> ResourceBuilder.create().cache(null).of(new File(this.folder.getRoot(), "missing.png")).blocking().prepareBind());
			Assert.assertTrue(missing, missing.startsWith("[JOID] The resource "));
			Assert.assertTrue(missing, missing.endsWith(", check that the file or the URL exists and can be read. It is drawn as a magenta and black checkerboard in dev mode, and nothing is drawn in production." + System.lineSeparator()));
		} finally {
			JOIDEnv.devMode(false);
		}
	}

	@Test
	public void warnsAboutTheVideoNativesWithTheirOwnAdvice() {
		final ResourceData data = new ResourceData("intro.mp4", null);
		JOIDEnv.devMode(true);
		try {
			final String warning = ResourceFailureTest.capture(() -> data.fail(new VideoNativesException("The FFmpeg natives of linux-x86_64 cannot be downloaded, check the connection", new IOException("offline"))));
			Assert.assertEquals("[JOID] The resource intro.mp4 cannot be read: The FFmpeg natives of linux-x86_64 cannot be downloaded, check the connection (java.io.IOException: offline). It is drawn as a magenta and black checkerboard in dev mode, and nothing is drawn in production." + System.lineSeparator(), warning);
		} finally {
			JOIDEnv.devMode(false);
		}
		Assert.assertTrue(data.isFailed());
	}

	@Test
	public void staysSilentOutOfDevMode() {
		Assert.assertEquals("", ResourceFailureTest.capture(() -> ResourceBuilder.create().cache(null).of(new BytesAsset("photo.heic", false, ResourceFailureTest.HEIC))));
	}

	@Test
	public void drawsAFailedResourceEmpty() {
		final Resource resource = ResourceBuilder.create().cache(null).of(new BytesAsset("photo.heic", false, ResourceFailureTest.HEIC));
		DrawUtils.RESOURCE.drawResource(10D, 10D, 100D, 100D, resource);
		ResourceNode.create(10D, 10D, 100D, 100D).resource(resource).draw(0D, 0D);
		ResourcePlayerNode.create(10D, 10D, 100D, 100D).resource(resource).draw(0D, 0D);
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void drawsTheMissingImageInDevMode() {
		final Resource resource = ResourceBuilder.create().cache(null).of(new BytesAsset("photo.heic", false, ResourceFailureTest.HEIC));
		JOIDEnv.devMode(true);
		try {
			ResourceFailureTest.capture(() -> {
				DrawUtils.RESOURCE.drawResource(10D, 10D, 100D, 100D, 0D, 0D, 50D, 50D, resource);
				ResourceNode.create(10D, 10D, 100D, 100D).resource(resource).draw(0D, 0D);
				ResourcePlayerNode.create(10D, 10D, 100D, 100D).resource(resource).draw(0D, 0D);
			});
		} finally {
			JOIDEnv.devMode(false);
		}
		Assert.assertEquals(3, this.bridges.getRender().getDraws().size());
		final RecordingTexture missing = (RecordingTexture) resource.getResourceData().getMissingTexture();
		Assert.assertEquals(8, missing.getWidth());
		Assert.assertEquals(0xFFFF00FF, missing.getPixels()[0]);
		Assert.assertEquals(0xFF000000, missing.getPixels()[1]);
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

	private static final class BytesAsset extends Asset {

		private final byte[]  bytes;
		private final boolean remote;

		private BytesAsset(final String uniqueId, final boolean remote, final byte[] bytes) {
			super(uniqueId);
			this.remote = remote;
			this.bytes = bytes;
		}

		@Override
		public @NonNull InputStream open() {
			return new ByteArrayInputStream(this.bytes);
		}

		@Override
		public boolean isRemote() {
			return this.remote;
		}

	}

}