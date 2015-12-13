package addonOsu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.AddOn;
import base.AmeBot;
import base.ApiConnection;
import base.MessageSender;

// last beatmap update: 2015-11-24

public class aoOsu implements AddOn
{
    private static DecimalFormat df = new DecimalFormat("0.000");

    private String channel;
    private MessageSender messageSender;
    private ApiConnection api;
    private String apiKey;
    private long apiThrottle = 0;

    private Random r = new Random();
    
    private Map<Integer,OsuSong> songsByID = new HashMap<Integer, OsuSong>();
    private Map<OsuMode, List<OsuSong>> beatmapsByMode;
    private diffComparator difficulty = new diffComparator();
    private Map<String, List<OsuSong>> listsByCommand;
    
    public aoOsu(String channel, MessageSender messageSender, ApiConnection api, String botName)
    {
    	// init beatmapsByMode
    	beatmapsByMode = new HashMap<OsuMode, List<OsuSong>>();
    	for (OsuMode mode : OsuMode.values()) {
    		beatmapsByMode.put(mode, new ArrayList<OsuSong>());
    	}
    	
    	// have 'mania' category return OsuMode.MANIA
    	beatmapsByMode.get(OsuMode.MANIA).add(OsuSong.getManiaMarker());
    	
    	listsByCommand = new HashMap<String, List<OsuSong>>();
    	String[] commands = {"find", "count", "diff"};
    	for (OsuMode key : beatmapsByMode.keySet()) {
    		for (String command : commands) {
        		listsByCommand.put("_"+key+command, beatmapsByMode.get(key));	
    		}
    	}
    	System.out.println(listsByCommand);

        this.channel = channel;
        this.messageSender = messageSender;
        this.api = api;
        
        System.out.println("aoOsu been installed in "+channel);
        
        File keyFile = new File("osuAPI.txt");
        getApiKey(keyFile);
        
        File songFile = new File("beatmaps_2015-11-24");
        loadBeatmaps(songFile);
    }
    
    private String getApiKey(File keyFile) {
    	String key = "";

        Scanner sKey;
        try {
            sKey = new Scanner(keyFile);
            apiKey = sKey.nextLine();
            sKey.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        
    	return key;
    }
    
    private void loadBeatmaps(File songFile) { 
        try {
            BufferedReader br = new BufferedReader(new FileReader(songFile));
            String nextLine = br.readLine();
            while (nextLine != null) {
                OsuSong current = new OsuSong(nextLine);
                if (!songsByID.containsKey(current.getBeatmap_id())) {
                    songsByID.put(current.getBeatmap_id(), current);
                    OsuMode currentMode = OsuMode.getMode(current);
                    beatmapsByMode.get(currentMode).add(current);
                    if (currentMode.getModeNumber() == OsuMode.MANIA.getModeNumber()) {
                        beatmapsByMode.get(OsuMode.MANIA).add(current);
                    }
                }
                nextLine = br.readLine();
            }
            br.close();

            for (OsuMode mode : beatmapsByMode.keySet()) {
                Collections.sort(beatmapsByMode.get(mode), difficulty);
                System.out.println("Mode "+mode+": "+beatmapsByMode.get(mode).size()+" beatmaps loaded");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public int search(double diff, double margin, List<OsuSong> mapList)
    {
        boolean startFound = false;
        int startIndex = mapList.size()-1;
        int endIndex = mapList.size()-1;
        for (int i = 0; i < mapList.size(); i++)
        {
            if (!startFound)
            {
                if (mapList.get(i).getDifficultyrating() >= diff - margin)
                {
                    startIndex = i;
                    startFound = true;
                }
            }
            if (startFound)
            {
                if (mapList.get(i).getDifficultyrating() >= diff + margin)
                {
                    endIndex = i;
                    break;
                }
            }
        }

        int index = startIndex + r.nextInt(endIndex - startIndex + 1);
        if (index == 0) index += r.nextInt(5);
        if (index == mapList.size() - 1) index -= r.nextInt(5);

        return index;
    }

    public int rangeDiff(double lower, double upper, List<OsuSong> songlist)
    {
        int count = 0;
        boolean lowerFound = false;
        for (int i = 0; i < songlist.size(); i++)
        {
            if (!lowerFound && songlist.get(i).getDifficultyrating() >= lower)
            {
                lowerFound = true;
            }
            if (songlist.get(i).getDifficultyrating() > upper)
            {
                break;
            }
            if (lowerFound) count++;
        }
        return count;
    }

    public String mapString(int beatmapID)
    {
        OsuSong selected = songsByID.get(beatmapID);
        return selected.toString()+" ["+selected.getLink()+"]";
    }

	private void modeFind(String[] trailing, List<OsuSong> maps) {
		if (trailing.length == 1)
			return;
		try {
			Double diff = Double.parseDouble(trailing[1]);

			double margin = 0.0123;
			if (trailing.length >= 3) {
				try {
					margin = Double.parseDouble(trailing[2]);
				} catch (NumberFormatException e) {
					System.err.println("rip: "+e);
				}
			}
			
			int index = search(diff, margin, maps);
			messageSender.sendMessage(channel, mapString(maps.get(index).getBeatmap_id()));
		} catch (NumberFormatException e) {
			System.out.println("nope: " + trailing[1]);
		}
	}
	
	private void modeCount(String[] trailing, List<OsuSong> songlist) {
        if (trailing.length < 3) return;
        try {
        	OsuMode mode = OsuMode.getMode(songlist.get(0));
            Double lower = Double.parseDouble(trailing[1]);
            Double upper = Double.parseDouble(trailing[2]);

            if (lower > upper) {
                Double temp = lower;
                lower = upper;
                upper = temp;
            }
            messageSender.sendMessage(channel, mode+" maps between "+lower+" and "+upper+": "+rangeDiff(lower, upper, songlist));
        } catch (NumberFormatException e) {
            System.out.println("nope: "+trailing[1]);
        }
	}
	
	private void modeDiff(String[] trailing, List<OsuSong> songlist) {
        if (trailing.length == 1) return;
        if (System.currentTimeMillis() - apiThrottle < 1000)
		{
			messageSender.sendMessage(channel, "Too many API calls. :<");
		    return;
		}
		apiThrottle = System.currentTimeMillis();
		
		String user = trailing[1];
		OsuMode mode = OsuMode.getMode(songlist.get(0));
		
		OsuScore[] bestPlays = getBestPlays(user, mode.getModeNumber());
		List<OsuScore> filteredPlays = filterScores(bestPlays, mode);
		
		if (filteredPlays.size() == 0) {
			messageSender.sendMessage(channel, "No "+mode+" plays found for "+user+". :(");
		} else {
			double diff = estimateDiff(filteredPlays);
			String recDiff = "Recommended "+mode+" difficulty for "+user+" based on "+filteredPlays.size()+" score(s): "+df.format(diff);
			messageSender.sendMessage(channel, recDiff);
		}
	}
	
	private double estimateDiff(List<OsuScore> scores) {
        double ppSum = 0;
        for (OsuScore o : scores) {
            ppSum += o.getpp();
        }

        double recDiff = 0;
        for (OsuScore o : scores) {
            //System.out.println();
            OsuSong song = songsByID.get(o.getbeatmap_id());
            double diff = song.getDifficultyrating();
            double acc = o.getAcc(song.getMode());
            double skew = 0.5*(acc-0.9)/0.1; // i dont quite remember how this skews

            recDiff += (diff+skew)*o.getpp()/ppSum;
        }
        return recDiff;
	}
	
	private List<OsuScore> filterScores(OsuScore[] scores, OsuMode mode) {
		List<OsuScore> toReturn = new ArrayList<OsuScore>();
		for (OsuScore score : scores) {
			// modless, known ranked, mode
			if (score.getenabled_mods() == 0 
					&& songsByID.containsKey(score.getbeatmap_id())) {
				// TODO: fix ugly condition for mania scores :x
				OsuMode scoreMode = OsuMode.getMode(songsByID.get(score.getbeatmap_id()));
				if (mode == OsuMode.MANIA && scoreMode.getModeNumber() == OsuMode.MANIA.getModeNumber()) {
					toReturn.add(score);
				} else if (scoreMode == mode) {
					toReturn.add(score);
				}
			}
		}
		return toReturn;
	}
	
	private OsuScore[] getBestPlays(String username, int mode) {
    	OsuScore[] toReturn = new OsuScore[0];
		
    	String toParse;
		try {
			toParse = api.doGet("https://osu.ppy.sh/api/get_user_best?k="+apiKey+"&u="+username+"&limit=50&m="+mode);            
	        
	        if (toParse.length() > 10) {
	            String trim = toParse.substring(2, toParse.length()-2);
	            String[] scores = trim.split("\\},\\{");
	            
	            toReturn = new OsuScore[scores.length];
	            for (int i = 0; i < toReturn.length; i++) {
	            	toReturn[i] = new OsuScore(scores[i]);
	            }
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return toReturn;
	}
	
    @Override
    public void checkMessage(String[] message)
    {
        String[] trailing = message[3].split("[ ]+");
        String command = trailing[0].toLowerCase();
        if (listsByCommand.containsKey(command)) {
        	List<OsuSong> mapList = listsByCommand.get(command);
        	if (command.endsWith("find")) {
        		modeFind(trailing, mapList);
        	} else if (command.endsWith("count")) {
        		modeCount(trailing, mapList);
        	} else if (command.endsWith("diff")) {
        		modeDiff(trailing, mapList);
        	}
        } else if (trailing[0].startsWith("_") 
				&& trailing[0].toLowerCase().endsWith("find")) {
			messageSender.sendMessage(channel, "what scene");
        }
    }

    class diffComparator implements Comparator<OsuSong>
    {
        @Override
        public int compare(OsuSong o1, OsuSong o2)
        {
            double diff = o1.getDifficultyrating() - o2.getDifficultyrating();
            if (diff > 0) return 1;
            else if (diff < 0) return -1;
            else return 0;
        }
    }
}