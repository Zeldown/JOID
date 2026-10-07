package dev.joid.lib.shader.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.DrawMode;
import dev.joid.lib.render.framebuffer.FrameBuffer;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import dev.joid.lib.ui.node.Node;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderPipeline {

	private static final Map<List<Integer>, PooledFrameBuffers> FBO_POOL = new HashMap<>();
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

			float expansion = 0F;
			for (final ShaderPass pass : sorted) {
				expansion = Math.max(expansion, pass.expansion());
			}
			ShaderPipeline.renderMultiPass(ShaderPassContext.create(x, y, width, height, expansion, BridgeHandler.RENDER.get().getPixelGrid()), sorted, baseDraw);
		} finally {
			ShaderPipeline.pipelineDepth--;
		}
	}

	public static void cleanup() {
		for (final PooledFrameBuffers pooled : ShaderPipeline.FBO_POOL.values()) {
			pooled.delete();
		}
		ShaderPipeline.FBO_POOL.clear();
	}

	public static void releaseUnused() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final Iterator<PooledFrameBuffers> iterator = ShaderPipeline.FBO_POOL.values().iterator();
		while (iterator.hasNext()) {
			final PooledFrameBuffers pooled = iterator.next();
			if (now - pooled.lastUse >= 5000L) {
				pooled.delete();
				iterator.remove();
			}
		}
	}

	private static void drawTexturedQuad(final @NonNull FrameBuffer frameBuffer, final @NonNull ShaderPassContext context) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final boolean aligned = render.getPixelGrid().isAligned();
		render.blend(BlendState.PREMULTIPLIED);
		render.texture(frameBuffer.getHandle().getTexture(), TextureFilter.LINEAR, aligned ? TextureWrap.CLAMP_TO_BORDER : TextureWrap.CLAMP_TO_EDGE);
		render.color(1F, 1F, 1F, 1F);

		final double growU = aligned ? 0D : 1D / context.getTextureWidth();
		final double growV = aligned ? 0D : 1D / context.getTextureHeight();
		final double x = context.getRegionX() - context.getRegionWidth() * growU;
		final double y = context.getRegionY() - context.getRegionHeight() * growV;
		final double w = context.getRegionWidth() * (1D + growU * 2D);
		final double h = context.getRegionHeight() * (1D + growV * 2D);
		final Tessellator tess = Tessellator.inst();
		tess.start(DrawMode.QUADS);
		tess.addVertexWithUV(x, y + h, 0D, -growU, -growV);
		tess.addVertexWithUV(x + w, y + h, 0D, 1D + growU, -growV);
		tess.addVertexWithUV(x + w, y, 0D, 1D + growU, 1D + growV);
		tess.addVertexWithUV(x, y, 0D, -growU, 1D + growV);
		tess.draw();

		render.blend(BlendState.DISABLED);
		render.resetTexture();
	}

	private static void renderMultiPass(final @NonNull ShaderPassContext context, final @NonNull List<ShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (context.getWidth() <= 0D || context.getHeight() <= 0D) {
			baseDraw.run();
			return;
		}

		final FrameBuffer[] fbos = ShaderPipeline.getOrCreateFBOs(context.getTextureWidth(), context.getTextureHeight()).frameBuffers;
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
		render.pushProjection();
		render.pushMatrix();
		try {
			target.fill(() -> {
				render.viewport(0, 0, context.getTextureWidth(), context.getTextureHeight());
				render.clear(0F, 0F, 0F, 0F);
				render.ortho(context.getRegionX(), context.getRegionX() + context.getRegionWidth(), context.getRegionY() + context.getRegionHeight(), context.getRegionY(), -1000D, 1000D);
				render.loadIdentity();
				draw.run();
			});
		} finally {
			render.popMatrix();
			render.popProjection();
		}
	}

	private static void drawPass(final @NonNull ShaderPass pass, final @NonNull ShaderPassContext context, final @NonNull FrameBuffer source) {
		pass.bindForTexture(context);
		try {
			ShaderPipeline.drawTexturedQuad(source, context);
		} finally {
			pass.unbind();
		}
	}

	private static PooledFrameBuffers getOrCreateFBOs(final int width, final int height) {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final List<Integer> key = Arrays.asList(ShaderPipeline.pipelineDepth, width, height);
		final PooledFrameBuffers pooled = ShaderPipeline.FBO_POOL.get(key);
		if (pooled != null) {
			pooled.lastUse = now;
			return pooled;
		}

		final PooledFrameBuffers created = new PooledFrameBuffers(new FrameBuffer[] {FrameBuffer.create(width, height, TextureFilter.LINEAR), FrameBuffer.create(width, height, TextureFilter.LINEAR)}, now);
		ShaderPipeline.FBO_POOL.put(key, created);
		return created;
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class PooledFrameBuffers {

		private final FrameBuffer[] frameBuffers;

		private long lastUse;

		private void delete() {
			this.frameBuffers[0].delete();
			this.frameBuffers[1].delete();
		}

	}

}