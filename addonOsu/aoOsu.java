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
    List<List<OsuSong>> songsByMode = new ArrayList<List<OsuSong>>();
    private diffComparator difficulty = new diffComparator();

    public aoOsu(String channel, MessageSender messageSender, String botName)
    {
        for (int i = 0; i < 4; i++)
        {
            songsByMode.add(new ArrayList<OsuSong>());
        }

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

        // third file is bogus
        String[] songFiles = {"taikoSongs.txt", "osuSongs.txt", "maniaSongs.txt", "maniaSongs.txt"};

        try
        {
            for (int i = 0; i < songFiles.length; i++)
            {
                File f = new File(songFiles[i]);
                BufferedReader br = new BufferedReader(new FileReader(f));
                String nextLine = br.readLine();
                while (nextLine != null)
                {
                    OsuSong current = new OsuSong(nextLine);
                    if (!songsByID.containsKey(current.getBeatmapID()))
                    {
                        songsByID.put(current.getBeatmapID(), current);
                        songsByMode.get(current.getMode()).add(current);
                    }
                    nextLine = br.readLine();
                }
                br.close();
            }

            for (List<OsuSong> types : songsByMode)
            {
                Collections.sort(types, difficulty);
            }
            for (int i = 0; i < 4; i++)
            {
                System.out.println("Mode "+i+": "+songsByMode.get(i).size()+" songs loaded");
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

    public int search(double diff, double margin, int mode)
    {   
        List<OsuSong> type = songsByMode.get(mode);
        boolean startFound = false;
        int startIndex = type.size()-1;
        int endIndex = type.size()-1;
        for (int i = 0; i < type.size(); i++)
        {
            if (!startFound)
            {
                if (type.get(i).getDifficultyRating() >= diff - margin)
                {
                    startIndex = i;
                    startFound = true;
                }
            }
            if (startFound)
            {
                if (type.get(i).getDifficultyRating() >= diff + margin)
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

    public int rangeDiff(double lower, double upper, int mode)
    {
        List<OsuSong> type = songsByMode.get(mode);
        int count = 0;
        boolean lowerFound = false;
        for (int i = 0; i < type.size(); i++)
        {
            if (!lowerFound && type.get(i).getDifficultyRating() >= lower)
            {
                lowerFound = true;
            }
            if (type.get(i).getDifficultyRating() > upper)
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

	private void modeFind(String[] trailing, int mode) {
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
			messageSender.sendMessage(channel, mapString(songsByMode.get(mode)
					.get(index).getBeatmapID()));
		} catch (NumberFormatException e) {
			System.out.println("nope: " + trailing[1]);
		}
	}
	
	private void modeCount(String[] trailing, int mode, String modeName) {
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
		double diff = getDiff(user, mode);
		
		if (diff == Double.MIN_VALUE)
		{
			messageSender.sendMessage(channel, "No "+modeName+" plays found for "+user+". :(");
		    return;
		}
		
		String recDiff = "Recommended "+modeName+" difficulty for "+user+": "+df.format(diff);
		messageSender.sendMessage(channel, recDiff);
	}
    
    @Override
    public void checkMessage(String[] message)
    {
        String[] trailing = message[3].split("[ ]+");
		if (trailing[0].equalsIgnoreCase("_taikofind")) {
			modeFind(trailing, 1);
		} else if (trailing[0].equalsIgnoreCase("_osufind")) {
			modeFind(trailing, 0);
		} else if (trailing[0].equalsIgnoreCase("_maniafind")) {
			modeFind(trailing, 3);
		}  else if (trailing[0].equalsIgnoreCase("_taikocount")) {
			modeCount(trailing, 1, "Taiko");
		} else if (trailing[0].equalsIgnoreCase("_osucount")) {
			modeCount(trailing, 0, "Osu");
		} else if (trailing[0].equalsIgnoreCase("_maniacount")) {
			modeCount(trailing, 3, "Mania");
		} else if (trailing[0].equalsIgnoreCase("_taikodiff")) {
			modeDiff(trailing, 1, "taiko");
		} else if (trailing[0].equalsIgnoreCase("_osudiff")) {
			modeDiff(trailing, 0, "osu");
		} else if (trailing[0].equalsIgnoreCase("_maniadiff")) {
			modeDiff(trailing, 3, "mania");
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
            double diff = o1.getDifficultyRating() - o2.getDifficultyRating();
            if (diff > 0) return 1;
            else if (diff < 0) return -1;
            else return 0;
        }
    }

    public double getDiff(String username, int mode)
    {

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
                return Double.MIN_VALUE;
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
                    counted.add(o);
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
                double diff = song.getDifficultyRating();
                double acc = o.getAcc(song.getMode());
                double skew = 0.5*(acc-0.9)/0.1;

                //System.out.println(df.format(diff)+" "+df.format(skew)+" "+song.toString());
                //System.out.println(df.format(acc)+" ["+df.format(o.getpp()/ppSum)+"] "+o);

                recDiff += (diff+skew)*o.getpp()/ppSum;
            }
            return recDiff;
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
        return 0;
    }
}