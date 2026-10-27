---
summary: A one-line field that holds an `Integer` between a minimum and a maximum, stepped by Up, Down and the wheel.
---

The user types digits, and a minus sign when `min` is below `0`.
A typed value out of the range shows as its bound when the field loses the focus.
An empty field that the user leaves takes back its last value, `0` before any; with `allowEmpty(true)`, it holds `null`.

Learn: [Text fields](/use/learn/interactivity/text-fields/#ask-for-a-number).

## create(double,double,double)

Creates the field at `x`, `y` with a `width` in design pixels; its height is the height of its font plus its top and bottom margins.

## create(double,double,double,double)

Creates the field at `x`, `y` with a `width` and a `height` in design pixels.

## IntegerFieldNode(double,double,double,double)

Creates the field, from the constructor of a subclass.

## min

The smallest value; a typed value below it shows as `min` when the field loses the focus.

## max

The largest value; a typed value above it shows as `max` when the field loses the focus.

## step

The amount that Up, Down and the wheel add or remove.
A step of `0` or less throws an `IllegalArgumentException`.

## value(int)

Gives the field its start value, brought into the range.

## value(Signal)

Binds the value to a signal both ways: the field shows the signal, and writes the value of each valid edit into it.
Throws an `IllegalArgumentException` for a signal that cannot be written.

## change(int)

Sets the value as if the user typed it: `beforeChange` can refuse it, then `onChange` fires.

## parse(String)

Reads an optional minus sign and digits; a number beyond the range of `int` gives `Integer.MAX_VALUE` or `Integer.MIN_VALUE`, any other text `null`.

## format(Integer)

Writes the value in decimal digits.

## correct(Integer)

Brings the value between `min` and `max`.

## accepts(String)

Accepts digits only, with a leading minus sign when `min` is below `0`.

## increment(Integer,int)

Returns the value `count` steps away, kept between `min` and `max`.
