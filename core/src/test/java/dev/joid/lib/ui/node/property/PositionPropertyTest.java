package dev.joid.lib.ui.node.property;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PositionPropertyTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void placesAChildFromItsParent() {
		final RectNode child = RectNode.create(10D, 20D, 30D, 30D).color(new Color(0.3F, 0.5F, 0.7F, 1F));
		Assert.assertSame(PositionProperty.RELATIVE, child.getPosition());
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 100D).append(child))).frame();
		Assert.assertEquals(110D, child.getAbsoluteX(), 0D);
		Assert.assertEquals(120D, child.getAbsoluteY(), 0D);
		final Draw draw = this.child();
		Assert.assertEquals(110D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(120D, draw.getTop(), 1E-3D);
	}

	@Test
	public void placesAnAbsoluteChildFromTheUi() {
		final RectNode child = RectNode.create(10D, 20D, 30D, 30D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).position(PositionProperty.ABSOLUTE);
		this.bridges.open(new NodeUI(RectNode.create(100D, 100D, 200D, 100D).append(child))).frame();
		Assert.assertEquals(10D, child.getAbsoluteX(), 0D);
		Assert.assertEquals(20D, child.getAbsoluteY(), 0D);
		final Draw draw = this.child();
		Assert.assertEquals(10D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(20D, draw.getTop(), 1E-3D);
	}

	private Draw child() {
		final List<Draw> draws = this.bridges.getRender().getDraws(0.3F, 0.5F, 0.7F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

}