---
summary: A node that draws nothing and groups its children, to move, clip or scroll them together.
---
Learn: [Layout](/use/learn/describing-the-ui/layout/#group-nodes-without-drawing-them).

## create(double,double,double,double)

Creates a container at `x`, `y` with this width and height.

## ContainerNode(double,double,double,double)

Called by a subclass with the same arguments as `create`.

## drawSkeleton(double,double)

Draws nothing while the container waits, so that only its own skeleton or its children show.
