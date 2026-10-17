package dev.joid.lib.resource.decoder;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.resource.ResourceData;
import lombok.NonNull;

public class IResourceDecoderTest {

	@Test
	public void isSettledAndMipmappableByDefault() {
		final Decoder decoder = new Decoder();
		Assert.assertTrue(decoder.isSettled());
		Assert.assertTrue(decoder.isMipmappable());
	}

	@Test
	public void ignoresTheRequestedSizeByDefault() {
		final ResourceData data = ResourceData.create("image", null).width(4).height(2);
		new Decoder().request(data, 64, 32, true, false);
		Assert.assertEquals(4, data.getWidth());
		Assert.assertEquals(2, data.getHeight());
		Assert.assertNull(data.getTextures());
	}

	private static final class Decoder implements IResourceDecoder {

		@Override
		public void clear(final @NonNull ResourceData resource) {}

		@Override
		public void decode(final @NonNull ResourceData resource) {}

		@Override
		public void upload(final @NonNull ResourceData resource) {}

		@Override
		public void prepare(final @NonNull ResourceData resource) {}

	}

}