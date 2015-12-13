package addonMisc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import base.AddOn;
import base.AmeBot;


public class aoBansonRune implements AddOn
{
    String channel;
    AmeBot bot;
    String botName;

    HashMap<String, ArrayList<String>> kanjiToKanji;
    Random rand;
    
    public aoBansonRune(String channel, AmeBot bot, String botName)
    {
        this.channel = channel;
        this.bot = bot;
        this.botName = botName;
        rand = new Random();
        
        HashMap<String, ArrayList<String>> kanjiToKana = new HashMap<>();
        HashMap<String, ArrayList<String>> kanaToKanji = new HashMap<>();
        kanjiToKanji = new HashMap<>();
        File f = new File("edict2uMod.txt");
        try
        {
            Scanner s = new Scanner(f, "UTF-8");
            while (s.hasNext())
            {
                String gg = s.nextLine();
                //System.out.println(gg);
                String[] split = gg.split(" ");
                String[] kanji = split[0].split(";");
                String[] kana = split[1].split(";");
                
                for (String sKanji : kanji)
                {
                    if (!kanjiToKana.containsKey(sKanji))
                    {
                        kanjiToKana.put(sKanji, new ArrayList<String>());
                    }
                    ArrayList<String> kanaString = kanjiToKana.get(sKanji);
                    for (String sKana : kana)
                    {
                        if (sKana.contains("("))
                        {
                            String onlyFor = sKana.substring(sKana.indexOf('(')+1,sKana.indexOf(')'));
                            String ssKana = sKana.substring(0, sKana.indexOf('('));
                            String[] kanjiFor = onlyFor.split(",");
                            for (String ssKanji : kanjiFor)
                            {
                                //System.out.println("ssKanji: "+ssKanji);
                                if (ssKanji.equals(sKanji))
                                {
                                    if (!kanaToKanji.containsKey(ssKana))
                                    {
                                        kanaToKanji.put(ssKana, new ArrayList<String>());
                                    }
                                    kanaString.add(ssKana);
                                    kanaToKanji.get(ssKana).add(sKanji);
                                }
                            }
                        }
                        else
                        {
                            if (!kanaToKanji.containsKey(sKana))
                            {
                                kanaToKanji.put(sKana, new ArrayList<String>());
                            }
                            kanaString.add(sKana);
                            kanaToKanji.get(sKana).add(sKanji);
                        }
                    }
                }
            }
            s.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("kanji loaded: "+kanjiToKana.size());
        System.out.println("kana loaded: "+kanaToKanji.size());
        for (String toCollapse : kanjiToKana.keySet())
        {
            HashSet<String> currSet = new HashSet<String>();
            for (String toAdd : kanjiToKana.get(toCollapse))
            {
                for (String toActuallyAdd : kanaToKanji.get(toAdd))
                {
                    currSet.add(toActuallyAdd);
                }
            }
            ArrayList<String> currArray = new ArrayList<>();
            for (String s : currSet)
            {
                currArray.add(s);
            }
            
            kanjiToKanji.put(toCollapse, currArray);
        }
        System.out.println("maps loaded: "+kanjiToKanji.size());
        System.out.println("aoBonsonRune been installed in "+channel);
    }

    @Override
    //:<prefix> <command> <params> :<trailing>
    public void checkMessage(String[] message)
    {
        String[] trailing = message[3].split("[ ]+");
        if (trailing[0].equalsIgnoreCase(".bansonrune"))
        {
            if (trailing.length == 1) return;
            if (!message[2].equalsIgnoreCase(channel)) return;
            String toBanson = message[3].substring(message[3].indexOf(" ")+1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < toBanson.length(); i++)
            {
                if (toBanson.length() > 1 && i%2 == 1 && rand.nextDouble() > 0.2)
                {
                    sb.append(toBanson.charAt(i));
                    continue;
                }
                String bansonChar = String.valueOf(toBanson.charAt(i));
                if (kanjiToKanji.containsKey(bansonChar))
                {
                    ArrayList<String> choices = kanjiToKanji.get(bansonChar);
                    sb.append(choices.get(rand.nextInt(choices.size())));
                }
                else
                {
                    sb.append(toBanson.charAt(i));
                }
            }
            try
            {
                bot.sendPrivmsg(channel, sb.toString());
                return;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
