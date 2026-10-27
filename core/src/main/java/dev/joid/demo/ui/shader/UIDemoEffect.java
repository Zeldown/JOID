package dev.joid.demo.ui.shader;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.MaskNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoEffect extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoEffect.INK);
		final Resource image = Resource.of("assets/demo/textures/image/placeholder.webp");

		RectNode.create(55, 40, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(20F)).attach(this);
		TextNode.create(155, 175).text(Text.create("Rounded", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 40, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(40F).top(false).right(false)).effect(ShadowNodeEffect.create(UIDemoEffect.INK, 16F)).attach(this);
		TextNode.create(385, 175).text(Text.create("Some corners", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(515, 40, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(6F)).attach(this);
		TextNode.create(615, 175).text(Text.create("Small radius", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(745, 40, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(60F)).attach(this);
		TextNode.create(845, 175).text(Text.create("Pill", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1015, 40, 120, 120).color(UIDemoEffect.INK).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(1075, 175).text(Text.create("Circle", caption)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1205, 40, 200, 120).resource(image).stretch(StretchType.COVER).effect(RoundedNodeEffect.create(20F)).attach(this);
		TextNode.create(1305, 175).text(Text.create("Rounded image", caption)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1475, 40, 120, 120).resource(image).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(1535, 175).text(Text.create("Circle image", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1665, 40, 200, 120)
		.color(UIDemoEffect.INK)
		.effect(RoundedNodeEffect.create(30F).scope(NodeEffectScope.CHILDREN))
		.body(rect -> {
			RectNode.create(-20, 60, 240, 80).color(UIDemoEffect.PLACEHOLDER).attach(rect);
		})
		.attach(this);
		TextNode.create(1765, 175).text(Text.create("Rounded subtree", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(55, 210, 200, 120).color(Color.GRAY).effect(ShadowNodeEffect.create(Color.WHITE.withAlpha(0.5F), 16F).offsetY(8D)).attach(this);
		TextNode.create(155, 345).text(Text.create("Shadow", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 210, 200, 120).color(Color.WHITE).effect(ShadowNodeEffect.create(UIDemoEffect.INK, 24F)).attach(this);
		TextNode.create(385, 345).text(Text.create("Glow", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(515, 210, 200, 120)
		.color(Color.WHITE)
		.effect(RoundedNodeEffect.create(20F))
		.effect(ShadowNodeEffect.create(UIDemoEffect.INK, 24F))
		.attach(this);
		TextNode.create(615, 345).text(Text.create("Rounded glow", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(785, 210, 120, 120)
		.color(UIDemoEffect.INK)
		.effect(CircleNodeEffect.create())
		.effect(ShadowNodeEffect.create(UIDemoEffect.INK, 24F))
		.attach(this);
		TextNode.create(845, 345).text(Text.create("Circle glow", caption)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(975, 210, 200, 120).resource(image).stretch(StretchType.COVER).effect(BlurNodeEffect.create(3F)).attach(this);
		TextNode.create(1075, 345).text(Text.create("Blur", caption)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1205, 210, 200, 120).resource(image).stretch(StretchType.COVER).effect(BlurNodeEffect.create(10F)).attach(this);
		TextNode.create(1305, 345).text(Text.create("Strong blur", caption)).anchorX(Align.CENTER).attach(this);

		TextNode.create(1535, 240).text(Text.create("Blurred", TextInfo.create(DemoFont.MONTSERRAT, 44).color(Color.WHITE))).effect(BlurNodeEffect.create(4F)).anchorX(Align.CENTER).attach(this);
		TextNode.create(1535, 345).text(Text.create("Blurred text", caption)).anchorX(Align.CENTER).attach(this);

		ResourceNode
		.create(1665, 210, 200, 120)
		.resource(image)
		.stretch(StretchType.COVER)
		.effect(RoundedNodeEffect.create(30F))
		.effect(BlurNodeEffect.create(6F))
		.attach(this);
		TextNode.create(1765, 345).text(Text.create("Rounded blur", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(55, 380, 200, 120).color(UIDemoEffect.INK).effect(BorderNodeEffect.create(Color.WHITE, 4F)).attach(this);
		TextNode.create(155, 515).text(Text.create("Border out", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 380, 200, 120).color(UIDemoEffect.INK).effect(BorderNodeEffect.create(Color.WHITE, 4F).mode(BorderMode.IN)).attach(this);
		TextNode.create(385, 515).text(Text.create("Border in", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(515, 380, 200, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(RoundedNodeEffect.create(20F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F))
		.attach(this);
		TextNode.create(615, 515).text(Text.create("Rounded out", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(745, 380, 200, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(RoundedNodeEffect.create(20F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F).mode(BorderMode.IN))
		.attach(this);
		TextNode.create(845, 515).text(Text.create("Rounded in", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1015, 380, 120, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(CircleNodeEffect.create())
		.effect(BorderNodeEffect.create(Color.WHITE, 5F))
		.attach(this);
		TextNode.create(1075, 515).text(Text.create("Circle out", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1245, 380, 120, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(CircleNodeEffect.create())
		.effect(BorderNodeEffect.create(Color.WHITE, 5F).mode(BorderMode.IN))
		.attach(this);
		TextNode.create(1305, 515).text(Text.create("Circle in", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1435, 380, 200, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(RoundedNodeEffect.create(20F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F))
		.body(rect -> {
			ResourceNode.create(50, 30, 100, 60).resource(image).attach(rect);
		})
		.attach(this);
		TextNode.create(1535, 515).text(Text.create("Out with child", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1665, 380, 200, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(RoundedNodeEffect.create(20F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F).mode(BorderMode.IN))
		.body(rect -> {
			ResourceNode.create(50, 30, 100, 60).resource(image).attach(rect);
		})
		.attach(this);
		TextNode.create(1765, 515).text(Text.create("In with child", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(55, 550, 200, 120).color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK)).effect(MaskNodeEffect.create().width(140D).height(80D)).attach(this);
		TextNode.create(155, 685).text(Text.create("Mask", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 550, 200, 120).color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK)).effect(MaskNodeEffect.create().x(50D).y(30D).width(100D).height(60D)).attach(this);
		TextNode.create(385, 685).text(Text.create("Offset mask", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(555, 550, 120, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(MaskNodeEffect.create().resource(Resource.of("assets/demo/textures/vector/mask.svg")))
		.attach(this);
		TextNode.create(615, 685).text(Text.create("Image mask", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(745, 550, 200, 120)
		.color(UIDemoEffect.INK)
		.effect(MaskNodeEffect.create())
		.body(rect -> {
			RectNode.create(-30, 70, 260, 80).color(UIDemoEffect.PLACEHOLDER).attach(rect);
		})
		.attach(this);
		TextNode.create(845, 685).text(Text.create("Masked children", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(995, 570, 160, 80)
		.color(UIDemoEffect.INK)
		.effect(TransformNodeEffect.create(Transformation.create().rotate(15D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(1075, 685).text(Text.create("Rotate", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1245, 570, 120, 80)
		.color(UIDemoEffect.INK)
		.effect(TransformNodeEffect.create(Transformation.create().scale(Scale.create(1.5D, 0.7D, 1D))))
		.attach(this);
		TextNode.create(1305, 685).text(Text.create("Scale", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1435, 550, 120, 80).color(UIDemoEffect.INK).effect(TransformNodeEffect.create(Transformation.create().translate(Vector.create(60D, 30D)))).attach(this);
		TextNode.create(1535, 685).text(Text.create("Translate", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1705, 570, 120, 80)
		.color(UIDemoEffect.INK)
		.effect(TransformNodeEffect.create(Transformation.create().rotate(-12D, Rotation.ROLL).scale(Scale.create(1.3D, 1.3D, 1D))))
		.attach(this);
		TextNode.create(1765, 685).text(Text.create("Transformation", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(95, 720, 120, 120)
		.color(UIDemoEffect.INK)
		.effect(CircleNodeEffect.create().scope(NodeEffectScope.SELF))
		.body(rect -> {
			RectNode.create(-40, 45, 200, 30).color(UIDemoEffect.PLACEHOLDER).attach(rect);
		})
		.attach(this);
		TextNode.create(155, 855).text(Text.create("Self scope", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(325, 720, 120, 120)
		.color(UIDemoEffect.INK)
		.effect(CircleNodeEffect.create().scope(NodeEffectScope.CHILDREN))
		.body(rect -> {
			RectNode.create(-40, 45, 200, 30).color(UIDemoEffect.PLACEHOLDER).attach(rect);
		})
		.attach(this);
		TextNode.create(385, 855).text(Text.create("Children scope", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(535, 740, 160, 80)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(MaskNodeEffect.create().priority(0))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(20D, Rotation.ROLL)).priority(1))
		.attach(this);
		TextNode.create(615, 855).text(Text.create("Mask first", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(765, 740, 160, 80)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(20D, Rotation.ROLL)).priority(0))
		.effect(MaskNodeEffect.create().priority(1))
		.attach(this);
		TextNode.create(845, 855).text(Text.create("Rotation first", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(975, 720, 200, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.effect(RoundedNodeEffect.create(24F))
		.effect(BorderNodeEffect.create(Color.WHITE, 3F))
		.effect(ShadowNodeEffect.create(UIDemoEffect.INK, 20F))
		.attach(this);
		TextNode.create(1075, 855).text(Text.create("Stacked effects", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1205, 720, 200, 120).color(UIDemoEffect.INK.withAlpha(0.3F)).effect(BorderNodeEffect.create(UIDemoEffect.INK, 3F)).attach(this);
		TextNode.create(1305, 855).text(Text.create("Alpha and border", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1435, 720, 200, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.hoveredColor(UIDemoEffect.INK.toGradient(UIDemoEffect.PLACEHOLDER))
		.effect(RoundedNodeEffect.create(20F))
		.attach(this);
		TextNode.create(1535, 855).text(Text.create("Hover gradient", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1705, 720, 120, 120)
		.color(UIDemoEffect.PLACEHOLDER.toGradient(UIDemoEffect.INK))
		.hoveredColor(UIDemoEffect.INK.toGradient(UIDemoEffect.PLACEHOLDER))
		.effect(CircleNodeEffect.create())
		.attach(this);
		TextNode.create(1765, 855).text(Text.create("Hover circle", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(55, 890, 200, 120).color(UIDemoEffect.INK).effect(BorderNodeEffect.create(Color.WHITE, 8F).corners(false)).attach(this);
		TextNode.create(155, 1025).text(Text.create("No corners", caption)).anchorX(Align.CENTER).attach(this);
	}

}