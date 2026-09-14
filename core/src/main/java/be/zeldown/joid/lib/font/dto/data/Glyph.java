package be.zeldown.joid.lib.font.dto.data;

import be.zeldown.joid.lib.font.dto.atlas.AtlasBounds;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Glyph {

	private final int unicode;
	private final float advance;
	private final PlaneBounds planeBounds;
	private final AtlasBounds atlasBounds;

}