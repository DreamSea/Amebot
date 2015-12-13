package base;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.JFrame;

import addonMisc.aoBansonRune;
import addonMisc.aoHanninParty;
import addonMisc.aoHi;
import addonMisc.aoSokuBet;
import addonOsu.OsuScore;
import addonOsu.aoOsu;

//<<< :DreamSea!~DreamSea@limited.blade.works PRIVMSG Amebot :gg			/msg Amebot gg
//<<< :DreamSea!~DreamSea@limited.blade.works PRIVMSG #amebottest :gg		gg
//<<< :DreamSea!~DreamSea@limited.blade.works NICK :DreamSea_
//<<< :DreamSea_!~DreamSea@limited.blade.works PRIVMSG #amebottest :gg
//<<< :DreamSea_!~DreamSea@limited.blade.works NICK :DreamSea


public class AmeBot implements MessageSender, ApiConnection {

	private String nick;
	private String user;
	private String pass;
	private String realName = "AmeBot";
	private String host = "irc.rizon.net";
	private int port = 6669;

	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	private HashSet<String> channels;
	private HashSet<String> whitelist;
	
	private HashMap<String, ArrayList<String>> whoChannel;
	
	private ArrayList<AddOn> addons = new ArrayList<>();
	private HashMap<String, AddOn> addonsPool = new HashMap<>();
	
	private JFrame control;
	
	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.exit(0);
		}
		new AmeBot(args[0], args[1]);
	}

	public AmeBot(String nick, String pass)
	{
		whitelist = new HashSet<>();
		File safe = new File("whitelist.txt");
		whoChannel = new HashMap<String, ArrayList<String>>();
		Scanner s;
		try {
			s = new Scanner(safe);
			while (s.hasNextLine())
			{
				whitelist.add(s.nextLine());
			}
			s.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		channels = new HashSet<String>();
		this.nick = nick;
		this.user = nick;
		this.pass = pass;
		control = new ControlFrame (this);
		try {
			connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeLine(String message) throws IOException
	{
		if (!message.endsWith("\r\n"))
			message = message + "\r\n";
		
		System.out.print("  >> "+message);
		out.write(message);
		out.flush();
	}

	private void connect() throws IOException
	{
		socket = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		writeLine("NICK :" + nick);
		writeLine("USER " + user + " * * :" + realName);

		String line = null;
		//String[] args = null;
		//String command = null;
		String[] message = null;
		
		while((line = in.readLine()) != null) {
			System.out.println("<<   " + line);
			if (line.startsWith("PING")) {
				writeLine(line.replace("PING", "PONG"));
			}
			else
			{
				//:<prefix> <command> <params> :<trailing>
				message = Message.parseMessage(line);
				//System.out.println("<<   ["+message[0]+"]["+message[1]+"]["+message[2]+"]["+message[3]+"]");
				if (message[1].equals("001"))
				{					
					sendPrivmsg("NickServ", "identify "+pass);	//identify
					//writeLine("MODE Amebot R");					//iden only
					//writeLine("CAP LS");
					//writeLine("CAP REQ :multi-prefix"); //away-notify multi-prefix userhost-in-names
					//writeLine("CAP REQ :multi-prefix userhost-in-names");
					//writeLine("CAP END");
				} else if (message[1].equals("352")) {
					String[] channelStuff = message[2].split(" ");
					String channel = channelStuff[1];
					String denizen = channelStuff[5];
					if (whoChannel.containsKey(channel)) {
						whoChannel.get(channel).add(denizen);
					}
				}
				else if (message[1].equals("PRIVMSG"))
				{
					//http://www.irchelp.org/irchelp/rfc/ctcpspec.html
					if (message[3].equals("VERSION"))
					{
						writeLine("NOTICE "+message[0].substring(0, message[0].indexOf('!'))+" :\001VERSION Amebot 0.0.1\001");
					}
                    else if (message[3].startsWith(".install hanninparty"))
                    {
                         if (message[3].equals(".install hanninparty"))
                         {
                             String hanninInfo = "hanninparty "+message[2];
                             addonsPool.put(hanninInfo, new aoHanninParty(message[2], this, nick));
                             addons.add(addonsPool.get(hanninInfo));
                         }
                         else
                         {
                             String channelInfo = message[3].substring(message[3].indexOf(' ', 9)+1).toLowerCase();
                             if (channels.contains(channelInfo))
                             {
                                 String hanninInfo = "hanninparty "+channelInfo;    //skip hashmark
                                 addonsPool.put(hanninInfo, new aoHanninParty(channelInfo, this, nick));
                                 addons.add(addonsPool.get(hanninInfo));
                             }
                         }
                     }
					else if (message[3].startsWith(".install osu"))
                    {
                         if (message[3].equals(".install osu"))
                         {
                             String osuInfo = "osu "+message[2];
                             addonsPool.put(osuInfo, new aoOsu(message[2], this, this, nick));
                             addons.add(addonsPool.get(osuInfo));
                         }
                         else
                         {
                             String channelInfo = message[3].substring(message[3].indexOf(' ', 9)+1).toLowerCase();
                             if (channels.contains(channelInfo))
                             {
                                 String osuInfo = "osu "+channelInfo;    //skip hashmark
                                 addonsPool.put(osuInfo, new aoOsu(channelInfo, this, this, nick));
                                 addons.add(addonsPool.get(osuInfo));
                             }
                         }
                     }
					//whitelisted stuff
					/*else if (whitelist.contains(message[0]))
					{
						if (message[3].startsWith(".install sokubet"))
						{
							if (message[3].equals(".install sokubet"))
							{
								String sokubetInfo = "sokubet "+message[2];
								addonsPool.put(sokubetInfo, new aoSokuBet(message[2], this));
								addons.add(addonsPool.get(sokubetInfo));
							}
							else
							{
								String channelInfo = message[3].substring(message[3].indexOf(' ', 13)+1).toLowerCase();
								if (channels.contains(channelInfo))
								{
									String sokubetInfo = "sokubet "+channelInfo;	//skip hashmark
									addonsPool.put(sokubetInfo, new aoSokuBet(channelInfo, this));
									addons.add(addonsPool.get(sokubetInfo));
								}
							}
						}
						else if (message[3].equals(".remove sokubet"))
						{
							String sokubetInfo = "sokubet "+message[2];
							AddOn toRemove = addonsPool.get(sokubetInfo);
							if (toRemove != null)
							{
								addons.remove(addonsPool.get(sokubetInfo));
								addonsPool.remove(sokubetInfo);
							}
						}
						else if (message[3].startsWith(".install hi"))
                        {
                            if (message[3].equals(".install hi"))
                            {
                                String hiInfo = "hi "+message[2];
                                addonsPool.put(hiInfo, new aoHi(message[2], this, nick));
                                addons.add(addonsPool.get(hiInfo));
                            }
                            else
                            {
                                String channelInfo = message[3].substring(message[3].indexOf(' ', 9)+1).toLowerCase();
                                if (channels.contains(channelInfo))
                                {
                                    String hiInfo = "hi "+channelInfo;    //skip hashmark
                                    addonsPool.put(hiInfo, new aoHi(channelInfo, this, nick));
                                    addons.add(addonsPool.get(hiInfo));
                                }
                            }
                        }
						else if (message[3].startsWith(".install bansonrune"))
                        {
                            if (message[3].equals(".install bansonrune"))
                            {
                                String bansonruneInfo = "bansonrune "+message[2];
                                addonsPool.put(bansonruneInfo, new aoBansonRune(message[2], this, nick));
                                addons.add(addonsPool.get(bansonruneInfo));
                            }
                            else
                            {
                                String channelInfo = message[3].substring(message[3].indexOf(' ', 9)+1).toLowerCase();
                                if (channels.contains(channelInfo))
                                {
                                    String bansonruneInfo = "hi "+channelInfo;    //skip hashmark
                                    addonsPool.put(bansonruneInfo, new aoBansonRune(channelInfo, this, nick));
                                    addons.add(addonsPool.get(bansonruneInfo));
                                }
                            }
                        }
					}*/
				}
				for (AddOn a : addons)
				{
					a.checkMessage(message);
				}
			}
		}
	}

	public boolean isWhiteListed(String toCheck)
	{
		return whitelist.contains(toCheck);
	}
	
	public void sendPrivmsg(String target, String message) throws IOException {
		writeLine("PRIVMSG " + target + " :" + message);
	}
	
	public void who(String channel) {
		try {
			writeLine("WHO "+channel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		whoChannel.put(channel, new ArrayList<String>());
	}

	public void disconnect()
	{
		try {
			//for (String s : channels)
			//{
			//	writeLine("PART :"+s);
			//}
			writeLine("QUIT :hi");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void exit()
	{
		System.out.println("ggnore");
		System.exit(0);
	}
	
	public void joinChannel(String s)
	{
		try {
			writeLine("JOIN :"+s);
			channels.add(s);
			System.out.println(channels);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void sendMessage(String recipient, String message) {
		try {
			sendPrivmsg(recipient, message);
		} catch (IOException e) {
			System.err.println("sendMessage rip: "+e.toString());
		}
		
	}

	public ArrayList<String> getWho(String channel) {
		return whoChannel.get(channel);
	}

	@Override
	public String doGet(String target) throws MalformedURLException {
		URL url;
        StringBuilder toReturn = new StringBuilder();

		try {
			url = new URL(target);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            System.out.println("GET: "+url);
            String nextRead = in.readLine();
            while (nextRead != null) {
            	toReturn.append(nextRead);
            	nextRead = in.readLine();
            	if (nextRead != null) {
                	toReturn.append('\n');
            	}
            }
            System.out.println("Info returned.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return toReturn.toString();
	}


}
