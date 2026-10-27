package com.example.describing.media;

import java.awt.image.BufferedImage;
import java.io.File;

import dev.joid.lib.resource.Resource;

public final class MediaSources {

	private MediaSources() {}

	public static Resource[] load(final BufferedImage chart) {
		// #region sources
		final Resource icon = Resource.of("images/placeholder.png");
		final Resource photo = Resource.of(new File("photos/beach.png"));
		final Resource banner = Resource.of("https://example.com/banner.png");
		final Resource logo = Resource.of(MediaSources.class.getResourceAsStream("/images/placeholder.png"));
		final Resource drawn = Resource.of(chart);
		// #endregion
		return new Resource[] {icon, photo, banner, logo, drawn};
	}

}