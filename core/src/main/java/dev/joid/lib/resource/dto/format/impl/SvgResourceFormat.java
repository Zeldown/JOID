package dev.joid.lib.resource.dto.format.impl;

import java.nio.charset.StandardCharsets;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.VectorResourceDecoder;
import dev.joid.lib.resource.dto.format.IResourceFormat;
import lombok.NonNull;

public class SvgResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		String text = new String(header, StandardCharsets.UTF_8);
		if (text.startsWith("\uFEFF")) {
			text = text.substring(1);
		}

		while (true) {
			text = text.trim();
			if (text.startsWith("<?")) {
				text = SvgResourceFormat.after(text, "?>");
			} else if (text.startsWith("<!--")) {
				text = SvgResourceFormat.after(text, "-->");
			} else if (text.startsWith("<!")) {
				text = SvgResourceFormat.after(text, ">");
			} else {
				return text.startsWith("<svg") && (text.length() == 4 || !Character.isLetterOrDigit(text.charAt(4)));
			}
		}
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		return new VectorResourceDecoder(asset);
	}

	private static String after(final String text, final String end) {
		final int index = text.indexOf(end);
		return index < 0 ? "" : text.substring(index + end.length());
	}

}