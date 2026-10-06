package dev.joid.lib.ui.node.property.overflow;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class OverflowPropertyTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void letsChildrenOverflowByDefault() {
		final RectNode parent = OverflowPropertyTest.parent();
		Assert.assertSame(OverflowProperty.NONE, parent.getOverflow());
		this.bridges.open(new NodeUI(parent)).frame();
		Assert.assertTrue(this.bridges.getRender().getDraws(1F, 0F, 0F).isEmpty());
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.3F, 0.5F, 0.7F).size());
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.5F, 0.7F, 0.9F).size());
	}

	@Test
	public void hidesWhatOverflows() {
		this.bridges.open(new NodeUI(OverflowPropertyTest.parent().overflow(OverflowProperty.HIDDEN))).frame();
		final List<Draw> masks = this.bridges.getRender().getDraws(1F, 0F, 0F);
		Assert.assertEquals(1, masks.size());
		Assert.assertEquals(100D, masks.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(100D, masks.get(0).getTop(), 1E-3D);
		Assert.assertEquals(300D, masks.get(0).getRight(), 1E-3D);
		Assert.assertEquals(200D, masks.get(0).getBottom(), 1E-3D);
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.3F, 0.5F, 0.7F).size());
		Assert.assertTrue(this.bridges.getRender().getDraws(0.5F, 0.7F, 0.9F).isEmpty());
	}

	@Test
	public void scrollsWhatOverflows() {
		this.bridges.open(new NodeUI(OverflowPropertyTest.parent().overflow(OverflowProperty.SCROLL))).frame();
		this.bridges.move(200D, 150D).frames(2).scroll(-120);
		this.bridges.frames(100);
		Assert.assertEquals(1, this.bridges.getRender().getDraws(1F, 0F, 0F).size());
		Assert.assertTrue(this.bridges.getRender().getDraws(0.3F, 0.5F, 0.7F).get(0).getTop() < 150D);
	}

	@Test
	public void staysInPlaceWhenOnlyHidden() {
		this.bridges.open(new NodeUI(OverflowPropertyTest.parent().overflow(OverflowProperty.HIDDEN))).frame();
		this.bridges.move(200D, 150D).frames(2).scroll(-120);
		this.bridges.frames(100);
		Assert.assertEquals(150D, this.bridges.getRender().getDraws(0.3F, 0.5F, 0.7F).get(0).getTop(), 1E-3D);
	}

	private static RectNode parent() {
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).color(new Color(0.1F, 0.3F, 0.5F, 1F));
		RectNode.create(0D, 50D, 200D, 100D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).attach(parent);
		RectNode.create(0D, 150D, 200D, 50D).color(new Color(0.5F, 0.7F, 0.9F, 1F)).attach(parent);
		return parent;
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

}