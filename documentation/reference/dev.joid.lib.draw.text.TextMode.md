---
summary: How a text fits a box, on one line, cut, wrapped, or wrapped and clipped.
---

Learn: [Text](/use/learn/describing-the-ui/text/).

## NORMAL

Draws the text on one line, aligned in the box, even when it is wider than the box.

## OVERFLOW

Draws the text on one line and cuts it to the width of the box, ending with the overflow marker of the [[Text]].

## SPLIT

Wraps the text into lines that fit the width of the box; the lines may run past its bottom.

## BOX

Wraps the text like `SPLIT` and skips the lines that do not fit inside the height of the box.
