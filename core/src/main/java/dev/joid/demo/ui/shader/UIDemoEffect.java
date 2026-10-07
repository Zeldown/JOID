package dev.joid.demo.ui.shader;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.render.modifier.Rotation;
import dev.joid.lib.render.modifier.Scale;
import dev.joid.lib.render.modifier.Vector;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.operation.RotateOperation;
import dev.joid.lib.render.transform.operation.ScaleOperation;
import dev.joid.lib.render.transform.operation.TranslateOperation;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.MaskNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoEffect extends UIDemo {

	private static final Color INK = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoEffect.INK);
		final Resource image = Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"));

		RectNode.create(55, 40, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(20F)).attach(this);
		TextNode.create(155, 175).text(Text.create("Rounded", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 40, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(40F, true, false, false, true)).attach(this);
		TextNode.create(385, 175).text(Text.create("Some corners", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(515, 40, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(6F)).attach(this);
		TextNode.create(615, 175).text(Text.create("Small radius", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(745, 40, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(60F)).attach(this);
		TextNode.create(845, 175).text(Text.create("Pill", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1015, 40, 120, 120).color(Color.MAGENTA).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(1075, 175).text(Text.create("Circle", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1205, 40, 200, 120).resource(image).stretch(StretchType.COVER).effect(RoundedNodeEffect.create(20F)).attach(this);
		TextNode.create(1305, 175).text(Text.create("Rounded image", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1475, 40, 120, 120).resource(image).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(1535, 175).text(Text.create("Circle image", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1665, 40, 200, 120)
		.color(Color.BLUE)
		.effect(RoundedNodeEffect.create(30F).scope(NodeEffectScope.CHILDREN))
		.body(rect -> {
			RectNode.create(-20, 60, 240, 80).color(Color.ORANGE).attach(rect);
		})
		.attach(this);
		TextNode.create(1765, 175).text(Text.create("Rounded subtree", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(55, 240, 200, 120).color(Color.GRAY).effect(ShadowNodeEffect.create(Color.WHITE.copyAlpha(0.5F), 16F, 0D, 8D)).attach(this);
		TextNode.create(155, 375).text(Text.create("Shadow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 240, 200, 120).color(Color.WHITE).effect(ShadowNodeEffect.create(Color.CYAN, 24F)).attach(this);
		TextNode.create(385, 375).text(Text.create("Glow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(515, 240, 200, 120)
		.color(Color.WHITE)
		.effect(RoundedNodeEffect.create(20F))
		.effect(ShadowNodeEffect.create(Color.CYAN, 24F))
		.attach(this);
		TextNode.create(615, 375).text(Text.create("Rounded glow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(785, 240, 120, 120)
		.color(Color.MAGENTA)
		.effect(CircleNodeEffect.create())
		.effect(ShadowNodeEffect.create(Color.MAGENTA, 24F))
		.attach(this);
		TextNode.create(845, 375).text(Text.create("Circle glow", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(975, 240, 200, 120).resource(image).stretch(StretchType.COVER).effect(BlurNodeEffect.create(3F)).attach(this);
		TextNode.create(1075, 375).text(Text.create("Blur", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1205, 240, 200, 120).resource(image).stretch(StretchType.COVER).effect(BlurNodeEffect.create(10F)).attach(this);
		TextNode.create(1305, 375).text(Text.create("Strong blur", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		TextNode.create(1535, 270).text(Text.create("Blurred", TextInfo.create(DemoFont.MONTSERRAT, 44, Color.WHITE))).effect(BlurNodeEffect.create(4F)).anchorX(Align.CENTER).attach(this);
		TextNode.create(1535, 375).text(Text.create("Blurred text", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode
		.create(1665, 240, 200, 120)
		.resource(image)
		.stretch(StretchType.COVER)
		.effect(RoundedNodeEffect.create(30F))
		.effect(BlurNodeEffect.create(6F))
		.attach(this);
		TextNode.create(1765, 375).text(Text.create("Rounded blur", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(55, 440, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.OUT)).attach(this);
		TextNode.create(155, 575).text(Text.create("Border out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 440, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.IN)).attach(this);
		TextNode.create(385, 575).text(Text.create("Border in", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(515, 440, 200, 120)
		.color(Color.GREEN.toGradient(Color.BLUE))
		.effect(RoundedNodeEffect.create(20F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.OUT))
		.attach(this);
		TextNode.create(615, 575).text(Text.create("Rounded out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(745, 440, 200, 120)
		.color(Color.GREEN.toGradient(Color.BLUE))
		.effect(RoundedNodeEffect.create(20F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.IN))
		.attach(this);
		TextNode.create(845, 575).text(Text.create("Rounded in", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1015, 440, 120, 120)
		.color(Color.RED.toGradient(Color.YELLOW))
		.effect(CircleNodeEffect.create())
		.effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.OUT))
		.attach(this);
		TextNode.create(1075, 575).text(Text.create("Circle out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1245, 440, 120, 120)
		.color(Color.RED.toGradient(Color.YELLOW))
		.effect(CircleNodeEffect.create())
		.effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.IN))
		.attach(this);
		TextNode.create(1305, 575).text(Text.create("Circle in", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1435, 440, 200, 120)
		.color(Color.GREEN.toGradient(Color.BLUE))
		.effect(RoundedNodeEffect.create(20F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.OUT))
		.body(rect -> {
			ResourceNode.create(50, 30, 100, 60).resource(image).attach(rect);
		})
		.attach(this);
		TextNode.create(1535, 575).text(Text.create("Out with child", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1665, 440, 200, 120)
		.color(Color.GREEN.toGradient(Color.BLUE))
		.effect(RoundedNodeEffect.create(20F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.IN))
		.body(rect -> {
			ResourceNode.create(50, 30, 100, 60).resource(image).attach(rect);
		})
		.attach(this);
		TextNode.create(1765, 575).text(Text.create("In with child", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(55, 640, 200, 120).color(Color.RED.toGradient(Color.BLUE)).effect(MaskNodeEffect.create(140D, 80D)).attach(this);
		TextNode.create(155, 775).text(Text.create("Mask", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 640, 200, 120).color(Color.RED.toGradient(Color.BLUE)).effect(MaskNodeEffect.create(50D, 30D, 100D, 60D)).attach(this);
		TextNode.create(385, 775).text(Text.create("Offset mask", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(555, 640, 120, 120)
		.color(Color.ORANGE.toGradient(Color.MAGENTA))
		.self(rect -> rect.effect(MaskNodeEffect.create(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/mask.svg")), rect)))
		.attach(this);
		TextNode.create(615, 775).text(Text.create("Image mask", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(745, 640, 200, 120)
		.color(Color.BLUE)
		.self(rect -> rect.effect(MaskNodeEffect.create(rect)))
		.body(rect -> {
			RectNode.create(-30, 70, 260, 80).color(Color.ORANGE).attach(rect);
		})
		.attach(this);
		TextNode.create(845, 775).text(Text.create("Masked children", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(995, 660, 160, 80)
		.color(Color.RED)
		.self(rect -> rect.effect(TransformNodeEffect.create(new RotateOperation(15D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
		.attach(this);
		TextNode.create(1075, 775).text(Text.create("Rotate", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1245, 660, 120, 80)
		.color(Color.RED)
		.self(rect -> rect.effect(TransformNodeEffect.create(new ScaleOperation(Scale.create(1.5D, 0.7D, 1D), Vector.create(() -> rect.getX() + 60D, () -> rect.getY() + 40D)))))
		.attach(this);
		TextNode.create(1305, 775).text(Text.create("Scale", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1435, 640, 120, 80).color(Color.RED).effect(TransformNodeEffect.create(new TranslateOperation(Vector.create(60D, 30D)))).attach(this);
		TextNode.create(1535, 775).text(Text.create("Translate", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1705, 660, 120, 80)
		.color(Color.RED)
		.self(rect -> rect.effect(TransformNodeEffect.create(Transformation.create().rotate(-12D, Rotation.ROLL, Vector.create(() -> rect.getX() + 60D, () -> rect.getY() + 40D)).scale(Scale.create(1.3D, 1.3D, 1D), Vector.create(() -> rect.getX() + 60D, () -> rect.getY() + 40D)))))
		.attach(this);
		TextNode.create(1765, 775).text(Text.create("Transformation", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(95, 840, 120, 120)
		.color(Color.RED)
		.effect(CircleNodeEffect.create().scope(NodeEffectScope.SELF))
		.body(rect -> {
			RectNode.create(-40, 45, 200, 30).color(Color.ORANGE).attach(rect);
		})
		.attach(this);
		TextNode.create(155, 975).text(Text.create("Self scope", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(325, 840, 120, 120)
		.color(Color.RED)
		.effect(CircleNodeEffect.create().scope(NodeEffectScope.CHILDREN))
		.body(rect -> {
			RectNode.create(-40, 45, 200, 30).color(Color.ORANGE).attach(rect);
		})
		.attach(this);
		TextNode.create(385, 975).text(Text.create("Children scope", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(535, 860, 160, 80)
		.color(Color.RED.toGradient(Color.BLUE))
		.self(rect -> rect.effect(MaskNodeEffect.create(rect).priority(0)))
		.self(rect -> rect.effect(TransformNodeEffect.create(new RotateOperation(20D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D))).priority(1)))
		.attach(this);
		TextNode.create(615, 975).text(Text.create("Mask first", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(765, 860, 160, 80)
		.color(Color.RED.toGradient(Color.BLUE))
		.self(rect -> rect.effect(TransformNodeEffect.create(new RotateOperation(20D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D))).priority(0)))
		.self(rect -> rect.effect(MaskNodeEffect.create(rect).priority(1)))
		.attach(this);
		TextNode.create(845, 975).text(Text.create("Rotation first", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(975, 840, 200, 120)
		.color(Color.BLUE.toGradient(Color.CYAN))
		.effect(RoundedNodeEffect.create(24F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.OUT))
		.effect(ShadowNodeEffect.create(Color.CYAN, 20F))
		.attach(this);
		TextNode.create(1075, 975).text(Text.create("Stacked effects", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1205, 840, 200, 120).color(Color.RED.copyAlpha(0.3F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(this);
		TextNode.create(1305, 975).text(Text.create("Alpha and border", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1435, 840, 200, 120)
		.color(Color.RED.toGradient(Color.BLUE))
		.hoveredColor(Color.GREEN.toGradient(Color.YELLOW))
		.effect(RoundedNodeEffect.create(20F))
		.attach(this);
		TextNode.create(1535, 975).text(Text.create("Hover gradient", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1705, 840, 120, 120)
		.color(Color.RED.toGradient(Color.YELLOW))
		.hoveredColor(Color.BLUE.toGradient(Color.GREEN))
		.effect(CircleNodeEffect.create())
		.attach(this);
		TextNode.create(1765, 975).text(Text.create("Hover circle", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);
	}

}