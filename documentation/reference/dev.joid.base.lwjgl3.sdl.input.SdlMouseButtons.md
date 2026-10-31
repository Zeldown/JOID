---
summary: Maps the mouse buttons of SDL 3, which count from 1, to the mouse buttons of JOID.
---

Learn: [Mouse buttons](/build/base-modules/lwjgl3-sdl/#mouse-buttons).

## getButton(int)

Returns the button of the SDL button index: `SDL_BUTTON_LEFT` gives `MouseButton.LEFT`, `SDL_BUTTON_X1` and `SDL_BUTTON_X2` give `BACK` and `FORWARD`, any other index `OTHER`.
