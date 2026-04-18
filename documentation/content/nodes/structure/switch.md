# SwitchNode

A multi-state switch that cycles through a list of named states. `SwitchNode` is **abstract** — you subclass it to provide the rendering. The base class owns the state list (`ListSignal<String>`), the current index (`IntegerSignal`), and re-renders the node whenever either signal changes.

## Usage

```java
public class MySwitch extends SwitchNode {

    public MySwitch(final double x, final double y, final double w, final double h) {
        super(x, y, w, h);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            Color.decode("#1f2937"), 8F);
        DrawUtils.TEXT.drawText(getX() + getWidth() / 2, getY() + getHeight() / 2,
            getState(), info, Align.CENTER, Align.CENTER);
    }
}
```

Then use it:

```java
new MySwitch(0, 0, 200, 40)
    .state("low", "medium", "high", "ultra")
    .index("medium")
    .onChange((node, next) -> System.out.println("switched to " + next))
    .attach(parent);
```

## API

```java
T state(String... states)
T state(List<String> states, String active)
T state(List<String> states, int index)

T index(String state)              // jump to a named state
T index(int index)                 // jump by index

String getState()                  // currently active state string
ListSignal<String> getStateList()
IntegerSignal getStateIndex()

T onChange(NodeSwitchChangeCallback<T> callback)
```

All `state(...)` factories reset the current index. The two `index(...)` variants fire the change callback through `executeCallback`.

## Behavior

The node internally watches `stateList` and `stateIndex` with `WatchProperty.CLEAR_CHILDREN` + `WatchProperty.RELOAD`, so any change to the list or index rebuilds children and reruns `init`. Implement click cycling in your subclass if you want the switch to advance on each click.

## Example — graphics preset

```java
public class GraphicsSwitch extends MySwitch {
    @Override
    public void mousePressed(double mx, double my, ClickType ct, InternalContext ctx) {
        if (!isHovered(mx, my)) return;
        ctx.cancel(() -> {
            int next = (getStateIndex().getOrDefault() + 1) % getStateList().size();
            index(next);
        });
    }
}
```

## See also

- `ToggleNode` — two-value toggle.
- `CheckboxNode` — boolean.
- `SelectorNode` — dropdown for long option lists.