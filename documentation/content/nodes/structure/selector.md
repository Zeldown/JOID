# SelectorNode

Dropdown selector. Click to open a list; click an option to select.

## Create

```java
SelectorNode.create(x, y, width, height)
    .options("Red", "Green", "Blue", "Yellow")
    .value("Red")
    .attach(parent);
```

## API

```java
node.options(String... options);
node.options(List<String>);
node.value(String);
node.value(Supplier<String>);
node.placeholder(String);
node.backgroundColor(Color);
node.hoveredColor(Color);
node.textInfo(TextInfo);
node.maxVisibleOptions(int);        // scrollable dropdown beyond this
```

## Callbacks

```java
node.onChange((selector, value) -> {
    System.out.println("Selected: " + value);
});
```

## Example — language picker

```java
final StringSignal locale = new StringSignal("en");

SelectorNode.create(40, 40, 200, 36)
    .options("English", "Français", "Deutsch", "Español", "日本語")
    .value("English")
    .backgroundColor(Color.decode("#1f2937"))
    .hoveredColor(Color.decode("#374151"))
    .effect(RoundedNodeEffect.create(6F))
    .onChange((sel, val) -> locale.set(normalize(val)))
    .attach(parent);
```

## Search / filter

For long option lists, enable filtering:

```java
node.searchable(true);    // shows a text field at the top of the dropdown
```

## Best practices

- **Keep option lists short** when not searchable. 3–8 options max.
- **Use `maxVisibleOptions`** to avoid a dropdown that eats the whole screen.
- **Sort options meaningfully** — alphabetical or frequency-based, not random.

## See also

- [ToggleNode](toggle.md) — for 2–5 options without dropdown.
