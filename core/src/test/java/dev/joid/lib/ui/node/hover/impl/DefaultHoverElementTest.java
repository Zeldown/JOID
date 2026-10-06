package dev.joid.lib.ui.node.hover.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.hover.HoverSupplier;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

import lombok.NonNull;

public class DefaultHoverElementTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void handsItsLinesToTheUi() {
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D);
		final HoverUI ui = new HoverUI(rect);
		this.bridges.open(ui).frames(30);
		new DefaultHoverElement(Arrays.asList("Save", "Ctrl+S")).render(rect, 150D, 160D);
		Assert.assertEquals(Collections.singletonList(Arrays.asList(Arrays.asList("Save", "Ctrl+S"), 150D, 160D)), ui.hovers);
	}

	@Test
	public void showsTheTextHoverOfANode() {
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(() -> "Save");
		final HoverUI ui = new HoverUI(rect);
		this.bridges.open(ui).frames(30);
		Assert.assertTrue(ui.hovers.isEmpty());
		this.bridges.move(150D, 160D).frames(1);
		Assert.assertEquals(Collections.singletonList(Arrays.asList(Collections.singletonList("Save"), 150D, 160D)), ui.hovers);
	}

	@Test
	public void ignoresANodeOutsideAUi() {
		new DefaultHoverElement(Arrays.asList("Save", "Ctrl+S")).render(RectNode.create(0D, 0D, 10D, 10D), 150D, 160D);
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void showsNoTooltipForANullLine() {
		final HoverSupplier supplier = () -> null;
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).hover(supplier);
		final HoverUI ui = new HoverUI(rect);
		this.bridges.open(ui).frames(30);
		this.bridges.move(150D, 160D).frames(1);
		Assert.assertTrue(ui.hovers.toString(), ui.hovers.isEmpty());
	}

	public static final class HoverUI extends UI {

		private final List<Object> hovers = new ArrayList<>();
		private final Node[]       nodes;

		private HoverUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

		@Override
		public void drawHover(final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {
			this.hovers.add(Arrays.asList(lines, mouseX, mouseY));
		}

	}

}