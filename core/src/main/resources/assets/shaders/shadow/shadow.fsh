in vec2 vPosition;
in vec4 vColor;

uniform float u_Radius;
uniform float u_Blur;
uniform vec4 u_Box;
uniform vec4 u_Sides;

vec2 approximateErf(vec2 x) {
    vec2 signs = sign(x);
    vec2 a = abs(x);
    vec2 y = 1.0 + (0.278393 + (0.230389 + 0.078108 * (a * a)) * a) * a;
    y *= y;
    return signs - signs / (y * y);
}

float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma)) / (2.506628 * sigma);
}

float shadowRow(float x, float y, float sigma, vec2 corners, vec2 halfSize) {
    vec2 delta = min(halfSize.y - corners - abs(y), 0.0);
    vec2 curved = halfSize.x - corners + sqrt(max(vec2(0.0), corners * corners - delta * delta));
    vec2 integral = 0.5 + 0.5 * approximateErf((x + vec2(-curved.y, curved.x)) * (0.707107 / sigma));
    return integral.y - integral.x;
}

void main() {
    float sigma = max(u_Blur * 0.5, 0.001);
    vec2 halfSize = (u_Box.zw - u_Box.xy) * 0.5;
    vec2 point = vPosition - (u_Box.xy + u_Box.zw) * 0.5;
    float corner = min(u_Radius, min(halfSize.x, halfSize.y));
    vec4 corners = corner * u_Sides.xyzw * u_Sides.yzwx;

    float first = clamp(-3.0 * sigma, point.y - halfSize.y, point.y + halfSize.y);
    float last = clamp(3.0 * sigma, point.y - halfSize.y, point.y + halfSize.y);
    float stride = (last - first) / 4.0;
    float y = first + stride * 0.5;
    float coverage = 0.0;
    for (int i = 0; i < 4; i++) {
        float row = point.y - y;
        coverage += shadowRow(point.x, row, sigma, row < 0.0 ? corners.xy : corners.wz, halfSize) * gaussian(y, sigma) * stride;
        y += stride;
    }

    fragColor = vec4(vColor.rgb, vColor.a * coverage);
}