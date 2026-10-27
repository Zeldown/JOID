---
summary: How the vertices of a draw join, in segments or in triangles.
---

`draw(primitive, buffer)` of the render bridge passes it to the backend, which maps it to the topology of its API; it is part of the [[PipelineKey]] too.
The [[Tessellator]] turns every drawing mode into one of the two.

Learn: [A draw call](/build/pipeline/#a-draw-call).

## LINES

Every two vertices form a segment.

## TRIANGLES

Every three vertices form a triangle.
