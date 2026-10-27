---
summary: The shader of soft shadows, which draws the blurred silhouette of a rounded box.
---

[[DrawShape#drawShadow]] uses it.

Learn: [Shaders](/use/learn/going-further/shaders/).

## inst()

Returns the shared shader.

## use(float,float,float,float,float,float,Runnable)

Runs `runnable` with the shader bound on the box `x1`, `y1`, `x2`, `y2`, its corner `radius` and its `blur`, then restores the previous shader.
Draw a quad that covers the box plus 1.5 times the blur on each side.

## bind(float,float,float,float,float,float)

Binds the shader on the box, with every corner rounded.

## sides(boolean,boolean,boolean,boolean)

Rounds only the corners of the chosen sides, as [[DrawShape#drawRoundedRect]] does; call it after `bind`.
