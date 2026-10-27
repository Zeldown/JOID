package com.example.further;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.format.IResourceFormat;

// #region format
public class FormatsPpmResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final byte[] header) {
		return header.length >= 3 && header[0] == 'P' && header[1] == '3' && Character.isWhitespace(header[2]);
	}

	@Override
	public IResourceDecoder decoder(final Asset asset, final byte[] header) {
		return new FormatsPpmResourceDecoder(asset);
	}

}
// #endregion