---
summary: One step of a transition, opening or closing, that animates a progress from 0 to 1 and draws the UI through its drawing.
---
The progress starts at the first frame the UI is drawn, so a UI opened during a heavy frame still plays its whole step.
When a UI closes during its opening, the closing step starts from the progress the opening had reached.

Learn: [Transitions](/use/learn/style-and-motion/transitions/#write-a-transition-of-your-own).

## create(float,TweenEquation)

Creates a step that lasts this many ms with this easing, and that draws the UI unchanged until you give it a drawing.

## TransitionStep(float,TweenEquation)

Called by a subclass with the same arguments as `create`.

## init(UI)

Hook that JOID calls when the UI opens, before the opening step starts, for each enabled step; the default does nothing.

## draw

How the step draws the UI for a progress between `0` and `1`; the drawing must run the body it receives.

## enabled

If `false`, the step does not play and the UI opens or closes at once.

## start()

Plays the step from the start.

## start(float)

Plays the step from this progress, for the remaining share of its duration.

## stop()

Stops the step; the UI is drawn without it from then on.

## render(UI,Runnable)

Draws the body of the UI through the drawing of the step while the step plays, and as it is otherwise.

## getAnimator()

The animator that holds the progress of the step.

## isRunning()

If `true`, the step is playing.
