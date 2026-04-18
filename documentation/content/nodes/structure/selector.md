# SelectorNode

Dropdown-style option selector. `SelectorNode` is **abstract** — you subclass it to render the background of the collapsed and expanded states. Each option is a regular child `Node` attached to the selector; clicking an unselected child picks it, clicking the selected one collapses the dropdown.

## Usage

```java
public class MySelector extends SelectorNode {

    public MySelector(final double x, final double y, final double w, final double h) {
        super(x, y, w, h);
    }

    @Override
    public void drawBackground(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            Color.decode("#1f2937"), 6F);
    }
}
```

Attach options as children:

```java
final MySelector picker = new MySelector(40, 40, 240, 36);
picker.direction(SelectorDirection.DOWN)
      .selected(optionRed)                // pre-select a specific child
      .onChange((sel, selectedNode) -> System.out.println("picked " + selectedNode));

optionRed.attach(picker);
optionGreen.attach(picker);
optionBlue.attach(picker);
picker.attach(parent);
```

Children are drawn stacked: the selected one at `y = 0`, the rest below (or above, with `SelectorDirection.UP`) when the selector is `active`.

## API

```java
T direction(SelectorDirection direction)   // UP or DOWN (default DOWN)
T active(boolean active)                   // open / close the dropdown
T selected(Node selected)                  // force a selection

Node getSelected()
boolean isActive()
SelectorDirection getDirection()
boolean isSelected(Node node)

T onChange(NodeSelectorChangeCallback<T> callback)
abstract void drawBackground(double mouseX, double mouseY)
```

`SelectorNode` has **no** `options(...)`, `value(String)`, `placeholder`, `backgroundColor`, `hoveredColor`, `textInfo`, `maxVisibleOptions`, or `searchable` setters — those features do not exist on this class. Each option's look is the responsibility of the child `Node`.

## `SelectorDirection`

```java
SelectorDirection.UP     // options open upward
SelectorDirection.DOWN   // options open downward (default)
```

The inner helper `isDown()` returns `true` for `DOWN`.

## See also

- `ToggleNode` / `SwitchNode` — simpler alternatives when there are few options.