package dev.joid.lib.ui.node;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Vector3f;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.replay.SetterUI;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.impl.design.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.design.progress.ProgressNode.ProgressDirection;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNodeTest.Checkbox;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.impl.structure.sw.SwitchNodeTest.Switch;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class NodeSetterTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void followsTheNativeExpressionsGivenToTheSetters() {
		final SetterUI ui = new SetterUI();
		this.bridges.open(ui);
		Assert.assertEquals(0D, ui.getRect().getX(), 0D);
		Assert.assertSame(Color.GRAY, ui.getRect().getColor());
		Assert.assertEquals("Step 0", ui.getText().getText().getText());
		ui.getStep().set(1);
		this.bridges.frame();
		Assert.assertEquals(10D, ui.getRect().getX(), 0D);
		Assert.assertEquals(6D, ui.getRect().getY(), 0D);
		Assert.assertEquals(11D, ui.getRect().getWidth(), 0D);
		Assert.assertEquals(19D, ui.getRect().getHeight(), 0D);
		Assert.assertSame(Color.GREEN, ui.getRect().getColor());
		Assert.assertSame(Color.RED, ui.getRect().getHoveredColor());
		Assert.assertSame(Color.BLUE, ui.getRect().getBorderColor());
		Assert.assertEquals(2D, ui.getRect().getBorderStroke(), 0D);
		Assert.assertFalse(ui.getRect().isBorderFill());
		Assert.assertEquals(3D, ui.getRect().getZlevel(), 0D);
		Assert.assertSame(Color.GREEN, ui.getCircle().getColor());
		Assert.assertSame(Align.END, ui.getCircle().getAnchorX());
		Assert.assertEquals("Step 1", ui.getText().getText().getText());
		Assert.assertSame(TextMode.SPLIT, ui.getText().getMode());
		Assert.assertEquals(0.25F, ui.getProgress().getProgress(), 0F);
		Assert.assertSame(ProgressDirection.RIGHT_TO_LEFT, ui.getProgress().getDirection());
		Assert.assertSame(Color.GREEN, ui.getProgress().getForeground());
		Assert.assertEquals(0.9F, ui.getPlayer().getVolume(), 1E-6F);
		Assert.assertTrue(ui.getPlayer().isLoop());
		Assert.assertEquals(new Vector3f(1F, 0F, 1F), ui.getPlayer().getLocation());
		Assert.assertEquals(2D, ui.getModel().getSize(), 0D);
		Assert.assertEquals(90D, ui.getModel().getRotationYaw(), 0D);
		Assert.assertEquals("Name 1", ui.getField().getText());
		Assert.assertEquals("Type 1", ui.getField().getPlaceholder());
		Assert.assertEquals(11, ui.getField().getMaxTextLength());
		Assert.assertEquals(2D, ui.getField().getMarginLeft(), 0D);
		Assert.assertTrue(ui.getCheckbox().isChecked());
		Assert.assertTrue(ui.getToggle().isToggle());
		Assert.assertEquals("medium", ui.getIndexed().getState());
		Assert.assertEquals("high", ui.getNamed().getState());
		Assert.assertEquals("second", ui.getSelector().getValue());
		Assert.assertEquals(2, ui.getSlider().getValue().intValue());
		Assert.assertEquals(5D, ui.getFlex().getMargin(), 0D);
		Assert.assertSame(Align.CENTER, ui.getFlex().getAlign());
		Assert.assertEquals(2F, ui.getBorder().getWidthSupplier().get(), 0F);
		Assert.assertSame(Color.RED, ui.getBorder().getColorSupplier().get());
		Assert.assertFalse(ui.getBorder().isFill());
		Assert.assertEquals(1D, ui.getShadow().getOffsetX(), 0D);
		Assert.assertEquals(3F, ui.getShadow().getBlur(), 0F);
		Assert.assertEquals(10, ui.getRadar().getValue().intValue());
		Assert.assertEquals("Label 1", ui.getRadar().getLabel());
		ui.getStep().set(4);
		this.bridges.frame();
		Assert.assertFalse(ui.getRect().isEnabled());
		Assert.assertTrue(ui.getRect().isVisible());
		Assert.assertTrue(ui.getSelector().isActive());
		ui.getStep().set(5);
		this.bridges.frame();
		Assert.assertFalse(ui.getRect().isVisible());
	}

	@Test
	public void keepsAPlainValueWithoutPollingIt() {
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).x(5D).width(30D).zlevel(2D);
		Assert.assertEquals(5D, rect.getX(), 0D);
		Assert.assertEquals(30D, rect.getWidth(), 0D);
		Assert.assertTrue(rect.getSourceMap().isEmpty());
		this.bridges.open(new NodeUI(rect));
		Assert.assertEquals(5D, rect.getX(), 0D);
		Assert.assertEquals(30D, rect.getWidth(), 0D);
	}

	@Test
	public void readsASupplierOnEveryFrame() {
		final double[] width = {10D};
		final Color[] color = {Color.GRAY};
		final float[] progress = {0F};
		final boolean[] checked = {false};
		final List<Boolean> changes = new ArrayList<>();
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).color(() -> color[0]).width(() -> width[0]);
		final ProgressNode bar = ProgressNode.create(0D, 0D, 100D, 10D).progress(() -> progress[0]);
		final Checkbox checkbox = new Checkbox().checked(() -> checked[0]).onChange((node, value) -> changes.add(value));
		this.bridges.open(new NodeUI(rect, bar, checkbox));
		width[0] = 30D;
		color[0] = Color.GREEN;
		progress[0] = 0.5F;
		checked[0] = true;
		Assert.assertSame(Color.GREEN, rect.getColor());
		Assert.assertEquals(10D, rect.getWidth(), 0D);
		this.bridges.frame();
		Assert.assertEquals(30D, rect.getWidth(), 0D);
		Assert.assertEquals(0.5F, bar.getProgress(), 0F);
		Assert.assertTrue(checkbox.isChecked());
		Assert.assertEquals(Arrays.asList(true), changes);
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(true), changes);
	}

	@Test
	public void followsASignalPassedToASetter() {
		final DoubleSignal width = new DoubleSignal(10D);
		final Signal<Color> color = Signal.of(Color.GRAY);
		final BooleanSignal checked = new BooleanSignal(false);
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).color(color).width(width);
		final Checkbox checkbox = new Checkbox().checked(checked);
		this.bridges.open(new NodeUI(rect, checkbox));
		width.set(40D);
		color.set(Color.GREEN);
		checked.set(true);
		this.bridges.frame();
		Assert.assertEquals(40D, rect.getWidth(), 0D);
		Assert.assertSame(Color.GREEN, rect.getColor());
		Assert.assertTrue(checkbox.isChecked());
		checkbox.checked(false);
		Assert.assertTrue(checked.get());
		Assert.assertFalse(checkbox.isChecked());
	}

	@Test
	public void letsTheLayoutTakeOverAFollowedPosition() {
		final DoubleSignal x = new DoubleSignal(10D);
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).x(x);
		final FlexNode flex = FlexNode.vertical(0D, 0D, 100D).align(Align.START);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(10D, rect.getX(), 0D);
		flex.append(rect);
		this.bridges.frame();
		Assert.assertEquals(0D, rect.getX(), 0D);
		Assert.assertFalse(rect.getSourceMap().containsKey("x"));
	}

	@Test
	public void replacesTheSourceOfAPropertyWithTheLastSetter() {
		final double[] width = {10D};
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).width(() -> width[0]);
		Assert.assertTrue(rect.getSourceMap().containsKey("width"));
		rect.width(50D);
		Assert.assertFalse(rect.getSourceMap().containsKey("width"));
		this.bridges.open(new NodeUI(rect));
		width[0] = 30D;
		this.bridges.frame();
		Assert.assertEquals(50D, rect.getWidth(), 0D);
	}

	@Test
	public void keepsDrawingWhenAFollowedValueIsRefused() {
		final IntegerSignal index = IntegerSignal.of(0);
		final DoubleSignal width = new DoubleSignal(10D);
		final Switch node = new Switch().states("low", "high").index(index);
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).width(width);
		this.bridges.open(new NodeUI(node, rect));
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			index.set(5);
			width.set(30D);
			this.bridges.frame();
		} finally {
			System.setErr(error);
		}
		Assert.assertEquals("low", node.getState());
		Assert.assertEquals(30D, rect.getWidth(), 0D);
		Assert.assertTrue(new String(output.toByteArray(), StandardCharsets.UTF_8).startsWith("[JOID] The index of Switch cannot take its new value: java.lang.IllegalArgumentException: The index 5 is out of the state list [low, high]"));
	}

	@Test
	public void followsTheSettersOfAnEffect() {
		final float[] radius = {2F};
		final Signal<NodeEffectScope> scope = Signal.of(NodeEffectScope.SELF);
		final BlurNodeEffect blur = BlurNodeEffect.create(1F).radius(() -> radius[0]).priority(3).scope(scope);
		radius[0] = 4F;
		scope.set(NodeEffectScope.CHILDREN);
		Assert.assertEquals(4F, blur.getRadiusSupplier().get(), 0F);
		Assert.assertEquals(3, blur.getPriority());
		Assert.assertSame(NodeEffectScope.CHILDREN, blur.getScope());
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		public NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}