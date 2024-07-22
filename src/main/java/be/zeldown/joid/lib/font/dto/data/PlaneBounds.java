package be.zeldown.joid.lib.font.dto.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaneBounds {

	private final float left;
	private final float bottom;
	private final float right;
	private final float top;

	public float getBottom() {
		return this.bottom + 0.025F;
	}

	public float getTop() {
		return this.top + 0.025F;
	}

}