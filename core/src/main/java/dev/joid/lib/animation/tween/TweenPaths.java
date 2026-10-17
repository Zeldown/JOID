package dev.joid.lib.animation.tween;



import dev.joid.lib.animation.tween.path.CatmullRom;
import dev.joid.lib.animation.tween.path.Linear;

public interface TweenPaths {

	public static final Linear     LINEAR      = new Linear();
	public static final CatmullRom CATMULL_ROM = new CatmullRom();

}