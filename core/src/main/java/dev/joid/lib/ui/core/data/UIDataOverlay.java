package dev.joid.lib.ui.core.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIDataOverlay {

	public boolean                  active()      default false;
	public UIDataOverlayRender      render()      default @UIDataOverlayRender;
	public UIDataOverlayInteraction interaction() default @UIDataOverlayInteraction;

}