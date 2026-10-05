package be.zeldown.joid.lib.resource.dto.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;

public class ResourceFormatTest {

	private static final byte[] PNG  = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
	private static final byte[] MP4  = {0, 0, 0, 32, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm', 0, 0, 2, 0};
	private static final byte[] WEBM = {(byte) 0x1A, (byte) 0x45, (byte) 0xDF, (byte) 0xA3, 1, 0, 0, 0, 0, 0, 0, 31};

	@Test
	public void picksTheRasterDecoderOtherwise() {
		Assert.assertTrue(ResourceFormat.decoder(Asset.of(new ByteArrayInputStream(ResourceFormatTest.PNG))) instanceof RasterResourceDecoder);
	}

	@Test
	public void picksTheVideoDecoderOnAVideoHeader() {
		for (final byte[] header : new byte[][] {ResourceFormatTest.MP4, ResourceFormatTest.WEBM}) {
			final IResourceDecoder decoder = ResourceFormat.decoder(Asset.of(new ByteArrayInputStream(header)));
			Assert.assertTrue(decoder instanceof VideoResourceDecoder);
			Assert.assertFalse(((VideoResourceDecoder) decoder).isLoop());
		}
	}

	@Test
	public void leavesTheAssetUntouched() throws IOException {
		final Asset asset = Asset.of(new ByteArrayInputStream(ResourceFormatTest.PNG));
		ResourceFormat.decoder(asset);
		Assert.assertArrayEquals(ResourceFormatTest.PNG, asset.read());
	}

}