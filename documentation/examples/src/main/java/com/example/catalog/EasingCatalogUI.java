package com.example.catalog;

import dev.joid.lib.animation.tween.TweenEquation;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.geom.Point;

public class EasingCatalogUI extends CatalogUI {

	private static final Color OUT = new Color(102, 102, 102).lock();

	@Override
	public void init() {
		final TweenEquation[][] families = {
			{TweenEquations.LINEAR},
			{TweenEquations.QUAD_IN, TweenEquations.QUAD_OUT, TweenEquations.QUAD_IN_OUT},
			{TweenEquations.CUBIC_IN, TweenEquations.CUBIC_OUT, TweenEquations.CUBIC_IN_OUT},
			{TweenEquations.QUART_IN, TweenEquations.QUART_OUT, TweenEquations.QUART_IN_OUT},
			{TweenEquations.QUINT_IN, TweenEquations.QUINT_OUT, TweenEquations.QUINT_IN_OUT},
			{TweenEquations.SINE_IN, TweenEquations.SINE_OUT, TweenEquations.SINE_IN_OUT},
			{TweenEquations.EXPO_IN, TweenEquations.EXPO_OUT, TweenEquations.EXPO_IN_OUT},
			{TweenEquations.CIRC_IN, TweenEquations.CIRC_OUT, TweenEquations.CIRC_IN_OUT},
			{TweenEquations.BACK_IN, TweenEquations.BACK_OUT, TweenEquations.BACK_IN_OUT},
			{TweenEquations.BOUNCE_IN, TweenEquations.BOUNCE_OUT, TweenEquations.BOUNCE_IN_OUT},
			{TweenEquations.ELASTIC_IN, TweenEquations.ELASTIC_OUT, TweenEquations.ELASTIC_IN_OUT}
		};
		for (int i = 0; i < families.length; i++) {
			final TweenEquation[] family = families[i];
			super.card(i).body(card -> new CurveNode(family).attach(card)).attach(this);
		}
	}

	private static final class CurveNode extends Node {

		private final TweenEquation[] equations;

		private CurveNode(final TweenEquation[] equations) {
			super(40, 60, 260, 180);
			this.equations = equations;
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			final Color[] colors = {CatalogUI.INK, EasingCatalogUI.OUT, Color.WHITE};
			double min = 0D;
			double max = 1D;
			for (final TweenEquation equation : this.equations) {
				for (int step = 0; step <= 60; step++) {
					min = Math.min(min, equation.compute(step / 60F));
					max = Math.max(max, equation.compute(step / 60F));
				}
			}

			final double unit = Math.min(super.getHeight(), 240D / (max - min));
			final double middle = super.getY() + super.getHeight() / 2D + unit * (max + min) / 2D;
			DrawUtils.SHAPE.drawBorder(super.getX(), middle - unit, super.getWidth(), unit, Color.WHITE, 2D);
			for (int i = 0; i < this.equations.length; i++) {
				final Point[] points = new Point[61];
				for (int step = 0; step < points.length; step++) {
					final float t = step / 60F;
					points[step] = Point.of(super.getX() + t * super.getWidth(), middle - this.equations[i].compute(t) * unit);
				}
				DrawUtils.SHAPE.drawLine(colors[i], 4F, points);
			}
		}

	}

}