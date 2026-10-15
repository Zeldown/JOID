package dev.joid.lib.ui.core.data;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
public @interface UIDataDebug {

	public boolean profiler()  default true;

	public boolean hotreload() default true;

}