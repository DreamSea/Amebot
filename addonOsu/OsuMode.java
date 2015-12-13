package addonOsu;


public enum OsuMode {
	OSU(0), TAIKO(1), FRUIT(2), MANIA(3),
	MANIA4K(3), MANIA5K(3), MANIA6K(3), MANIA7K(3),
	MANIA8K(3), MANIA9K(3);
	
	private int modeNumber;

	private static OsuMode[] baseModes;
	private static OsuMode[] maniaModes;
	
	static {
		baseModes = new OsuMode[4];
		baseModes[0] = OSU;
		baseModes[1] = TAIKO;
		baseModes[2] = FRUIT;
		baseModes[3] = MANIA;
		
		maniaModes = new OsuMode[10];
		maniaModes[4] = MANIA4K;
		maniaModes[5] = MANIA5K;
		maniaModes[6] = MANIA6K;
		maniaModes[7] = MANIA7K;
		maniaModes[8] = MANIA8K;
		maniaModes[9] = MANIA9K;
	}
	
	private OsuMode(int modeNumber) {
		this.modeNumber = modeNumber;
	}
	
	public int getModeNumber() {
		return modeNumber;
	}
	
	@Override public String toString() {
		return this.name().toLowerCase();
	}
	
	public static OsuMode getMode(OsuSong beatmap) {
		int mode = beatmap.getMode();
		if (mode == 3) { // mania
			int numKeys = (int) beatmap.getDiff_size();
			if (numKeys == -1) {
				return MANIA;
			}
			return maniaModes[numKeys];
		} else { // not mania
			return baseModes[mode];
		}
	}
}
