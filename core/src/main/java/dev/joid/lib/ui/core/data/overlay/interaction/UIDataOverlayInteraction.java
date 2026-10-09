package dev.joid.lib.ui.core.data.overlay.interaction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIDataOverlayInteraction {

	public boolean active()         default false;
	public boolean cancelClick()    default true;
	public boolean cancelScroll()   default true;
	public boolean cancelKeyboard() default true;

}