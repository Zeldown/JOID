---
summary: The operation that combines the weighted source and destination colors of a blend state.
---

`S` is the color being drawn times its source factor, `D` the color on the target times its destination factor.
`MIN` and `MAX` ignore the factors.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/#blend-with-what-is-under).

## ADD

`S + D`, the usual choice.

## SUBTRACT

`S − D`, clamped at zero.

## REVERSE_SUBTRACT

`D − S`, clamped at zero: darkens the target by what you draw.

## MIN

The smaller of the source and the destination, channel by channel, without factors.

## MAX

The larger of the source and the destination, channel by channel, without factors.
