package be.zeldown.joid.msdf.atlas;

import be.zeldown.joid.msdf.geometry.Shape;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class GlyphEntry {

	private final int     codepoint;
	private final double  advance;
	private final Shape   shape;
	private final double[] bounds;

	@Setter private int x;
	@Setter private int y;
	@Setter private int width;
	@Setter private int height;
	@Setter private double left;
	@Setter private double bottom;
	@Setter private double right;
	@Setter private double top;

	public GlyphEntry(final int codepoint, final double advance, final Shape shape) {
		this.codepoint = codepoint;
		this.advance = advance;
		this.shape = shape;
		this.bounds = shape == null || shape.isEmpty() ? null : shape.bounds();
	}

	public boolean isDrawable() {
		return this.bounds != null;
	}

	public void measure(final double size, final double range) {
		final double margin = range / size / 2D;
		this.left = Math.floor((this.bounds[0] - margin) * size) / size;
		this.bottom = Math.floor((this.bounds[1] - margin) * size) / size;
		this.right = Math.ceil((this.bounds[2] + margin) * size) / size;
		this.top = Math.ceil((this.bounds[3] + margin) * size) / size;
		this.width = (int) Math.round((this.right - this.left) * size);
		this.height = (int) Math.round((this.top - this.bottom) * size);
	}

}