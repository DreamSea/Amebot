package addonOsu;

import java.text.DecimalFormat;
import java.util.Scanner;

public class OsuSong
{
	private int beatmapset_id = -1;
	private int beatmap_id = -1;
	private int approved = -1;
	private int total_length = -1;
	private int hit_length = -1;
	private String version = "null";
	private String file_md5 = "null";
	private double diff_size = -1.0;
	private double diff_overall = -1.0;
	private double diff_approach = -1.0;
	private double diff_drain = -1.0;
	private int mode = -1;;
	private String approved_date = "null";
	private String last_update = "null";
	private String artist = "null";
	private String title = "null";
	private String creator = "null";
	private double bpm = -1.0;
	private String source = "null";
	private String tags = "null";
	private int genre_id = -1;
	private int language_id = -1;
	private int favourite_count = -1;
	private int playcount = -1;
	private int passcount = -1;
	private int max_combo = -1;
	private double difficultyrating = -1.0;
    
    private static DecimalFormat df = new DecimalFormat("0.000");
    
    public OsuSong(String toParse)
    {
    	Scanner s = new Scanner(toParse);
    	s.useDelimiter("\t");
    	while (s.hasNext()) {
    		String key = s.next();
    		String value = s.next();
    		try {
        		if (key.equals("beatmapset_id")) {
        			setBeatmapset_id(Integer.parseInt(value));
        		} else if (key.equals("beatmap_id")) {
        			setBeatmap_id(Integer.parseInt(value));
        		} else if (key.equals("approved")) {
        			setApproved(Integer.parseInt(value));
        		} else if (key.equals("total_length")) {
        			setTotal_length(Integer.parseInt(value));
        		} else if (key.equals("hit_length")) {
        			setHit_length(Integer.parseInt(value));
        		} else if (key.equals("version")) {
        			setVersion(value);
        		} else if (key.equals("file_md5")) {
        			setFile_md5(value);
        		} else if (key.equals("diff_size")) {
        			setDiff_size(Double.parseDouble(value));
        		} else if (key.equals("diff_overall")) {
        			setDiff_overall(Double.parseDouble(value));
        		} else if (key.equals("diff_approach")) {
        			setDiff_approach(Double.parseDouble(value));
        		} else if (key.equals("diff_drain")) {
        			setDiff_drain(Double.parseDouble(value));
        		} else if (key.equals("mode")) {
        			setMode(Integer.parseInt(value));
        		} else if (key.equals("approved_date")) {
        			setApproved_date(value);
        		} else if (key.equals("last_update")) {
        			setLast_update(value);
        		} else if (key.equals("artist")) {
        			setArtist(value);
        		} else if (key.equals("title")) {
        			setTitle(value);
        		} else if (key.equals("creator")) {
        			setCreator(value);
        		} else if (key.equals("bpm")) {
        			setBpm(Double.parseDouble(value));
        		} else if (key.equals("source")) {
        			setSource(value);
        		} else if (key.equals("tags")) {
        			setTags(value);
        		} else if (key.equals("genre_id")) {
        			setGenre_id(Integer.parseInt(value));
        		} else if (key.equals("language_id")) {
        			setLanguage_id(Integer.parseInt(value));
        		} else if (key.equals("favourite_count")) {
        			setFavourite_count(Integer.parseInt(value));
        		} else if (key.equals("playcount")) {
        			setPlaycount(Integer.parseInt(value));
        		} else if (key.equals("passcount")) {
        			setPasscount(Integer.parseInt(value));
        		} else if (key.equals("max_combo")) {
        			setMax_combo(Integer.parseInt(value));
        		} else if (key.equals("difficultyrating")) {
        			setDifficultyrating(Double.parseDouble(value));
        		}
    			
    		} catch (NumberFormatException e) {
    			
    		}
    	}
    	s.close();
    }
    
    private void setBeatmapset_id(int beatmapset_id) {
		this.beatmapset_id = beatmapset_id;
	}

	private void setBeatmap_id(int beatmap_id) {
		this.beatmap_id = beatmap_id;
	}

	private void setApproved(int approved) {
		this.approved = approved;
	}

	private void setTotal_length(int total_length) {
		this.total_length = total_length;
	}

	private void setHit_length(int hit_length) {
		this.hit_length = hit_length;
	}

	private void setVersion(String version) {
		this.version = version;
	}

	private void setFile_md5(String file_md5) {
		this.file_md5 = file_md5;
	}

	private void setDiff_size(double diff_size) {
		this.diff_size = diff_size;
	}

	private void setDiff_overall(double diff_overall) {
		this.diff_overall = diff_overall;
	}

	private void setDiff_approach(double diff_approach) {
		this.diff_approach = diff_approach;
	}

	private void setDiff_drain(double diff_drain) {
		this.diff_drain = diff_drain;
	}

	private void setMode(int mode) {
		this.mode = mode;
	}

	private void setApproved_date(String approved_date) {
		this.approved_date = approved_date;
	}

	private void setLast_update(String last_update) {
		this.last_update = last_update;
	}

	private void setArtist(String artist) {
		this.artist = artist;
	}

	private void setTitle(String title) {
		this.title = title;
	}

	private void setCreator(String creator) {
		this.creator = creator;
	}

	private void setBpm(double bpm) {
		this.bpm = bpm;
	}

	private void setSource(String source) {
		this.source = source;
	}

	private void setTags(String tags) {
		this.tags = tags;
	}

	private void setGenre_id(int genre_id) {
		this.genre_id = genre_id;
	}

	private void setLanguage_id(int language_id) {
		this.language_id = language_id;
	}

	private void setFavourite_count(int favourite_count) {
		this.favourite_count = favourite_count;
	}

	private void setPlaycount(int playcount) {
		this.playcount = playcount;
	}

	private void setPasscount(int passcount) {
		this.passcount = passcount;
	}

	private void setMax_combo(int max_combo) {
		this.max_combo = max_combo;
	}

	private void setDifficultyrating(double difficultyrating) {
		this.difficultyrating = difficultyrating;
	}

	public int getBeatmapset_id() {
		return beatmapset_id;
	}

	public int getBeatmap_id() {
		return beatmap_id;
	}

	public int getApproved() {
		return approved;
	}

	public int getTotal_length() {
		return total_length;
	}

	public int getHit_length() {
		return hit_length;
	}

	public String getVersion() {
		return version;
	}

	public String getFile_md5() {
		return file_md5;
	}

	public double getDiff_size() {
		return diff_size;
	}

	public double getDiff_overall() {
		return diff_overall;
	}

	public double getDiff_approach() {
		return diff_approach;
	}

	public double getDiff_drain() {
		return diff_drain;
	}

	public int getMode() {
		return mode;
	}

	public String getApproved_date() {
		return approved_date;
	}

	public String getLast_update() {
		return last_update;
	}

	public String getArtist() {
		return artist;
	}

	public String getTitle() {
		return title;
	}

	public String getCreator() {
		return creator;
	}

	public double getBpm() {
		return bpm;
	}

	public String getSource() {
		return source;
	}

	public String getTags() {
		return tags;
	}

	public int getGenre_id() {
		return genre_id;
	}

	public int getLanguage_id() {
		return language_id;
	}

	public int getFavourite_count() {
		return favourite_count;
	}

	public int getPlaycount() {
		return playcount;
	}

	public int getPasscount() {
		return passcount;
	}

	public int getMax_combo() {
		return max_combo;
	}

	public double getDifficultyrating() {
		return difficultyrating;
	}

	@Override
    public String toString()
    {
        return getArtist()+" - "+getTitle()+" - "+getVersion()+" - "+df.format(getDifficultyrating());
    }
    
    public String getLink()
    {
        return "https://osu.ppy.sh/b/"+getBeatmap_id()+"?m="+getMode();
    }
    
    public static OsuSong getManiaMarker() {
    	OsuSong toReturn = new OsuSong("");
    	toReturn.setMode(3);			// indicate mania
    	toReturn.setDiff_size(-1);		// marker for 'all mania'
    	toReturn.setBeatmap_id(-1);		// no set collision
    	toReturn.setBeatmapset_id(-1);	// no set collision
    	return toReturn;
    }
    
}
