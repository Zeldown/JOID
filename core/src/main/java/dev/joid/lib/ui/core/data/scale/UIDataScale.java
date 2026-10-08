package dev.joid.lib.ui.core.data.scale;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UIDataScale {

	public boolean active()  default true;
	public boolean limited() default false;
	public double  limit()   default 1D;

}