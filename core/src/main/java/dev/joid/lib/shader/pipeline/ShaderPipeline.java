package dev.joid.lib.shader.pipeline;

import java.util.Arrays;
import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.render.framebuffer.FrameBufferPool;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderPipeline {

	private static int pipelineDepth = 0;

	public static void render(final double x, final double y, final double width, final double height, final @NonNull Runnable baseDraw, final @NonNull IShaderPass... passes) {
		ShaderPipeline.render(x, y, width, height, Arrays.asList(passes), baseDraw);
	}

	public static void render(final double x, final double y, final double width, final double height, final @NonNull List<IShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (passes.isEmpty()) {
			baseDraw.run();
			return;
		}

		ShaderPipeline.pipelineDepth++;
		try {
			float expansion = 0F;
			for (final IShaderPass pass : passes) {
				expansion = Math.max(expansion, pass.expansion());
			}
			ShaderPipeline.renderMultiPass(ShaderPassContext.create(x, y, width, height, expansion, BridgeHandler.RENDER.get().getPixelGrid()), passes, baseDraw);
		} finally {
			ShaderPipeline.pipelineDepth--;
		}
	}

	private static void drawTexturedQuad(final @NonNull IFrameBuffer frameBuffer, final @NonNull ShaderPassContext context) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final boolean aligned = render.getPixelGrid().isAligned();
		render.getState().blend(BlendState.PREMULTIPLIED);
		render.getState().texture(frameBuffer.getTexture()).textureFilter(TextureFilter.LINEAR).textureWrap(aligned ? TextureWrap.CLAMP_TO_BORDER : TextureWrap.CLAMP_TO_EDGE);
		render.getState().color(1F, 1F, 1F, 1F);

		final double growU = aligned ? 0D : 1D / context.getTextureWidth();
		final double growV = aligned ? 0D : 1D / context.getTextureHeight();
		final double scaleU = (double) context.getTextureWidth() / context.getFrameBufferWidth();
		final double scaleV = (double) context.getTextureHeight() / context.getFrameBufferHeight();
		final double x = context.getRegionX() - context.getRegionWidth() * growU;
		final double y = context.getRegionY() - context.getRegionHeight() * growV;
		final double w = context.getRegionWidth() * (1D + growU * 2D);
		final double h = context.getRegionHeight() * (1D + growV * 2D);
		final Tessellator tess = Tessellator.inst();
		tess.start(DrawMode.QUADS);
		tess.addVertexWithUV(x, y + h, 0D, -growU * scaleU, -growV * scaleV);
		tess.addVertexWithUV(x + w, y + h, 0D, (1D + growU) * scaleU, -growV * scaleV);
		tess.addVertexWithUV(x + w, y, 0D, (1D + growU) * scaleU, (1D + growV) * scaleV);
		tess.addVertexWithUV(x, y, 0D, -growU * scaleU, (1D + growV) * scaleV);
		tess.draw();
	}

	private static void renderMultiPass(final @NonNull ShaderPassContext area, final @NonNull List<IShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (area.getWidth() <= 0D || area.getHeight() <= 0D) {
			baseDraw.run();
			return;
		}

		final IFrameBuffer[] fbos = FrameBufferPool.allocate(ShaderPipeline.pipelineDepth, 2, area.getTextureWidth(), area.getTextureHeight());
		final ShaderPassContext context = area.withFrameBuffer(fbos[0].getWidth(), fbos[0].getHeight());
		BridgeHandler.RENDER.get().withState(render -> {
			ShaderPipeline.drawInto(fbos[0], context, () -> {
				render.getState().blend(BlendState.NORMAL);
				baseDraw.run();
			});

			IFrameBuffer src = fbos[0];
			IFrameBuffer dst = fbos[1];
			for (int i = 0; i < passes.size() - 1; i++) {
				final IShaderPass pass = passes.get(i);
				final IFrameBuffer source = src;
				ShaderPipeline.drawInto(dst, context, () -> ShaderPipeline.drawPass(pass, context, source));
				src = dst;
				dst = source;
			}

			ShaderPipeline.drawPass(passes.get(passes.size() - 1), context, src);
		});
	}

	private static void drawInto(final @NonNull IFrameBuffer target, final @NonNull ShaderPassContext context, final @NonNull Runnable draw) {
		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().frameBuffer(target).viewport(0, 0, context.getTextureWidth(), context.getTextureHeight());
			render.getProjection().with(projection -> render.getModelView().with(modelView -> {
				projection.ortho(context.getRegionX(), context.getRegionX() + context.getRegionWidth(), context.getRegionY() + context.getRegionHeight(), context.getRegionY(), -1000D, 1000D);
				modelView.identity();
				render.clearColor(0F, 0F, 0F, 0F);
				draw.run();
			}));
		});
	}

	private static void drawPass(final @NonNull IShaderPass pass, final @NonNull ShaderPassContext context, final @NonNull IFrameBuffer source) {
		pass.bind(context);
		try {
			ShaderPipeline.drawTexturedQuad(source, context);
		} finally {
			pass.unbind();
		}
	}

}