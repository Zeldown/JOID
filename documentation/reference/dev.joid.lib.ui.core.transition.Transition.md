---
summary: The pair of steps that a UI plays when it opens and when it closes.
---
Give one to a UI with [[UI#transition]]; a popup gets a [[PopTransition]] from `@UIDataPopup`.
Write your own by extending this class and passing two [[TransitionStep]] to the constructor.
Several UIs can share one transition: each UI plays copies of its steps, made when the transition is set.

Learn: [Transitions](/use/learn/style-and-motion/transitions/).

## Transition(TransitionStep,TransitionStep)

Creates a transition from its opening step and its closing step.

## getIn()

The opening step that the UIs copy; [[UI#getTransitionIn]] returns the copy that a UI plays.

## getOut()

The closing step that the UIs copy; [[UI#getTransitionOut]] returns the copy that a UI plays.
