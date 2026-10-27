---
summary: One entry of a radar chart: a label and its value.
---
Learn: [Charts](/use/learn/going-further/charts/#draw-a-radar-chart).

## create(String)

Creates an entry with this label and no value yet.

## create(String,Number)

Creates an entry with this label and this value.

## RadarChartData(String)

Called by a subclass with the label.

## RadarChartData(String,Number)

Called by a subclass with the label and the value.

## label

The label of the entry.

## value

The value of the entry; `null` means it has none yet.

## isEmpty()

If `true`, the entry has no value.

## clear()

Removes the value of the entry.
