package be.zeldown.joid.lib.resource.dto.decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.decoder.impl.ImageResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;

public class ResourceDecoderTest {

	private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
	private static final byte[] GIF = {'G', 'I', 'F', '8', '9', 'a', 1, 0, 1, 0, 0, 0, 0, 0, 0, 0};

	@Test
	public void picksTheVideoDecoderOnAVideoHeader() {
		final IResourceDecoder decoder = ResourceDecoder.of(Asset.of(new ByteArrayInputStream(ResourceDecoderTest.GIF)));
		Assert.assertTrue(decoder instanceof VideoResourceDecoder);
		Assert.assertTrue(((VideoResourceDecoder) decoder).isLoop());
	}

	@Test
	public void picksTheImageDecoderOtherwise() {
		Assert.assertTrue(ResourceDecoder.of(Asset.of(new ByteArrayInputStream(ResourceDecoderTest.PNG))) instanceof ImageResourceDecoder);
	}

	@Test
	public void leavesTheAssetUntouched() throws IOException {
		final Asset asset = Asset.of(new ByteArrayInputStream(ResourceDecoderTest.PNG));
		ResourceDecoder.of(asset);
		Assert.assertArrayEquals(ResourceDecoderTest.PNG, asset.read());
	}

	@Test
	public void doesNotReadTheAssetWhenBuildingTheDecoder() {
		ResourceDecoder.image(Asset.of(new ByteArrayInputStream("not an image".getBytes(StandardCharsets.UTF_8))));
	}

}