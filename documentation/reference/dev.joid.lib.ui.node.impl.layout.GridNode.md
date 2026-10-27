---
summary: Places its visible children in rows that wrap at its width, and takes the height of its content.
---
A child goes to the next row when it does not fit in the width that is left.
A height of `0` takes the height of the rows; a declared height is a minimum without overflow, and a fixed height with an overflow.

Learn: [Grid layout](/use/learn/describing-the-ui/grid-layout/).

## create(double,double,double,double)

Creates a grid at `x`, `y` with this width and height; a height of `0` follows the rows.

## alignX

Where each row sits in the width: `START`, `CENTER` or `END`.

## margin

Sets the space between the cells on both axes, in design pixels.

## marginHorizontal

Space between two cells of a row, in design pixels.

## marginVertical

Space between two rows, in design pixels.
