# Custom Nodes

A custom node extends `Node` (or any built-in node) and overrides its hooks: `draw` for the visuals, `init` and `update` for state, the input hooks for interaction. Write one when no built-in node and no [effect](../styling/effects.md) gives you the visual or the behavior you need.

```java
public class DotNode extends Node {

	protected DotNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DotNode create(final double x, final double y, final double size) {
		return new DotNode(x, y, size, size);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawCircle(super.getX() + super.dw(2D), super.getY() + super.dh(2D), Color.decode("#DDDDDD"), super.dw(2D));
		DrawUtils.SHAPE.drawCircle(super.getX() + super.dw(2D), super.getY() + super.dh(2D), Color.decode("#999999"), super.dw(4D));
	}

}
```

```java
DotNode.create(100, 100, 40).attach(this);
DotNode.create(160, 100, 40).attach(this);
DotNode.create(220, 100, 40).attach(this);
```

![Three light gray disks, each with a smaller gray disk at its center](../images/custom-node-minimal.png "Each DotNode draws two circles in its bounds")

The node attaches, nests and positions like any built-in node. `draw` runs every frame and paints with [`DrawUtils`](../drawing/drawing.md) at the node's own position; `dw(2D)` is half the width.

## A complete node

A color swatch that the user selects with a click. It has reactive setters, an input hook, a two-way signal and a callback of its own, `onSelect`. First the callback interface:

```java
@FunctionalInterface
public interface NodeSwatchSelectCallback<T extends SwatchNode> extends NodeCallback {

	public void apply(final @NonNull T node, final boolean selected);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final boolean selected) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final boolean selected) {
		context.cancel(() -> this.apply(node, selected));
	}

}
```

Then the node:

```java
@SuppressWarnings("unchecked")
public class SwatchNode extends Node {

	private static final int CALLBACK_SELECT = NodeCallbackRegistry.next(NodeSwatchSelectCallback.class);

	private Supplier<Color> color;
	private boolean         selected;

	private Signal<Boolean>            signal;
	private ISignalSubscriber<Boolean> subscription;

	protected SwatchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		this.color = Signal.from(Color.decode("#DDDDDD"));
	}

	public static @NonNull SwatchNode create(final double x, final double y, final double size) {
		return new SwatchNode(x, y, size, size);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.selected) {
			DrawUtils.SHAPE.drawRect(super.getX() - 4D, super.getY() - 4D, super.getWidth() + 8D, super.getHeight() + 8D, Color.WHITE);
		}

		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.color.get().to(Color.BLACK, super.hoverValue(0.2F)));
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (context.isCancelled() || !button.isLeft() || !super.isHovered()) {
			return;
		}

		context.cancel(() -> super.executeCallback(SwatchNode.CALLBACK_SELECT, DispatchContext.create(), () -> {
			this.selected = !this.selected;
			super.sync(this.signal, this.selected);
		}, !this.selected));
	}

	public final <T extends SwatchNode> @NonNull T color(final @NonNull Color color) {
		return this.color(Signal.from(color));
	}

	public final <T extends SwatchNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends SwatchNode> @NonNull T selected(final boolean selected) {
		return this.selected(Signal.from(selected));
	}

	public final <T extends SwatchNode> @NonNull T selected(final @NonNull Supplier<@NonNull Boolean> selected) {
		return super.follow("selected", selected, value -> this.selected = value);
	}

	public final <T extends SwatchNode> @NonNull T signal(final @NonNull Signal<Boolean> signal) {
		this.signal = signal;
		this.subscription = super.rebind(this.subscription, signal, value -> this.selected = Boolean.TRUE.equals(value));
		return (T) this;
	}

	public final <T extends SwatchNode> @NonNull T onSelect(final @NonNull NodeSwatchSelectCallback<T> callback) {
		return super.registerCallback(SwatchNode.CALLBACK_SELECT, callback);
	}

	public final boolean isSelected() {
		return this.selected;
	}

}
```

And its use in a UI:

```java
private final IntegerSignal level = IntegerSignal.of(0);

SwatchNode
.create(100, 100, 40)
.color(this.level.get() > 2 ? Color.WHITE : Color.GRAY)
.onSelect((swatch, selected) -> System.out.println("Selected: " + selected))
.attach(this);
```

![A gray swatch that darkens on hover and gets a white frame when clicked](../images/custom-node-swatch.gif "The swatch follows the hover and fires onSelect")

## Constructor and factory

- Give the node a `protected` constructor and a `public static` factory that returns the concrete type, usually `create(...)`. `Node` has the constructors `(x, y)` and `(x, y, width, height)`.
- Keep a constructor `(double, double, double, double)`, `(double, double)` or `()`: `copy()` and copy drags build copies through it.
- Setters are `public final <T extends YourNode> T name(...)` ending with `return (T) this;`, with `@SuppressWarnings("unchecked")` on the class. Name them after the property (`color(...)`, not `setColor(...)`).

## Hooks you can override

Every hook does nothing by default: override only those you need.

| Hook | Called |
| --- | --- |
| `init(UI ui)` | On every load: when the node joins a UI, when the UI opens or reloads. Keep it repeatable. |
| `draw(double mouseX, double mouseY)` | Every frame while the node is visible and mounted. |
| `drawSkeleton(double mouseX, double mouseY)` | Instead of `draw` while the node is not mounted. Fills the bounds with `Color.LOADING()` by default. |
| `update()` | On every update tick, also for hidden nodes. |
| `detach()` | When the node is detached or its UI closes: release threads and resources here. |
| `mousePressed`, `mouseReleased` `(double mouseX, double mouseY, MouseButton button, DispatchContext context)` | Mouse button pressed or released. |
| `mouseDragged(double mouseX, double mouseY, MouseButton button, long deltaTime, DispatchContext context)` | Mouse moved with a button held; `deltaTime` in milliseconds since the press. |
| `mouseScroll(double mouseX, double mouseY, double notchesX, double notchesY, DispatchContext context)` | Mouse wheel, in notches. |
| `keyPressed(Key key, DispatchContext context)` | Key pressed or repeated. |
| `charTyped(int codepoint, DispatchContext context)` | Character typed, one code point per call. |

In `draw`, the matrix is at the parent's origin: draw at `getX()`, `getY()` with `getWidth()`, `getHeight()`, in canvas units. `hoverValue(float max)` returns `max` times the hover animation progress, to blend colors or sizes on hover.

## Handling input with DispatchContext

Input hooks run on every visible, interactive node, wherever the mouse is: check `isHovered()`, which is `true` when the node is the mouse target or one of its parents. One `DispatchContext` travels through the whole dispatch. A node that handles the event cancels it; the nodes after it see `isCancelled()` and step aside.

![Diagram of the order in which a node and its children see a mouse press](../images/diagram-input-dispatch.png "Dispatch order of a mouse press")

| Method | Description |
| --- | --- |
| `isCancelled()` | `true` when a node already consumed the event. |
| `cancel()`, `cancel(Runnable runnable)` | Consumes the event; the second form first runs `runnable`, only when not cancelled yet. |
| `cancelIf(Supplier<Boolean> supplier)` | Cancels when not cancelled yet and `supplier` returns `true`. |
| `execute(Runnable runnable)` | Runs `runnable` when not cancelled, without cancelling. |
| `reset()` | Clears the cancellation. |
| `DispatchContext.create()` | A fresh context, to fire your own callbacks. |

## Firing your own callbacks

1. **The interface** extends `NodeCallback`, is a `@FunctionalInterface` with one `apply` method, and has two default methods annotated `@NodeCallbackMethod(Phase.PRE)` and `@NodeCallbackMethod(Phase.POST)`. Both return `void` and take the node, a `DispatchContext`, then the event arguments. The POST phase calls `apply` through `context.cancel(...)`.
2. **The id**: `NodeCallbackRegistry.next(YourCallback.class)` validates the interface and returns an id, kept in a `static final int`.
3. **The registration**: an `onX` setter stores the callback with `super.registerCallback(id, callback)`.
4. **The firing**: `executeCallback(id, context, runnable, args...)` runs the PRE phases, then, unless a PRE phase cancelled the context, `runnable` and the POST phases. Pass `DispatchContext.create()` for an action of your own.

For an event that only passes the node, reuse `NodeEventCallback<T>`:

```java
@SuppressWarnings("unchecked")
public class DrawerNode extends Node {

	private static final int CALLBACK_OPEN = NodeCallbackRegistry.next(NodeEventCallback.class);

	private boolean opened;

	protected DrawerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DrawerNode create(final double x, final double y, final double width, final double height) {
		return new DrawerNode(x, y, width, height);
	}

	public final <T extends DrawerNode> @NonNull T onOpen(final @NonNull NodeEventCallback<T> callback) {
		return super.registerCallback(DrawerNode.CALLBACK_OPEN, callback);
	}

	public final <T extends DrawerNode> @NonNull T open() {
		super.executeCallback(DrawerNode.CALLBACK_OPEN, DispatchContext.create(), () -> this.opened = true);
		return (T) this;
	}

	public final boolean isOpened() {
		return this.opened;
	}

}
```

## Reactive setters and signals

Each property setter comes in pairs, like the built-in ones: the value overload only calls `this.x(Signal.from(value))`, so the caller's expression is followed; the `Supplier` overload stores the source.

- A value read while drawing (`color`): keep the `Supplier` in a field and call `get()` in `draw`.
- Any other property (`selected`): `super.follow("name", supplier, consumer)` applies the value at once, then calls `consumer` each time the source changes.
- A two-way binding (`signal`): `super.rebind(previous, signal, consumer)` follows the signal while the UI is open and replaces the previous subscription; `super.sync(signal, value)` writes the signal when it holds another value. `bind` and `unbind` add or remove a subscription.

## Reference

| Member | Description |
| --- | --- |
| `dw(double)`, `dh(double)` | Width or height divided by a value. |
| `registerCallback(int, NodeCallback)` | Protected. Stores a callback; returns the node. |
| `executeCallback(int, DispatchContext, Runnable, Object...)` | PRE phases, action, POST phases. Without the `Runnable`: the phases only. |
| `executePreCallback`, `executePostCallback` | One phase only. |
| `hasCallback(int)`, `getCallbackList(int)` | Registered callbacks. |
| `follow(String, Supplier<V>, Consumer<V>)` | Protected. Applies the value now and on each change of the source. |
| `bind`, `unbind`, `rebind` | Protected. Signal subscriptions that follow the node's UI. |
| `sync(Signal<V>, V)` | Protected. Sets the signal when it is not `null` and holds another value. |
| `NodeCallbackRegistry.next(Class)` | Validates a callback interface and returns its new id. |

## Good to know

- `NodeCallbackRegistry.next` throws when the interface misses a phase or `@FunctionalInterface`: the node class fails to load.
- A value setter that computes before `Signal.from` (`Signal.from("> " + text)`) cannot follow the caller's expression: pass the parameter as is.
- A two-way `signal(...)` receives its own writes back: make the consumer harmless for an unchanged value.

## See also

- [Drawing](../drawing/drawing.md)
- [Nodes](../concepts/nodes.md)
- [Input](../concepts/input.md)
- [Signals and State](../concepts/state.md)
- [Custom Effects](../styling/custom-effects.md)