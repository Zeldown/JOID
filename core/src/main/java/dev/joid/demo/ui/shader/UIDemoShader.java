package dev.joid.demo.ui.shader;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.render.transform.operation.RotateTransformOperation;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.shader.impl.GradientShader;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoShader extends UIDemo {

	private static final Color INK = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoShader.INK);
		final Resource image = Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"));

		RectNode.create(55, 40, 200, 120).color(Color.RED).attach(this);
		TextNode.create(155, 175).text(Text.create("Solid", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 40, 200, 120).color(Color.BLUE.toGradient(Color.GREEN)).attach(this);
		TextNode.create(385, 175).text(Text.create("Gradient", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(515, 40, 200, 120).color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))).attach(this);
		TextNode.create(615, 175).text(Text.create("Vertical gradient", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(745, 40, 200, 120).color(Color.ORANGE.toGradient(Color.BLUE, new Vector4f(0F, 0F, 1F, 1F))).attach(this);
		TextNode.create(845, 175).text(Text.create("Diagonal gradient", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1045, 30, 60, 140).color(Color.WHITE).attach(this);
		RectNode.create(975, 40, 200, 120).color(Color.RED.copyAlpha(0.5F)).attach(this);
		TextNode.create(1075, 175).text(Text.create("Transparent", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1205, 40, 200, 120).color(Color.RED.toGradient(Color.RED.copyAlpha(0F))).attach(this);
		TextNode.create(1305, 175).text(Text.create("Fading gradient", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1435, 40, 200, 120).color(Color.RAINBOW).attach(this);
		TextNode.create(1535, 175).text(Text.create("Rainbow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1665, 40, 200, 120).color(Color.LOADING).attach(this);
		TextNode.create(1765, 175).text(Text.create("Loading", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(55, 240, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.WHITE, 20F)))
		.attach(this);
		TextNode.create(155, 375).text(Text.create("Rounded rect", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(285, 240, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.WHITE, 30F, true, true, false, false)))
		.attach(this);
		TextNode.create(385, 375).text(Text.create("Some corners", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(515, 240, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.WHITE, 20F, 3D)))
		.attach(this);
		TextNode.create(615, 375).text(Text.create("Rounded stroke", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(745, 240, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.ORANGE, 30F, 1D)))
		.attach(this);
		TextNode.create(845, 375).text(Text.create("Thin stroke", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(975, 240, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawBorder(node.getX(), node.getY(), node.getX() + node.getWidth(), node.getY() + node.getHeight(), Color.WHITE, 3D)))
		.attach(this);
		TextNode.create(1075, 375).text(Text.create("Border", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1205, 240, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawFilledBorder(node.getX(), node.getY(), node.getX() + node.getWidth(), node.getY() + node.getHeight(), Color.WHITE, 6D)))
		.attach(this);
		TextNode.create(1305, 375).text(Text.create("Filled border", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1435, 240, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawCircle(node.getX() + 100D, node.getY() + 60D, Color.MAGENTA, 60D)))
		.attach(this);
		TextNode.create(1535, 375).text(Text.create("Circle", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1665, 240, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawPolygon(Color.GREEN, new Vector2d(node.getX() + 100D, node.getY()), new Vector2d(node.getX() + 200D, node.getY() + 120D), new Vector2d(node.getX(), node.getY() + 120D))))
		.attach(this);
		TextNode.create(1765, 375).text(Text.create("Polygon", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(55, 440, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(Color.WHITE, 3F, new Vector2d(node.getX(), node.getY() + 110D), new Vector2d(node.getX() + 200D, node.getY() + 10D))))
		.attach(this);
		TextNode.create(155, 575).text(Text.create("Line", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(285, 440, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(Color.WHITE, 3F, new Vector2d(node.getX(), node.getY() + 100D), new Vector2d(node.getX() + 50D, node.getY() + 20D), new Vector2d(node.getX() + 100D, node.getY() + 100D), new Vector2d(node.getX() + 150D, node.getY() + 20D), new Vector2d(node.getX() + 200D, node.getY() + 100D))))
		.attach(this);
		TextNode.create(385, 575).text(Text.create("Polyline", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(515, 440, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 3F, new Vector2d(node.getX(), node.getY() + 110D), new Vector2d(node.getX() + 200D, node.getY() + 110D), new Vector2d(node.getX() + 100D, node.getY() - 90D))))
		.attach(this);
		TextNode.create(615, 575).text(Text.create("Curve", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(745, 440, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 3F, new Vector2d(node.getX(), node.getY() + 60D), new Vector2d(node.getX() + 70D, node.getY() - 60D), new Vector2d(node.getX() + 200D, node.getY() + 60D), new Vector2d(node.getX() + 130D, node.getY() + 180D))))
		.attach(this);
		TextNode.create(845, 575).text(Text.create("Bezier", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(975, 440, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, Color.CYAN, new Vector2d(node.getX() + 100D, node.getY()), new Vector2d(node.getX() + 200D, node.getY() + 60D), new Vector2d(node.getX() + 100D, node.getY() + 120D), new Vector2d(node.getX(), node.getY() + 60D))))
		.attach(this);
		TextNode.create(1075, 575).text(Text.create("Line loop", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1205, 440, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawShape(DrawMode.TRIANGLES, Color.ORANGE, new Vector2d(node.getX(), node.getY()), new Vector2d(node.getX() + 90D, node.getY() + 120D), new Vector2d(node.getX(), node.getY() + 120D), new Vector2d(node.getX() + 110D, node.getY()), new Vector2d(node.getX() + 200D, node.getY()), new Vector2d(node.getX() + 200D, node.getY() + 120D))))
		.attach(this);
		TextNode.create(1305, 575).text(Text.create("Triangles", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1435, 440, 200, 120)
		.self(node -> node.layer((mouseX, mouseY) -> GradientShader.use(new Vector2f(0F, 0F), new Vector2f(1F, 0F), Color.RED, Color.YELLOW, () -> DrawUtils.SHAPE.drawRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.WHITE), new Vector4f((float) node.getX(), (float) node.getY(), (float) (node.getX() + node.getWidth()), (float) (node.getY() + node.getHeight())))))
		.attach(this);
		TextNode.create(1535, 575).text(Text.create("Shader bound", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		TextNode.create(1765, 470).text(Text.create("Gradient", TextInfo.create(DemoFont.MONTSERRAT, 44, Color.RED.toGradient(Color.YELLOW, new Vector4f(0F, 0F, 1F, 0F))))).anchorX(Align.CENTER).attach(this);
		TextNode.create(1765, 575).text(Text.create("Gradient text", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(75, 660, 160, 80)
		.color(Color.RED)
		.self(rect -> rect.effect(TransformNodeEffect.create(new RotateTransformOperation(10D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
		.attach(this);
		TextNode.create(155, 775).text(Text.create("Rotated", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(305, 660, 160, 80)
		.color(Color.ORANGE.toGradient(Color.PINK))
		.self(rect -> rect.effect(TransformNodeEffect.create(new RotateTransformOperation(-8D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
		.attach(this);
		TextNode.create(385, 775).text(Text.create("Rotated gradient", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(535, 660, 160, 80)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.WHITE, 20F)))
		.self(node -> node.effect(TransformNodeEffect.create(new RotateTransformOperation(10D, Rotation.ROLL, Vector.create(() -> node.getX() + 80D, () -> node.getY() + 40D)))))
		.attach(this);
		TextNode.create(615, 775).text(Text.create("Rotated rounded", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(765, 660, 160, 80)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.CYAN, 20F, 3D)))
		.self(node -> node.effect(TransformNodeEffect.create(new RotateTransformOperation(-10D, Rotation.ROLL, Vector.create(() -> node.getX() + 80D, () -> node.getY() + 40D)))))
		.attach(this);
		TextNode.create(845, 775).text(Text.create("Rotated stroke", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(995, 660, 160, 80)
		.color(Color.BLUE)
		.self(rect -> rect.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawBorder(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), Color.WHITE, 3D)))
		.self(rect -> rect.effect(TransformNodeEffect.create(new RotateTransformOperation(6D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
		.attach(this);
		TextNode.create(1075, 775).text(Text.create("Rotated border", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1225, 660, 160, 80)
		.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawPolygon(Color.GREEN, new Vector2d(node.getX(), node.getY() + 80D), new Vector2d(node.getX() + 160D, node.getY() + 80D), new Vector2d(node.getX() + 110D, node.getY()), new Vector2d(node.getX() + 30D, node.getY()))))
		.self(node -> node.effect(TransformNodeEffect.create(new RotateTransformOperation(12D, Rotation.ROLL, Vector.create(() -> node.getX() + 80D, () -> node.getY() + 40D)))))
		.attach(this);
		TextNode.create(1305, 775).text(Text.create("Rotated polygon", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode
		.create(1475, 640, 120, 120)
		.resource(image)
		.self(node -> node.effect(TransformNodeEffect.create(new RotateTransformOperation(10D, Rotation.ROLL, Vector.create(() -> node.getX() + 60D, () -> node.getY() + 60D)))))
		.attach(this);
		TextNode.create(1535, 775).text(Text.create("Rotated image", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1685, 660, 160, 80)
		.self(node -> node.layer((mouseX, mouseY) -> GradientShader.use(new Vector2f(0F, 0F), new Vector2f(1F, 0F), Color.RED, Color.YELLOW, () -> DrawUtils.SHAPE.drawRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), Color.WHITE), new Vector4f((float) node.getX(), (float) node.getY(), (float) (node.getX() + node.getWidth()), (float) (node.getY() + node.getHeight())))))
		.self(node -> node.effect(TransformNodeEffect.create(new RotateTransformOperation(8D, Rotation.ROLL, Vector.create(() -> node.getX() + 80D, () -> node.getY() + 40D)))))
		.attach(this);
		TextNode.create(1765, 775).text(Text.create("Rotated shader", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(95, 840, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(155, 975).text(Text.create("Warm circle", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(325, 840, 120, 120).color(Color.CYAN.toGradient(Color.MAGENTA)).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(385, 975).text(Text.create("Cool circle", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(555, 840, 120, 120).color(Color.GREEN.toGradient(Color.BLUE, new Vector4f(0F, 0F, 0F, 1F))).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(615, 975).text(Text.create("Vertical circle", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(745, 840, 200, 120).color(Color.RED.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).attach(this);
		TextNode.create(845, 975).text(Text.create("Rounded gradient", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(975, 840, 200, 120).color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))).effect(RoundedNodeEffect.create(30F)).attach(this);
		TextNode.create(1075, 975).text(Text.create("Vertical rounded", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1205, 840, 200, 120).color(Color.ORANGE.toGradient(Color.PINK)).effect(RoundedNodeEffect.create(60F)).attach(this);
		TextNode.create(1305, 975).text(Text.create("Pill gradient", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		TextNode.create(1535, 870).text(Text.create("Vertical", TextInfo.create(DemoFont.MONTSERRAT, 44, Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))))).anchorX(Align.CENTER).attach(this);
		TextNode.create(1535, 975).text(Text.create("Vertical text", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		TextNode
		.create(1765, 870)
		.text(Text.create("Bordered", TextInfo.create(DemoFont.MONTSERRAT, 44, Color.RED.toGradient(Color.BLUE))))
		.effect(BorderNodeEffect.create(Color.BLUE.toGradient(Color.RED), 2F))
		.anchorX(Align.CENTER)
		.attach(this);
		TextNode.create(1765, 975).text(Text.create("Bordered text", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);
	}

}