package dev.joid.tool.msdf.atlas;

import dev.joid.tool.msdf.geometry.Shape;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class GlyphEntry {

	private final Shape   shape;
	private final int     codepoint;
	private final double  advance;
	private final double[] bounds;

	@Setter private int x;
	@Setter private int y;

	private int    width;
	private int    height;
	private double top;
	private double left;
	private double right;
	private double bottom;

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
		this.left = this.bounds[0] - margin;
		this.bottom = this.bounds[1] - margin;
		this.width = (int) Math.ceil((this.bounds[2] + margin - this.left) * size);
		this.height = (int) Math.ceil((this.bounds[3] + margin - this.bottom) * size);
		this.right = this.left + this.width / size;
		this.top = this.bottom + this.height / size;
	}

}