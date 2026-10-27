---
summary: The pair of steps that a UI plays when it opens and when it closes.
---
Give one to a UI with [[UI#transition]]; a popup gets a [[PopTransition]] from `@UIDataPopup`.
Write your own by extending this class and passing two [[TransitionStep]] to the constructor.

Learn: [Transitions](/use/learn/style-and-motion/transitions/).

## Transition(TransitionStep,TransitionStep)

Creates a transition from its opening step and its closing step.

## getIn()

The step played when the UI opens.

## getOut()

The step played when the UI closes.
