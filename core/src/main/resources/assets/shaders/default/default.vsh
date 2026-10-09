out vec2 vTexCoord;
out vec4 vColor;
out vec4 vLitColor;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;

    vec3 normal = uNormalMatrix * aNormal;
    float diffuse = dot(normal, normal) > 0.0 ? max(normalize(normal).z, 0.0) : 0.0;
    vLitColor = vec4(clamp(aColor.rgb * (0.6 + diffuse), 0.0, 1.0), aColor.a);

    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}