package dev.joid.lib.animation.tweenengine;



import dev.joid.lib.animation.tweenengine.path.CatmullRom;
import dev.joid.lib.animation.tweenengine.path.Linear;

public interface TweenPaths {

	public static final Linear linear = new Linear();
	public static final CatmullRom catmullRom = new CatmullRom();

}