---
summary: A mouse button, as the mouse events of nodes and UIs report it.
---

Every mouse event holds the button that sent it: [[NodeMouseEvent#getButton]] on a node, [[UIMouseEvent#getButton]] on a UI.
Test it with the `isX()` predicates, for example to open a context menu on a right press only.

Learn: [Events](/use/learn/interactivity/events/).

## of(int)

Returns the button of a button number of the window library: `0` left, `1` right, `2` middle, `3` back, `4` forward.
Any other number gives [[MouseButton#OTHER]].

## getButton()

Returns the button number of this button, the inverse of `of(int)`.
[[MouseButton#OTHER]] returns `-1`, because it stands for every other number.

## isLeft()

Returns `true` for [[MouseButton#LEFT]].

## isRight()

Returns `true` for [[MouseButton#RIGHT]].

## isMiddle()

Returns `true` for [[MouseButton#MIDDLE]].

## isBack()

Returns `true` for [[MouseButton#BACK]].

## isForward()

Returns `true` for [[MouseButton#FORWARD]].

## isOther()

Returns `true` for [[MouseButton#OTHER]].

## LEFT

The primary button, number `0`.
[[Node#onClick]], the controls and the drag of a node answer this button only.

## RIGHT

The secondary button, number `1`.

## MIDDLE

The wheel button, number `2`.

## BACK

The side button that a browser maps to "back", number `3`.

## FORWARD

The side button that a browser maps to "forward", number `4`.

## OTHER

Any button beyond the first five; JOID does not tell them apart.
