package dev.joid.lib.font.impl.msdf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MsdfAtlas {

	private final int   width;
	private final int   height;
	private final float size;
	private final float distanceRange;

}