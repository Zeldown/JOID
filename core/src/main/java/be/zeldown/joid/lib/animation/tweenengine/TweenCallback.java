package be.zeldown.joid.lib.animation.tweenengine;

public interface TweenCallback {

	public static final int BEGIN         = 0x01;
	public static final int START         = 0x02;
	public static final int END           = 0x04;
	public static final int COMPLETE      = 0x08;
	public static final int BACK_BEGIN    = 0x10;
	public static final int BACK_START    = 0x20;
	public static final int BACK_END      = 0x40;
	public static final int BACK_COMPLETE = 0x80;
	public static final int ANY_FORWARD   = 0x0F;
	public static final int ANY_BACKWARD  = 0xF0;
	public static final int ANY           = 0xFF;

	public void onEvent(int type, BaseTween<?> source);

	default TweenCallback andThen(final TweenCallback that) {
		return (type, source) -> {
			this.onEvent(type, source);
			that.onEvent(type, source);
		};
	}

}