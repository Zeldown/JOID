package dev.joid.lib.asset.dto.locator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.dto.impl.FileAsset;
import dev.joid.lib.asset.dto.impl.StreamAsset;
import dev.joid.lib.asset.dto.impl.UrlAsset;
import lombok.NonNull;

public class AssetLocatorTest {

	@Test
	public void supportsEveryBuiltInHandle() {
		Assert.assertTrue(AssetLocator.supports(new File("image.png")));
		Assert.assertTrue(AssetLocator.supports("https://example.invalid/image.png"));
		Assert.assertTrue(AssetLocator.supports(new ByteArrayInputStream(new byte[0])));
	}

	@Test
	public void supportsAnAsset() {
		Assert.assertTrue(AssetLocator.supports(FileAsset.create(new File("image.png"))));
	}

	@Test
	public void supportsNoOtherHandle() {
		Assert.assertFalse(AssetLocator.supports(new Object()));
	}

	@Test
	public void locatesEveryBuiltInHandle() {
		Assert.assertTrue(AssetLocator.locate(new File("image.png")) instanceof FileAsset);
		Assert.assertTrue(AssetLocator.locate("https://example.invalid/image.png") instanceof UrlAsset);
		Assert.assertTrue(AssetLocator.locate(new ByteArrayInputStream(new byte[0])) instanceof StreamAsset);
	}

	@Test
	public void returnsAnAssetAsIs() {
		final Asset asset = FileAsset.create(new File("image.png"));
		Assert.assertSame(asset, AssetLocator.locate(asset));
	}

	@Test
	public void prefersTheLastRegisteredLocator() {
		AssetLocator.register(new SpecialLocator());
		Assert.assertEquals("special", AssetLocator.locate(new SpecialStream()).getUniqueId());
		Assert.assertTrue(AssetLocator.locate(new ByteArrayInputStream(new byte[0])) instanceof StreamAsset);
	}

	@Test
	public void namesTheTypeOfAnUnknownHandle() {
		try {
			AssetLocator.locate(new Object());
			Assert.fail("An unknown handle must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("No asset locator found for input of type java.lang.Object", expected.getMessage());
		}
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullLocator() {
		AssetLocator.register(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToSupportANullHandle() {
		AssetLocator.supports(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToLocateANullHandle() {
		AssetLocator.locate(null);
	}

	private static final class SpecialStream extends ByteArrayInputStream {

		private SpecialStream() {
			super(new byte[0]);
		}

	}

	private static final class SpecialAsset extends Asset {

		private SpecialAsset() {
			super("special");
		}

		@Override
		public @NonNull InputStream open() {
			return new ByteArrayInputStream(new byte[0]);
		}

	}

	private static final class SpecialLocator implements IAssetLocator {

		@Override
		public boolean supports(final @NonNull Object handle) {
			return handle instanceof SpecialStream;
		}

		@Override
		public @NonNull Asset locate(final @NonNull Object handle) {
			return new SpecialAsset();
		}

	}

}