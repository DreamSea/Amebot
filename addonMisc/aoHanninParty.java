package addonMisc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import base.AddOn;
import base.AmeBot;

public class aoHanninParty implements AddOn
{
    String channel;
    AmeBot bot;
    String botName;
    private HanninParty party;
    
    public aoHanninParty(String channel, AmeBot bot, String botName)
    {
        System.out.println("aoHanninParty been installed in "+channel);
        this.channel = channel;
        this.bot = bot;
        this.botName = botName;
    }
    
    @Override
    //:<prefix> <command> <params> :<trailing>
    public void checkMessage(String[] message)
    {
    	if (message[3].equals(".hannin")) {
            party = new HanninParty(this, 40, 20, 40, bot, channel);
    		bot.who(channel);
    	} else if (message[3].startsWith(".hannin ")) {
            if (party == null || party.getState() == hpState.COMPLETE) {
            	try {
            		String[] times = message[3].split(" ", 4);
            		party = new HanninParty(this, 
            					Integer.parseInt(times[1]), 
            					Integer.parseInt(times[2]), 
            					Integer.parseInt(times[3]), bot, channel);
            		bot.who(channel);
            	} catch (Exception e) {
                    sendPrivmsg("usage: .hannin <tLobby> <tNight> <tDay>");
            	}
            }
        } else if (message[3].equals(".join")) {
            if (party != null) {
                party.addPlayer(message[0].split("!")[0]);
            }
        } else if (message[3].startsWith(".accuse ")) {
            if (party != null) {
                try {
                    int num = Integer.parseInt(message[3].split(" ")[1]);
                    party.accuse(message[0].split("!")[0], num);
                } catch (NumberFormatException e) {
                    System.out.println("rip "+Arrays.toString(message)+": "+e);
                }
            }
        } else if (message[3].startsWith(".corpse ")) {
            if (party != null) {
                try {
                    int num = Integer.parseInt(message[3].split(" ")[1]);
                    party.corpse(message[0].split("!")[0], num);
                } catch (NumberFormatException e) {
                    System.out.println("rip "+Arrays.toString(message)+": "+e);
                }
            }
        }
    }
    
    public void sendPrivmsg(String user, String message) {
        try
        {
            bot.sendPrivmsg(user, message);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

    
    public void sendPrivmsg(String message) {
        try
        {
            bot.sendPrivmsg(channel, message);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

    
    

}

enum hpState {
    LOBBY, CORPSING, COMPLETE;
}

class HanninParty {
    private Set<String> players;
    private ArrayList<String> playerList;
    private hpState gameState = hpState.LOBBY;
    private int gameTick;
    private long lobbyTime;
    private long nightTime;
    private long dayTime;
    private Timer hanninTimer;
    private String corpse;
    private String hannin;
    private Random hanninRandom;
    int[] vote;
    aoHanninParty ao;
    
    HanninParty(aoHanninParty aohp, int lobby, int night, int day, final AmeBot bot, final String channel) {
        int tLobby = lobby > 60 ? 60 : lobby;
        int tNight = night > 60 ? 60 : night;
        int tDay = night > 60 ? 60 : day;
    	tLobby = tLobby > 5 ? tLobby : 5;
    	tLobby = tNight > 5 ? tLobby : 5;
    	tLobby = tDay > 5 ? tLobby : 5;
    	
        
        lobbyTime = tLobby*1000;
    	nightTime = tNight*1000;
    	dayTime = tDay*1000;
        
    	ao = aohp;
        players = new HashSet<String>();
        System.out.println(players);
        playerList = new ArrayList<String>();
        ao.sendPrivmsg("Gathering baddies: "+lobbyTime/1000+" seconds");
        hanninTimer = new Timer();
        hanninRandom = new Random();
        hanninTimer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                if (playerList.size() == 0) {
                    ao.sendPrivmsg("No baddies found.");
                    gameState = hpState.COMPLETE;
                    return;
                } 
//                else if (playerList.size() == 1) {
//                	ao.sendPrivmsg("One baddie found: "+playerList.get(0)+". They are probably the hannin.");
//                    gameState = hpState.COMPLETE;
//                    return;
//                }
                System.out.println(bot.getWho(channel));
            	ArrayList<String> idles = bot.getWho(channel);
                for (String player : playerList) {
                	idles.remove(player);
                }
                Collections.shuffle(idles);
                int toAdd = 5 - playerList.size();
                for (int i = 0; i < 5-toAdd; i++) {
                	String idle = idles.get(i);
                	playerList.add(idle.substring(0, idle.length()-1)+"(loitering)");
                }
            	
                StringBuilder sb = new StringBuilder();
                sb.append("Baddies collected: ");
                for (String s : playerList) {
                    sb.append(s+", ");
                }
                
                ao.sendPrivmsg(sb.substring(0, sb.length()-2));
                
                hannin = playerList.get(hanninRandom.nextInt(playerList.size()));
                if (!hannin.contains("(loitering)")) {
                    ao.sendPrivmsg(hannin, "You are the hannin! Turn everyone else into corpses!");	
                }
                gameState = hpState.CORPSING;
                doTick();
            }
        }, lobbyTime);
        gameTick = -1;
    	
    }

	private void doTick() {
        if (playerList.size() == 0) {
            ao.sendPrivmsg("The room is full of corpses.");
            gameState = hpState.COMPLETE;
        } else if (!playerList.contains(hannin)) {
            ao.sendPrivmsg("The corpse ("+hannin+") is full of hannin.");
            gameState = hpState.COMPLETE;
        } else if (playerList.size() == 1) {
            ao.sendPrivmsg("The hannin ("+hannin+") is full on corpse.");
            gameState = hpState.COMPLETE;
        }
        if (!gameState.equals(hpState.CORPSING)) {
            return;
        }
        
        gameTick++;
        if (gameTick%2 == 0) {
            ao.sendPrivmsg("Beginning night: "+nightTime/1000+" seconds");
            StringBuilder corpseChoice = new StringBuilder();
            corpseChoice.append("Who will you corpse [.corpse #]? ");
            for (int i = 0; i < playerList.size(); i++) {
                corpseChoice.append("("+i+")"+playerList.get(i)+", ");
            }
            ao.sendPrivmsg(hannin, corpseChoice.toString());
            
            corpse = null;
            hanninTimer.schedule(new TimerTask() {
                @Override
                public void run()
                {
                    if (corpse == null) {
                        ao.sendPrivmsg("No corpses were found.");
                    } else {
                        ao.sendPrivmsg(corpse+" has been corpsed by the hannin!");
                        playerList.remove(corpse);
                    }
                    doTick();
                }
            }, nightTime);
        } else {
            ao.sendPrivmsg("Beginning day: "+dayTime/1000+" seconds");
            StringBuilder suspects = new StringBuilder();
            vote = new int[playerList.size()];
            Arrays.fill(vote, -1);
            for (int i = 0; i < playerList.size(); i++) {
                suspects.append("("+i+")"+playerList.get(i)+", ");
            }
            ao.sendPrivmsg("Hannin suspects left: "+suspects.substring(0, suspects.length()-2));
            
            hanninTimer.schedule(new TimerTask() {
                @Override
                public void run()
                {
                    int[] tally = new int[vote.length];
                    for (int i = 0; i < vote.length; i++) {
                        if (vote[i] < tally.length && vote[i] >= 0) {
                            tally[vote[i]]++;
                        }
                    }
                    int max = 0;
                    for (int i = 0; i < tally.length; i++) {
                        if (tally[i] > max) max = tally[i];
                    }
                    if (max == 0) {
                        String corpse = playerList.get(hanninRandom.nextInt(playerList.size()));
                        ao.sendPrivmsg(corpse+" spontaneously corpses.");
                        playerList.remove(corpse);
                    } else {
                        ArrayList<String> corpsePotential = new ArrayList<String>();
                        StringBuilder accuseTotal = new StringBuilder();
                        accuseTotal.append("Hannin likelihood: ");
                        for (int i = 0; i < tally.length; i++) {
                            if (tally[i] == max) {
                                corpsePotential.add(playerList.get(i));
                            }
                            accuseTotal.append(tally[i]+" - "+playerList.get(i)+", ");
                        }
                        ao.sendPrivmsg(accuseTotal.substring(0, accuseTotal.length()-2));
                        
                        String corpse = corpsePotential.get(hanninRandom.nextInt(corpsePotential.size()));
                        StringBuilder corpseReason = new StringBuilder();
                        corpseReason.append(corpse+" is turned into a corpse by ");
                        for (int i = 0; i < vote.length; i++) {
                        	if (vote[i] < 0 || vote[i] >= playerList.size()) continue;
                            if (playerList.get(vote[i]).equals(corpse)) {
                                corpseReason.append(playerList.get(i)+", ");
                            }
                        }
                        corpseReason.setLength(corpseReason.length()-2);
                        corpseReason.append(". ");
                        
                        if (corpse.equals(hannin)) {
                            
                        } else {
                            corpseReason.append(corpse+" was not the hannin.");
                        }
                        ao.sendPrivmsg(corpseReason.toString());
                        playerList.remove(corpse);
                    }
                    doTick();
                }
            }, dayTime);
        }
    }
    
    public void addPlayer(String player) {
        if (getState() == hpState.LOBBY) {
            if (players.add(player)) {
                playerList.add(player);
            }
        }
    }
    
    public void accuse(String blamer, int blamee) {
        if (getState() == hpState.CORPSING && playerList.contains(blamer)) {
            vote[playerList.indexOf(blamer)] = blamee;
        }
    }
    
    public void corpse(String corpser, int toCorpse) {
    	if (!hannin.equals(corpser)) {
    		return;
    	}
        if (getState() == hpState.CORPSING && playerList.size() > toCorpse && toCorpse >= 0) {
            corpse = playerList.get(toCorpse);
        }
    }
    
    public hpState getState() {
        return gameState;
    }
}