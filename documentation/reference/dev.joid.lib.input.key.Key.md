---
summary: A key of the keyboard, as the key events, the shortcuts and the key state name it.
---

A key event holds two keys: [[NodeKeyEvent#getKey]], the key that the layout of the user names, and [[NodeKeyEvent#getPhysicalKey]], the place of the key on a US QWERTY keyboard.
Only the letters and the punctuation keys differ between the two; the digits, the arrows, the modifiers and the function keys are the same.
Each constant below names the key by what it types on a US keyboard.

Backend note: on LWJGL 2, JOID learns the key that a place types from the characters the user types, so a letter or punctuation key pressed with Shift or Alt before it was ever typed alone reports its place.

Learn: [Keyboard](/use/learn/interactivity/keyboard/#tell-a-letter-from-its-place).

## isDown()

Returns `true` while a key that the layout names this way is held.
For a letter or punctuation key, it tests every held place that typed this key; for the other keys, it gives the same answer as `isPhysicalDown()`.

## isPhysicalDown()

Returns `true` while the key at this place of a US QWERTY keyboard is held, whatever the layout names it.
It asks the window bridge, so it answers even for a key that no UI received.

## A

The letter key that types `a` in the layout of the user.

## B

The letter key that types `b` in the layout of the user.

## C

The letter key that types `c` in the layout of the user.

## D

The letter key that types `d` in the layout of the user.

## E

The letter key that types `e` in the layout of the user.

## F

The letter key that types `f` in the layout of the user.

## G

The letter key that types `g` in the layout of the user.

## H

The letter key that types `h` in the layout of the user.

## I

The letter key that types `i` in the layout of the user.

## J

The letter key that types `j` in the layout of the user.

## K

The letter key that types `k` in the layout of the user.

## L

The letter key that types `l` in the layout of the user.

## M

The letter key that types `m` in the layout of the user.

## N

The letter key that types `n` in the layout of the user.

## O

The letter key that types `o` in the layout of the user.

## P

The letter key that types `p` in the layout of the user.

## Q

The letter key that types `q` in the layout of the user.

## R

The letter key that types `r` in the layout of the user.

## S

The letter key that types `s` in the layout of the user.

## T

The letter key that types `t` in the layout of the user.

## U

The letter key that types `u` in the layout of the user.

## V

The letter key that types `v` in the layout of the user.

## W

The letter key that types `w` in the layout of the user.

## X

The letter key that types `x` in the layout of the user.

## Y

The letter key that types `y` in the layout of the user.

## Z

The letter key that types `z` in the layout of the user.

## DIGIT_0

The `0` key of the top row.

## DIGIT_1

The `1` key of the top row.

## DIGIT_2

The `2` key of the top row.

## DIGIT_3

The `3` key of the top row.

## DIGIT_4

The `4` key of the top row.

## DIGIT_5

The `5` key of the top row.

## DIGIT_6

The `6` key of the top row.

## DIGIT_7

The `7` key of the top row.

## DIGIT_8

The `8` key of the top row.

## DIGIT_9

The `9` key of the top row.

## F1

The function key F1.

## F2

The function key F2.

## F3

The function key F3.

## F4

The function key F4.

## F5

The function key F5.

## F6

The function key F6.

## F7

The function key F7.

## F8

The function key F8.

## F9

The function key F9.

## F10

The function key F10.

## F11

The function key F11.

## F12

The function key F12.

## F13

The function key F13.

## F14

The function key F14.

## F15

The function key F15.

## F16

The function key F16.

## F17

The function key F17.

## F18

The function key F18.

## F19

The function key F19.

## F20

The function key F20.

## F21

The function key F21.

## F22

The function key F22.

## F23

The function key F23.

## F24

The function key F24.

## F25

The function key F25.

## ESCAPE

The Escape key. Unconsumed, it closes the top UI when that UI is closeable; a focused text field consumes it to lose the focus.

## ENTER

The Enter key of the main block. A one-line text field submits on it; a multiline field starts a new line.

## TAB

The Tab key.

## BACKSPACE

The Backspace key, which deletes the character before the cursor of a text field.

## INSERT

The Insert key.

## DELETE

The Delete key, which deletes the character after the cursor of a text field.

## RIGHT

The right arrow key.

## LEFT

The left arrow key.

## DOWN

The down arrow key.

## UP

The up arrow key.

## PAGE_UP

The Page Up key.

## PAGE_DOWN

The Page Down key.

## HOME

The Home key, which moves the cursor of a text field to the start.

## END

The End key, which moves the cursor of a text field to the end.

## CAPS_LOCK

The Caps Lock key.

## SCROLL_LOCK

The Scroll Lock key.

## NUM_LOCK

The Num Lock key.

## PRINT_SCREEN

The Print Screen key.

## PAUSE

The Pause key.

## SPACE

The space bar.

## APOSTROPHE

The punctuation key that types `'` in the layout of the user.

## COMMA

The punctuation key that types `,` in the layout of the user.

## MINUS

The punctuation key that types `-` in the layout of the user. In dev mode, with Ctrl or Alt, it zooms out a zoomable UI.

## PERIOD

The punctuation key that types `.` in the layout of the user.

## SLASH

The punctuation key that types `/` in the layout of the user.

## SEMICOLON

The punctuation key that types `;` in the layout of the user.

## EQUAL

The punctuation key that types `=` in the layout of the user. In dev mode, with Ctrl or Alt, it zooms in a zoomable UI.

## LEFT_BRACKET

The punctuation key that types `[` in the layout of the user.

## BACKSLASH

The punctuation key that types `\` in the layout of the user.

## RIGHT_BRACKET

The punctuation key that types `]` in the layout of the user.

## GRAVE_ACCENT

The punctuation key that types `` ` `` in the layout of the user.

## NUMPAD_0

The `0` key of the numeric keypad.

## NUMPAD_1

The `1` key of the numeric keypad.

## NUMPAD_2

The `2` key of the numeric keypad.

## NUMPAD_3

The `3` key of the numeric keypad.

## NUMPAD_4

The `4` key of the numeric keypad.

## NUMPAD_5

The `5` key of the numeric keypad.

## NUMPAD_6

The `6` key of the numeric keypad.

## NUMPAD_7

The `7` key of the numeric keypad.

## NUMPAD_8

The `8` key of the numeric keypad.

## NUMPAD_9

The `9` key of the numeric keypad.

## NUMPAD_DECIMAL

The decimal point key of the numeric keypad.

## NUMPAD_DIVIDE

The divide key of the numeric keypad.

## NUMPAD_MULTIPLY

The multiply key of the numeric keypad.

## NUMPAD_SUBTRACT

The minus key of the numeric keypad. In dev mode, with Ctrl or Alt, it zooms out a zoomable UI.

## NUMPAD_ADD

The plus key of the numeric keypad. In dev mode, with Ctrl or Alt, it zooms in a zoomable UI.

## NUMPAD_ENTER

The Enter key of the numeric keypad; text fields treat it as `ENTER`.

## NUMPAD_EQUAL

The equal key of the numeric keypad.

## LEFT_SHIFT

The left Shift key. [[UI#isShiftKeyDown]] tests both Shift keys.

## LEFT_CONTROL

The left Control key. [[UI#isCtrlKeyDown]] tests both Control keys.

## LEFT_ALT

The left Alt key. [[UI#isAltKeyDown]] tests both Alt keys.

## LEFT_SUPER

The left system key: Windows on a PC, Command on a Mac, where text fields use it for their shortcuts.

## RIGHT_SHIFT

The right Shift key.

## RIGHT_CONTROL

The right Control key.

## RIGHT_ALT

The right Alt key, AltGr on many layouts.

## RIGHT_SUPER

The right system key: Windows on a PC, Command on a Mac.

## MENU

The context menu key.

## UNKNOWN

A key that the window library reports and JOID does not name.
