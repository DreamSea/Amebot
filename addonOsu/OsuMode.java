package addonOsu;

public enum OsuMode {
	OSU(0), TAIKO(1), CTB(2), MANIA(3);
	
	private int modeNumber;
	
	private OsuMode(int modeNumber) {
		this.modeNumber = modeNumber;
	}
	
	/**
	 * (0 = osu!, 1 = Taiko, 2 = CtB, 3 = osu!mania)
	 * 
	 */
	public static OsuMode getMode(int modeNumber) {
		for (OsuMode mode : values()) {
			if (mode.getModeNumber() == modeNumber) {
				return mode;
			}
		}
		throw new IllegalArgumentException();
	}
	
	public int getModeNumber() {
		return modeNumber;
	}
}
