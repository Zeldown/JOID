---
summary: A weight that a blend state applies to the source or the destination color before its equation.
---

The source is the color being drawn, the destination the color already on the target; every weight is on 1.

Learn: [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/#blend-with-what-is-under).

## ZERO

Weight `0`: drops that side.

## ONE

Weight `1`: keeps that side as it is.

## SRC_COLOR

Multiplies by the color being drawn, channel by channel.

## ONE_MINUS_SRC_COLOR

Multiplies by `1` minus the color being drawn, channel by channel.

## DST_COLOR

Multiplies by the color on the target, channel by channel: with `ZERO` as destination factor, the two colors multiply.

## ONE_MINUS_DST_COLOR

Multiplies by `1` minus the color on the target, channel by channel.

## SRC_ALPHA

Multiplies by the alpha of the color being drawn.

## ONE_MINUS_SRC_ALPHA

Multiplies by `1` minus the alpha of the color being drawn.

## DST_ALPHA

Multiplies by the alpha on the target.

## ONE_MINUS_DST_ALPHA

Multiplies by `1` minus the alpha on the target.
