package dev.joid.lib.ui.core.data.overlay.render;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface UIDataOverlayRender {

	public boolean always()  default false;
	public boolean screens() default false;
	public int     zindex()  default 0;

}