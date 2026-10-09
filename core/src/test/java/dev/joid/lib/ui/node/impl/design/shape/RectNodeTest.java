package dev.joid.lib.ui.node.impl.design.shape;

import java.util.List;
import java.util.Map;

import javax.vecmath.Vector4f;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader;
import dev.joid.lib.shader.impl.GradientShader;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;

public class RectNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader border;

	@Before
	public void clearTheBorderUniforms() {
		this.border = (RecordingShader) BorderShader.inst().getShader();
		this.border.getValues().clear();
	}

	@Test
	public void staysTransparentWithoutAColor() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D);
		Assert.assertSame(Color.TRANSPARENT, rect.getColor());
		Assert.assertSame(Color.TRANSPARENT, rect.getBorderColor());
		Assert.assertEquals(0D, rect.getBorderStroke(), 0D);
		Assert.assertTrue(rect.isBorderFill());
		Assert.assertTrue(rect.getEffectMap().isEmpty());
		this.bridges.open(new NodeUI(rect)).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertEquals(0F, draws.get(0).getAlpha(), 0F);
	}

	@Test
	public void drawsItsBoxInItsColor() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)))).frame();
		final Draw draw = this.single(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(100D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(200D, draw.getTop(), 1E-3D);
		Assert.assertEquals(300D, draw.getRight(), 1E-3D);
		Assert.assertEquals(250D, draw.getBottom(), 1E-3D);
		Assert.assertEquals(1F, draw.getAlpha(), 0F);
		Assert.assertNull(draw.getShader());
	}

	@Test
	public void drawsInsideItsParent() {
		final RectNode parent = RectNode.create(100D, 200D, 200D, 50D);
		RectNode.create(10D, 20D, 30D, 10D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).attach(parent);
		this.bridges.open(new NodeUI(parent)).frame();
		final Draw draw = this.single(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(110D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(220D, draw.getTop(), 1E-3D);
		Assert.assertEquals(140D, draw.getRight(), 1E-3D);
		Assert.assertEquals(230D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void readsASuppliedColorOnEveryFrame() {
		final Color[] color = {new Color(0.2F, 0.4F, 0.6F, 1F)};
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).color(() -> color[0]);
		this.bridges.open(new NodeUI(rect)).frame();
		this.single(0.2F, 0.4F, 0.6F);
		color[0] = new Color(0.6F, 0.4F, 0.2F, 1F);
		this.bridges.frame();
		this.single(0.6F, 0.4F, 0.2F);
		Assert.assertSame(color[0], rect.getColor());
	}

	@Test
	public void followsAColorSignal() {
		final IntegerSignal clicks = IntegerSignal.of(0);
		final Signal<Color> color = Signal.of(new Color(0.2F, 0.4F, 0.6F, 1F));
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).color(color);
		final RectNode mapped = RectNode.create(100D, 300D, 200D, 50D).color(clicks.map(value -> value >= 3 ? Color.GREEN : Color.GRAY));
		color.set(new Color(0.6F, 0.4F, 0.2F, 1F));
		Assert.assertSame(color.peek(), rect.getColor());
		Assert.assertSame(Color.GRAY, mapped.getColor());
		clicks.set(3);
		Assert.assertSame(Color.GREEN, mapped.getColor());
		this.bridges.open(new NodeUI(rect)).frame();
		this.single(0.6F, 0.4F, 0.2F);
	}

	@Test
	public void turnsIntoItsHoveredColorUnderTheMouse() {
		final Color hovered = new Color(0.6F, 0.4F, 0.2F, 1F);
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor(hovered);
		Assert.assertSame(hovered, rect.getHoveredColor());
		this.bridges.open(new NodeUI(rect)).frame();
		this.single(0.2F, 0.4F, 0.6F);
		this.bridges.move(150D, 220D).frames(20);
		this.single(0.6F, 0.4F, 0.2F);
		this.bridges.move(10D, 10D).frames(20);
		this.single(0.2F, 0.4F, 0.6F);
	}

	@Test
	public void fadesTowardsItsHoveredColor() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor(new Color(0.6F, 0.4F, 0.2F, 1F)))).frame();
		this.bridges.move(150D, 220D).frames(6);
		final Draw draw = this.bridges.getRender().getDraws().get(0);
		Assert.assertTrue(draw.getRed() > 0.2F && draw.getRed() < 0.6F);
		Assert.assertTrue(draw.getBlue() > 0.2F && draw.getBlue() < 0.6F);
		Assert.assertEquals(0.4F, draw.getGreen(), 1E-4F);
	}

	@Test
	public void takesAHoveredColorOfItsOwn() {
		final Color hovered = new Color(0.6F, 0.4F, 0.2F, 1F);
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor(hovered);
		Assert.assertSame(hovered, rect.getHoveredColor());
		this.bridges.open(new NodeUI(rect)).move(150D, 220D).frames(20);
		this.single(0.6F, 0.4F, 0.2F);
	}

	@Test
	public void readsASuppliedHoveredColor() {
		final Color[] hovered = {new Color(0.6F, 0.4F, 0.2F, 1F)};
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor(() -> hovered[0]);
		this.bridges.open(new NodeUI(rect)).move(150D, 220D).frames(20);
		this.single(0.6F, 0.4F, 0.2F);
		hovered[0] = new Color(0.4F, 0.6F, 0.2F, 1F);
		this.bridges.frame();
		this.single(0.4F, 0.6F, 0.2F);
	}

	@Test
	public void readsBothSuppliedColors() {
		final Color color = new Color(0.2F, 0.4F, 0.6F, 1F);
		final Color hovered = new Color(0.6F, 0.4F, 0.2F, 1F);
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).color(() -> color).hoveredColor(() -> hovered);
		Assert.assertSame(color, rect.getColor());
		Assert.assertSame(hovered, rect.getHoveredColor());
		this.bridges.open(new NodeUI(rect)).move(150D, 220D).frames(20);
		this.single(0.6F, 0.4F, 0.2F);
	}

	@Test
	public void keepsItsColorWhenTheHoveredOneIsMissing() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hoveredColor((Color) null);
		Assert.assertNull(rect.getHoveredColor());
		this.bridges.open(new NodeUI(rect)).move(150D, 220D).frames(20);
		this.single(0.2F, 0.4F, 0.6F);
	}

	@Test
	public void drawsAGradientThroughItsShader() {
		final RecordingShader shader = (RecordingShader) GradientShader.inst().getShader();
		this.bridges.open(new NodeUI(RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F).toGradient(new Color(0.6F, 0.4F, 0.2F, 0.5F))))).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertSame(shader, draws.get(0).getShader());
		Assert.assertEquals(100D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(250D, draws.get(0).getBottom(), 1E-3D);
		final Map<String, Object> values = shader.getValues();
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) values.get("startColor"), 0F);
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 0.5F}, (float[]) values.get("endColor"), 0F);
		Assert.assertArrayEquals(new float[] {0F, 0F}, (float[]) values.get("startPos"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 0F}, (float[]) values.get("endPos"), 0F);
		Assert.assertArrayEquals(new float[] {100F, 200F, 300F, 250F}, (float[]) values.get("canvas"), 1E-3F);
	}

	@Test
	public void drawsAGradientInItsDirection() {
		final RecordingShader shader = (RecordingShader) GradientShader.inst().getShader();
		this.bridges.open(new NodeUI(RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F).toGradient(new Color(0.6F, 0.4F, 0.2F, 1F), new Vector4f(0F, 0F, 0F, 1F))))).frame();
		Assert.assertArrayEquals(new float[] {0F, 0F}, (float[]) shader.getValues().get("startPos"), 0F);
		Assert.assertArrayEquals(new float[] {0F, 1F}, (float[]) shader.getValues().get("endPos"), 0F);
	}

	@Test
	public void outlinesItselfWithAFilledBorder() {
		final Color color = new Color(0.6F, 0.4F, 0.2F, 1F);
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).borderColor(color).borderStroke(3D);
		Assert.assertSame(color, rect.getBorderColor());
		Assert.assertEquals(3D, rect.getBorderStroke(), 0D);
		Assert.assertTrue(rect.isBorderFill());
		this.bridges.open(new NodeUI(rect)).frame();
		final Map<String, Object> values = this.border.getValues();
		Assert.assertEquals(3F, (Float) values.get("u_BorderWidth"), 1E-4F);
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 1F}, (float[]) values.get("u_BorderColor"), 0F);
		Assert.assertEquals(1, values.get("u_Fill"));
		Assert.assertEquals(0, values.get("u_HasGradient"));
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw quad = draws.get(draws.size() - 1);
		Assert.assertSame(this.border, quad.getShader());
		Assert.assertEquals(95D, quad.getLeft(), 1E-3D);
		Assert.assertEquals(195D, quad.getTop(), 1E-3D);
		Assert.assertEquals(305D, quad.getRight(), 1E-3D);
		Assert.assertEquals(255D, quad.getBottom(), 1E-3D);
	}

	@Test
	public void leavesTheStrokeEmptyWithoutFill() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).borderStroke(3D).borderFill(false);
		Assert.assertFalse(rect.isBorderFill());
		this.bridges.open(new NodeUI(rect)).frame();
		Assert.assertEquals(0, this.border.getValues().get("u_Fill"));
	}

	@Test
	public void readsASuppliedBorderColor() {
		final Color[] color = {new Color(0.6F, 0.4F, 0.2F, 1F)};
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(() -> color[0]).borderStroke(2D);
		Assert.assertTrue(rect.isBorderFill());
		this.bridges.open(new NodeUI(rect)).frame();
		color[0] = new Color(0.4F, 0.6F, 0.2F, 1F);
		this.bridges.frame();
		Assert.assertArrayEquals(new float[] {0.4F, 0.6F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
		Assert.assertEquals(2F, (Float) this.border.getValues().get("u_BorderWidth"), 1E-4F);
	}

	@Test
	public void readsASuppliedBorderColorWithoutFill() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(() -> new Color(0.6F, 0.4F, 0.2F, 1F)).borderStroke(2D).borderFill(false);
		this.bridges.open(new NodeUI(rect)).frame();
		Assert.assertEquals(0, this.border.getValues().get("u_Fill"));
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
	}

	@Test
	public void turnsTheBorderIntoItsHoveredColorUnderTheMouse() {
		final Color hovered = new Color(0.4F, 0.6F, 0.2F, 1F);
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).hoveredBorderColor(hovered).borderStroke(3D).borderFill(true);
		Assert.assertSame(hovered, rect.getHoveredBorderColor());
		this.bridges.open(new NodeUI(rect)).frame();
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
		this.bridges.move(150D, 220D).frames(20);
		Assert.assertArrayEquals(new float[] {0.4F, 0.6F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
		this.bridges.move(10D, 10D).frames(20);
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
	}

	@Test
	public void readsBothSuppliedBorderColors() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(() -> new Color(0.6F, 0.4F, 0.2F, 1F)).hoveredBorderColor(() -> new Color(0.4F, 0.6F, 0.2F, 1F)).borderStroke(3D).borderFill(false);
		Assert.assertFalse(rect.isBorderFill());
		this.bridges.open(new NodeUI(rect)).move(150D, 220D).frames(20);
		Assert.assertArrayEquals(new float[] {0.4F, 0.6F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
	}

	@Test
	public void takesAHoveredBorderColorOfItsOwn() {
		final Color hovered = new Color(0.4F, 0.6F, 0.2F, 1F);
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).borderStroke(3D).hoveredBorderColor(hovered);
		Assert.assertSame(hovered, rect.getHoveredBorderColor());
		this.bridges.open(new NodeUI(rect)).move(150D, 220D).frames(20);
		Assert.assertArrayEquals(new float[] {0.4F, 0.6F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
	}

	@Test
	public void readsASuppliedHoveredBorderColor() {
		final Color[] hovered = {new Color(0.4F, 0.6F, 0.2F, 1F)};
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).borderStroke(3D).hoveredBorderColor(() -> hovered[0]);
		this.bridges.open(new NodeUI(rect)).move(150D, 220D).frames(20);
		hovered[0] = new Color(0.2F, 0.4F, 0.6F, 1F);
		this.bridges.frame();
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
	}

	@Test
	public void keepsItsBorderColorWhenTheHoveredOneIsMissing() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).hoveredBorderColor((Color) null).borderStroke(3D).borderFill(true);
		Assert.assertNull(rect.getHoveredBorderColor());
		this.bridges.open(new NodeUI(rect)).move(150D, 220D).frames(20);
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
	}

	@Test
	public void drawsAGradientBorder() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F).toGradient(new Color(0.2F, 0.4F, 0.6F, 1F))).borderStroke(2D);
		this.bridges.open(new NodeUI(rect)).frame();
		final Map<String, Object> values = this.border.getValues();
		Assert.assertEquals(1, values.get("u_HasGradient"));
		Assert.assertArrayEquals(new float[] {0.6F, 0.4F, 0.2F, 1F}, (float[]) values.get("u_GradientStart"), 0F);
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) values.get("u_GradientEnd"), 0F);
	}

	@Test
	public void dropsTheBorderWithoutStroke() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).borderStroke(3D);
		Assert.assertEquals(1, rect.getEffectMap().size());
		rect.borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).borderStroke(0D);
		Assert.assertFalse(rect.shouldApplyEffect(rect.getEffectMap().values().iterator().next()));
		this.bridges.open(new NodeUI(rect)).frame();
		Assert.assertTrue(this.border.getValues().isEmpty());
	}

	@Test
	public void keepsASingleBorder() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).borderStroke(3D).borderColor(new Color(0.4F, 0.6F, 0.2F, 1F)).borderStroke(5D);
		Assert.assertEquals(1, rect.getEffectMap().size());
		final NodeEffect<Node> effect = rect.getEffectMap().values().iterator().next();
		Assert.assertTrue(effect instanceof BorderNodeEffect);
		this.bridges.open(new NodeUI(rect)).frame();
		Assert.assertEquals(5F, (Float) this.border.getValues().get("u_BorderWidth"), 1E-4F);
		Assert.assertArrayEquals(new float[] {0.4F, 0.6F, 0.2F, 1F}, (float[]) this.border.getValues().get("u_BorderColor"), 0F);
	}

	@Test
	public void returnsItselfFromEachSetter() {
		final RectNode rect = RectNode.create(100D, 200D, 200D, 50D);
		final Color color = new Color(0.2F, 0.4F, 0.6F, 1F);
		Assert.assertSame(rect, rect.color(color));
		Assert.assertSame(rect, rect.color(() -> color));
		Assert.assertSame(rect, rect.color(color).hoveredColor(color));
		Assert.assertSame(rect, rect.color(() -> color).hoveredColor(() -> color));
		Assert.assertSame(rect, rect.hoveredColor(color));
		Assert.assertSame(rect, rect.hoveredColor(() -> color));
		Assert.assertSame(rect, rect.borderColor(color).borderStroke(1D));
		Assert.assertSame(rect, rect.borderColor(color).borderStroke(1D).borderFill(false));
		Assert.assertSame(rect, rect.borderColor(() -> color).borderStroke(1D));
		Assert.assertSame(rect, rect.borderColor(() -> color).borderStroke(1D).borderFill(false));
		Assert.assertSame(rect, rect.borderColor(color).hoveredBorderColor(color).borderStroke(1D).borderFill(false));
		Assert.assertSame(rect, rect.borderColor(() -> color).hoveredBorderColor(() -> color).borderStroke(1D).borderFill(false));
		Assert.assertSame(rect, rect.hoveredBorderColor(color));
		Assert.assertSame(rect, rect.hoveredBorderColor(() -> color));
	}

	@Test
	public void hasNoHoveredColorByDefault() {
		Assert.assertNull(RectNode.create(100D, 200D, 200D, 50D).getHoveredColor());
	}

	@Test
	public void hasNoHoveredBorderColorByDefault() {
		Assert.assertNull(RectNode.create(100D, 200D, 200D, 50D).borderColor(new Color(0.6F, 0.4F, 0.2F, 1F)).borderStroke(3D).getHoveredBorderColor());
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	@UIData(background = false)
	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}