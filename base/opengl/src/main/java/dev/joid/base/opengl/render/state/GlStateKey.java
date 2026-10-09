package dev.joid.base.opengl.render.state;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlStateKey {

	READ_BUFFER(0),
	FRAMEBUFFER(1),
	RENDERBUFFER(1),
	PROGRAM(2),
	VERTEX_ATTRIBUTE(3),
	VERTEX_ARRAY(4),
	BUFFER(5),
	TEXTURE_PARAMETER(6),
	TEXTURE(7),
	SAMPLER(7),
	CURRENT_VERTEX_ATTRIBUTE(8),
	CAPABILITY(9),
	BLEND_EQUATION(9),
	BLEND_FUNCTION(9),
	COLOR_MASK(9),
	CLEAR_COLOR(9),
	CLEAR_DEPTH(9),
	CLEAR_STENCIL(9),
	DEPTH_MASK(9),
	DEPTH_FUNCTION(9),
	STENCIL_MASK(9),
	STENCIL_FUNCTION(9),
	STENCIL_OPERATION(9),
	VIEWPORT(9),
	LINE_WIDTH(9),
	POLYGON_MODE(9),
	FRONT_FACE(9),
	CULL_FACE(9),
	PIXEL_STORE(9),
	ACTIVE_TEXTURE(10);

	private final int rank;

}