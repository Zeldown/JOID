package dev.joid.lib.ui.node.callback;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface NodeCallbackMethod {

	public Phase value();

	public enum Phase {
		PRE, POST;
	}

}