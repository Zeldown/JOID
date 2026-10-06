package dev.joid.lib.shader.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.DrawMode;
import dev.joid.lib.render.framebuffer.FrameBuffer;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import dev.joid.lib.ui.node.Node;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderPipeline {

	private static final Map<Long, FrameBuffer[]> FBO_POOL = new HashMap<>();
	private static int pipelineDepth = 0;

	public static void render(final @NonNull Node node, final @NonNull Runnable baseDraw, final @NonNull ShaderPass... passes) {
		ShaderPipeline.render(node, new ArrayList<>(Arrays.asList(passes)), baseDraw);
	}

	public static void render(final @NonNull Node node, final @NonNull List<ShaderPass> passes, final @NonNull Runnable baseDraw) {
		ShaderPipeline.render(node.getX(), node.getY(), node.getWidth(), node.getHeight(), passes, baseDraw);
	}

	public static void render(final double x, final double y, final double width, final double height, final @NonNull Runnable baseDraw, final @NonNull ShaderPass... passes) {
		ShaderPipeline.render(x, y, width, height, new ArrayList<>(Arrays.asList(passes)), baseDraw);
	}

	public static void render(final double x, final double y, final double width, final double height, final @NonNull List<ShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (passes.isEmpty()) {
			baseDraw.run();
			return;
		}

		ShaderPipeline.pipelineDepth++;
		try {
			final List<ShaderPass> sorted = new ArrayList<>(passes);
			sorted.sort(Comparator.comparingInt(ShaderPass::priority));

			final IRenderBridge render = BridgeHandler.RENDER.get();
			final PixelGrid grid = render.getPixelGrid();
			if (sorted.size() == 1 && ShaderPipeline.pipelineDepth == 1 && sorted.get(0).expansion() == 0F && sorted.get(0).supportsDirectBind()) {
				final IShader previousShader = render.getShader();
				sorted.get(0).bindDirect(ShaderPassContext.create(x, y, width, height, 0D, grid));
				try {
					baseDraw.run();
				} finally {
					sorted.get(0).unbind();
					render.shader(previousShader);
				}
				return;
			}

			float expansion = 0F;
			for (final ShaderPass pass : sorted) {
				expansion = Math.max(expansion, pass.expansion());
			}
			ShaderPipeline.renderMultiPass(ShaderPassContext.create(x, y, width, height, expansion, grid), sorted, baseDraw);
		} finally {
			ShaderPipeline.pipelineDepth--;
		}
	}

	public static void cleanup() {
		for (final FrameBuffer[] fbos : ShaderPipeline.FBO_POOL.values()) {
			fbos[0].delete();
			fbos[1].delete();
		}
		ShaderPipeline.FBO_POOL.clear();
	}

	private static void drawTexturedQuad(final @NonNull FrameBuffer frameBuffer, final double x, final double y, final double w, final double h) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.blend(BlendState.PREMULTIPLIED);
		render.texture(frameBuffer.getHandle().getTexture(), TextureFilter.LINEAR, TextureWrap.CLAMP_TO_BORDER);
		render.color(1F, 1F, 1F, 1F);

		final Tessellator tess = Tessellator.inst();
		tess.start(DrawMode.QUADS);
		tess.addVertexWithUV(x, y + h, 0D, 0D, 0D);
		tess.addVertexWithUV(x + w, y + h, 0D, 1D, 0D);
		tess.addVertexWithUV(x + w, y, 0D, 1D, 1D);
		tess.addVertexWithUV(x, y, 0D, 0D, 1D);
		tess.draw();

		render.blend(BlendState.DISABLED);
		render.resetTexture();
	}

	private static void renderMultiPass(final @NonNull ShaderPassContext context, final @NonNull List<ShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (context.getWidth() <= 0D || context.getHeight() <= 0D) {
			baseDraw.run();
			return;
		}

		final FrameBuffer[] fbos = ShaderPipeline.getOrCreateFBOs(context.getTextureWidth(), context.getTextureHeight());
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		try {
			ShaderPipeline.drawInto(fbos[0], context, () -> {
				render.blend(BlendState.NORMAL);
				baseDraw.run();
			});

			FrameBuffer src = fbos[0];
			FrameBuffer dst = fbos[1];
			for (int i = 0; i < passes.size() - 1; i++) {
				final ShaderPass pass = passes.get(i);
				final FrameBuffer source = src;
				ShaderPipeline.drawInto(dst, context, () -> ShaderPipeline.drawPass(pass, context, source));
				src = dst;
				dst = source;
			}

			ShaderPipeline.drawPass(passes.get(passes.size() - 1), context, src);
		} finally {
			render.popState();
		}
	}

	private static void drawInto(final @NonNull FrameBuffer target, final @NonNull ShaderPassContext context, final @NonNull Runnable draw) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		render.pushProjection();
		render.pushMatrix();
		try {
			target.bind();
			render.viewport(0, 0, context.getTextureWidth(), context.getTextureHeight());
			render.clear(0F, 0F, 0F, 0F);
			render.ortho(context.getRegionX(), context.getRegionX() + context.getRegionWidth(), context.getRegionY() + context.getRegionHeight(), context.getRegionY(), -1000D, 1000D);
			render.loadIdentity();
			draw.run();
		} finally {
			render.popMatrix();
			render.popProjection();
			render.popState();
		}
	}

	private static void drawPass(final @NonNull ShaderPass pass, final @NonNull ShaderPassContext context, final @NonNull FrameBuffer source) {
		pass.bindForTexture(context);
		try {
			ShaderPipeline.drawTexturedQuad(source, context.getRegionX(), context.getRegionY(), context.getRegionWidth(), context.getRegionHeight());
		} finally {
			pass.unbind();
		}
	}

	private static FrameBuffer[] getOrCreateFBOs(final int width, final int height) {
		final long key = (long) ShaderPipeline.pipelineDepth << 32 | (long) width << 16 | height;
		FrameBuffer[] fbos = ShaderPipeline.FBO_POOL.get(key);
		if (fbos != null) {
			return fbos;
		}

		fbos = new FrameBuffer[] {FrameBuffer.create(width, height, TextureFilter.LINEAR), FrameBuffer.create(width, height, TextureFilter.LINEAR)};
		ShaderPipeline.FBO_POOL.put(key, fbos);
		return fbos;
	}

}