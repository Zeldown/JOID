package dev.joid.lib.ui.node;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.replay.SetterUI;
import dev.joid.internal.TestUtils;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.binding.NodeBindings;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNodeTest.Checkbox;
import dev.joid.lib.ui.node.impl.control.choice.SwitchNodeTest.Switch;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode.ProgressDirection;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

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
		Assert.assertFalse(ui.getRect().isBorderCorners());
		Assert.assertEquals(3D, ui.getRect().getZlevel(), 0D);
		Assert.assertSame(Color.GREEN, ui.getCircle().getColor());
		Assert.assertSame(Align.END, ui.getCircle().getAnchorX());
		Assert.assertEquals("Step 1", ui.getText().getText().getText());
		Assert.assertSame(TextMode.SPLIT, ui.getText().getMode());
		Assert.assertEquals(0.25F, ui.getProgress().getProgress(), 0F);
		Assert.assertSame(ProgressDirection.RIGHT_TO_LEFT, ui.getProgress().getDirection());
		Assert.assertEquals(0.9F, ui.getPlayer().getVolume(), 1E-6F);
		Assert.assertTrue(ui.getPlayer().isLoop());
		Assert.assertEquals(Point.of(1D, 0D, 1D), ui.getPlayer().getLocation());
		Assert.assertEquals(2D, ui.getModel().getZoom(), 0D);
		Assert.assertEquals(90D, ui.getModel().getRotationYaw(), 0D);
		Assert.assertEquals("Type 1", ui.getField().getPlaceholder());
		Assert.assertEquals(11, ui.getField().getMaxTextLength());
		Assert.assertEquals(2D, ui.getField().getMarginLeft(), 0D);
		Assert.assertEquals(5D, ui.getFlex().getMargin(), 0D);
		Assert.assertSame(Align.CENTER, ui.getFlex().getAlign());
		Assert.assertEquals(2F, ui.getBorder().getWidth(), 0F);
		Assert.assertSame(Color.RED, ui.getBorder().getColor(ui.getFlex()));
		Assert.assertFalse(ui.getBorder().isCorners());
		Assert.assertEquals(1D, ui.getShadow().getOffsetX(), 0D);
		Assert.assertEquals(3F, ui.getShadow().getBlur(), 0F);
		Assert.assertEquals(3F, ui.getBlur().getRadius(), 0F);
		Assert.assertEquals(4F, ui.getRounded().getRadius(), 0F);
		Assert.assertEquals(2F, ui.getOutline().getWidth(), 0F);
		Assert.assertSame(Color.RED, ui.getOutline().getColor(ui.getFlex()));
		Assert.assertEquals(3F, ui.getGlow().getBlur(), 0F);
		Assert.assertSame(Color.RED, ui.getGlow().getColor());
		Assert.assertEquals(10, ui.getRadar().getValue().intValue());
		Assert.assertEquals("Label 1", ui.getRadar().getLabel());
		ui.getStep().set(4);
		this.bridges.frame();
		Assert.assertFalse(ui.getRect().isEnabled());
		Assert.assertTrue(ui.getRect().isVisible());
		ui.getStep().set(5);
		this.bridges.frame();
		Assert.assertFalse(ui.getRect().isVisible());
	}

	@Test
	public void followsABooleanSignalReadInAConditionOrAConcatenationWithoutWarning() {
		final SetterUI ui = new SetterUI();
		final String output = TestUtils.captureErr(true, () -> this.bridges.open(ui));
		Assert.assertEquals("", output);
		ui.getMuted().set(true);
		this.bridges.frame();
		Assert.assertEquals("Muted", ui.getState().getText().getText());
		Assert.assertEquals("Muted true", ui.getLabel().getText().getText());
	}

	@Test
	public void followsTheStyleAndTheTextThatSignalsChooseInAText() {
		final SetterUI ui = new SetterUI();
		Assert.assertEquals("", TestUtils.captureErr(true, () -> this.bridges.open(ui)));
		Assert.assertSame(ui.getInfo(), ui.getStyled().getText().get(0).getInfo());
		ui.getMuted().set(true);
		this.bridges.frame();
		Assert.assertEquals("Muted", ui.getStyled().getText().getText());
		Assert.assertSame(ui.getQuiet(), ui.getStyled().getText().get(0).getInfo());
		Assert.assertEquals("Muted", ui.getMixed().getText().getText());
		Assert.assertSame(ui.getInfo(), ui.getMixed().getText().get(0).getInfo());
		ui.getStep().set(1);
		this.bridges.frame();
		Assert.assertEquals("Muted", ui.getMixed().getText().getText());
		Assert.assertSame(ui.getQuiet(), ui.getMixed().getText().get(0).getInfo());
		ui.getMuted().set(false);
		this.bridges.frame();
		Assert.assertEquals("Sound on", ui.getStyled().getText().getText());
		Assert.assertSame(ui.getInfo(), ui.getStyled().getText().get(0).getInfo());
		Assert.assertEquals("Sound on", ui.getMixed().getText().getText());
		Assert.assertSame(ui.getQuiet(), ui.getMixed().getText().get(0).getInfo());
	}

	@Test
	public void followsTheStyleChosenInEachTurnOfALoop() {
		final SetterUI ui = new SetterUI();
		Assert.assertEquals("", TestUtils.captureErr(true, () -> this.bridges.open(ui)));
		ui.getSwitchList().get(1).set(true);
		this.bridges.frame();
		Assert.assertEquals("Off", ui.getRowList().get(0).getText().getText());
		Assert.assertSame(ui.getInfo(), ui.getRowList().get(0).getText().get(0).getInfo());
		Assert.assertEquals("On", ui.getRowList().get(1).getText().getText());
		Assert.assertSame(ui.getQuiet(), ui.getRowList().get(1).getText().get(0).getInfo());
		ui.getSwitchList().get(0).set(true);
		this.bridges.frame();
		Assert.assertEquals("On", ui.getRowList().get(0).getText().getText());
		Assert.assertSame(ui.getQuiet(), ui.getRowList().get(0).getText().get(0).getInfo());
	}

	@Test
	public void followsEachSignalGivenToAnEffectFactory() {
		final SetterUI ui = new SetterUI();
		Assert.assertEquals("", TestUtils.captureErr(true, () -> this.bridges.open(ui)));
		ui.getMuted().set(true);
		Assert.assertSame(Color.RED, ui.getEdge().getColor(ui.getFlex()));
		Assert.assertEquals(1F, ui.getEdge().getWidth(), 0F);
		Assert.assertSame(Color.RED, ui.getHalo().getColor());
		Assert.assertEquals(2F, ui.getHalo().getBlur(), 0F);
		ui.getStep().set(1);
		Assert.assertSame(Color.RED, ui.getEdge().getColor(ui.getFlex()));
		Assert.assertEquals(2F, ui.getEdge().getWidth(), 0F);
		Assert.assertSame(Color.RED, ui.getHalo().getColor());
		Assert.assertEquals(3F, ui.getHalo().getBlur(), 0F);
	}

	@Test
	public void followsABoundSignalButNotAStartValue() {
		final BooleanSignal checked = new BooleanSignal(true);
		final Checkbox checkbox = new Checkbox().checked(checked);
		Assert.assertTrue(NodeSetterTest.isFollowed(checkbox, "checked"));
		checkbox.checked(false);
		Assert.assertFalse(NodeSetterTest.isFollowed(checkbox, "checked"));
		Assert.assertTrue(NodeSetterTest.isFollowed(RectNode.create(0D, 0D, 10D, 10D).width(checked.map(value -> value ? 20D : 10D)), "width"));
	}

	@Test
	public void keepsTheStartValueOfAValueTheUserChanges() {
		final SetterUI ui = new SetterUI();
		this.bridges.open(ui);
		ui.getStep().set(1);
		this.bridges.frame();
		Assert.assertEquals("Name 0", ui.getField().getText());
		Assert.assertFalse(ui.getCheckbox().isChecked());
		Assert.assertFalse(ui.getToggle().isToggle());
		Assert.assertEquals("low", ui.getIndexed().getValue());
		Assert.assertEquals("low", ui.getNamed().getValue());
		Assert.assertEquals("first", ui.getSelector().getValue());
		Assert.assertEquals(1, ui.getSlider().getValue().intValue());
		ui.getStep().set(4);
		this.bridges.frame();
		Assert.assertFalse(ui.getSelector().isActive());
	}

	@Test
	public void keepsAPlainValueWithoutPollingIt() {
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).x(5D).width(30D).zlevel(2D);
		Assert.assertEquals(5D, rect.getX(), 0D);
		Assert.assertEquals(30D, rect.getWidth(), 0D);
		Assert.assertFalse(NodeSetterTest.isFollowed(rect, "x"));
		Assert.assertFalse(NodeSetterTest.isFollowed(rect, "width"));
		Assert.assertFalse(NodeSetterTest.isFollowed(rect, "zlevel"));
		this.bridges.open(new NodeUI(rect));
		Assert.assertEquals(5D, rect.getX(), 0D);
		Assert.assertEquals(30D, rect.getWidth(), 0D);
	}

	@Test
	public void readsASupplierOnEveryFrame() {
		final double[] width = {10D};
		final Color[] color = {Color.GRAY};
		final float[] progress = {0F};
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).color(() -> color[0]).width(() -> width[0]);
		final ProgressNode bar = ProgressNode.create(0D, 0D, 100D, 10D).fill(RectNode.create(0D, 0D, 0D, 0D)).progress(() -> progress[0]);
		this.bridges.open(new NodeUI(rect, bar));
		width[0] = 30D;
		color[0] = Color.GREEN;
		progress[0] = 0.5F;
		Assert.assertSame(Color.GRAY, rect.getColor());
		Assert.assertEquals(10D, rect.getWidth(), 0D);
		this.bridges.frame();
		Assert.assertSame(Color.GREEN, rect.getColor());
		Assert.assertEquals(30D, rect.getWidth(), 0D);
		Assert.assertEquals(0.5F, bar.getProgress(), 0F);
	}

	@Test
	public void followsASignalPassedToASetter() {
		final DoubleSignal width = new DoubleSignal(10D);
		final Signal<Color> color = new Signal<>(Color.GRAY);
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
	public void keepsAFollowedPositionUnderTheLayout() {
		final DoubleSignal x = new DoubleSignal(10D);
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).x(x);
		final FlexNode flex = FlexNode.vertical(0D, 0D, 100D).align(Align.START);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(10D, rect.getX(), 0D);
		flex.append(rect);
		this.bridges.frame();
		Assert.assertEquals(0D, rect.getX(), 0D);
		Assert.assertEquals(-10D, rect.getLayoutX(), 0D);
		Assert.assertTrue(NodeSetterTest.isFollowed(rect, "x"));
	}

	@Test
	public void replacesTheSourceOfAPropertyWithTheLastSetter() {
		final double[] width = {10D};
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).width(() -> width[0]);
		Assert.assertTrue(NodeSetterTest.isFollowed(rect, "width"));
		rect.width(50D);
		Assert.assertFalse(NodeSetterTest.isFollowed(rect, "width"));
		this.bridges.open(new NodeUI(rect));
		width[0] = 30D;
		this.bridges.frame();
		Assert.assertEquals(50D, rect.getWidth(), 0D);
	}

	@Test
	public void keepsDrawingWhenAFollowedValueIsRefused() {
		final IntegerSignal index = new IntegerSignal(0);
		final DoubleSignal width = new DoubleSignal(10D);
		final Switch node = new Switch().values(index.map(value -> value == 0 ? Arrays.asList("low", "high") : Collections.<String>emptyList()));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).width(width);
		this.bridges.open(new NodeUI(node, rect));
		final String output = TestUtils.captureErr(() -> {
			index.set(5);
			width.set(30D);
			this.bridges.frame();
		});
		Assert.assertEquals("low", node.getValue());
		Assert.assertEquals(30D, rect.getWidth(), 0D);
		Assert.assertTrue(output.startsWith("[JOID] The values of Switch cannot take its new value (java.lang.IllegalArgumentException: The Switch needs at least one value"));
	}

	@Test
	public void followsOneSourcePerPropertyBehindAShorthand() {
		final Align[] anchor = {Align.CENTER};
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).anchor(() -> anchor[0]).anchorX(Align.END);
		this.bridges.open(new NodeUI(rect));
		anchor[0] = Align.START;
		this.bridges.frame();
		Assert.assertSame(Align.END, rect.getAnchorX());
		Assert.assertSame(Align.START, rect.getAnchorY());
		rect.anchor(Align.CENTER);
		Assert.assertFalse(NodeSetterTest.isFollowed(rect, "anchorY"));
		final double[] margin = {4D};
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D).margin(() -> margin[0]).marginTop(1D).marginHorizontal(2D);
		Assert.assertEquals(1D, field.getMarginTop(), 0D);
		Assert.assertEquals(2D, field.getMarginLeft(), 0D);
		Assert.assertEquals(4D, field.getMarginBottom(), 0D);
		Assert.assertFalse(NodeSetterTest.isFollowed(field, "marginTop"));
		Assert.assertFalse(NodeSetterTest.isFollowed(field, "marginRight"));
		Assert.assertTrue(NodeSetterTest.isFollowed(field, "marginBottom"));
		final GridNode grid = GridNode.create(0D, 0D, 100D, 100D).margin(() -> margin[0]).marginVertical(3D);
		Assert.assertEquals(3D, grid.getMarginVertical(), 0D);
		Assert.assertEquals(4D, grid.getMarginHorizontal(), 0D);
		Assert.assertFalse(NodeSetterTest.isFollowed(grid, "marginVertical"));
		Assert.assertTrue(NodeSetterTest.isFollowed(grid, "marginHorizontal"));
	}

	@Test
	public void followsTheSettersOfAnEffect() {
		final float[] radius = {2F};
		final Signal<NodeEffectScope> scope = new Signal<>(NodeEffectScope.SELF);
		final BlurNodeEffect blur = BlurNodeEffect.create(1F).radius(() -> radius[0]).priority(3).scope(scope);
		radius[0] = 4F;
		scope.set(NodeEffectScope.CHILDREN);
		Assert.assertEquals(4F, blur.getRadius(), 0F);
		Assert.assertEquals(3, blur.getPriority());
		Assert.assertSame(NodeEffectScope.CHILDREN, blur.getScope());
	}

	private static boolean isFollowed(final Node node, final String property) {
		try {
			final Field bindings = Node.class.getDeclaredField("bindings");
			bindings.setAccessible(true);
			final Field bindingMap = NodeBindings.class.getDeclaredField("bindingMap");
			bindingMap.setAccessible(true);
			return ((Map<?, ?>) bindingMap.get(bindings.get(node))).containsKey(property);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

}