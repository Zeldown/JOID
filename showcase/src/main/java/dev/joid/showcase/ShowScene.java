package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShowScene {

	HERO(ShowHero.class, "Dashboard", "Video, chart, 3D model and a toast on one screen", ShowUI.VIOLET),
	REACTIVE(ShowReactive.class, "Reactive", "Sliders drive a glowing orb", ShowUI.PINK),
	TYPE(ShowType.class, "Typography", "Sharp text at any size and zoom", ShowUI.CYAN),
	EFFECTS(ShowEffects.class, "Effects", "Seven switches, seven GPU effects", ShowUI.FUCHSIA),
	MOTION(ShowMotion.class, "Motion", "Six easing curves side by side", ShowUI.AMBER),
	FORM(ShowForm.class, "Form", "Type, select and see it update", ShowUI.EMERALD),
	PLAYER(ShowPlayer.class, "Video player", "Pause, seek and play a video", ShowUI.ORANGE),
	MODEL(ShowModel.class, "3D model", "Turn and zoom a textured teapot", ShowUI.SKY),
	LISTS(ShowLists.class, "Playlist", "Drag to reorder, scroll for more", ShowUI.VIOLET),
	DRAG(ShowDrag.class, "Drag and drop", "Drop a record into the player", ShowUI.PINK),
	THEME(ShowTheme.class, "Theme", "Light and dark with one toggle", ShowUI.AMBER),
	INSPECT(ShowInspect.class, "Dev mode", "F3 opens the inspector panel", ShowUI.CYAN),
	DESIGN(ShowDesign.class, "Two kits", "One screen, neon or paper", ShowUI.FUCHSIA);

	private final Class<? extends ShowUI> type;
	private final String                  title;
	private final String                  caption;
	private final Color                   accent;

	public ShowUI create() {
		try {
			return this.type.newInstance();
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to create the showcase scene " + this.title, exception);
		}
	}

	public static ShowScene of(final UI ui) {
		for (final ShowScene scene : ShowScene.values()) {
			if (scene.type == ui.getClass()) {
				return scene;
			}
		}
		return null;
	}

}