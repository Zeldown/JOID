# ContainerNode

A passive wrapper. No visual, no layout logic — just a parent for children with its own position and size. Think of it as a `<div>` for organization and clipping.

## Create

```java
ContainerNode.create(x, y, width, height)
    .body(container -> {
        // children
    })
    .attach(parent);
```

## When to use

- Grouping nodes for effect application (a shared `RoundedNodeEffect`).
- Isolating clipping regions (`overflow(OverflowProperty.HIDDEN)`).
- Providing a coordinate origin for complex sub-layouts.
- A root container at `0, 0, 1920, 1080` for the whole UI.

## Example — root container

```java
@Override
public void init() {
    ContainerNode.create(0, 0, 1920, 1080)
        .body(root -> {
            FlexNode.vertical(0, 0, 200).body(nav -> { ... }).attach(root);
            ContainerNode.create(200, 0, 1720, 1080).body(main -> { ... }).attach(root);
        })
        .attach(this);
}
```

## Best practices

- **Don't overuse.** If a node's only children are one `RectNode`, skip the container.
- **Use containers for effect grouping.** One effect on a container applies across all its children (via framebuffer composition).
- **Name containers when extracting.** Not in code — in your mental model. "This is the sidebar container." Clarity pays off in `reload()`.

## See also

- [Node Fundamentals](../node-fundamentals.md) — overflow, draggable, effects.
