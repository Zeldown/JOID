package dev.joid.lib.ui.core.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIDataOverlayRender {

	public boolean always()       default false;
	public boolean aboveScreens() default false;

}