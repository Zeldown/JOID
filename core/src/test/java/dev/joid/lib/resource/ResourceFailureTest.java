package dev.joid.lib.resource;

import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;

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
import dev.joid.lib.ui.node.NodeTest.NodeUI;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.video.VideoNativesException;
import lombok.Getter;
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
		Assert.assertEquals("photo.heic is a HEIF or AVIF image, which JOID cannot decode, convert it to PNG, JPEG or WebP", errors.get(0).getMessage());
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
		resource.onError((failed, error) -> errors.add(error));
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
		Assert.assertEquals(1, errors.size());
	}

	@Test
	public void readsAMissingPathAgainOnceReloaded() throws IOException {
		final File file = new File(this.folder.getRoot(), "late.png");
		final Resource resource = ResourceBuilder.create().of(file.getPath()).blocking();
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
		Assert.assertSame(resource.getResourceData(), ResourceBuilder.create().of(file.getPath()).getResourceData());
		ImageIO.write(new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB), "png", file);
		resource.reload();
		resource.prepareBind();
		Assert.assertFalse(resource.isFailed());
		Assert.assertEquals(3, resource.getWidth());
	}

	@Test
	public void loadsABlockingRemoteImageInItsFirstFrameOnceReloaded() throws IOException {
		final byte[][] bytes = {ResourceFailureTest.HEIC};
		final Resource resource = ResourceBuilder.create().cache(null).of(new Asset("https://joid.invalid/late.png") {

			@Override
			public @NonNull InputStream open() {
				return new ByteArrayInputStream(bytes[0]);
			}

			@Override
			public boolean isRemote() {
				return true;
			}

		}).blocking();
		resource.prepareBind();
		Assert.assertTrue(resource.isFailed());
		final ByteArrayOutputStream image = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB), "png", image);
		bytes[0] = image.toByteArray();
		resource.reload();
		resource.prepareBind();
		Assert.assertTrue(resource.isLoaded());
		Assert.assertEquals(3, resource.getWidth());
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
			final String path = ResourceFailureTest.capture(() -> ResourceBuilder.create().cache(null).of("joid-missing/missing.png").blocking().prepareBind());
			Assert.assertTrue(path, path.endsWith("cannot be read: The asset joid-missing/missing.png is not found in the classpath nor as a file, check the path or give a URL with its scheme (https:, file:, jar:). It is drawn as a magenta and black checkerboard in dev mode, and nothing is drawn in production." + System.lineSeparator()));
			final String damaged = ResourceFailureTest.capture(() -> ResourceBuilder.create().cache(null).of(new BytesAsset("broken.webp", false, new byte[] {'R', 'I', 'F', 'F', 30, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', 'X', 10, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'A', 'N', 'M', 'F', (byte) 0xF8, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF})).blocking().prepareBind());
			Assert.assertFalse(damaged, damaged.contains("exists"));
			Assert.assertTrue(damaged, damaged.contains("The WebP chunk ANMF runs past the end of the file, the file is damaged: export it again"));
		} finally {
			JOIDEnv.devMode(false);
		}
	}

	@Test
	public void warnsAboutTheVideoNativesWithTheirOwnAdvice() {
		final ResourceData data = ResourceData.create("intro.mp4", null);
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
		this.bridges.open(new NodeUI(ResourcePlayerNode.create(10D, 10D, 100D, 100D).resource(resource)));
		DrawUtils.RESOURCE.drawResource(10D, 10D, 100D, 100D, resource);
		ResourceNode.create(10D, 10D, 100D, 100D).resource(resource).draw(0D, 0D);
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void drawsTheMissingImageInDevMode() {
		final Resource resource = ResourceBuilder.create().cache(null).of(new BytesAsset("photo.heic", false, ResourceFailureTest.HEIC));
		JOIDEnv.devMode(true);
		try {
			ResourceFailureTest.capture(() -> {
				this.bridges.open(new NodeUI(ResourcePlayerNode.create(10D, 10D, 100D, 100D).resource(resource)));
				DrawUtils.RESOURCE.drawResource(10D, 10D, 100D, 100D, 0D, 0D, 50D, 50D, resource);
				ResourceNode.create(10D, 10D, 100D, 100D).resource(resource).draw(0D, 0D);
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
		@Getter private final boolean remote;

		private BytesAsset(final String uniqueId, final boolean remote, final byte[] bytes) {
			super(uniqueId);
			this.remote = remote;
			this.bytes = bytes;
		}

		@Override
		public @NonNull InputStream open() {
			return new ByteArrayInputStream(this.bytes);
		}

	}

}