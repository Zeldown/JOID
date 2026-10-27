---
summary: The event of `onAnimate`, with the animator of a node and the value it reached on this frame.
---

[[Node#onAnimate]] fires on the frames where an animator given to [[Node#animate]] changes its value.

Learn: [Animation](/use/learn/style-and-motion/animation/).

## create(N,TweenAnimator,float)

Creates the event for a node, one of its animators and the value of that animator.

## getValue()

Returns the value of the animator on this frame.

## getAnimator()

Returns the [[TweenAnimator]] whose value changed.
