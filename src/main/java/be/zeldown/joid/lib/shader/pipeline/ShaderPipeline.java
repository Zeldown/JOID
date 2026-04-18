package be.zeldown.joid.lib.shader.pipeline;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import be.zeldown.joid.lib.opengl.framebuffer.FrameBuffer;
import be.zeldown.joid.lib.tessellator.T9R;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

public final class ShaderPipeline {

	private static final Map<Long, FrameBuffer[]> FBO_POOL = new HashMap<>();
	private static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
	private static int pipelineDepth = 0;

	private ShaderPipeline() {}

	public static void render(final Node node, final @NonNull List<ShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (passes.isEmpty()) {
			baseDraw.run();
			return;
		}

		passes.sort(Comparator.comparingInt(ShaderPass::priority));

		final boolean needsFBO = passes.size() > 1 || passes.stream().anyMatch(p -> p.expansion() > 0F);
		if (!needsFBO) {
			passes.get(0).bindDirect(node);
			baseDraw.run();
			passes.get(0).unbind();
			return;
		}

		if (node != null) {
			ShaderPipeline.renderMultiPass(node.getX(), node.getY(), node.getWidth(), node.getHeight(), ShaderPipeline.scaleFactor(node.getUi()), node, passes, baseDraw);
		}
	}

	public static void render(final Node node, final @NonNull Runnable baseDraw, final @NonNull ShaderPass... passes) {
		ShaderPipeline.render(node, new ArrayList<>(Arrays.asList(passes)), baseDraw);
	}

	public static void render(final double x, final double y, final double width, final double height, final @NonNull Runnable baseDraw, final @NonNull ShaderPass... passes) {
		ShaderPipeline.render(x, y, width, height, new ArrayList<>(Arrays.asList(passes)), baseDraw);
	}

	public static void render(final double x, final double y, final double width, final double height, final @NonNull List<ShaderPass> passes, final @NonNull Runnable baseDraw) {
		if (passes.isEmpty()) {
			baseDraw.run();
			return;
		}

		passes.sort(Comparator.comparingInt(ShaderPass::priority));
		if (passes.size() == 1 && passes.get(0).expansion() == 0F) {
			passes.get(0).bindDirect(null);
			baseDraw.run();
			passes.get(0).unbind();
			return;
		}

		ShaderPipeline.renderMultiPass(x, y, width, height, 2, null, passes, baseDraw);
	}

	public static int scaleFactor(final UI ui) {
		if (ui == null || ui.getWidth() <= 0D) {
			return 2;
		}
		return Math.max(1, (int) Math.ceil(ui.getViewportWidth() / ui.getWidth()));
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

		ShaderPipeline.pipelineDepth++;
		final FrameBuffer[] fbos = ShaderPipeline.getOrCreateFBOs(pixelW, pixelH);
		final FrameBuffer fboA = fbos[0];
		final FrameBuffer fboB = fbos[1];

		final int prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		ShaderPipeline.VIEWPORT_BUFFER.clear();
		GL11.glGetInteger(GL11.GL_VIEWPORT, ShaderPipeline.VIEWPORT_BUFFER);
		final int prevVpX = ShaderPipeline.VIEWPORT_BUFFER.get(0);
		final int prevVpY = ShaderPipeline.VIEWPORT_BUFFER.get(1);
		final int prevVpW = ShaderPipeline.VIEWPORT_BUFFER.get(2);
		final int prevVpH = ShaderPipeline.VIEWPORT_BUFFER.get(3);

		/* [ Pass 0 : Base ] */
		fboA.bind();
		GL11.glViewport(0, 0, pixelW, pixelH);
		GL11.glClearColor(0F, 0F, 0F, 0F);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(expX, expX + expW, expY + expH, expY, -1000D, 1000D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		baseDraw.run();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		fboA.unbind();

		/* [ Post-Processing Passes ] */
		FrameBuffer src = fboA;
		FrameBuffer dst = fboB;

		for (int i = 0; i < passes.size() - 1; i++) {
			dst.bind();
			GL11.glViewport(0, 0, pixelW, pixelH);
			GL11.glClearColor(0F, 0F, 0F, 0F);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glOrtho(expX, expX + expW, expY + expH, expY, -1000D, 1000D);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();

			passes.get(i).bindForTexture(node);
			ShaderPipeline.drawTexturedQuad(src.getTexture(), expX, expY, expW, expH);
			passes.get(i).unbind();

			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);

			dst.unbind();

			final FrameBuffer temp = src;
			src = dst;
			dst = temp;
		}

		/* [ Final Pass ] */
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
		GL11.glViewport(prevVpX, prevVpY, prevVpW, prevVpH);

		passes.get(passes.size() - 1).bindForTexture(node);
		ShaderPipeline.drawTexturedQuad(src.getTexture(), expX, expY, expW, expH);
		passes.get(passes.size() - 1).unbind();

		ShaderPipeline.pipelineDepth--;
	}

	private static void drawTexturedQuad(final int textureId, final double x, final double y, final double w, final double h) {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendEquation(GL14.GL_FUNC_ADD);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glColor4f(1F, 1F, 1F, 1F);

		final T9R tess = T9R.inst();
		tess.start(GL11.GL_QUADS);
		tess.addVertexWithUV(x, y + h, 0D, 0D, 0D);
		tess.addVertexWithUV(x + w, y + h, 0D, 1D, 0D);
		tess.addVertexWithUV(x + w, y, 0D, 1D, 1D);
		tess.addVertexWithUV(x, y, 0D, 0D, 1D);
		tess.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	private static FrameBuffer[] getOrCreateFBOs(final int width, final int height) {
		final long key = (long) ShaderPipeline.pipelineDepth << 32 | (long) width << 16 | height;
		FrameBuffer[] fbos = ShaderPipeline.FBO_POOL.get(key);
		if (fbos != null) {
			return fbos;
		}

		fbos = new FrameBuffer[] {new FrameBuffer().prepare(width, height, GL11.GL_LINEAR), new FrameBuffer().prepare(width, height, GL11.GL_LINEAR)};
		ShaderPipeline.FBO_POOL.put(key, fbos);
		return fbos;
	}

	public static void cleanup() {
		for (final FrameBuffer[] fbos : ShaderPipeline.FBO_POOL.values()) {
			fbos[0].delete();
			fbos[1].delete();
		}
		ShaderPipeline.FBO_POOL.clear();
	}

}