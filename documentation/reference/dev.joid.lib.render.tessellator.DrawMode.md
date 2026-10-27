---
summary: How the Tessellator joins the vertices you give it into lines or triangles.
---

Learn: [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/).

## LINES

Each pair of vertices is one segment.

## LINE_STRIP

Each vertex is joined to the next one, an open line.

## LINE_LOOP

Each vertex is joined to the next one, and the last one to the first, a closed line.

## TRIANGLES

Each group of three vertices is one triangle.

## QUADS

Each group of four vertices is one quad, drawn as two triangles.

## POLYGON

The vertices form one convex polygon, drawn as a fan of triangles from the first vertex.
