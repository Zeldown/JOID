package be.zeldown.joid.demo.ui.shader;

import javax.vecmath.Vector4f;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.color.ColorGradient;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.shader.impl.BorderShader.BorderMode;
import be.zeldown.joid.lib.shader.pipeline.ShaderPipeline;
import be.zeldown.joid.lib.shader.pipeline.pass.GradientShaderPass;
import be.zeldown.joid.lib.shader.pipeline.pass.RoundedShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.GradientNodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;

public class UIDemoShader extends UIDemo {

	private Node lastRow;

	@Override
	public void init() {
		FlexNode.vertical(40, 40, 1840).margin(15).body(flex -> {
			FlexNode.horizontal(0, 0, 120).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.BLUE.toGradient(Color.GREEN)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.MAGENTA).effect(CircleNodeEffect.create()).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 120).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))).effect(RoundedNodeEffect.create(30F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE.toGradient(Color.PINK)).effect(RoundedNodeEffect.create(60F)).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 120).margin(20).body(node -> {
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.CYAN.toGradient(Color.MAGENTA)).effect(CircleNodeEffect.create()).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.GREEN.toGradient(Color.BLUE, new Vector4f(0F, 0F, 0F, 1F))).effect(CircleNodeEffect.create()).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 130).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE)).border(Color.WHITE, 3).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE.toGradient(Color.PINK)).border(Color.WHITE, 2).effect(RoundedNodeEffect.create(40F)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.CYAN.toGradient(Color.MAGENTA)).border(Color.WHITE, 3).effect(CircleNodeEffect.create()).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED).border(Color.CYAN, 3).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.BLUE).border(Color.WHITE, 3, false).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 130).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.GREEN.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.OUT)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.GREEN.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.IN)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.OUT)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.IN)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.OUT)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.IN)).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 60).margin(20).body(node -> {
				TextNode.create(0, 0).text(Text.create("Gradient Text", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.WHITE))).effect(GradientNodeEffect.create(Color.RED, Color.BLUE)).effect(BorderNodeEffect.create(Color.BLUE.toGradient(Color.RED), 1F)).attach(node);
				TextNode.create(0, 0).text(Text.create("Rainbow", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.WHITE))).effect(GradientNodeEffect.create(Color.RED, Color.YELLOW, new Vector4f(0F, 0F, 1F, 0F))).attach(node);
				TextNode.create(0, 0).text(Text.create("Vertical", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.WHITE))).effect(GradientNodeEffect.create(Color.CYAN, Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))).attach(node);
			}).attach(flex);
			this.lastRow = FlexNode.horizontal(0, 0, 120).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE), Color.GREEN.toGradient(Color.YELLOW)).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE), Color.CYAN).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE, Color.CYAN.toGradient(Color.MAGENTA)).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED, Color.GREEN).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW), Color.BLUE.toGradient(Color.GREEN)).effect(CircleNodeEffect.create()).attach(node);
			}).attach(flex);
		}).attach(this);
	}

	@Override
	public void postDraw(final double mouseX, final double mouseY) {
		if (this.lastRow == null) {
			return;
		}

		final double x = 40D;
		final double y = this.lastRow.getAbsoluteY() + this.lastRow.getHeight() + 15D;
		final double w = 200D;
		final double h = 120D;

		final float r = 20F;
		final Vector4f canvas = new Vector4f((float) x, (float) y, (float) (x + w), (float) (y + h));
		ShaderPipeline.render(x, y, w, h, () -> {
			Color.WHITE.bind();
			DrawUtils.SHAPE.drawRawRect(x, y, w, h);
			Color.reset();
		}, new GradientShaderPass(new ColorGradient(Color.RED, Color.CYAN, new Vector4f(0F, 0F, 1F, 0F)), canvas), new RoundedShaderPass(r, (float) (x + r), (float) (y + r), (float) (x + w - r), (float) (y + h - r)));
	}

}