package be.zeldown.joid.lib.font.dto.atlas;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Atlas {

	private final String type;
	private final float distanceRange;
	private final float size;
	private final float width;
	private final float height;
	private final String yOrigin;
	private final float baseCharHeight;
	private final float belowLineHeight;
	private final float shadowHeight;

}