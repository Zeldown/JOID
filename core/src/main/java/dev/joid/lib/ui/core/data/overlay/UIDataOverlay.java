package dev.joid.lib.ui.core.data.overlay;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.joid.lib.ui.core.data.overlay.interaction.UIDataOverlayInteraction;
import dev.joid.lib.ui.core.data.overlay.render.UIDataOverlayRender;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIDataOverlay {

	public boolean                  active()      default false;
	public UIDataOverlayRender      render()      default @UIDataOverlayRender;
	public UIDataOverlayInteraction interaction() default @UIDataOverlayInteraction;

}