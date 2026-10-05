package be.zeldown.joid.lib.shader.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.render.framebuffer.FrameBuffer;
import be.zeldown.joid.lib.render.tessellator.Tessellator;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

public final class ShaderPipeline {

	private static final Map<Long, FrameBuffer[]> FBO_POOL = new HashMap<>();
	private static int pipelineDepth = 0;

	private ShaderPipeline() {}

	public static void cleanup() {
		for (final FrameBuffer[] fbos : ShaderPipeline.FBO_POOL.values()) {
			fbos[0].delete();
			fbos[1].delete();
		}
		ShaderPipeline.FBO_POOL.clear();
	}

	public static int scaleFactor(final UI ui) {
		if (ui == null || ui.getWidth() <= 0D) {
			return 2;
		}
		return Math.max(1, (int) Math.ceil(ui.getViewportWidth() / ui.getWidth()));
	}

	public static void render(final Node node, final @NonNull Runnable baseDraw, final @NonNull ShaderPass... passes) {
		ShaderPipeline.render(node, new ArrayList<>(Arrays.asList(passes)), baseDraw);
	}

	public static void render(final Node node, final @NonNull List<ShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (passes.isEmpty()) {
			baseDraw.run();
			return;
		}

		ShaderPipeline.pipelineDepth++;
		try {
			passes.sort(Comparator.comparingInt(ShaderPass::priority));

			final boolean needsFBO = passes.size() > 1 || ShaderPipeline.pipelineDepth > 1 || passes.stream().anyMatch(p -> p.expansion() > 0F || !p.supportsDirectBind());
			if (!needsFBO) {
				final IRenderBridge render = BridgeHandler.RENDER.get();
				final IShader previousShader = render.getShader();
				passes.get(0).bindDirect(node);
				baseDraw.run();
				passes.get(0).unbind();
				render.shader(previousShader);
				return;
			}

			if (node != null) {
				ShaderPipeline.renderMultiPass(node.getX(), node.getY(), node.getWidth(), node.getHeight(), ShaderPipeline.scaleFactor(node.getUi()), node, passes, baseDraw);
			}
		} finally {
			ShaderPipeline.pipelineDepth--;
		}
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
			passes.sort(Comparator.comparingInt(ShaderPass::priority));
			if (passes.size() == 1 && ShaderPipeline.pipelineDepth <= 1 && passes.get(0).expansion() == 0F && passes.get(0).supportsDirectBind()) {
				final IRenderBridge render = BridgeHandler.RENDER.get();
				final IShader previousShader = render.getShader();
				passes.get(0).bindDirect(null);
				baseDraw.run();
				passes.get(0).unbind();
				render.shader(previousShader);
				return;
			}

			ShaderPipeline.renderMultiPass(x, y, width, height, 2, null, passes, baseDraw);
		} finally {
			ShaderPipeline.pipelineDepth--;
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

	private static void drawTexturedQuad(final @NonNull FrameBuffer frameBuffer, final double x, final double y, final double w, final double h) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.blend(BlendState.NORMAL);
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

	private static void renderMultiPass(final double nodeX, final double nodeY, final double nodeW, final double nodeH, final int scaleFactor, final Node node, final @NonNull List<ShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (nodeW <= 0D || nodeH <= 0D) {
			baseDraw.run();
			return;
		}

		float expansion = 0F;
		for (final ShaderPass pass : passes) {
			expansion = Math.max(expansion, pass.expansion());
		}

		final double expX = nodeX - expansion;
		final double expY = nodeY - expansion;
		final double expW = nodeW + expansion * 2D;
		final double expH = nodeH + expansion * 2D;

		final int pixelW = Math.max(1, (int) Math.ceil(expW * scaleFactor));
		final int pixelH = Math.max(1, (int) Math.ceil(expH * scaleFactor));

		final FrameBuffer[] fbos = ShaderPipeline.getOrCreateFBOs(pixelW, pixelH);
		final FrameBuffer fboA = fbos[0];
		final FrameBuffer fboB = fbos[1];

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();

		render.pushState();
		fboA.bind();
		render.viewport(0, 0, pixelW, pixelH);
		render.clear(0F, 0F, 0F, 0F);
		render.blend(BlendState.COMPOSITE);

		render.pushProjection();
		render.ortho(expX, expX + expW, expY + expH, expY, -1000D, 1000D);
		render.pushMatrix();
		render.loadIdentity();

		baseDraw.run();
		render.blend(BlendState.NORMAL);

		render.popMatrix();
		render.popProjection();
		render.popState();

		FrameBuffer src = fboA;
		FrameBuffer dst = fboB;

		for (int i = 0; i < passes.size() - 1; i++) {
			render.pushState();
			dst.bind();
			render.viewport(0, 0, pixelW, pixelH);
			render.clear(0F, 0F, 0F, 0F);

			render.pushProjection();
			render.ortho(expX, expX + expW, expY + expH, expY, -1000D, 1000D);
			render.pushMatrix();
			render.loadIdentity();

			passes.get(i).bindForTexture(node);
			ShaderPipeline.drawTexturedQuad(src, expX, expY, expW, expH);
			passes.get(i).unbind();

			render.popMatrix();
			render.popProjection();
			render.popState();

			final FrameBuffer temp = src;
			src = dst;
			dst = temp;
		}

		passes.get(passes.size() - 1).bindForTexture(node);
		ShaderPipeline.drawTexturedQuad(src, expX, expY, expW, expH);
		passes.get(passes.size() - 1).unbind();

		render.popState();
	}

}