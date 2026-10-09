package dev.joid.lib.draw.shape;

import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4f;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.shader.impl.GradientShader;
import dev.joid.lib.shader.impl.RoundedShader;
import dev.joid.lib.shader.impl.RoundedShaderType;
import dev.joid.lib.shader.impl.ShadowShader;

public class DrawShapeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Before
	public void useAFractionalScale() {
		this.bridges.resize(1366, 768);
		this.bridges.getRender().getProjection().ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
	}

	@Test
	public void snapsTheEdgesOfABox() {
		DrawUtils.SHAPE.drawRect(10.3D, 20.6D, 100D, 50D, new Color(1F, 0F, 0F, 1F));
		final Draw draw = this.single(1F, 0F, 0F);
		Assert.assertEquals(Math.rint(10.3D * 1366D / 1920D), draw.getLeft(), 1E-3D);
		Assert.assertEquals(Math.rint(20.6D * 768D / 1080D), draw.getTop(), 1E-3D);
		Assert.assertEquals(Math.rint(110.3D * 1366D / 1920D), draw.getRight(), 1E-3D);
		Assert.assertEquals(Math.rint(70.6D * 768D / 1080D), draw.getBottom(), 1E-3D);
	}

	@Test
	public void keepsTouchingBoxesTogether() {
		DrawUtils.SHAPE.drawRect(0D, 10.3D, 100D, 40.4D, new Color(1F, 0F, 0F, 1F));
		DrawUtils.SHAPE.drawRect(0D, 50.7D, 100D, 30D, new Color(0F, 1F, 0F, 1F));
		Assert.assertEquals(this.single(1F, 0F, 0F).getBottom(), this.single(0F, 1F, 0F).getTop(), 1E-3D);
	}

	@Test
	public void drawsAThinRectangleWithTheSameThicknessEverywhere() {
		for (final double y : new double[] {10D, 10.2D, 10.5D, 10.8D, 133.3D}) {
			DrawUtils.SHAPE.drawRect(0D, y, 100D, 3D, new Color(0F, 0F, 1F, 1F));
		}

		final List<Draw> draws = this.bridges.getRender().getDraws(0F, 0F, 1F);
		Assert.assertEquals(5, draws.size());
		for (final Draw draw : draws) {
			Assert.assertEquals(2D, draw.getBottom() - draw.getTop(), 1E-3D);
			Assert.assertEquals(Math.rint(draw.getTop()), draw.getTop(), 1E-3D);
			Assert.assertEquals(1F, draw.getAlpha(), 0F);
		}
	}

	@Test
	public void lightensALineThinnerThanAPixel() {
		DrawUtils.SHAPE.drawRect(0D, 10.3D, 100D, 1D, new Color(1F, 1F, 1F, 1F));
		final Draw draw = this.single(1F, 1F, 1F);
		Assert.assertEquals(1D, draw.getBottom() - draw.getTop(), 1E-3D);
		Assert.assertEquals(768F / 1080F, draw.getAlpha(), 1E-4F);
	}

	@Test
	public void anchorsABorderOnItsBox() {
		DrawUtils.SHAPE.drawRect(10.3D, 10.3D, 100D, 50D, new Color(0F, 0F, 1F, 1F));
		DrawUtils.SHAPE.drawBorder(10.3D, 10.3D, 110.3D, 60.3D, new Color(1F, 0F, 0F, 1F), 3D);
		final Draw box = this.single(0F, 0F, 1F);
		final List<Draw> sides = this.bridges.getRender().getDraws(1F, 0F, 0F);
		Assert.assertEquals(4, sides.size());
		Assert.assertEquals(box.getTop(), sides.get(0).getBottom(), 1E-3D);
		Assert.assertEquals(box.getLeft(), sides.get(1).getRight(), 1E-3D);
		Assert.assertEquals(box.getBottom(), sides.get(2).getTop(), 1E-3D);
		Assert.assertEquals(box.getRight(), sides.get(3).getLeft(), 1E-3D);
		for (final Draw side : sides) {
			Assert.assertEquals(2D, Math.min(side.getBottom() - side.getTop(), side.getRight() - side.getLeft()), 1E-3D);
		}
	}

	@Test
	public void drawsACircleOnItsExactQuad() {
		DrawUtils.SHAPE.drawCircle(50.3D, 50.3D, new Color(0F, 1F, 0F, 1F), 10D);
		final Draw draw = this.single(0F, 1F, 0F);
		Assert.assertEquals(40.3D * 1366D / 1920D, draw.getLeft(), 1E-4D);
		Assert.assertEquals(60.3D * 1366D / 1920D, draw.getRight(), 1E-4D);
	}

	@Test
	public void leavesARotatedRectangleExact() {
		this.bridges.getRender().getModelView().push();
		try {
			this.bridges.getRender().getModelView().rotate(30D, 0D, 0D, 1D);
			DrawUtils.SHAPE.drawRect(10.3D, 20.6D, 100D, 50D, new Color(1F, 0F, 0F, 1F));
		} finally {
			this.bridges.getRender().getModelView().pop();
		}

		final Draw draw = this.single(1F, 0F, 0F);
		Assert.assertNotEquals(Math.rint(draw.getLeft()), draw.getLeft(), 1E-3D);
	}

	@Test
	public void smoothsTheEdgesOfARotatedRectangle() {
		this.bridges.resize(1920, 1080);
		this.bridges.getRender().getModelView().push();
		try {
			this.bridges.getRender().getModelView().translate(200D, 0D, 0D);
			this.bridges.getRender().getModelView().rotate(90D, 0D, 0D, 1D);
			DrawUtils.SHAPE.drawRect(10D, 20D, 100D, 50D, new Color(1F, 0F, 0F, 1F));
		} finally {
			this.bridges.getRender().getModelView().pop();
		}

		final Draw draw = this.single(1F, 0F, 0F);
		final RecordingShader shader = (RecordingShader) RoundedShader.inst().getShader();
		Assert.assertSame(shader, draw.getShader());
		Assert.assertEquals(0F, (Float) shader.getValues().get("u_Radius"), 0F);
		Assert.assertArrayEquals(new float[] {10.5F, 20.5F, 109.5F, 69.5F}, (float[]) shader.getValues().get("u_InnerRect"), 1E-4F);
		Assert.assertEquals(0, shader.getValues().get("u_Aligned"));
		Assert.assertEquals(9F, Float.intBitsToFloat(Tessellator.inst().getRawBuffer()[0]), 0F);
		Assert.assertEquals(71F, Float.intBitsToFloat(Tessellator.inst().getRawBuffer()[1]), 0F);
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void smoothsTheStraightEdgesOfARotatedRoundedRectangle() {
		this.bridges.resize(1920, 1080);
		this.bridges.getRender().getModelView().push();
		try {
			this.bridges.getRender().getModelView().translate(200D, 0D, 0D);
			this.bridges.getRender().getModelView().rotate(90D, 0D, 0D, 1D);
			DrawUtils.SHAPE.drawRoundedRect(10D, 20D, 100D, 50D, new Color(0F, 1F, 0F, 1F), 8F);
		} finally {
			this.bridges.getRender().getModelView().pop();
		}

		final Draw draw = this.single(0F, 1F, 0F);
		final RecordingShader shader = (RecordingShader) RoundedShader.inst().getShader();
		Assert.assertSame(shader, draw.getShader());
		Assert.assertArrayEquals(new float[] {18F, 28F, 102F, 62F}, (float[]) shader.getValues().get("u_InnerRect"), 1E-4F);
		Assert.assertEquals(0, shader.getValues().get("u_Aligned"));
		Assert.assertEquals(9F, Float.intBitsToFloat(Tessellator.inst().getRawBuffer()[0]), 0F);
		Assert.assertEquals(71F, Float.intBitsToFloat(Tessellator.inst().getRawBuffer()[1]), 0F);
	}

	@Test
	public void keepsTheBoundShaderOnARotatedRectangle() {
		this.bridges.resize(1920, 1080);
		final RecordingShader shader = new RecordingShader();
		shader.bind();
		this.bridges.getRender().getModelView().push();
		try {
			this.bridges.getRender().getModelView().translate(200D, 0D, 0D);
			this.bridges.getRender().getModelView().rotate(90D, 0D, 0D, 1D);
			DrawUtils.SHAPE.drawRect(10D, 20D, 100D, 50D, new Color(1F, 0F, 0F, 1F));
		} finally {
			this.bridges.getRender().getModelView().pop();
		}

		final Draw draw = this.single(1F, 0F, 0F);
		Assert.assertSame(shader, draw.getShader());
		Assert.assertEquals(30, draw.getXs().length);
		Assert.assertEquals(10.5F, Float.intBitsToFloat(Tessellator.inst().getRawBuffer()[0]), 0F);
		Assert.assertEquals(69.5F, Float.intBitsToFloat(Tessellator.inst().getRawBuffer()[1]), 0F);
		Assert.assertEquals(255, Tessellator.inst().getRawBuffer()[5] >>> 24);
		Assert.assertEquals(0, Tessellator.inst().getRawBuffer()[14 * 8 + 5] >>> 24);
	}

	@Test(expected = RuntimeException.class)
	public void refusesASecondInstance() {
		Assert.assertSame(DrawUtils.SHAPE, DrawShape.getInstance());
		new DrawShape();
	}

	@Test(expected = NullPointerException.class)
	public void refusesARectangleWithoutColor() {
		DrawUtils.SHAPE.drawRect(0D, 0D, 10D, 10D, null);
	}

	@Test
	public void roundsTheCornersInsideTheSnappedEdges() {
		DrawUtils.SHAPE.drawRoundedRect(10.3D, 20.6D, 100D, 50D, new Color(0F, 0F, 1F, 1F), 8F);
		final Draw draw = this.single(0F, 0F, 1F);
		final RecordingShader shader = (RecordingShader) RoundedShader.inst().getShader();
		final float[] inner = (float[]) shader.getValues().get("u_InnerRect");
		Assert.assertSame(shader, draw.getShader());
		Assert.assertEquals(Math.rint(10.3D * 1366D / 1920D), draw.getLeft(), 1E-3D);
		Assert.assertEquals(Math.rint(70.6D * 768D / 1080D), draw.getBottom(), 1E-3D);
		Assert.assertEquals(8F, (Float) shader.getValues().get("u_Radius"), 0F);
		Assert.assertEquals(draw.getLeft() * 1920D / 1366D + 8D, inner[0], 1E-3D);
		Assert.assertEquals(draw.getTop() * 1080D / 768D + 8D, inner[1], 1E-3D);
		Assert.assertEquals(draw.getRight() * 1920D / 1366D - 8D, inner[2], 1E-3D);
		Assert.assertEquals(draw.getBottom() * 1080D / 768D - 8D, inner[3], 1E-3D);
		Assert.assertEquals(RoundedShaderType.AUTO.ordinal(), shader.getValues().get("u_Type"));
		Assert.assertEquals(1, shader.getValues().get("u_Aligned"));
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void keepsTheSquareCornersOfTheFlatSides() {
		this.bridges.resize(1920, 1080);
		final RecordingShader shader = (RecordingShader) RoundedShader.inst().getShader();
		DrawUtils.SHAPE.drawRoundedRect(10D, 20D, 100D, 50D, new Color(0F, 0F, 1F, 1F), 8F, true, false, false, true);
		Assert.assertArrayEquals(new float[] {18F, 20F, 110F, 62F}, (float[]) shader.getValues().get("u_InnerRect"), 1E-3F);
		DrawUtils.SHAPE.drawRoundedRect(10D, 20D, 100D, 50D, new Color(0F, 0F, 1F, 1F), 8F, false, true, true, false);
		Assert.assertArrayEquals(new float[] {10F, 28F, 102F, 70F}, (float[]) shader.getValues().get("u_InnerRect"), 1E-3F);
	}

	@Test
	public void strokesTheRoundedBorderInsideItsBox() {
		this.bridges.resize(1920, 1080);
		final RecordingShader shader = (RecordingShader) RoundedShader.inst().getShader();
		DrawUtils.SHAPE.drawRoundedBorder(10D, 20D, 100D, 50D, new Color(0F, 0F, 1F, 1F), 8F, 3D);
		final Draw draw = this.single(0F, 0F, 1F);
		Assert.assertSame(shader, draw.getShader());
		Assert.assertEquals(10D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(20D, draw.getTop(), 1E-3D);
		Assert.assertEquals(110D, draw.getRight(), 1E-3D);
		Assert.assertEquals(70D, draw.getBottom(), 1E-3D);
		Assert.assertEquals(3F, (Float) shader.getValues().get("u_Stroke"), 0F);
		Assert.assertArrayEquals(new float[] {18F, 28F, 102F, 62F}, (float[]) shader.getValues().get("u_InnerRect"), 1E-3F);
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void strokesTheRoundedBorderOneUnitWideByDefault() {
		final RecordingShader shader = (RecordingShader) RoundedShader.inst().getShader();
		DrawUtils.SHAPE.drawRoundedBorder(10D, 20D, 100D, 50D, new Color(0F, 0F, 1F, 1F), 8F);
		Assert.assertEquals(1F, (Float) shader.getValues().get("u_Stroke"), 0F);
		DrawUtils.SHAPE.drawRoundedRect(10D, 20D, 100D, 50D, new Color(0F, 0F, 1F, 1F), 8F);
		Assert.assertEquals(0F, (Float) shader.getValues().get("u_Stroke"), 0F);
	}

	@Test
	public void spreadsTheShadowAroundItsBox() {
		this.bridges.resize(1920, 1080);
		final RecordingShader shader = (RecordingShader) ShadowShader.inst().getShader();
		DrawUtils.SHAPE.drawShadow(100D, 100D, 200D, 50D, new Color(0F, 0F, 1F, 0.5F), 10F, 8F);
		final Draw draw = this.single(0F, 0F, 1F);
		final Map<String, Object> values = shader.getValues();
		Assert.assertSame(shader, draw.getShader());
		Assert.assertEquals(88D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(88D, draw.getTop(), 1E-3D);
		Assert.assertEquals(312D, draw.getRight(), 1E-3D);
		Assert.assertEquals(162D, draw.getBottom(), 1E-3D);
		Assert.assertEquals(10F, (Float) values.get("u_Radius"), 0F);
		Assert.assertEquals(8F, (Float) values.get("u_Blur"), 0F);
		Assert.assertArrayEquals(new float[] {100F, 100F, 300F, 150F}, (float[]) values.get("u_Box"), 1E-3F);
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void drawsASharpShadowWithoutBlur() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawShadow(100D, 100D, 200D, 50D, new Color(0F, 0F, 1F, 0.5F), 10F, 0F);
		final Draw draw = this.single(0F, 0F, 1F);
		Assert.assertSame(RoundedShader.inst().getShader(), draw.getShader());
		Assert.assertEquals(100D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(300D, draw.getRight(), 1E-3D);
	}

	@Test
	public void shadesAGradientWithItsFirstColor() {
		DrawUtils.SHAPE.drawShadow(100D, 100D, 200D, 50D, new Color(0F, 0F, 1F, 1F).toGradient(new Color(1F, 0F, 0F, 1F)), 10F, 8F);
		Assert.assertSame(ShadowShader.inst().getShader(), this.single(0F, 0F, 1F).getShader());
	}

	@Test
	public void carvesTheCircleAroundItsCenter() {
		DrawUtils.SHAPE.drawCircle(50.3D, 60.6D, new Color(0F, 1F, 0F, 1F), 10D);
		final Draw draw = this.single(0F, 1F, 0F);
		final RecordingShader shader = (RecordingShader) CircleShader.inst().getShader();
		Assert.assertSame(shader, draw.getShader());
		Assert.assertEquals(50.6D * 768D / 1080D, draw.getTop(), 1E-4D);
		Assert.assertEquals(70.6D * 768D / 1080D, draw.getBottom(), 1E-4D);
		Assert.assertEquals(10F, (Float) shader.getValues().get("radius"), 0F);
		Assert.assertArrayEquals(new float[] {50.3F, 60.6F}, (float[]) shader.getValues().get("center"), 0F);
		Assert.assertEquals(RoundedShaderType.AUTO.ordinal(), shader.getValues().get("type"));
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void outlinesABoxWithAFadedPixelAndClosedCorners() {
		DrawUtils.SHAPE.drawRect(10.3D, 10.3D, 100D, 50D, new Color(0F, 0F, 1F, 1F));
		DrawUtils.SHAPE.drawBorder(10.3D, 10.3D, 110.3D, 60.3D, new Color(0F, 1F, 1F, 1F));
		final Draw box = this.single(0F, 0F, 1F);
		final List<Draw> sides = this.bridges.getRender().getDraws(0F, 1F, 1F);
		Assert.assertEquals(4, sides.size());
		Assert.assertEquals(box.getLeft() - 1D, sides.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(box.getRight() + 1D, sides.get(0).getRight(), 1E-3D);
		Assert.assertEquals(box.getTop(), sides.get(1).getTop(), 1E-3D);
		Assert.assertEquals(box.getBottom(), sides.get(1).getBottom(), 1E-3D);
		Assert.assertEquals(1D, sides.get(0).getBottom() - sides.get(0).getTop(), 1E-3D);
		Assert.assertEquals(1D, sides.get(1).getRight() - sides.get(1).getLeft(), 1E-3D);
		Assert.assertEquals(768F / 1080F, sides.get(2).getAlpha(), 1E-4F);
		Assert.assertEquals(1366F / 1920F, sides.get(3).getAlpha(), 1E-4F);
	}

	@Test
	public void fillsTheCornersOfAFilledBorder() {
		DrawUtils.SHAPE.drawRect(10.3D, 10.3D, 100D, 50D, new Color(0F, 0F, 1F, 1F));
		DrawUtils.SHAPE.drawFilledBorder(10.3D, 10.3D, 110.3D, 60.3D, new Color(0F, 1F, 1F, 1F), 3D);
		final Draw box = this.single(0F, 0F, 1F);
		final List<Draw> sides = this.bridges.getRender().getDraws(0F, 1F, 1F);
		Assert.assertEquals(4, sides.size());
		Assert.assertEquals(box.getLeft() - 2D, sides.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(box.getRight() + 2D, sides.get(0).getRight(), 1E-3D);
		Assert.assertEquals(box.getTop() - 2D, sides.get(0).getTop(), 1E-3D);
		Assert.assertEquals(box.getTop(), sides.get(0).getBottom(), 1E-3D);
		Assert.assertEquals(box.getLeft() - 2D, sides.get(1).getLeft(), 1E-3D);
		Assert.assertEquals(box.getLeft(), sides.get(1).getRight(), 1E-3D);
		Assert.assertEquals(box.getTop(), sides.get(1).getTop(), 1E-3D);
		Assert.assertEquals(box.getBottom(), sides.get(1).getBottom(), 1E-3D);
		Assert.assertEquals(box.getBottom() + 2D, sides.get(2).getBottom(), 1E-3D);
		Assert.assertEquals(box.getRight() + 2D, sides.get(2).getRight(), 1E-3D);
		Assert.assertEquals(box.getRight(), sides.get(3).getLeft(), 1E-3D);
		Assert.assertEquals(box.getRight() + 2D, sides.get(3).getRight(), 1E-3D);
		for (final Draw side : sides) {
			Assert.assertEquals(1F, side.getAlpha(), 0F);
		}
	}

	@Test
	public void strokesARotatedBorderAsOneOutline() {
		this.bridges.resize(1920, 1080);
		this.bridges.getRender().getModelView().push();
		try {
			this.bridges.getRender().getModelView().translate(200D, 0D, 0D);
			this.bridges.getRender().getModelView().rotate(90D, 0D, 0D, 1D);
			DrawUtils.SHAPE.drawBorder(10D, 20D, 110D, 70D, new Color(0F, 1F, 1F, 1F), 3D);
		} finally {
			this.bridges.getRender().getModelView().pop();
		}

		final Draw draw = this.single(0F, 1F, 1F);
		final RecordingShader shader = (RecordingShader) RoundedShader.inst().getShader();
		Assert.assertSame(shader, draw.getShader());
		Assert.assertEquals(0F, (Float) shader.getValues().get("u_Radius"), 0F);
		Assert.assertEquals(2.5F, (Float) shader.getValues().get("u_Stroke"), 0F);
		Assert.assertArrayEquals(new float[] {7.5F, 17.5F, 112.5F, 72.5F}, (float[]) shader.getValues().get("u_InnerRect"), 1E-4F);
		Assert.assertEquals(0, shader.getValues().get("u_Aligned"));
		Assert.assertEquals(6F, Float.intBitsToFloat(Tessellator.inst().getRawBuffer()[0]), 0F);
		Assert.assertEquals(74F, Float.intBitsToFloat(Tessellator.inst().getRawBuffer()[1]), 0F);
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void drawsAFilledBorderLikeABorder() {
		DrawUtils.SHAPE.drawBorder(10.3D, 10.3D, 110.3D, 60.3D, new Color(1F, 0F, 0F, 1F), 3D);
		DrawUtils.SHAPE.drawFilledBorder(10.3D, 10.3D, 110.3D, 60.3D, new Color(0F, 1F, 1F, 1F), 3D);
		final List<Draw> border = this.bridges.getRender().getDraws(1F, 0F, 0F);
		final List<Draw> filled = this.bridges.getRender().getDraws(0F, 1F, 1F);
		Assert.assertEquals(border.size(), filled.size());
		for (int i = 0; i < border.size(); i++) {
			Assert.assertEquals(border.get(i).getLeft(), filled.get(i).getLeft(), 1E-3D);
			Assert.assertEquals(border.get(i).getTop(), filled.get(i).getTop(), 1E-3D);
			Assert.assertEquals(border.get(i).getRight(), filled.get(i).getRight(), 1E-3D);
			Assert.assertEquals(border.get(i).getBottom(), filled.get(i).getBottom(), 1E-3D);
		}
	}

	@Test
	public void fadesAFilledBorderThinnerThanAPixel() {
		DrawUtils.SHAPE.drawFilledBorder(10.3D, 10.3D, 110.3D, 60.3D, new Color(0F, 1F, 1F, 1F));
		final List<Draw> sides = this.bridges.getRender().getDraws(0F, 1F, 1F);
		Assert.assertEquals(4, sides.size());
		Assert.assertEquals(1D, sides.get(0).getBottom() - sides.get(0).getTop(), 1E-3D);
		Assert.assertEquals(sides.get(1).getLeft(), sides.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(768F / 1080F, sides.get(0).getAlpha(), 1E-4F);
		Assert.assertEquals(1366F / 1920F, sides.get(1).getAlpha(), 1E-4F);
	}

	@Test
	public void keepsAnAxisAlignedPolygonOnItsExactPoints() {
		DrawUtils.SHAPE.drawPolygon(new Color(0F, 1F, 0F, 1F), new Vector2d(10.3D, 20.6D), new Vector2d(110.3D, 20.6D), new Vector2d(110.3D, 70.6D), new Vector2d(10.3D, 70.6D));
		final Draw draw = this.single(0F, 1F, 0F);
		Assert.assertSame(Primitive.TRIANGLES, draw.getPrimitive());
		Assert.assertEquals(6, draw.getXs().length);
		Assert.assertEquals(10.3D * 1366D / 1920D, draw.getLeft(), 1E-4D);
		Assert.assertEquals(110.3D * 1366D / 1920D, draw.getRight(), 1E-4D);
		Assert.assertEquals(20.6D * 768D / 1080D, draw.getTop(), 1E-4D);
		Assert.assertEquals(70.6D * 768D / 1080D, draw.getBottom(), 1E-4D);
	}

	@Test
	public void smoothsTheSlantedEdgesOfAPolygon() {
		DrawUtils.SHAPE.drawPolygon(new Color(0F, 1F, 0F, 0.8F), new Vector2d(10D, 20D), new Vector2d(110D, 20D), new Vector2d(60D, 70D));
		final Draw draw = this.single(0F, 1F, 0F);
		Assert.assertSame(Primitive.TRIANGLES, draw.getPrimitive());
		Assert.assertEquals(21, draw.getXs().length);
		Assert.assertTrue(draw.getLeft() < 10D * 1366D / 1920D);
		Assert.assertTrue(draw.getBottom() > 70D * 768D / 1080D);
		Assert.assertEquals(204, Tessellator.inst().getRawBuffer()[5] >>> 24);
	}

	@Test
	public void smoothsTheEdgesOfARotatedPolygon() {
		this.bridges.resize(1920, 1080);
		this.bridges.getRender().getModelView().push();
		try {
			this.bridges.getRender().getModelView().rotate(10D, 0D, 0D, 1D);
			DrawUtils.SHAPE.drawPolygon(new Color(0F, 1F, 0F, 1F), new Vector2d(10D, 20D), new Vector2d(110D, 20D), new Vector2d(110D, 70D), new Vector2d(10D, 70D));
		} finally {
			this.bridges.getRender().getModelView().pop();
		}

		Assert.assertEquals(30, this.single(0F, 1F, 0F).getXs().length);
	}

	@Test
	public void keepsAConcavePolygonOnItsExactPoints() {
		DrawUtils.SHAPE.drawPolygon(new Color(0F, 1F, 0F, 1F), new Vector2d(0D, 0D), new Vector2d(100D, 0D), new Vector2d(50D, 20D), new Vector2d(100D, 100D), new Vector2d(0D, 100D));
		Assert.assertEquals(9, this.single(0F, 1F, 0F).getXs().length);
	}

	@Test
	public void drawsEveryModeWithTheColorOfTheShape() {
		final int[] counts = {6, 10, 12, 6, 6, 48};
		final Vector2d[] points = {new Vector2d(0D, 0D), new Vector2d(10D, 0D), new Vector2d(20D, 10D), new Vector2d(10D, 20D), new Vector2d(0D, 20D), new Vector2d(-10D, 10D)};
		for (final DrawMode mode : DrawMode.values()) {
			DrawUtils.SHAPE.drawShape(mode, new Color(0.2F, 0.4F, 0.6F, 0.8F), points);
			final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
			final Draw draw = draws.get(draws.size() - 1);
			Assert.assertSame(mode.ordinal() < DrawMode.TRIANGLES.ordinal() ? Primitive.LINES : Primitive.TRIANGLES, draw.getPrimitive());
			Assert.assertEquals(counts[mode.ordinal()], draw.getXs().length);
			Assert.assertEquals(0.8F, draw.getAlpha(), 0F);
		}
	}

	@Test
	public void releasesTheBlendingAndTheTextureAfterAShape() {
		DrawUtils.SHAPE.drawPolygon(new Color(0F, 1F, 0F, 1F), new Vector2d(0D, 0D), new Vector2d(10D, 0D), new Vector2d(10D, 10D));
		Assert.assertSame(BlendState.DISABLED, this.bridges.getRender().getState().getBlend());
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void restoresTheBlendingAndTheTextureAfterAShape() {
		final ITexture texture = this.bridges.getRender().createTexture();
		this.bridges.getRender().getState().blend(BlendState.PREMULTIPLIED);
		this.bridges.getRender().getState().texture(texture).textureFilter(TextureFilter.LINEAR).textureWrap(TextureWrap.CLAMP_TO_EDGE);
		DrawUtils.SHAPE.drawPolygon(new Color(0F, 1F, 0F, 1F), new Vector2d(0D, 0D), new Vector2d(10D, 0D), new Vector2d(10D, 10D));
		DrawUtils.SHAPE.drawRawRect(0D, 0D, 10D, 10D);
		Assert.assertSame(BlendState.PREMULTIPLIED, this.bridges.getRender().getState().getBlend());
		Assert.assertSame(texture, this.bridges.getRender().getState().getTexture());
		Assert.assertSame(TextureFilter.LINEAR, this.bridges.getRender().getState().getTextureFilter());
	}

	@Test
	public void paintsAGradientOverTheShape() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawRect(10D, 20D, 100D, 50D, new Color(0.2F, 0.4F, 0.6F, 1F).toGradient(new Color(0.8F, 0.6F, 0.4F, 0.5F), new Vector4f(0F, 0F, 0F, 1F)));
		final RecordingShader shader = (RecordingShader) GradientShader.inst().getShader();
		final Map<String, Object> values = shader.getValues();
		Assert.assertEquals(1, this.bridges.getRender().getDraws().size());
		Assert.assertSame(shader, this.bridges.getRender().getDraws().get(0).getShader());
		Assert.assertArrayEquals(new float[] {0F, 0F}, (float[]) values.get("startPos"), 0F);
		Assert.assertArrayEquals(new float[] {0F, 1F}, (float[]) values.get("endPos"), 0F);
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) values.get("startColor"), 0F);
		Assert.assertArrayEquals(new float[] {0.8F, 0.6F, 0.4F, 0.5F}, (float[]) values.get("endColor"), 0F);
		Assert.assertEquals(0, values.get("hasTexture"));
		Assert.assertArrayEquals(new float[] {10F, 20F, 110F, 70F}, (float[]) values.get("canvas"), 1E-3F);
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void spansTheGradientOverEveryPoint() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawPolygon(Color.BLUE.toGradient(Color.GREEN), new Vector2d(100D, 220D), new Vector2d(300D, 200D), new Vector2d(150D, 400D));
		Assert.assertArrayEquals(new float[] {100F, 200F, 300F, 400F}, (float[]) ((RecordingShader) GradientShader.inst().getShader()).getValues().get("canvas"), 0F);
	}

	@Test
	public void drawsARawRectangleWithTheBoundColor() {
		this.bridges.getRender().getState().color(0.2F, 0.4F, 0.6F, 1F);
		DrawUtils.SHAPE.drawRawRect(10.3D, 20.6D, 100D, 1D);
		final Draw draw = this.single(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(Math.rint(10.3D * 1366D / 1920D), draw.getLeft(), 1E-3D);
		Assert.assertEquals(Math.rint(110.3D * 1366D / 1920D), draw.getRight(), 1E-3D);
		Assert.assertEquals(1D, draw.getBottom() - draw.getTop(), 1E-3D);
		Assert.assertEquals(1F, draw.getAlpha(), 0F);
		Assert.assertSame(BlendState.DISABLED, this.bridges.getRender().getState().getBlend());
	}

	@Test
	public void smoothsALineOnItsExactPoints() {
		DrawUtils.SHAPE.drawLine(new Color(0F, 1F, 0F, 1F), new Vector2d(10D, 20D), new Vector2d(110D, 20D), new Vector2d(110D, 80D));
		final Draw draw = this.single(0F, 1F, 0F);
		Assert.assertSame(Primitive.TRIANGLES, draw.getPrimitive());
		Assert.assertEquals(12, draw.getXs().length);
		Assert.assertEquals(10D * 1366D / 1920D, draw.getLeft(), 1E-4D);
		Assert.assertEquals(110D * 1366D / 1920D, draw.getRight(), 1E-4D);
		Assert.assertEquals(80D * 768D / 1080D, draw.getBottom(), 1E-4D);
		Assert.assertEquals(1F, draw.getLineWidth(), 0F);
		Assert.assertFalse(this.bridges.getRender().getState().isLineSmooth());
	}

	@Test
	public void widensALineForItsDrawOnly() {
		DrawUtils.SHAPE.drawLine(new Color(0F, 1F, 0F, 1F), 4F, new Vector2d(10D, 20D), new Vector2d(110D, 20D));
		Assert.assertEquals(4F, this.single(0F, 1F, 0F).getLineWidth(), 0F);
		Assert.assertEquals(1F, this.bridges.getRender().getState().getLineWidth(), 0F);
		Assert.assertFalse(this.bridges.getRender().getState().isLineSmooth());
	}

	@Test
	public void restoresTheLineStateAfterALine() {
		this.bridges.getRender().getState().lineWidth(3F).lineSmooth(true);
		DrawUtils.SHAPE.drawLine(new Color(0F, 1F, 0F, 1F), new Vector2d(10D, 20D), new Vector2d(110D, 20D));
		DrawUtils.SHAPE.drawLine(new Color(0F, 1F, 0F, 1F), 4F, new Vector2d(10D, 20D), new Vector2d(110D, 20D));
		DrawUtils.SHAPE.drawDashedLine(new Color(0F, 1F, 0F, 1F), 10, 2F, new Vector2d(0D, 50D), new Vector2d(100D, 50D));
		DrawUtils.SHAPE.drawCurvedLine(new Color(0F, 1F, 0F, 1F), 5F, new Vector2d(0D, 0D), new Vector2d(10D, 0D), new Vector2d(5D, 5D));
		DrawUtils.SHAPE.drawCurvedLine(new Color(0F, 1F, 0F, 1F), 5F, new Vector2d(0D, 0D), new Vector2d(0D, 5D), new Vector2d(10D, 0D), new Vector2d(10D, 5D));
		Assert.assertEquals(3F, this.bridges.getRender().getState().getLineWidth(), 0F);
		Assert.assertTrue(this.bridges.getRender().getState().isLineSmooth());
	}

	@Test
	public void restoresTheLineStateWhenALineFails() {
		this.bridges.getRender().getState().lineWidth(3F);
		try {
			DrawUtils.SHAPE.drawLine(new Color(0F, 1F, 0F, 1F), 4F, new Vector2d(10D, 20D), null);
			Assert.fail();
		} catch (final NullPointerException exception) {
			Assert.assertEquals(3F, this.bridges.getRender().getState().getLineWidth(), 0F);
			Assert.assertFalse(this.bridges.getRender().getState().isLineSmooth());
		}
	}

	@Test
	public void cutsADashedLineIntoEqualDashesAndGaps() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawDashedLine(new Color(0F, 1F, 0F, 1F), 10, 2F, new Vector2d(0D, 50D), new Vector2d(100D, 50D));
		final Draw draw = this.single(0F, 1F, 0F);
		Assert.assertEquals(30, draw.getXs().length);
		for (int dash = 0; dash < 5; dash++) {
			Assert.assertEquals(dash * 20D, draw.getXs()[dash * 6], 1E-3D);
			Assert.assertEquals(dash * 20D + 10D, draw.getXs()[dash * 6 + 2], 1E-3D);
		}
		Assert.assertEquals(2F, draw.getLineWidth(), 0F);
		Assert.assertEquals(1F, this.bridges.getRender().getState().getLineWidth(), 0F);
		Assert.assertFalse(this.bridges.getRender().getState().isLineSmooth());
	}

	@Test
	public void dashesEverySegmentAndClipsTheLastDash() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawDashedLine(new Color(0F, 1F, 0F, 1F), 10, 1F, new Vector2d(0D, 0D), new Vector2d(25D, 0D), new Vector2d(25D, 25D));
		final Draw draw = this.single(0F, 1F, 0F);
		Assert.assertEquals(24, draw.getXs().length);
		Assert.assertEquals(20D, draw.getXs()[6], 1E-3D);
		Assert.assertEquals(25D, draw.getXs()[8], 1E-3D);
		Assert.assertEquals(20D, draw.getYs()[18], 1E-3D);
		Assert.assertEquals(25D, draw.getYs()[20], 1E-3D);
	}

	@Test
	public void drawsNoDashWithoutLength() {
		DrawUtils.SHAPE.drawDashedLine(new Color(0F, 1F, 0F, 1F), 10, 3F, new Vector2d(5D, 5D), new Vector2d(5D, 5D));
		DrawUtils.SHAPE.drawDashedLine(new Color(0F, 1F, 0F, 1F), 10, 3F, new Vector2d(5D, 5D));
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
		Assert.assertEquals(1F, this.bridges.getRender().getState().getLineWidth(), 0F);
	}

	@Test
	public void followsAQuadraticCurve() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawCurvedLine(new Color(0F, 1F, 0F, 1F), new Vector2d(0D, 0D), new Vector2d(100D, 0D), new Vector2d(50D, 50D));
		final List<Draw> draws = this.bridges.getRender().getDraws(0F, 1F, 0F);
		Assert.assertEquals(142D, draws.size(), 1D);
		Assert.assertEquals(0D, draws.get(0).getXs()[0], 1E-3D);
		for (int i = 0; i < draws.size(); i++) {
			final Draw draw = draws.get(i);
			for (int vertex = 0; vertex < draw.getXs().length; vertex++) {
				Assert.assertEquals(draw.getXs()[vertex] * (100D - draw.getXs()[vertex]) / 100D, draw.getYs()[vertex], 1E-3D);
			}
			if (i > 0) {
				Assert.assertEquals(draws.get(i - 1).getXs()[2], draw.getXs()[0], 1E-4D);
			}
		}
	}

	@Test
	public void followsACubicCurve() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawCurvedLine(new Color(0F, 1F, 0F, 1F), new Vector2d(0D, 0D), new Vector2d(0D, 50D), new Vector2d(100D, 0D), new Vector2d(100D, 50D));
		final List<Draw> draws = this.bridges.getRender().getDraws(0F, 1F, 0F);
		Assert.assertEquals(200D, draws.size(), 1D);
		double deepest = 0D;
		for (int i = 1; i < draws.size(); i++) {
			deepest = Math.max(deepest, draws.get(i).getBottom());
			Assert.assertEquals(draws.get(i - 1).getXs()[2], draws.get(i).getXs()[0], 1E-4D);
			Assert.assertTrue(draws.get(i).getXs()[2] > draws.get(i).getXs()[0]);
		}
		Assert.assertEquals(37.5D, deepest, 1E-2D);
	}

	@Test
	public void widensACurveForItsDrawOnly() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawCurvedLine(new Color(0F, 1F, 0F, 1F), 3F, new Vector2d(0D, 0D), new Vector2d(10D, 0D), new Vector2d(5D, 5D));
		Assert.assertEquals(3F, this.bridges.getRender().getDraws().get(0).getLineWidth(), 0F);
		Assert.assertEquals(1F, this.bridges.getRender().getState().getLineWidth(), 0F);
		DrawUtils.SHAPE.drawCurvedLine(new Color(0F, 1F, 0F, 1F), 5F, new Vector2d(0D, 0D), new Vector2d(0D, 5D), new Vector2d(10D, 0D), new Vector2d(10D, 5D));
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(5F, draws.get(draws.size() - 1).getLineWidth(), 0F);
		Assert.assertEquals(1F, this.bridges.getRender().getState().getLineWidth(), 0F);
	}

	@Test
	public void spansTheGradientOverAShapeOnNegativeCoordinates() {
		DrawUtils.SHAPE.drawPolygon(Color.BLUE.toGradient(Color.GREEN), new Vector2d(-300D, -380D), new Vector2d(-100D, -400D), new Vector2d(-150D, -200D));
		Assert.assertArrayEquals(new float[] {-300F, -400F, -100F, -200F}, (float[]) ((RecordingShader) GradientShader.inst().getShader()).getValues().get("canvas"), 0F);
	}

	@Test
	public void endsAShortCurveOnItsEndPoint() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawCurvedLine(new Color(0F, 1F, 0F, 1F), new Vector2d(0D, 0D), new Vector2d(3D, 0D), new Vector2d(1.5D, 3D));
		final List<Draw> draws = this.bridges.getRender().getDraws(0F, 1F, 0F);
		final Draw last = draws.get(draws.size() - 1);
		Assert.assertEquals(3D, last.getXs()[2], 1E-3D);
		Assert.assertEquals(0D, last.getYs()[2], 1E-3D);
	}

	@Test
	public void drawsACurveThatComesBackToItsStart() {
		this.bridges.resize(1920, 1080);
		DrawUtils.SHAPE.drawCurvedLine(new Color(0F, 1F, 0F, 1F), new Vector2d(0D, 0D), new Vector2d(0D, 0D), new Vector2d(50D, 50D));
		double right = 0D;
		for (final Draw draw : this.bridges.getRender().getDraws(0F, 1F, 0F)) {
			right = Math.max(right, draw.getRight());
		}
		Assert.assertEquals(25D, right, 1D);
	}

	@Test
	public void roundsTheCornersOfAGradient() {
		DrawUtils.SHAPE.drawRoundedRect(10D, 20D, 100D, 50D, Color.BLUE.toGradient(Color.GREEN), 8F);
		Assert.assertSame(RoundedShader.inst().getShader(), this.bridges.getRender().getDraws().get(0).getShader());
	}

	@Test
	public void carvesACircleFilledWithAGradient() {
		DrawUtils.SHAPE.drawCircle(50D, 50D, Color.BLUE.toGradient(Color.GREEN), 10D);
		Assert.assertSame(CircleShader.inst().getShader(), this.bridges.getRender().getDraws().get(0).getShader());
	}

	@Test
	public void keepsTheShaderBoundBeforeARoundedRectangle() {
		final RecordingShader shader = new RecordingShader();
		shader.bind();
		DrawUtils.SHAPE.drawRoundedRect(10D, 20D, 100D, 50D, new Color(0F, 1F, 0F, 1F), 8F);
		Assert.assertSame(shader, this.bridges.getRender().getState().getShader());
	}

	@Test
	public void keepsTheShaderBoundBeforeACircle() {
		final RecordingShader shader = new RecordingShader();
		shader.bind();
		DrawUtils.SHAPE.drawCircle(50D, 50D, new Color(0F, 1F, 0F, 1F), 10D);
		Assert.assertSame(shader, this.bridges.getRender().getState().getShader());
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

}