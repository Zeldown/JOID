package be.zeldown.joid.lib.font.dto.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Metrics {

	private final float lineHeight;
	private final float ascender;
	private final float descender;
	private final float underlineY;
	private final float underlineThickness;

}