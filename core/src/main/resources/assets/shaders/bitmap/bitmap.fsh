in vec2 vTexCoord;
in vec4 vColor;
in vec2 vPosition;

uniform sampler2D tex;
uniform vec4 color;
uniform vec2 texel;
uniform vec2 pixel;
uniform vec4 bounds;

uniform int u_HasGradient;
uniform vec4 u_GradientStart;
uniform vec4 u_GradientEnd;
uniform vec2 u_GradientStartPos;
uniform vec2 u_GradientEndPos;
uniform vec4 u_GradientCanvas;

vec2 snappedPosition() {
    return floor(vPosition * 256.0 + 0.5) / 256.0;
}

vec4 texelAt(vec2 index) {
    if (index.x < bounds.x || index.y < bounds.y || index.x >= bounds.z || index.y >= bounds.w) {
        return vec4(0.0);
    }

    vec4 value = texture(tex, (index + 0.5) * texel);
    return vec4(value.rgb * value.a, value.a);
}

float overlap(float start, float end, float index) {
    return max(min(index + 1.0, end) - max(index, start), 0.0);
}

vec4 boxSample() {
    vec2 extent = min(pixel, vec2(4.0)) * 0.5;
    vec2 position = vTexCoord / texel;
    vec2 start = position - extent;
    vec2 end = position + extent;
    vec2 first = floor(start);
    vec4 sum = vec4(0.0);
    if (pixel.x <= 1.0 && pixel.y <= 1.0) {
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                vec2 index = first + vec2(float(x), float(y));
                sum += texelAt(index) * overlap(start.x, end.x, index.x) * overlap(start.y, end.y, index.y);
            }
        }
    } else {
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                vec2 index = first + vec2(float(x), float(y));
                sum += texelAt(index) * overlap(start.x, end.x, index.x) * overlap(start.y, end.y, index.y);
            }
        }
    }

    return sum / (4.0 * extent.x * extent.y);
}

vec4 resolveColor() {
    if (u_HasGradient == 0) {
        return color;
    }

    vec2 normalizedPos;
    if (u_GradientCanvas.z > u_GradientCanvas.x && u_GradientCanvas.w > u_GradientCanvas.y) {
        vec2 rectSize = vec2(u_GradientCanvas.z - u_GradientCanvas.x, u_GradientCanvas.w - u_GradientCanvas.y);
        normalizedPos = (snappedPosition() - u_GradientCanvas.xy) / rectSize;
    } else {
        normalizedPos = snappedPosition();
    }

    vec2 dir = u_GradientEndPos - u_GradientStartPos;
    float t = 0.0;
    if (length(dir) > 0.001) {
        t = dot(normalizedPos - u_GradientStartPos, dir) / dot(dir, dir);
        t = clamp(t, 0.0, 1.0);
    }

    return mix(u_GradientStart, u_GradientEnd, t);
}

void main() {
    vec4 sampled = boxSample();

    if (sampled.a < 1.0 / 256.0) {
        discard;
    }

    vec4 baseColor = resolveColor();
    fragColor = vec4(baseColor.rgb * sampled.rgb / sampled.a, sampled.a * baseColor.a) * vColor;
}