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
import base.MessageSender;

// last taiko approved: 2015-05-19
// osu approved: 2014-01-01 to 2015-05-20

public class aoOsu implements AddOn
{

    private static DecimalFormat df = new DecimalFormat("0.000");

    private String channel;
    private MessageSender messageSender;
    private String botName;
    private String apiKey;
    private long apiThrottle = 0;

    Random r = new Random();
    int[] test = new int[5];
    Map<Integer,OsuSong> songsByID = new HashMap<Integer, OsuSong>();
    Map<String, List<OsuSong>> beatmapsByMode = new HashMap <String, List<OsuSong>>();
    private diffComparator difficulty = new diffComparator();

    public aoOsu(String channel, MessageSender messageSender, String botName)
    {
    	beatmapsByMode.put("osu", new ArrayList<OsuSong>());
    	beatmapsByMode.put("taiko", new ArrayList<OsuSong>());
    	beatmapsByMode.put("fruit", new ArrayList<OsuSong>());
    	beatmapsByMode.put("mania", new ArrayList<OsuSong>());
    	for (int keys = 1; keys <= 10; keys++) {
    		beatmapsByMode.put("mania"+keys+"k", new ArrayList<OsuSong>());
    	}
    	//System.out.println(beatmapsByMode.keySet());

        this.channel = channel;
        this.messageSender = messageSender;
        this.botName = botName;
        System.out.println("aoOsu been installed in "+channel);

        File key = new File("osuAPI.txt");
        Scanner sKey;
        try
        {
            sKey = new Scanner(key);
            apiKey = sKey.nextLine();
            sKey.close();
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        }

        
        String songFile = "beatmaps_2015-11-24";

        try
        {
            File f = new File(songFile);
            BufferedReader br = new BufferedReader(new FileReader(f));
            String nextLine = br.readLine();
            while (nextLine != null)
            {
                OsuSong current = new OsuSong(nextLine);
                if (!songsByID.containsKey(current.getBeatmap_id()))
                {
                    songsByID.put(current.getBeatmap_id(), current);
                    OsuMode currentMode = OsuMode.getMode(current.getMode());
                    //System.out.println(currentMode.toString().toLowerCase());
                    beatmapsByMode.get(currentMode.toString().toLowerCase()).add(current);
                    if (currentMode == OsuMode.MANIA) {
                    	int numKeys = (int) current.getDiff_size();
                    	//System.out.println(current);
                    	//System.out.println("mania"+numKeys+"k");
                        beatmapsByMode.get("mania"+numKeys+"k").add(current);
                    }
                }
                nextLine = br.readLine();
            }
            br.close();

            for (String osuMode : beatmapsByMode.keySet())
            {
                Collections.sort(beatmapsByMode.get(osuMode), difficulty);
                System.out.println("Mode "+osuMode+": "+beatmapsByMode.get(osuMode).size()+" beatmaps loaded");
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public int search(double diff, double margin, String mode)
    {   
        List<OsuSong> type = beatmapsByMode.get(mode);
        boolean startFound = false;
        int startIndex = type.size()-1;
        int endIndex = type.size()-1;
        for (int i = 0; i < type.size(); i++)
        {
            if (!startFound)
            {
                if (type.get(i).getDifficultyrating() >= diff - margin)
                {
                    startIndex = i;
                    startFound = true;
                }
            }
            if (startFound)
            {
                if (type.get(i).getDifficultyrating() >= diff + margin)
                {
                    endIndex = i;
                    break;
                }
            }
        }

        int index = startIndex + r.nextInt(endIndex - startIndex + 1);
        if (index == 0) index += r.nextInt(5);
        if (index == type.size() - 1) index -= r.nextInt(5);

        return index;
    }

    public int rangeDiff(double lower, double upper, String mode)
    {
        List<OsuSong> type = beatmapsByMode.get(mode);
        int count = 0;
        boolean lowerFound = false;
        for (int i = 0; i < type.size(); i++)
        {
            if (!lowerFound && type.get(i).getDifficultyrating() >= lower)
            {
                lowerFound = true;
            }
            if (type.get(i).getDifficultyrating() > upper)
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

	private void modeFind(String[] trailing, String mode) {
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
			int index = search(diff, margin, mode);
			messageSender.sendMessage(channel, mapString(beatmapsByMode.get(mode)
					.get(index).getBeatmap_id()));
		} catch (NumberFormatException e) {
			System.out.println("nope: " + trailing[1]);
		}
	}
	
	private void modeCount(String[] trailing, String mode, String modeName) {
        if (trailing.length < 3) return;
        try
        {
            Double lower = Double.parseDouble(trailing[1]);
            Double upper = Double.parseDouble(trailing[2]);

            if (lower > upper) {
                Double temp = lower;
                lower = upper;
                upper = temp;
            }
            messageSender.sendMessage(channel, modeName+" maps between "+lower+" and "+upper+": "+rangeDiff(lower, upper, mode));
        }
        catch (NumberFormatException e)
        {
            System.out.println("nope: "+trailing[1]);
        }
	}
	
	private void modeDiff(String[] trailing, int mode, String modeName) {
        if (trailing.length == 1) return;
        if (System.currentTimeMillis() - apiThrottle < 1000)
		{
			messageSender.sendMessage(channel, "Too many API calls. :<");
		    return;
		}
		apiThrottle = System.currentTimeMillis();
		
		String user = trailing[1];
		double diff;
		int count;
		if (OsuMode.getMode(mode) == OsuMode.MANIA && modeName.length() != 5) {
			double[] results = getDiff(user, mode, Integer.parseInt(modeName.substring(5, 6)));
			diff = results[0];
			count = (int) results[1];
		} else {
			double[] results = getDiff(user, mode, -1);
			diff = results[0];
			count = (int) results[1];
		}
		
		if (diff == Double.MIN_VALUE)
		{
			messageSender.sendMessage(channel, "No "+modeName+" plays found for "+user+". :(");
		    return;
		}
		
		String recDiff = "Recommended "+modeName+" difficulty for "+user+" based on "+count+" score(s): "+df.format(diff);
		messageSender.sendMessage(channel, recDiff);
	}
    
	Pattern modeFind = Pattern.compile("_(.*)find");
	Pattern modeCount = Pattern.compile("_(.*)count");
	Pattern modeDiff = Pattern.compile("_(.*)diff");
	
    @Override
    public void checkMessage(String[] message)
    {
        String[] trailing = message[3].split("[ ]+");
        Matcher m = modeFind.matcher(trailing[0]);
        if (m.find() && beatmapsByMode.containsKey(m.group(1).toLowerCase())) {
        	modeFind(trailing, m.group(1).toLowerCase());
        } else {
        	m = modeDiff.matcher(trailing[0]);
        	if (m.find() && beatmapsByMode.containsKey(m.group(1).toLowerCase())) {
        		if (m.group(1).equalsIgnoreCase("osu")) {
            		modeDiff(trailing, OsuMode.OSU.getModeNumber(), m.group(1));
        		} else if (m.group(1).equalsIgnoreCase("taiko")) {
            		modeDiff(trailing, OsuMode.TAIKO.getModeNumber(), m.group(1));
        		} else if (m.group(1).equalsIgnoreCase("fruit")) {
            		modeDiff(trailing, OsuMode.FRUIT.getModeNumber(), m.group(1));
        		} else {
            		modeDiff(trailing, OsuMode.MANIA.getModeNumber(), m.group(1));
        		}
        	} else {
        		m = modeCount.matcher(trailing[0]);
        		if (m.find() && beatmapsByMode.containsKey(m.group(1).toLowerCase())) {
        			modeCount(trailing, m.group(1).toLowerCase(), m.group(1));
        		} else if (trailing[0].startsWith("_") 
        				&& trailing[0].toLowerCase().endsWith("find")) {
        			messageSender.sendMessage(channel, "what scene");
        		}
        	}
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

    public double[] getDiff(String username, int mode, int numKeys)
    {

    	double[] toReturn = new double[2];
        URL url;
        try
        {
            url = new URL("https://osu.ppy.sh/api/get_user_best?k="+apiKey+"&u="+username+"&limit=50&m="+mode);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//            System.out.println("ping");
            System.out.println("API call to: "+url);
            String toParse = in.readLine();
            System.out.println("Info returned.");
            
            if (toParse.length() < 10)
            {
            	toReturn[0] = Double.MIN_VALUE;
                return toReturn;
            }
            
            String trim = toParse.substring(2, toParse.length()-2);
            //System.out.println(toParse);
            //System.out.println(trim);
            String[] scores = trim.split("\\},\\{");

            OsuScore[] os = new OsuScore[scores.length];
            for (int i = 0; i < os.length; i++)
            {
                os[i] = new OsuScore(scores[i]);
            }

            ArrayList<OsuScore> counted = new ArrayList<OsuScore>();
            for (OsuScore o : os)
            {
                if (o.getenabled_mods() == 0 && songsByID.containsKey(o.getbeatmap_id()))
                {
                	if (numKeys > 0) {
                		if (((int) songsByID.get(o.getbeatmap_id()).getDiff_size()) == numKeys) {
                			counted.add(o);
                		}
                	} else {
                        counted.add(o);
                	}
                }
            }

            double ppSum = 0;
            for (OsuScore o : counted)
            {
                ppSum += o.getpp();
            }
            
            System.out.println("songs used for diff: "+counted.size());

            double recDiff = 0;

            for (OsuScore o : counted)
            {
                //System.out.println();
                OsuSong song = songsByID.get(o.getbeatmap_id());
                double diff = song.getDifficultyrating();
                double acc = o.getAcc(song.getMode());
                double skew = 0.5*(acc-0.9)/0.1;

                //System.out.println(df.format(diff)+" "+df.format(skew)+" "+song.toString());
                //System.out.println(df.format(acc)+" ["+df.format(o.getpp()/ppSum)+"] "+o);

                recDiff += (diff+skew)*o.getpp()/ppSum;
            }
            toReturn[0] = recDiff;
            toReturn[1] = counted.size();
            return toReturn;
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return toReturn;
    }
}