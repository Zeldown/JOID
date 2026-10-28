---
summary: One step of a `ShaderPipeline`, which binds a shader that reads the previous result as a texture.
---

Implement it for a shader effect of your own, or return it from the shader passes of a node effect.
JOID calls `bind`, draws the previous result as a textured quad over the area, then calls `unbind`, on the render thread.

Learn: [Shaders](/use/learn/going-further/shaders/), [Custom effects](/use/learn/going-further/custom-effects/).

## bind(ShaderPassContext)

Binds your shader and sets its uniforms from `context`: the texel size, the area and its region on screen.
When the shader cannot draw, bind nothing: the quad is then drawn without a shader.

## unbind()

Unbinds the shader bound in `bind`; JOID calls it even when the draw throws.

## expansion()

Returns how far, in design pixels, the pass draws outside the area, such as the radius of a blur; the area grows by the largest expansion of the passes.
`0F` by default.

## reach(double)

Returns how far, in screen pixels at the screen scale `scale`, the pass reads outside the visible part of the area, so that the pipeline renders that margin too.
By default the expansion times `scale`, plus one pixel.
