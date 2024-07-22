package be.zeldown.joid.lib.font.dto.atlas;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AtlasBounds {

	private final float left;
	private final float bottom;
	private final float right;
	private final float top;

	public float getLeft() {
		return this.left + 0.5F;
	}

	public float getBottom() {
		return this.bottom + 0.5F;
	}

	public float getRight() {
		return this.right + 0.5F;
	}

	public float getTop() {
		return this.top + 0.5F;
	}

}