package be.zeldown.joid.lib.animation.tweenengine;



import be.zeldown.joid.lib.animation.tweenengine.path.CatmullRom;
import be.zeldown.joid.lib.animation.tweenengine.path.Linear;

public interface TweenPaths {

	public static final Linear LINEAR = new Linear();
	public static final CatmullRom CATMULL_ROM = new CatmullRom();

}