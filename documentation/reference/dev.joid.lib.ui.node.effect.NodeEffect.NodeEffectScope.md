---
summary: What the shader passes of an effect process, the node alone or the node with its children.
---

[[NodeEffect#scope]] takes it.
It changes the effects that work on pixels: rounded, circle, blur, border and the passes of your own effects.
A shadow, a mask, a transform and every `draw` of an effect always cover the children.

Learn: [Effects](/use/learn/style-and-motion/effects/#round-a-node-with-its-children).

## SELF

The passes process what the node draws itself; its children, its label and its layers draw on top, untouched.

## CHILDREN

The passes process the node with its children and its layers, as one picture: rounded corners also cut the children that reach the edges.
