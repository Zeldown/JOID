---
summary: One series of a chart: a value for each label of the X axis.
---
Learn: [Charts](/use/learn/going-further/charts/#update-the-data).

## create()

Creates an empty series.

## create(Map)

Creates a series that uses this map of values by label.

## ChartData()

Called by a subclass to create an empty series.

## ChartData(Map)

Called by a subclass with the map of values by label.

## dataMap

The values by label; setting it replaces the map.

## add(String,Number)

Sets the value of a label.

## remove(String)

Removes the value of a label.

## get(String)

The value of a label, or `null`.

## has(String)

If `true`, the series has a value for this label.

## isEmpty()

If `true`, the series has no value.

## getMin()

The smallest value, or `0` for an empty series.

## getMax()

The largest value, or `0` for an empty series.

## getAverage()

The average of the values, or `0` for an empty series.
