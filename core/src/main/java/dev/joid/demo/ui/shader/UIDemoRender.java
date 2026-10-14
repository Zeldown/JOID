package dev.joid.demo.ui.shader;

import dev.joid.demo.DemoFont;
import dev.joid.demo.shader.impl.DemoInvertShader;
import dev.joid.demo.shader.pipeline.pass.DemoInvertShaderPass;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.DemoFrameBufferNode;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.BlendState.Equation;
import dev.joid.lib.bridge.render.state.BlendState.Factor;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.shader.pipeline.ShaderPipeline;
import dev.joid.lib.shader.pipeline.pass.BlurShaderPass;
import dev.joid.lib.shader.pipeline.pass.BorderShaderPass;
import dev.joid.lib.shader.pipeline.pass.RoundedShaderPass;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.point.Point;

@UIData(background = true)
public class UIDemoRender extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private static final BlendState MULTIPLY = BlendState.create(Equation.ADD, Factor.DST_COLOR, Factor.ZERO);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoRender.INK);
		final TextInfo large = TextInfo.create(DemoFont.MONTSERRAT, 40, UIDemoRender.INK);
		final Resource image = Resource.of("assets/demo/textures/image/placeholder.webp");

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> {
				final Tessellator tessellator = Tessellator.inst();
				BridgeHandler.RENDER.get().withState(render -> {
					render.getState().blend(BlendState.NORMAL).texture(null);
					tessellator.start(DrawMode.TRIANGLES);
					tessellator.setColor(255, 255, 255);
					tessellator.addVertex(node.getX() + 100D, node.getY(), 0D);
					tessellator.setColor(153, 153, 153);
					tessellator.addVertex(node.getX() + 200D, node.getY() + 200D, 0D);
					tessellator.setColor(102, 102, 102);
					tessellator.addVertex(node.getX(), node.getY() + 200D, 0D);
					tessellator.draw();
				});
			}))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Tessellator", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> {
				BridgeHandler.RENDER.get().getModelView().with(matrix -> {
					matrix.translate(node.getX() + 100D, node.getY() + 100D, 0D);
					matrix.rotate(30D, 0D, 0D, 1D);
					DrawUtils.SHAPE.drawRect(-60D, -40D, 120D, 80D, UIDemoRender.INK);
				});
			}))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Matrix stack", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> {
				final Tessellator tessellator = Tessellator.inst();
				BridgeHandler.RENDER.get().withState(render -> {
					render.getState().blend(UIDemoRender.MULTIPLY).texture(null).color(0.6F, 0.6F, 0.6F, 1F);
					tessellator.start(DrawMode.QUADS);
					tessellator.addVertex(node.getX() + 20D, node.getY() + 140D, 0D);
					tessellator.addVertex(node.getX() + 140D, node.getY() + 140D, 0D);
					tessellator.addVertex(node.getX() + 140D, node.getY() + 20D, 0D);
					tessellator.addVertex(node.getX() + 20D, node.getY() + 20D, 0D);
					tessellator.addVertex(node.getX() + 60D, node.getY() + 180D, 0D);
					tessellator.addVertex(node.getX() + 180D, node.getY() + 180D, 0D);
					tessellator.addVertex(node.getX() + 180D, node.getY() + 60D, 0D);
					tessellator.addVertex(node.getX() + 60D, node.getY() + 60D, 0D);
					tessellator.draw();
				});
			}))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Multiply blend", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> {
				BridgeHandler.RENDER.get().withState(render -> {
					render.getState().color(0.6F, 0.6F, 0.6F, 1F);
					DrawUtils.SHAPE.drawRawRect(node.getX() + 40D, node.getY() + 40D, 120D, 120D);
				});
			}))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Raw rect", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.TEXT.drawText(node.getX() + 100D, node.getY() + 100D, "Drawn", large, Align.CENTER, Align.CENTER)))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Draw text", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawDashedLine(UIDemoRender.INK, 12, 4F, Point.create(node.getX(), node.getY() + 160D), Point.create(node.getX() + 100D, node.getY() + 40D), Point.create(node.getX() + 200D, node.getY() + 160D))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Dashed line", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> {
				DrawUtils.SHAPE.drawShadow(node.getX() + 20D, node.getY() + 50D, 160D, 100D, UIDemoRender.INK, 16F, 20F);
				DrawUtils.SHAPE.drawRoundedRect(node.getX() + 20D, node.getY() + 50D, 160D, 100D, Color.WHITE, 16F);
			}))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Shadow", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.RESOURCE.drawResource(node.getX(), node.getY(), 200D, 200D, image)))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Draw resource", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			DemoFrameBufferNode.create(60, 68, 200, 125).attach(rect);
			TextNode.create(160, 275).text(Text.create("Frame buffer", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> ShaderPipeline.render(node.getX(), node.getY(), node.getWidth(), node.getHeight(), () -> DrawUtils.RESOURCE.drawResource(node.getX(), node.getY(), 200D, 200D, image), new RoundedShaderPass(40F, (float) node.getX() + 40F, (float) node.getY() + 40F, (float) node.getX() + 160F, (float) node.getY() + 160F), new BorderShaderPass(4F, UIDemoRender.INK))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Pipeline", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> ShaderPipeline.render(node.getX(), node.getY(), node.getWidth(), node.getHeight(), () -> DrawUtils.RESOURCE.drawResource(node.getX(), node.getY(), 200D, 200D, image), new BlurShaderPass(12F, true))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Horizontal blur", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> ShaderPipeline.render(node.getX(), node.getY(), node.getWidth(), node.getHeight(), () -> DrawUtils.RESOURCE.drawResource(node.getX(), node.getY(), 200D, 200D, image), new DemoInvertShaderPass(1F))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Custom pass", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> {
				if (!DemoInvertShader.inst().canDraw()) {
					return;
				}

				final IShader previous = BridgeHandler.RENDER.get().getState().getShader();
				DemoInvertShader.inst().bind(1F);
				try {
					DrawUtils.RESOURCE.drawResource(node.getX(), node.getY(), 200D, 200D, image);
				} finally {
					DemoInvertShader.inst().unbind();
					BridgeHandler.RENDER.get().getState().shader(previous);
				}
			}))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Custom shader", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(UIDemoRender.INK, 14F, Point.create(node.getX() + 10D, node.getY() + 180D), Point.create(node.getX() + 100D, node.getY() + 20D), Point.create(node.getX() + 60D, node.getY() + 180D), Point.create(node.getX() + 190D, node.getY() + 120D))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Line joins", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoRender.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(60, 30, 200, 200)
			.self(node -> node.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawCurvedLine(Color.WHITE.toGradient(UIDemoRender.INK), 10F, Point.create(node.getX() + 10D, node.getY() + 180D), Point.create(node.getX() + 100D, node.getY() - 100D), Point.create(node.getX() + 190D, node.getY() + 180D))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Gradient curve", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}