---
summary: Collects vertices with optional texture coordinates, colors and normals, and sends them to the render bridge in one draw.
---

Start a shape, give it its vertices, then draw it ([Build your own geometry](/use/learn/going-further/framebuffers-and-models/#build-your-own-geometry)).

It draws with the current render state: the bound texture, the bound shader and the current color, which tints the vertices.
A texture coordinate, a color or a normal applies to every vertex added after it, until you set another one.
Lines wider than one pixel, or smoothed lines, draw as triangles with round joints.
It is not thread-safe: use it on the render thread only.

Learn: [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/).

## inst()

Returns the shared tessellator.

## start(DrawMode)

Starts a shape joined as `drawMode`, without texture coordinates, colors or normals.
Throws `IllegalStateException` while another shape is started.

## draw()

Sends the shape to the render bridge and ends it.
Throws `IllegalStateException` when no shape is started.

## abort()

Ends the shape without drawing it, for example after an exception in the middle of a shape.

## setColor(int,int,int)

Sets an opaque color for the next vertices, from 0 to 255.
Throws `IllegalStateException` when no shape is started, like every setter of the tessellator.

## setColor(int,int,int,int)

Sets the color of the next vertices, from 0 to 255, alpha included.

## setColor(float,float,float,float)

Sets the color of the next vertices, on 1.

## setTextureUV(double,double)

Sets the texture coordinates of the next vertices, on 1.

## setNormal(float,float,float)

Sets the normal of the next vertices, for lighting; give a vector of length 1.

## addVertex(double,double,double)

Adds a vertex at `x`, `y`, `z`, with the texture coordinates, the color and the normal set last.

## addVertexWithUV(double,double,double,double,double)

Sets the texture coordinates `u`, `v`, then adds the vertex at `x`, `y`, `z`.
