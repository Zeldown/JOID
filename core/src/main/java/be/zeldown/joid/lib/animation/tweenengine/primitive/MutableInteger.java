package be.zeldown.joid.lib.animation.tweenengine.primitive;



import be.zeldown.joid.lib.animation.tweenengine.TweenAccessor;

@SuppressWarnings("serial")
public class MutableInteger extends Number implements TweenAccessor<MutableInteger> {

	private int value;

	public MutableInteger(final int value) {
		this.value = value;
	}

	public void setValue(final int value) {
		this.value = value;
	}

	@Override
	public int intValue() {
		return this.value;
	}

	@Override
	public long longValue() {
		return this.value;
	}

	@Override
	public float floatValue() {
		return this.value;
	}

	@Override
	public double doubleValue() {
		return this.value;
	}

	@Override
	public int getValues(final MutableInteger target, final int tweenType, final float[] returnValues) {
		returnValues[0] = target.value;
		return 1;
	}

	@Override
	public void setValues(final MutableInteger target, final int tweenType, final float[] newValues) {
		target.value = (int) newValues[0];
	}

}