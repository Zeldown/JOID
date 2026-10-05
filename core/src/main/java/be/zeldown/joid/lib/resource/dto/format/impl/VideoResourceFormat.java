package be.zeldown.joid.lib.resource.dto.format.impl;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import be.zeldown.joid.lib.resource.dto.format.IResourceFormat;
import lombok.NonNull;

public class VideoResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return VideoResourceFormat.isGif(header) || VideoResourceFormat.isMp4(header) || VideoResourceFormat.isMatroska(header) || VideoResourceFormat.isAvi(header);
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		return new VideoResourceDecoder(asset).loop(VideoResourceFormat.isGif(header));
	}

	private static boolean isGif(final byte[] header) {
		return header.length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
	}

	private static boolean isMp4(final byte[] header) {
		return header.length >= 8 && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';
	}

	private static boolean isMatroska(final byte[] header) {
		return header.length >= 4 && header[0] == (byte) 0x1A && header[1] == (byte) 0x45 && header[2] == (byte) 0xDF && header[3] == (byte) 0xA3;
	}

	private static boolean isAvi(final byte[] header) {
		return header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' && header[8] == 'A' && header[9] == 'V' && header[10] == 'I' && header[11] == ' ';
	}

}