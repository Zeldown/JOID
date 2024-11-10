#version 120

varying vec2 pos;

uniform float radius;
uniform int pass;

uniform vec4 canvas;
uniform sampler2D texture;

void main() {
    vec2 uv = (pos.xy - canvas.xy) / canvas.zw;
    vec4 color = vec4(0.0);
	float total = 0.0;
	
	if (pass == -1) {
	    vec2 texOffset = 1.0 / canvas.zw;
	    for (float x = -radius; x <= radius; x++) {
	        for (float y = -radius; y <= radius; y++) {
	            float weight = exp(-(x * x + y * y) / (2.0 * radius * radius));
	            color += texture2D(texture, uv + vec2(x, y) * texOffset) * weight;
	            total += weight;
	        }
	    }
    } else if (pass == 0) {
	    vec2 texOffset = vec2(1.0 / canvas.z, 0.0);
	    for (float x = -radius; x <= radius; x++) {
	        float weight = exp(-(x * x) / (2.0 * radius * radius));
            vec4 currentColor = texture2D(texture, uv + texOffset * x);
	        color += (currentColor.rgb == vec3(0.0) ? vec4(vec3(1.0), 0.0) : currentColor) * weight;
	        total += weight;
	    }
    } else if (pass == 1) {
	    vec2 texOffset = vec2(0.0, 1.0 / canvas.w);
	    for (float y = -radius; y <= radius; y++) {
	        float weight = exp(-(y * y) / (2.0 * radius * radius));
            vec4 currentColor = texture2D(texture, uv + texOffset * y);
	        color += (currentColor.rgb == vec3(0.0) ? vec4(vec3(1.0), 0.0) : currentColor) * weight;
	        total += weight;
	    }
    }

    gl_FragColor = color / total;
}