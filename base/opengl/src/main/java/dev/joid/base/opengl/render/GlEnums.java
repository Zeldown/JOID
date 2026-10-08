package dev.joid.base.opengl.render;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.StencilFunction;
import dev.joid.lib.bridge.render.state.StencilOperation;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexComponent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlEnums {

	public static int mode(final @NonNull Primitive primitive) {
		return primitive == Primitive.LINES ? GlConstants.LINES : GlConstants.TRIANGLES;
	}

	public static int type(final @NonNull VertexComponent component) {
		switch (component) {
		case UNSIGNED_BYTE:
			return GlConstants.UNSIGNED_BYTE;
		case BYTE:
			return GlConstants.BYTE;
		default:
			return GlConstants.FLOAT;
		}
	}

	public static int wrap(final @NonNull TextureWrap wrap) {
		switch (wrap) {
		case CLAMP_TO_EDGE:
			return GlConstants.CLAMP_TO_EDGE;
		case CLAMP_TO_BORDER:
			return GlConstants.CLAMP_TO_BORDER;
		default:
			return GlConstants.REPEAT;
		}
	}

	public static int magFilter(final @NonNull TextureSampling sampling) {
		return sampling.getFilter() == TextureFilter.LINEAR ? GlConstants.LINEAR : GlConstants.NEAREST;
	}

	public static int minFilter(final @NonNull TextureSampling sampling) {
		return sampling.isMipmapFiltered() ? GlConstants.LINEAR_MIPMAP_LINEAR : GlEnums.magFilter(sampling);
	}

	public static int equation(final @NonNull BlendState.Equation equation) {
		switch (equation) {
		case SUBTRACT:
			return GlConstants.FUNC_SUBTRACT;
		case REVERSE_SUBTRACT:
			return GlConstants.FUNC_REVERSE_SUBTRACT;
		case MIN:
			return GlConstants.MIN;
		case MAX:
			return GlConstants.MAX;
		default:
			return GlConstants.FUNC_ADD;
		}
	}

	public static int factor(final @NonNull BlendState.Factor factor) {
		switch (factor) {
		case ZERO:
			return GlConstants.ZERO;
		case SRC_COLOR:
			return GlConstants.SRC_COLOR;
		case ONE_MINUS_SRC_COLOR:
			return GlConstants.ONE_MINUS_SRC_COLOR;
		case DST_COLOR:
			return GlConstants.DST_COLOR;
		case ONE_MINUS_DST_COLOR:
			return GlConstants.ONE_MINUS_DST_COLOR;
		case SRC_ALPHA:
			return GlConstants.SRC_ALPHA;
		case ONE_MINUS_SRC_ALPHA:
			return GlConstants.ONE_MINUS_SRC_ALPHA;
		case DST_ALPHA:
			return GlConstants.DST_ALPHA;
		case ONE_MINUS_DST_ALPHA:
			return GlConstants.ONE_MINUS_DST_ALPHA;
		default:
			return GlConstants.ONE;
		}
	}

	public static int function(final @NonNull StencilFunction function) {
		switch (function) {
		case NEVER:
			return GlConstants.NEVER;
		case LESS:
			return GlConstants.LESS;
		case LESS_EQUAL:
			return GlConstants.LEQUAL;
		case GREATER:
			return GlConstants.GREATER;
		case GREATER_EQUAL:
			return GlConstants.GEQUAL;
		case EQUAL:
			return GlConstants.EQUAL;
		case NOT_EQUAL:
			return GlConstants.NOTEQUAL;
		default:
			return GlConstants.ALWAYS;
		}
	}

	public static int operation(final @NonNull StencilOperation operation) {
		switch (operation) {
		case ZERO:
			return GlConstants.ZERO;
		case REPLACE:
			return GlConstants.REPLACE;
		case INCREMENT:
			return GlConstants.INCR;
		case DECREMENT:
			return GlConstants.DECR;
		case INVERT:
			return GlConstants.INVERT;
		default:
			return GlConstants.KEEP;
		}
	}

}