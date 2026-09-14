in vec4 vColor;
in float vAcross;
in float vAlong;
in float vLength;

uniform float u_Width;

void main() {
    float across = clamp(u_Width * 0.5 + 0.5 - abs(vAcross), 0.0, 1.0);
    float along = clamp(min(vAlong, vLength - vAlong) + 0.5, 0.0, 1.0);
    fragColor = vec4(vColor.rgb, vColor.a * across * along);
}