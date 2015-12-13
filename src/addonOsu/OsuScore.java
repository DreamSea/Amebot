package addonOsu;

public class OsuScore
{
    private int beatmap_id;
    private long score;
    private int maxcombo;
    private int count50;
    private int count100;
    private int count300;
    private int countmiss;
    private int countkatu;
    private int countgeki;
    private int perfect;
    private long enabled_mods;
    private long user_id;
    private String date;
    private String rank;
    private double pp;
    
    public OsuScore(String toParse)
    {
        int start = toParse.indexOf("\":\"")+3;
        int end = toParse.indexOf("\",\"");
        beatmap_id = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        score = Long.parseLong(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        maxcombo = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        count50 = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        count100 = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        count300 = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        countmiss = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        countkatu = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        countgeki = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        perfect = Integer.parseInt(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        enabled_mods = Long.parseLong(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        user_id = Long.parseLong(toParse.substring(start, end));
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        date = toParse.substring(start, end);
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.indexOf("\",\"", start);
        rank = toParse.substring(start, end);
        
        start = toParse.indexOf("\":\"",end)+3;
        end = toParse.length()-1;
        pp = Double.parseDouble(toParse.substring(start, end));
    }
    
    public int getbeatmap_id()
    {
        return beatmap_id;
    }
    
    public long getenabled_mods()
    {
        return enabled_mods;
    }
    
    public double getpp()
    {
        return pp;
    }
    
    public int getcount50()
    {
        return count50;
    }
    
    public int getcount100()
    {
        return count100;
    }
    
    public int getcount300()
    {
        return count300;
    }
    
    public int getcountkatu() {
    	return countkatu;
    }
    
    public int getcountgeki() {
    	return countgeki;
    }
    
	public double getAcc(int mode) {
		if (mode == 0) {
			double max = 300 * (countmiss + count50 + count100 + count300);
			double achi = count50 * 50 + count100 * 100 + count300 * 300;

			return achi / max;
		} else if (mode == 1) {
			double max = countmiss + count100 + count300;
			double achi = count300 + count100 * 0.5;
			return achi / max;
		} else if (mode == 3) {
			// geki = 300MAX, katu = 200
			double max = 300* (countmiss+count50+count100+countkatu+count300+countgeki);
			double achi = 50*count50 + 100*count100 + 200*countkatu + 300*count300 + 300*countgeki;
			//(Number of 50s * 50 + Number of 100s * 100 + Number of 200s * 200 + Number of 300s * 300 + Number of MAXes * 300)
			return achi / max;
		}
		return 0;
    }
    
    @Override
    public String toString()
    {
        return score+", "+maxcombo+", "+count300+"/"+count100+"/"+count50+", "+pp;
    }
}
