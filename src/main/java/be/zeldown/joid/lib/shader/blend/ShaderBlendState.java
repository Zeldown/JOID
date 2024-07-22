package be.zeldown.joid.lib.shader.blend;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Builder
@NoArgsConstructor
public class ShaderBlendState {

	public static ShaderBlendState NORMAL   = new ShaderBlendState(Equation.ADD, Param.SRC_ALPHA, Param.ONE_MINUS_SRC_ALPHA);
	public static ShaderBlendState DISABLED = ShaderBlendState.builder().equation(Equation.ADD).srcRgb(Param.ONE).srcAlpha(Param.ZERO).enabled(false).build();

	private Equation equation;
	private Param srcRgb;
	private Param dstRgb;
	private Param srcAlpha;
	private Param dstAlpha;

	@Builder.Default
	private final boolean enabled = true;

	public ShaderBlendState(final @NonNull Equation equation, final Param srcRgb, final Param dstRgb) {
		this(equation, srcRgb, dstRgb, srcRgb, dstRgb, true);
	}

	public ShaderBlendState(final @NonNull Equation equation, final Param srcRgb, final Param dstRgb, final Param srcAlpha, final Param dstAlpha, final boolean enable) {
		this.equation = equation;
		this.srcRgb = srcRgb;
		this.dstRgb = dstRgb;
		this.srcAlpha = srcRgb;
		this.dstAlpha = dstRgb;
		this.enabled = enable;

		if (srcAlpha != null) {
			this.srcAlpha = srcAlpha;
		}

		if (dstAlpha != null) {
			this.dstAlpha = dstAlpha;
		}
	}

	public static ShaderBlendState create() {
		return new ShaderBlendState(
				Equation.fromGl(GL11.glGetInteger(GL14.GL_BLEND_EQUATION)),
				Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB)),
				Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_DST_RGB)),
				Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA)),
				Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA)),
				GL11.glGetBoolean(GL11.GL_BLEND)
				);
	}

	public void apply() {
		this.applyState();
	}

	private void applyState() {
		if (this.enabled) {
			GL11.glEnable(GL11.GL_BLEND);
		} else {
			GL11.glDisable(GL11.GL_BLEND);
		}

		GL14.glBlendEquation(this.equation.glId);
		GL14.glBlendFuncSeparate(this.srcRgb.glId, this.dstRgb.glId, this.srcAlpha.glId, this.dstAlpha.glId);
	}

	@Getter
	public enum Equation {

		ADD("add", GL14.GL_FUNC_ADD),
		SUBTRACT("subtract", GL14.GL_FUNC_SUBTRACT),
		REVERSE_SUBTRACT("reverse_subtract", GL14.GL_FUNC_REVERSE_SUBTRACT),
		MIN("min", GL14.GL_MIN),
		MAX("max", GL14.GL_MAX);

		private static final Map<Integer, List<Equation>> EQUATION_BY_ID = Arrays.stream(Equation.values()).collect(Collectors.groupingBy(Equation::getGlId));

		private final int glId;
		private final String mcStr;

		private Equation(final String mcStr, final int glId) {
			this.mcStr = mcStr;
			this.glId = glId;
		}

		public static Equation fromGl(final int glId) {
			return Equation.EQUATION_BY_ID.get(glId).get(0);
		}

	}

	@Getter
	public enum Param {

		ZERO("0", GL11.GL_ZERO),
		ONE("1", GL11.GL_ONE),
		SRC_COLOR("srccolor", GL11.GL_SRC_COLOR),
		ONE_MINUS_SRC_COLOR("1-srccolor", GL11.GL_ONE_MINUS_SRC_COLOR),
		DST_COLOR("dstcolor", GL11.GL_DST_COLOR),
		ONE_MINUS_DST_COLOR("1-dstcolor", GL11.GL_ONE_MINUS_DST_COLOR),
		SRC_ALPHA("srcalpha", GL11.GL_SRC_ALPHA),
		ONE_MINUS_SRC_ALPHA("1-srcalpha", GL11.GL_ONE_MINUS_SRC_ALPHA),
		DST_ALPHA("dstalpha", GL11.GL_DST_ALPHA),
		ONE_MINUS_DST_ALPHA("1-dstalpha", GL11.GL_ONE_MINUS_DST_ALPHA);

		private static final Map<Integer, List<Param>> PARAM_BY_ID = Arrays.stream(Param.values()).collect(Collectors.groupingBy(Param::getGlId));

		private final int glId;
		private final String mcStr;

		private Param(final String mcStr, final int glId) {
			this.mcStr = mcStr;
			this.glId = glId;
		}

		public static Param fromGl(final int glId) {
			return Param.PARAM_BY_ID.get(glId).get(0);
		}

	}

}