package com.example.further;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.shader.pipeline.ShaderPipeline;
import dev.joid.lib.shader.pipeline.pass.BlurShaderPass;
import dev.joid.lib.shader.pipeline.pass.BorderShaderPass;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class ShadersUI extends UI {

	private static final Color INK  = new Color(153, 153, 153).lock();
	private static final Color DARK = new Color(85, 85, 85).lock();

	// #region tint-source
	private static final String TINT = "in vec4 vColor;\n"
	+ "uniform vec4 u_Tint;\n"
	+ "void main() { fragColor = vColor * u_Tint; }";
	// #endregion

	private IShader tint;

	@Override
	public void init() {
		// #region wave
		ContainerNode
		.create(80, 80, 400, 120)
		.layer((node, mouseX, mouseY) -> {
			final float time = BridgeHandler.CLOCK.get().currentTimeMillis() % 100000L / 1000F;
			final Tessellator tessellator = Tessellator.inst();
			WaveShader.use(time, () -> {
				tessellator.start(DrawMode.QUADS);
				tessellator.setColor(153, 153, 153);
				tessellator.addVertexWithUV(0D, node.h(), 0D, 0D, 1D);
				tessellator.addVertexWithUV(node.w(), node.h(), 0D, 1D, 1D);
				tessellator.addVertexWithUV(node.w(), 0D, 0D, 1D, 0D);
				tessellator.addVertexWithUV(0D, 0D, 0D, 0D, 0D);
				tessellator.draw();
			});
		})
		.attach(this);
		// #endregion

		// #region create
		ContainerNode
		.create(560, 80, 200, 120)
		.layer((node, mouseX, mouseY) -> {
			if (this.tint == null) {
				final ShaderSource vertex = ShaderSource.read(ShaderStage.VERTEX, "shaders/wave/wave.vsh");
				final ShaderSource fragment = ShaderSource.from(ShaderStage.FRAGMENT, ShadersUI.TINT);
				this.tint = BridgeHandler.RENDER.get().createShader(vertex, fragment, BlendState.NORMAL);
			}

			if (this.tint.isActive()) {
				BridgeHandler.RENDER.get().withState(render -> {
					// #region uniforms
					this.tint.uniform("u_Tint", 0.6F, 0.6F, 0.6F, 1F).bind();
					// #endregion
					DrawUtils.SHAPE.drawRect(0D, 0D, node.w(), node.h(), Color.WHITE);
					this.tint.unbind();
				});
			}
		})
		.attach(this);
		// #endregion

		// #region pipeline
		final BlurShaderPass blurX = BlurShaderPass.create(12F, true);
		final BlurShaderPass blurY = BlurShaderPass.create(12F, false);
		final BorderShaderPass border = BorderShaderPass.create(Color.WHITE, 4F);

		ContainerNode
		.create(80, 280, 400, 240)
		.layer((node, mouseX, mouseY) -> {
			final Runnable rect = () -> DrawUtils.SHAPE.drawRect(80D, 60D, 240D, 120D, ShadersUI.INK);
			ShaderPipeline.render(80D, 60D, 240D, 120D, rect, blurX, blurY, border);
		})
		.attach(this);
		// #endregion

		// #region custom-pass
		ContainerNode
		.create(560, 280, 400, 240)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawRect(20D, 60D, 160D, 120D, ShadersUI.DARK);
			final Runnable square = () -> DrawUtils.SHAPE.drawRect(220D, 60D, 160D, 120D, ShadersUI.DARK);
			ShaderPipeline.render(220D, 60D, 160D, 120D, square, InvertShaderPass.create(1F));
		})
		.attach(this);
		// #endregion
	}

}