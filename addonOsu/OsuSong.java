package addonOsu;

import java.text.DecimalFormat;

public class OsuSong
{
    private int beatmapset_id;
    private int beatmap_id;
    private int approved;
    private String approved_date;
    private String last_update;
    private int total_length;
    private int hit_length;
    private String version;
    private String artist;
    private String title;
    private String creator;
    private double bpm;
    private String source;
    private double difficultyrating;
    private double diff_size;
    private double diff_overall;
    private double diff_approach;
    private double diff_drain;
    private int mode;
    
    private static DecimalFormat df = new DecimalFormat("0.000");
    
    public OsuSong(String toParse)
    {
        int scanStart = toParse.indexOf("\":\"");
        int scanEnd = toParse.indexOf("\",\"");
        beatmapset_id = Integer.parseInt(toParse.substring(scanStart+3, scanEnd));

        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        beatmap_id = Integer.parseInt(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        approved = Integer.parseInt(toParse.substring(scanStart+3, scanEnd));

        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        approved_date = toParse.substring(scanStart+3, scanEnd);
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        last_update = toParse.substring(scanStart+3, scanEnd);
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        total_length = Integer.parseInt(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        hit_length = Integer.parseInt(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        version = toParse.substring(scanStart+3, scanEnd);
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        artist = toParse.substring(scanStart+3, scanEnd);
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        title = toParse.substring(scanStart+3, scanEnd);
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        creator = toParse.substring(scanStart+3, scanEnd);
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        bpm = Double.parseDouble(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        source = toParse.substring(scanStart+3, scanEnd);
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        difficultyrating = Double.parseDouble(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        diff_size = Double.parseDouble(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        diff_overall = Double.parseDouble(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        diff_approach = Double.parseDouble(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\",\"", scanStart);
        diff_drain = Double.parseDouble(toParse.substring(scanStart+3, scanEnd));
        
        scanStart = toParse.indexOf("\":\"", scanEnd);
        scanEnd = toParse.indexOf("\"}", scanStart);
        mode = Integer.parseInt(toParse.substring(scanStart+3, scanEnd));
    }
    
    @Override
    public String toString()
    {
        return artist+" - "+title+" - "+version+" - "+df.format(difficultyrating);
    }
    
    public String getLink()
    {
        return "https://osu.ppy.sh/b/"+beatmap_id+"?m="+mode;
    }
    
    public Double getDifficultyRating()
    {
        return difficultyrating;
    }
    
    public int getBeatmapID()
    {
        return beatmap_id;
    }
    
    public int getMode()
    {
        return mode;
    }
}
