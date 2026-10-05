package dev.joid.lib.font.impl.msdf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MsdfMetrics {

	private final float lineHeight;
	private final float ascender;
	private final float descender;
	private final float underlineY;
	private final float underlineThickness;

}