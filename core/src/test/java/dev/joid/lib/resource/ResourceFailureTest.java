package dev.joid.lib.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;

import lombok.NonNull;

public class ResourceFailureTest {

	private static final byte[] HEIC = {0, 0, 0, 24, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c', 0, 0, 0, 0, 'm', 'i', 'f', '1', 'h', 'e', 'i', 'c'};

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

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
		final Resource resource = ResourceBuilder.create().cache(null).of(new BytesAsset("broken.png", false, "not an image".getBytes(StandardCharsets.UTF_8))).onError((received, error) -> failed.add(received));
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
		final Resource resource = ResourceBuilder.create().cache(null).of(new File(this.folder.getRoot(), "missing.png"));
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
	}

	@Test
	public void failsARemoteImageOnItsTaskThread() {
		final Resource resource = ResourceBuilder.create().async().cache(null).of(new BytesAsset("https://joid.invalid/photo.heic", true, ResourceFailureTest.HEIC));
		resource.getResourceData().await();
		Assert.assertTrue(resource.isFailed());
	}

	@Test
	public void warnsInDevModeWithTheReasonAndTheAdvice() {
		JOID.inst().setDevMode(true);
		try {
			final String heif = ResourceFailureTest.capture(() -> ResourceBuilder.create().cache(null).of(new BytesAsset("photo.heic", false, ResourceFailureTest.HEIC)));
			Assert.assertEquals("[JOID] The resource photo.heic cannot be read and is drawn empty: photo.heic is a HEIF or AVIF image, which JOID cannot decode, convert it to PNG, JPEG or WebP" + System.lineSeparator(), heif);
			final String missing = ResourceFailureTest.capture(() -> ResourceBuilder.create().cache(null).of(new File(this.folder.getRoot(), "missing.png")).prepareBind());
			Assert.assertTrue(missing, missing.startsWith("[JOID] The resource "));
			Assert.assertTrue(missing, missing.endsWith(", check that the file or the URL exists and can be read" + System.lineSeparator()));
		} finally {
			JOID.inst().setDevMode(false);
		}
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
		JOID.inst().setDevMode(true);
		try {
			ResourceFailureTest.capture(() -> {
				DrawUtils.RESOURCE.drawResource(10D, 10D, 100D, 100D, 0D, 0D, 50D, 50D, resource);
				ResourceNode.create(10D, 10D, 100D, 100D).resource(resource).draw(0D, 0D);
				ResourcePlayerNode.create(10D, 10D, 100D, 100D).resource(resource).draw(0D, 0D);
			});
		} finally {
			JOID.inst().setDevMode(false);
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

		private final boolean remote;
		private final byte[]  bytes;

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