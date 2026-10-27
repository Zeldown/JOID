---
summary: Blurs the previous result along one axis; chain a horizontal and a vertical pass for a full blur.
---

Learn: [Shaders](/use/learn/going-further/shaders/).

## create(float,boolean)

Creates a blur of `radius` design pixels, along X when `horizontal` is `true`, along Y otherwise.

## BlurShaderPass(float,boolean)

Creates the pass, for a subclass.

## bind(ShaderPassContext)

Binds the blur shader with the radius in screen pixels and the direction of the pass.

## unbind()

Unbinds the blur shader.

## expansion()

Returns the radius: the blur spreads that far outside the area.

## reach(double)

Returns the radius in screen pixels, at least 32, plus one pixel.
