package be.zeldown.joid.msdf.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import be.zeldown.joid.msdf.geometry.Edge;
import be.zeldown.joid.msdf.geometry.Shape;
import be.zeldown.joid.msdf.geometry.Vector2;

public final class Glyphs {

	public static final double UNITS = 1000D;

	private static final double DENSITY = 1600D;

	private static final FontRenderContext CONTEXT = new FontRenderContext(null, false, true);

	public static Font load(final File file) throws Exception {
		return Font.createFont(Font.TRUETYPE_FONT, file).deriveFont((float) Glyphs.UNITS);
	}

	public static int code(final Font font, final int codepoint) {
		return font.createGlyphVector(Glyphs.CONTEXT, new String(Character.toChars(codepoint))).getGlyphCode(0);
	}

	public static double advance(final Font font, final int codepoint) {
		return font.createGlyphVector(Glyphs.CONTEXT, new String(Character.toChars(codepoint))).getGlyphMetrics(0).getAdvanceX() / Glyphs.UNITS;
	}

	public static double[] metrics(final Font font) {
		final LineMetrics metrics = font.getLineMetrics("Hxy", Glyphs.CONTEXT);
		final double ascender = metrics.getAscent() / Glyphs.UNITS;
		final double descender = metrics.getDescent() / Glyphs.UNITS;
		final double leading = metrics.getLeading() / Glyphs.UNITS;
		return new double[] {ascender + descender + leading, ascender, -descender, -metrics.getUnderlineOffset() / Glyphs.UNITS, metrics.getUnderlineThickness() / Glyphs.UNITS};
	}

	public static Shape outline(final Font font, final int codepoint) {
		final GlyphVector vector = font.createGlyphVector(Glyphs.CONTEXT, new String(Character.toChars(codepoint)));
		final PathIterator iterator = new Area(vector.getGlyphOutline(0)).getPathIterator(null);
		final Shape shape = new Shape();

		final double[] segment = new double[6];
		List<Edge> contour = new ArrayList<>();
		Vector2 start = null;
		Vector2 current = null;

		while (!iterator.isDone()) {
			switch (iterator.currentSegment(segment)) {
			case PathIterator.SEG_MOVETO:
				Glyphs.close(shape, contour, current, start);
				contour = new ArrayList<>();
				start = Glyphs.point(segment, 0);
				current = start;
				break;
			case PathIterator.SEG_LINETO:
				current = Glyphs.line(contour, current, Glyphs.point(segment, 0));
				break;
			case PathIterator.SEG_QUADTO:
				current = Glyphs.quadratic(contour, current, Glyphs.point(segment, 0), Glyphs.point(segment, 2));
				break;
			case PathIterator.SEG_CUBICTO:
				current = Glyphs.cubic(contour, current, Glyphs.point(segment, 0), Glyphs.point(segment, 2), Glyphs.point(segment, 4));
				break;
			case PathIterator.SEG_CLOSE:
				Glyphs.close(shape, contour, current, start);
				contour = new ArrayList<>();
				current = start;
				break;
			default:
				break;
			}
			iterator.next();
		}

		Glyphs.close(shape, contour, current, start);
		shape.orient();
		return shape;
	}

	private static void close(final Shape shape, final List<Edge> contour, final Vector2 current, final Vector2 start) {
		if (contour.isEmpty() || current == null || start == null) {
			return;
		}

		if (Math.abs(current.getX() - start.getX()) > 1E-9D || Math.abs(current.getY() - start.getY()) > 1E-9D) {
			Glyphs.line(contour, current, start);
		}

		shape.add(new ArrayList<>(contour));
	}

	private static Vector2 line(final List<Edge> contour, final Vector2 from, final Vector2 to) {
		final List<Vector2> points = new ArrayList<>();
		points.add(from);
		points.add(to);
		contour.add(new Edge(points));
		return to;
	}

	private static Vector2 quadratic(final List<Edge> contour, final Vector2 from, final Vector2 control, final Vector2 to) {
		final int steps = Glyphs.steps(from, control, to, null);
		final List<Vector2> points = new ArrayList<>();
		for (int i = 0; i <= steps; i++) {
			final double t = (double) i / steps;
			final double inverse = 1D - t;
			points.add(from.scale(inverse * inverse).add(control.scale(2D * inverse * t)).add(to.scale(t * t)));
		}

		contour.add(new Edge(points));
		return to;
	}

	private static Vector2 cubic(final List<Edge> contour, final Vector2 from, final Vector2 first, final Vector2 second, final Vector2 to) {
		final int steps = Glyphs.steps(from, first, second, to);
		final List<Vector2> points = new ArrayList<>();
		for (int i = 0; i <= steps; i++) {
			final double t = (double) i / steps;
			final double inverse = 1D - t;
			points.add(from.scale(inverse * inverse * inverse).add(first.scale(3D * inverse * inverse * t)).add(second.scale(3D * inverse * t * t)).add(to.scale(t * t * t)));
		}

		contour.add(new Edge(points));
		return to;
	}

	private static int steps(final Vector2 from, final Vector2 first, final Vector2 second, final Vector2 third) {
		double length = from.subtract(first).length() + first.subtract(second).length();
		if (third != null) {
			length += second.subtract(third).length();
		}

		return Math.max(8, Math.min(256, (int) Math.ceil(Math.sqrt(length * Glyphs.DENSITY))));
	}

	private static Vector2 point(final double[] segment, final int offset) {
		return new Vector2(segment[offset] / Glyphs.UNITS, -segment[offset + 1] / Glyphs.UNITS);
	}

}