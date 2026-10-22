in vec2 vTexCoord;
in vec2 vPosition;
in vec4 vColor;

uniform float radius;
uniform vec2 center;
uniform int type;
uniform float u_Pixel;

uniform int u_HasGradient;
uniform vec2 u_GradientStartPos;
uniform vec2 u_GradientEndPos;
uniform vec4 u_GradientStart;
uniform vec4 u_GradientEnd;
uniform vec4 u_GradientCanvas;

uniform sampler2D tex;

vec2 snappedPosition() {
	return floor(vPosition * 256.0 + 0.5) / 256.0;
}

vec4 gradientColor() {
	vec2 normalizedPos = snappedPosition();
	if (u_GradientCanvas.z > u_GradientCanvas.x && u_GradientCanvas.w > u_GradientCanvas.y) {
		normalizedPos = (snappedPosition() - u_GradientCanvas.xy) / (u_GradientCanvas.zw - u_GradientCanvas.xy);
	}

	vec2 direction = u_GradientEndPos - u_GradientStartPos;
	float t = 0.0;
	if (length(direction) > 0.001) {
		t = clamp(dot(normalizedPos - u_GradientStartPos, direction) / dot(direction, direction), 0.0, 1.0);
	}

	return mix(u_GradientStart, u_GradientEnd, t);
}

void main() {
	float dist = length(snappedPosition() - center);

	vec4 baseColor = texture(tex, vTexCoord) * vColor;
	if (type == 1) {
		baseColor = texture(tex, vTexCoord);
	} else if (type == 2) {
		baseColor = vColor;
	}

	if (u_HasGradient == 1) {
		baseColor *= gradientColor();
	}

	float mask = 1.0 - smoothstep(radius - u_Pixel, radius, dist);

	if (type == 1) {
		fragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
	} else {
		fragColor = vec4(baseColor.rgb, baseColor.a * mask);
	}
}