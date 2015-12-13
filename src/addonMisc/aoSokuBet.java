package addonMisc;
import java.io.IOException;
import java.util.HashMap;

import base.AddOn;
import base.AmeBot;

//TODO: check win/loss math
//TODO: change username requirement
//TODO: implement 

public class aoSokuBet implements AddOn {

	private String channel;
	private AmeBot bot;
	private HashMap<String, Integer> players = new HashMap<>();
	private HashMap<String, Integer> betsA = new HashMap<>();
	private HashMap<String, Integer> betsB = new HashMap<>();
	private int sumA;
	private int sumB;
	private String player;
	private String lowerCasePlayer;
	private String betSize;
	private int betCheck;
	private String choiceA;
	private String choiceB;
	private StringBuilder results = new StringBuilder();
	private int state = 0;


	public aoSokuBet(String channel, AmeBot bot)
	{
		this.channel = channel;
		this.bot = bot;
		System.out.println("aoSokuBet been installed in "+channel);
//		try {
//			bot.sendPrivmsg(channel, "SokuBetting has begun, \"/msg Amebot signup\" to join in~");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	//.sbA <stuff>, .sbB <stuff>, .sbStart, .sbClose, .sbResultA
	
	@Override
	//:<prefix> <command> <params> :<trailing>
	public void checkMessage(String[] message)
	{
//		System.out.println("sbchecking: "+message[3]);
		if (message[1].equals("PRIVMSG") && message[2].equals("Amebot"))
		{	
			player = message[0].substring(0, message[0].indexOf('!'));
			lowerCasePlayer = player.toLowerCase();
//			if (message[3].equals("signup"))
//			{
//				if (!players.containsKey(lowerCasePlayer))
//				{
//					players.put(lowerCasePlayer, 10);
//					try {
//						bot.sendPrivmsg(channel, player+" has been given 10 free stones for signing up~");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			} else
			if (state == 0 && bot.isWhiteListed(message[0]))
			{
				if (message[3].startsWith(".sbA "))
				{
					choiceA = message[3].substring(message[3].indexOf(' ')+1);
					System.out.println("A: "+choiceA);
				}
				else if (message[3].startsWith(".sbB "))
				{
					choiceB = message[3].substring(message[3].indexOf(' ')+1);
					System.out.println("B: "+choiceB);	
				}
				else if (message[3].startsWith(".sbStart "))
				{
					if (choiceA != null && choiceB != null)
					{
						try {
							bot.sendPrivmsg(channel, "A SokuBet has started: "+message[3].substring(message[3].indexOf(' ')+1));
							bot.sendPrivmsg(channel, "[A] "+choiceA+"   ||   [B] "+choiceB);
							bot.sendPrivmsg(channel, "ex. \"/msg Amebot voteA 10\"");
							state = 1;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						System.out.println("a choice hasn't been set yet");
					}
				}
			}
			else if (state == 1)
			{
				if (message[3].startsWith("voteA ") || message[3].startsWith("voteB "))
				{
					if (!players.containsKey(lowerCasePlayer))
					{
						try {
							bot.sendPrivmsg(player, "New player detected, you've been given 10 free stones to bet with~");
							players.put(lowerCasePlayer, 10);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					betSize = message[3].substring(message[3].indexOf(' ')+1);
					try
					{
						betCheck = Math.min(Integer.valueOf(betSize), players.get(lowerCasePlayer));
						if (betCheck <= 0) throw new Exception();
						if (message[3].startsWith("voteA "))
						{
							bot.sendPrivmsg(player, "Bet of "+betCheck+" stone(s) out of "+players.get(lowerCasePlayer)+" placed on [A] "+choiceA);
							betsA.put(lowerCasePlayer, betCheck);
							if (betsB.containsKey(lowerCasePlayer))
							{
								betsB.remove(lowerCasePlayer);
							}
						}
						else
						{
							bot.sendPrivmsg(player, "Bet of "+betCheck+" stone(s) out of "+players.get(lowerCasePlayer)+" placed on [B] "+choiceB);
							betsB.put(lowerCasePlayer, betCheck);
							if (betsA.containsKey(lowerCasePlayer))
							{
								betsA.remove(lowerCasePlayer);
							}
						}
					}
					catch (Exception ex)
					{
						try {
							bot.sendPrivmsg(player, "Invalid bet amount: "+betSize);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else if (message[3].equals(".sbClose") && bot.isWhiteListed(message[0]))
				{
					state = 2;
					sumA = 1;
					sumB = 1;
					for (Integer i : betsA.values())
					{
						sumA += i;
					}
					for (Integer i : betsB.values())
					{
						sumB += i;
					}
					try {
						bot.sendPrivmsg(channel, "Betting is closed~");
						bot.sendPrivmsg(channel, betsA.size()+" vote(s) for a total of "+(sumA-1)+" stone(s) on [A] "+choiceA);
						bot.sendPrivmsg(channel, betsB.size()+" vote(s) for a total of "+(sumB-1)+" stone(s) on [B] "+choiceB);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else if (state == 2 && bot.isWhiteListed(message[0]))
			{
				if (message[3].equals(".sbResultA") || message[3].equals(".sbResultB"))
				{
					System.out.println("finishing bet");
					state = 0;
					if (message[3].equals(".sbResultA"))
					{
						try {
							bot.sendPrivmsg(channel, "Winning Side: [A] "+choiceA);
							results = new StringBuilder();
							results.append("Winners: ");
							for (String p : betsA.keySet())
							{
								players.put(p, players.get(p)+(sumB-1)*betsA.get(p)/sumA);
								results.append(p);
								results.append("[+");
								results.append((sumB-1)*betsA.get(p)/sumA);
								results.append("][");
								results.append(players.get(p));
								results.append("], ");
							}
							if (betsA.size() > 0) results.delete(results.length()-2, results.length());
							bot.sendPrivmsg(channel, results.toString());
							results = new StringBuilder();
							results.append("Losers: ");
							for (String p : betsB.keySet())
							{
								players.put(p, players.get(p)-betsB.get(p));
								if (players.get(p) < 0)
								{
									players.put(p, 0);
								}
								results.append(p);
								results.append("[-");
								results.append(betsB.get(p));
								results.append("][");
								results.append(players.get(p));
								results.append("], ");
							}
							if (betsB.size() > 0) results.delete(results.length()-2, results.length());
							bot.sendPrivmsg(channel, results.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if (message[3].equals(".sbResultB"))
					{
						try {
							bot.sendPrivmsg(channel, "Winning Side: (B)"+choiceB);
							results = new StringBuilder();
							results.append("Winners: ");
							for (String p : betsB.keySet())
							{
								players.put(p, players.get(p)+(sumA-1)*betsB.get(p)/sumB);
								results.append(p);
								results.append("[+");
								results.append((sumA-1)*betsB.get(p)/sumB);
								results.append("][");
								results.append(players.get(p));
								results.append("], ");
							}
							if (betsB.size() > 0) results.delete(results.length()-2, results.length());
							bot.sendPrivmsg(channel, results.toString());
							results = new StringBuilder();
							results.append("Losers: ");
							for (String p : betsA.keySet())
							{
								players.put(p, players.get(p)-betsA.get(p));
								if (players.get(p) < 0)
								{
									players.put(p, 0);
								}
								results.append(p);
								results.append("[-");
								results.append(betsA.get(p));
								results.append("][");
								results.append(players.get(p));
								results.append("], ");
							}
							if (betsA.size() > 0) results.delete(results.length()-2, results.length());
							bot.sendPrivmsg(channel, results.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						bot.sendPrivmsg(channel, "All bettors have been given a daily free stone~");
						for (String p : betsA.keySet())
						{
							players.put(p, players.get(p)+1);
						}
						for (String p : betsB.keySet())
						{
							players.put(p, players.get(p)+1);
						}
						betsA.clear();
						betsB.clear();
						choiceA = null;
						choiceB = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}


