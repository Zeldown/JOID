package dev.joid.lib.ui.core.data.overlay.render;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIDataOverlayRender {

	public int     zindex()  default 0;
	public boolean always()  default false;
	public boolean screens() default false;

}