package be.zeldown.joid.lib.ui.core.data.debug;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
public @interface UIDataDebug {

	boolean profiler()  default true;
	boolean hotreload() default true;

}