package dev.joid.lib.ui.node;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;

public class NodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void scrollsItsChildrenTogetherByWholePixels() {
		this.bridges.resize(1366, 768).open(new ScrollUI()).frames(30);
		this.bridges.move(300D * 1366D / 1920D, 250D * 768D / 1080D).frames(2).scroll(-120);

		final Set<Double> gaps = new HashSet<>();
		final Set<Double> tops = new HashSet<>();
		for (int frame = 0; frame < 40; frame++) {
			this.bridges.frame();
			final Draw first = this.draw(0.2F, 0.4F, 0.6F);
			gaps.add(Math.rint(this.draw(0.6F, 0.4F, 0.2F).getTop() - first.getTop()));
			tops.add(Math.rint(first.getTop()));
		}

		Assert.assertEquals(1, gaps.size());
		Assert.assertTrue(tops.size() > 5);
	}

	@Test
	public void movesANodeAndItsChildrenByWholePixels() {
		final MovingUI ui = new MovingUI();
		this.bridges.resize(1366, 768).open(ui).frames(30);

		final Set<Double> gaps = new HashSet<>();
		for (int frame = 0; frame < 20; frame++) {
			ui.panel.y(ui.panel.getY() + 0.37D);
			this.bridges.frame();
			gaps.add(Math.rint(this.draw(0.6F, 0.4F, 0.2F).getTop() - this.draw(0.2F, 0.4F, 0.6F).getTop()));
		}

		Assert.assertEquals(1, gaps.size());
	}

	@Test
	public void drawsANodeAtItsExactPositionOnceItStops() {
		final MovingUI ui = new MovingUI();
		this.bridges.resize(1366, 768).open(ui).frames(30);
		for (int frame = 0; frame < 5; frame++) {
			ui.panel.y(ui.panel.getY() + 0.37D);
			this.bridges.frame();
		}

		this.bridges.frames(2);
		Assert.assertEquals(Math.floor(ui.panel.getAbsoluteY() * 768D / 1080D + 0.5D + 1E-6D), this.draw(0.2F, 0.4F, 0.6F).getTop(), 1E-3D);
	}

	private Draw draw(final float red, final float green, final float blue) {
		return this.bridges.getRender().getDraws(red, green, blue).get(0);
	}

	public static final class ScrollUI extends UI {

		@Override
		public void init() {
			final ContainerNode container = ContainerNode.create(100D, 100D, 400D, 300D).overflow(OverflowProperty.SCROLL);
			RectNode.create(0D, 100.3D, 200D, 20D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).attach(container);
			RectNode.create(0D, 140.8D, 200D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F)).attach(container);
			RectNode.create(0D, 1000D, 10D, 10D).color(new Color(0F, 0F, 1F, 1F)).attach(container);
			container.attach(this);
		}

	}

	public static final class MovingUI extends UI {

		private RectNode panel;

		@Override
		public void init() {
			this.panel = RectNode.create(100D, 100.3D, 300D, 200D).color(new Color(0.2F, 0.4F, 0.6F, 1F));
			RectNode.create(10D, 20.45D, 100D, 30D).color(new Color(0.6F, 0.4F, 0.2F, 1F)).attach(this.panel);
			this.panel.attach(this);
		}

	}

}