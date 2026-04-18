# ScrollbarNode

Visual scrollbar for a parent with `overflow(OverflowProperty.SCROLL)`. Auto-tracks scroll position and supports click-to-drag.

## Create

```java
RectNode.create(0, 0, 400, 300)
    .overflow(OverflowProperty.SCROLL)
    .body(wrapper -> {
        // your long content here
        FlexNode.vertical(0, 0, 400).body(list -> { /* ... */ }).attach(wrapper);
        
        ScrollbarNode.create(392, 0, 8, 300).attach(wrapper);
    })
    .attach(parent);
```

## Automatic linking

`ScrollbarNode` finds its parent automatically — no need to pass a reference. It updates `scrollHeight` and cursor position based on the parent's scroll offset and total content height.

## Styling

```java
scrollbar.color(Color trackColor, Color thumbColor);
scrollbar.effect(RoundedNodeEffect.create(4F));
```

## Scroll behavior

The parent's `scrollY` is updated on mouse wheel, and the scrollbar cursor follows. Click-and-drag on the thumb also moves the parent's scroll.

Control scroll speed on the parent:

```java
parent.scrollSpeed(2D);   // pixels per wheel tick multiplier
```

## Hiding when not needed

The scrollbar hides automatically when the content doesn't overflow. You can force visibility:

```java
scrollbar.visible(true);  // always shown
```

## Horizontal scrolling

Scrollbars are vertical by default; for horizontal use a wider-than-tall rect:

```java
ScrollbarNode.create(0, 292, 400, 8).horizontal(true).attach(wrapper);
```

## Best practices

- **Place the scrollbar last** among siblings to ensure it draws on top.
- **Use 6–10px widths.** Wider scrollbars feel heavy; narrower are hard to grab.
- **Round the track and thumb** for polish — one `RoundedNodeEffect` on the `ScrollbarNode` does both.

## See also

- [Node Fundamentals](../node-fundamentals.md) — `overflow` property.
