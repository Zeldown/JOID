---
summary: Base class of every element of a UI, a box in design pixels with children, fluent properties, input, hover, scroll, drag and effects.
---
Every shape, text, image, control and container of a UI extends `Node`.
You use it through its subclasses: create a node with the factory of its class, chain its setters, build its children in `body(...)`, react with `onX(...)` listeners, and end the chain with `attach(...)`:

```java file="com/example/reference/CardUI.java" region="node"
```

A node is placed relative to its parent, in design pixels; its children are drawn in the order of their `zindex`, over it, except those of negative `zindex`, drawn behind it; the children in front receive the input before it.
Most setters also take a `Supplier` or a signal, so that the property follows a value that changes.
To draw a node of your own, extend a node class and override the hooks of [[INode]].
JOID calls every hook and every listener of a node on the render thread.

Learn: [Nodes and the tree](/use/learn/describing-the-ui/nodes/), [Position and size](/use/learn/describing-the-ui/position-and-size/), [Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/), [Events](/use/learn/interactivity/events/).

## Node(double,double)

Creates a node at `x`, `y` with a size of `0`, for a subclass whose size comes from its content.

## Node(double,double,double,double)

Creates a node at `x`, `y` with this width and height, in design pixels.
A subclass calls it from its own constructor; [[Node#copy]] calls a constructor with these four arguments.

## x

Horizontal position of the anchor point of the node, in design pixels, from the left edge of its parent (or of the canvas for a root or `ABSOLUTE` node).
A writable signal is bound both ways: a drag or `move` writes the new position to it; a lambda or a read-only signal cannot be combined with a `MOVE` drag and throws an `IllegalStateException`.

## y

Vertical position of the anchor point of the node, in design pixels, from the top edge of its parent (or of the canvas for a root or `ABSOLUTE` node).
It binds to a signal like `x`.

## width

Width of the node, in design pixels.
`0` lets a node that measures its content (text, image, flex, grid) take the width of its content.

## height

Height of the node, in design pixels.
`0` lets a node that measures its content take the height of its content.

## position

`RELATIVE` places the node inside its parent; `ABSOLUTE` places it on the canvas whatever its parents, out of their layout, their scroll and their content size.

## anchor

Sets `anchorX` and `anchorY` to the same value.

## anchorX

Which point of the node sits at `x` and stays in place when the width changes: `START` the left edge, `CENTER` the middle, `END` the right edge.

## anchorY

Which point of the node sits at `y` and stays in place when the height changes: `START` the top edge, `CENTER` the middle, `END` the bottom edge.

## aspectRatio

Width divided by height that the node keeps on every frame: JOID computes the height from the width, or the width from the height when the width is `0`.
A value of `0` or below turns it off.

Default: none, the width and the height stay free

## zindex

Order of the node among its siblings: a higher value is drawn above and receives the input first.
A negative value draws the child behind its parent.

## zlevel

Depth of the node along the Z axis, added to the depth of its parent.

## skeleton

Placeholder node drawn instead of this node and its children while one of its `wait` conditions fails; the function receives the node and returns the placeholder, or `null` for the default skeleton.

## visible

If `false`, the node and its children are neither drawn nor sent any input, and a layout node leaves them out.

## enabled

If `false`, the node and its children are drawn but ignore the input and show no hover progress.

## interactive

If `false`, the node and its children let the mouse through to what is under them and are never hovered.

## cursor

Cursor shown while the mouse is over the node; `null` uses the cursor of the closest parent that has one.

Default: `null`: the cursor of the closest parent that sets one, `Cursor.DEFAULT` when none does

## hoverDuration

Time, in ms, that the hover progress takes to go from `0` to `1` and back.

## hoverEquation

Easing of the hover progress.

## hover

Adds a tooltip shown while the node is hovered: a [[IHoverElement]], a [[HoverSupplier]] that gives one line (`null` for none), or a `Supplier` of lines (`null` for none) drawn by [[UI#drawHover]].
Each call adds one more element.

## overflow

What the node does with children that go past its bounds: `NONE` draws them, `HIDDEN` clips them, `SCROLL` clips them and lets the wheel scroll them.

## scrollSpeed

Factor applied to the wheel: one notch scrolls 30 design pixels times this factor, twice as far with Control down.

## scrollbar

Scrollbar that scrolls this node; it is drawn when the overflow is `SCROLL` and the content goes past the node.

## label

Text drawn centered over the node, after the node itself and before its children; `null` draws none.

Default: `null`: no label

## draggable

Lets the user drag the node with the left button, inside the area and with the movement and the snapping of the [[DraggableProperty]]; `null` means the node cannot be dragged.

Default: `null`: the node cannot be dragged

## load(UI)

Attaches the node to the UI: loads its children, its scrollbar, its skeleton and its effects, runs `init` and fires `onInit`.
Throws an `IllegalStateException` when a required part or setting is missing; JOID calls it for you when the node joins an open UI.

## render(double,double)

Draws the node for this frame: applies its layout, its hover and its scroll, then draws its children behind it, itself, its label, its children in front and its layers, through its effects.
Its parent or its UI calls it; you never call it.

## renderHover(double,double)

Draws the tooltips of the node at the mouse and returns `true` when it has one.
The UI calls it on the hovered node, then on its parents until one draws.

## updateScroll()

Moves the shown scroll to its target at once, on both axes, without the smooth movement.

## updateScrollX()

Moves the shown horizontal scroll to its target at once.

## updateScrollY()

Moves the shown vertical scroll to its target at once.

## append(Node[])

Adds children to the node, in front of their siblings of the same `zindex`.
A node that has another parent or that is a root of a UI leaves it first; a child of this node moves to the front without being loaded again.
The children are loaded at once when the node is attached, and when it joins a UI otherwise; `onChildAppend` fires for each new child.

## remove(Node[])

Detaches these children from the node; a node that is not its child is ignored.

## attach(UI)

Attaches the node at the root of the UI, as [[UI#add]] does.

## attach(Node)

Adds the node to the children of this parent, as [[Node#append]] does.

## body(Runnable)

Runs the code at once, to create the children where the node is created.

## body(Consumer)

Runs the code at once with the node, to create its children; JOID keeps it to run it again when a `watch` rebuilds the node.

## self(Consumer)

Runs the code at once with the node, to set it up without breaking the chain.

## clearChildren()

Detaches every child of the node.

## getChild(int,Class)

The child at this index among the children of this class, or `null` when there is none at that index.

## getChildren()

The children of the node, in the order they were added; a read-only view.

## getChildren(Class)

The children of the node that are of this class, in the order they were added.

## getNodeListAt(double,double)

The node and every visible child under this point of the canvas, from the front-most to the back-most.

## getParent()

The parent of the node, or `null` for a root.

## isAttached()

If `true`, the node is loaded in a UI.

## getUi()

The UI of the node, or the UI whose code is running ([[UI#getCurrent]]) when the node is not attached yet.

## getAbsoluteX()

Horizontal position of the top-left corner of the node on the canvas, with the positions of its parents, their layout and their scroll.

## getAbsoluteY()

Vertical position of the top-left corner of the node on the canvas.

## getAbsoluteDefaultX()

Position on the canvas of the declared `x` of the node, before its layout and its anchor.

## getAbsoluteDefaultY()

Position on the canvas of the declared `y` of the node, before its layout and its anchor.

## toDrawX(double)

Converts a horizontal position of the canvas to the coordinates in which the node draws, where its left edge is `getX()`.

## toDrawY(double)

Converts a vertical position of the canvas to the coordinates in which the node draws.

## w()

The current width of the node.

## h()

The current height of the node.

## dw(double)

The width divided by the value: `dw(2D)` is half the width.

## dh(double)

The height divided by the value.

## mw(double)

The width multiplied by the value: `mw(0.25D)` is a quarter of the width.

## mh(double)

The height multiplied by the value.

## aw(double)

The width plus the value.

## ah(double)

The height plus the value.

## ax(double)

The current `x` plus the value.

## ay(double)

The current `y` plus the value.

## getDefaultWidth()

The width declared by the constructor or by `width(...)`; `0` when the width comes from the content.

## getDefaultHeight()

The height declared by the constructor or by `height(...)`; `0` when the height comes from the content.

## getDefaultX()

The `x` declared by the constructor, `x(...)`, `move` or a drag.

## getDefaultY()

The `y` declared by the constructor, `y(...)`, `move` or a drag.

## getLayoutX()

Horizontal offset that the layout of the parent adds to the declared position.

## getLayoutY()

Vertical offset that the layout of the parent adds to the declared position.

## getShiftX()

Horizontal offset that the scroll of the parent adds.

## getShiftY()

Vertical offset that the scroll of the parent adds.

## removeHover(IHoverElement)

Removes a tooltip element added with `hover(...)`.

## clearHover()

Removes every tooltip of the node.

## getResolvedCursor()

The cursor shown over the node: its own, the one of its closest parent that has one, or the default arrow.

## hoverValue(float)

The value multiplied by the hover progress, from `0` when the mouse is away to the value when the hover has fully faded in.

## getHoverAnimator()

The animator that holds the hover progress, from `0` to `1`.

## isHovered()

If `true`, the mouse is over the node or one of its children, and its UI is on top.

## hoverColor(Color,Color)

The first color blended toward the second by the hover progress; a `null` second color returns the first.

## handleMousePressed(EventListener)

Adds a listener that hears every mouse press of the UI, wherever the pointer is, after the children of the node in front and before its children behind; check `isHovered()` to react only over the node.
It does not run while the node is hidden, disabled or not interactive.
Consuming the event stops the nodes behind, the UI and the host; a `MONITOR` listener always hears it.

## handleMouseReleased(EventListener)

Adds a listener that hears a mouse release, wherever the mouse is.
It runs even on a hidden or disabled node, so that a gesture always ends.

## handleMouseDragged(EventListener)

Adds a listener that hears a mouse move while a button is held.
It does not run while the node is hidden, disabled or not interactive.

## handleMouseScroll(EventListener)

Adds a listener that hears every wheel turn of the UI, wherever the pointer is, before the node scrolls; check `isHovered()` to react only over the node.
It does not run while the node is hidden, disabled or not interactive.

## handleCharTyped(EventListener)

Adds a listener that hears a typed character, as a Unicode code point.
It does not run while the node is hidden or disabled, nor while it is scrolled out of view unless it holds the input.

## handleKeyPressed(EventListener)

Adds a listener that hears a key press.
It does not run while the node is hidden or disabled, nor while it is scrolled out of view unless it holds the input.

## handleKeyReleased(EventListener)

Adds a listener that hears a key release.
It runs even on a hidden or disabled node, so that a held key always ends.

## getLastClickTime()

Time of the last mouse press the node received, in ms of the clock bridge.

## getLastClickButton()

Button of the last mouse press the node received, or `null`.

## getLastKey()

The last key pressed while the node received the keyboard, or `null`.

## getLastKeyTime()

Time of the last key press the node received, in ms of the clock bridge.

## getLastCodepoint()

The last character the node received, as a Unicode code point.

## scrollX(double)

Sets the horizontal scroll, in design pixels: `0` shows the start, a negative value moves the content to the left.
The value is kept between the end of the content and `0`; a positive value warns once in dev mode.

## scrollX(Signal)

Binds the horizontal scroll to a signal: the node follows it, and a scroll by the user writes back to it.

## scrollY(double)

Sets the vertical scroll, in design pixels: `0` shows the top, a negative value moves the content up.
The value is kept between the end of the content and `0`; a positive value warns once in dev mode.

## scrollY(Signal)

Binds the vertical scroll to a signal, both ways.

## scrollTo(double,double)

Scrolls to these offsets as a change: the `beforeScroll` listeners can refuse it, and the content moves there smoothly.

## beforeScroll(EventListener)

Adds a listener that runs before the scroll changes, by the wheel, a scrollbar or `scrollTo`; cancelling the event refuses the change.

## onScroll(EventListener)

Adds a listener that runs after the scroll has changed.

## onScrollToEnd(EventListener)

Adds a listener that runs when a scroll heads for the end of the content, before the content gets there: the moment to load more.

## onScrollEndReached(EventListener)

Adds a listener that runs when the shown content has reached the end after a scroll toward it.

## hasOverflowX()

If `true`, the overflow is `SCROLL` and the content is wider than the node.

## hasOverflowY()

If `true`, the overflow is `SCROLL` and the content is taller than the node.

## getOverflowArea()

The node whose `HIDDEN` or `SCROLL` overflow clips this one, or `null` when nothing clips it.

## getScrollX()

Horizontal scroll shown on this frame, moving toward the target.

## getScrollY()

Vertical scroll shown on this frame, moving toward the target.

## getTargetScrollX()

Horizontal scroll that the content moves to.

## getTargetScrollY()

Vertical scroll that the content moves to.

## getMaxScrollX()

How far the content can scroll horizontally, in design pixels.

## getMaxScrollY()

How far the content can scroll vertically, in design pixels.

## isScrollEndX()

If `true`, a horizontal scroll toward the end has not reached it yet.

## isScrollEndY()

If `true`, a vertical scroll toward the end has not reached it yet.

## startDragging(double,double)

Starts a drag as if the user had pressed the left button at this point of the canvas; the `beforeDragStart` listeners can refuse it.
Throws an `IllegalStateException` when the node has no draggable property, when it is disabled, or when the area is `PARENT` and the node has no parent.

## stopDragging()

Drops the node as if the user had released the button: it snaps to a target, or goes back to its start when it has targets and none fits or a `beforeDragDrop` listener refused.

## fireDragStart(Runnable)

Runs the `beforeDragStart` listeners, then the start and the `onDragStart` listeners unless a listener cancelled; returns `false` when it was cancelled.
A node that runs a drag of its own, such as a reorderable list, calls it.

## fireDrag(Runnable)

Runs the move of the drag, then the `onDrag` listeners.

## fireDragDrop(Node,Runnable)

Runs the `beforeDragDrop` listeners, then the drop and the `onDragDrop` listeners unless a listener cancelled; returns `false` when it was cancelled.
`target` is the snap target, or `null`.

## beforeDragStart(EventListener)

Adds a listener that runs before a drag starts; cancelling the event keeps the node in place.

## onDragStart(EventListener)

Adds a listener that runs once a drag has started.

## onDrag(EventListener)

Adds a listener that runs at every mouse move of the drag.

## beforeDragDrop(EventListener)

Adds a listener that runs when the user releases the node; cancelling the event sends the node back to where the drag started.

## onDragDrop(EventListener)

Adds a listener that runs once the node has been dropped; the event holds the snap target, or `null`.

## onDragSnap(EventListener)

Adds a listener that runs when the node is dropped on one of its snap targets, before `onDragDrop`; a `beforeDragDrop` listener that cancels skips it.

## isDragging()

If `true`, the user holds the node, or a reorderable list drags it.

## isDragged()

If `true`, the node is held or is still moving to where it was dropped.

## getDragX()

Horizontal distance between the left edge of the node and the point where it was grabbed.

## getDragY()

Vertical distance between the top edge of the node and the point where it was grabbed.

## getDragSource()

For the copy shown by a `COPY` drag, the node it copies; `null` for any other node.

## getDraggedNode()

The copy that follows the mouse during a `COPY` drag, or `null`.

## getStartDragX()

Horizontal position of the node on the canvas when the drag started.

## getStartDragY()

Vertical position of the node on the canvas when the drag started.

## getTargetDragX()

Horizontal position on the canvas that the dragged node moves to.

## getTargetDragY()

Vertical position on the canvas that the dragged node moves to.

## isStepping()

If `true`, the drag animation is moving the node during this frame.

## move(double,double)

Moves the node to this declared position as a change: the `beforeMove` listeners can refuse it, bound signals are written, and a running drop animation stops there.

## beforeMove(EventListener)

Adds a listener that runs before the node moves, by a drag or `move`; cancelling the event keeps the node in place.

## onMove(EventListener)

Adds a listener that runs after the node has moved.

## effect(NodeEffect)

Adds an effect to the node; an effect of the same class takes the place of the previous one.

## removeEffect(Class)

Removes the effects of this class and of its subclasses.

## clearEffects()

Removes every effect of the node.

## hasEffect(Class)

If `true`, the node has an effect of this class or of a subclass.

## getEffect(Class)

The effect of this class or of a subclass, or `null`.

## layer(NodeLayer)

Adds a drawing done after the children, in the coordinates of the node: `(0, 0)` is its top-left corner.

## layer(int,NodeLayer)

Inserts a drawing at this place among the layers of the node.

## clearLayers()

Removes every layer of the node.

## animate(TweenAnimator)

Follows an animator: `onAnimate` fires on every frame where its value changes.

## removeAnimator(TweenAnimator)

Stops following an animator.

## onAnimate(EventListener)

Adds a listener that runs when the value of a followed animator changes, during the update of the node.

## wait(Predicate)

Adds a condition that the node needs before it is mounted: until every condition holds, the skeleton is drawn instead of the node and its children.

## isMounted()

If `true`, every `wait` condition of the node and of its parents holds.

## onMount(EventListener)

Adds a listener that runs on each frame where the node becomes mounted: the first frame where it is drawn mounted, then again after a detach or after a `wait` condition stopped holding.

## watch(Signal,WatchProperty[])

Applies these properties each time the signal changes, such as running the body again to rebuild the children.

## watch(Signal,Supplier,WatchProperty[])

Applies these properties each time the signal changes while the condition gives `true`.

## onWatch(EventListener)

Adds a listener that runs after a `watch` of the node has applied its properties.

## hasListener(EventType)

If `true`, the node has a listener for this event type.

## isVisibleProperty()

If `true`, the node itself is set visible, whatever its parents and its clipping.

## getIndex()

The `zindex` of the node, or the highest value while it is dragged with `DraggableType.MOVE` so that it is drawn above its siblings.

## getHierarchy()

The class names from the root down to the node, such as `FlexNode - RectNode`.

## getMappedIndex()

The path of the node in the tree, its index at each level, such as `0.2.1`; `N/A` when it is not attached.

## getTaskList()

The tasks attached to the node; a read-only view.

## getShortcutList()

The shortcuts attached to the node; a read-only view.

## toJson()

The position and the size of the node as JSON, with its state in dev mode.

## toString()

The JSON of `toJson()`, indented in dev mode.

## getEventBus()

The listeners of the node.

## getBodyConsumer()

The code given to `body(Consumer)`, or `null`.

## getLoadCount()

How many times the node has been loaded into a UI.

## getRenderTime()

Time that the last frame of the node took to draw, children included, in nanoseconds.

## copy()

Returns an unattached copy of the node and of its children, with their properties, listeners and effects.
A field that holds a node of the copied tree holds its copy; `transient` fields are left out.
Throws an `IllegalStateException` when the class has no constructor `(x, y, width, height)`, `(x, y)` or without argument and does not override `instantiate()`.

## useStore(Class,Supplier)

Returns the store of this class from the UI of the node, as [[UI#useStore]] does.

## addTask(Task)

Attaches a task to the node; it runs while the node belongs to an open UI.

## removeTask(Task)

Detaches a task from the node.

## addShortcut(Shortcut)

Attaches a shortcut to the node; it fires while the node is visible and enabled.

## removeShortcut(Shortcut)

Detaches a shortcut from the node.

## layoutPosition(double,double)

Sets the offset that a layout adds to the declared position of the node.
A layout node or a control calls it on its children in `layout()`.

## layoutSize(double,double)

Sets the size of the node for this frame without changing its declared size.

## fit(double,double)

Gives the node the size of its content on each axis whose declared size is `0`, and keeps the current size on the other.

## onClick(EventListener)

Adds a listener that runs when the left button is pressed on the hovered node, unless a node in front consumed the press; the press is then consumed.

## onInit(EventListener)

Adds a listener that runs when the node is loaded into a UI, after `init`.

## onUpdate(EventListener)

Adds a listener that runs on every frame, after the children and the `update` hook.

## onDraw(EventListener)

Adds a listener that runs after the node has drawn itself, before its label and its children in front.

## onRender(EventListener)

Adds a listener that runs at the end of the drawing of the node, after its children and its layers.

## onChildAppend(EventListener)

Adds a listener that runs when a child is added to the node; moving an existing child to the front does not fire it.

## onDetach(EventListener)

Adds a listener that runs when the node leaves its UI, after its children and its own `detach`.

## refreshHover(double,double)

Reads again whether the node is hovered, from the hovered path of its UI, and starts its hover animation and fires `onHoverStart` or `onHoverEnd` when that changed; the mouse is at this point of the design canvas.
The node calls it while it draws, and `UI.moveMouse` at every mouse event.

## onHoverStart(EventListener)

Adds a listener that runs when the mouse enters the node.

## onHover(EventListener)

Adds a listener that runs on every frame while the node is hovered.

## onHoverEnd(EventListener)

Adds a listener that runs when the mouse leaves the node, or when the node is hidden or detached while hovered.

## drawSkeleton(double,double)

Draws the default skeleton, a rectangle of the loading color on the bounds of the node.

## fireUpdate()

Updates the node and its children for this frame: pulls its reactive values, runs `update` and fires `onUpdate`.
Its UI calls it once per frame; an `update` that throws prints a warning once.

## fireDetach()

Detaches the node and its children from their UI: runs `detach`, ends the drag and the hover, detaches the effects and fires `onDetach`.

## fireMousePressed(double,double,MouseButton)

Sends a mouse press at this point to the node and its children, as the UI does, and returns `true` when something consumed it; use it to simulate an input.

## fireMousePressed(double,double,MouseButton,IConsumableEvent)

Sends a mouse press that belongs to this source event: consuming it consumes the source.

## fireMouseReleased(double,double,MouseButton,long)

Sends a mouse release of this button, pressed `deltaTime` ms before, to the node and its children and returns `true` when something consumed it.

## fireMouseReleased(double,double,MouseButton,long,IConsumableEvent)

Sends a mouse release that belongs to this source event.

## fireMouseDragged(double,double,MouseButton,long)

Sends a mouse move with this button held for `deltaTime` ms and returns `true` when something consumed it.

## fireMouseDragged(double,double,MouseButton,long,IConsumableEvent)

Sends a mouse move that belongs to this source event.

## fireMouseScroll(double,double,double,double)

Sends a wheel turn, in notches, and returns `true` when something consumed it.

## fireMouseScroll(double,double,double,double,IConsumableEvent)

Sends a wheel turn that belongs to this source event.

## fireCharTyped(int)

Sends a typed character and returns `true` when something consumed it.

## fireCharTyped(int,IConsumableEvent)

Sends a typed character that belongs to this source event.

## fireKeyPressed(Key,Key)

Sends a key press, its position on the keyboard then the key that the layout gives, and returns `true` when something consumed it.

## fireKeyPressed(Key,Key,IConsumableEvent)

Sends a key press that belongs to this source event.

## fireKeyReleased(Key,Key)

Sends a key release and returns `true` when something consumed it.

## fireKeyReleased(Key,Key,IConsumableEvent)

Sends a key release that belongs to this source event.

## listen(EventType,EventListener)

Adds a listener for an event type of the subclass; the `onX` methods of a node of your own call it.

## fire(EventType,Supplier)

Fires an event of the subclass to its listeners and returns it, or `null` when no listener exists, in which case the event is never built.

## start(String,V,Consumer)

Sets a property to a plain value and applies it, replacing a previous `follow` or `bind` of the same property.

## follow(String,Supplier,Consumer)

Makes a property follow a `Supplier`: the applier receives each new value, when the signals it reads change or on every frame for a lambda.

## bind(String,Signal,Consumer)

Binds a property to a signal: the applier receives each value of the signal.

## bind(String,Signal,Consumer,Function)

Binds a property to a signal both ways: `sync` writes the value back through the writer.

## bindNullable(String,Signal,Consumer,Function)

Binds a property to a signal both ways, accepting `null` values.

## sync(String,Object)

Writes the value applied to a bound property back to its signal.

## require(String,String,Predicate)

Declares a setting that the node needs: when the node loads without it, it throws an `IllegalStateException` that names the setting and gives the advice.

## part(Node,N)

Replaces a part of the node, a child that the subclass places itself, by another one at the same place among the children, and returns it.

## reorderChildren(List)

Puts the children in this order, for drawing and for the layout.

## instantiate()

Creates the instance that `copy()` fills: it tries the constructors `(x, y, width, height)`, `(x, y)` and without argument.
Override it when the class has none of them.

## holdsInput()

If `true`, the node keeps receiving the input while it is scrolled out of the area of its container; `false` by default.
[[FieldNode]] holds it while it has the focus or a selection goes on, [[BaseSelectorNode]] while it is open.

## isDraggingChild(Node)

If `true`, the parent raises this child above its siblings because it is being dragged; a layout node that drags its children overrides it.

## getBindings()

The reactive bindings of the node's properties, for a subclass.
