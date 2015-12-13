package base;

public class Message {
	private static final String EMPTY = "";
	//private static final String SPACE = " ";
	private static final String TRAIL = " :";
	private static String[] parsed = new String[4];	//prefix, command, param, trailing

	private static int commandStart;
	private static int paramsEnd;
	private static int commandSpace;
	
	private Message()
	{
		
	}

	//http://calebdelnay.com/blog/2010/11/parsing-the-irc-message-format-as-a-client
	//:<prefix> <command> <params> :<trailing>
	public static String[] parseMessage(String s)
	{
		parsed[0] = EMPTY;
		parsed[1] = EMPTY;
		parsed[2] = EMPTY;
		parsed[3] = EMPTY;
		commandStart = 0;
		//prefix
		if (s.charAt(0) == ':')
		{
			commandStart = s.indexOf(' ')+1;
			parsed[0] = s.substring(1, commandStart-1);
		}
		
		//trailing
		paramsEnd = s.indexOf(TRAIL);
		if (paramsEnd >= 0)
		{
		    parsed[3] = s.substring(paramsEnd + 2);
		}
		else
		{
		    paramsEnd = s.length();
		}
		//////////////////////////////////////////////////////
		commandSpace = s.indexOf(' ', commandStart);
		if (commandSpace < 0 || commandSpace == paramsEnd)
		{
			parsed[1] = s.substring(commandStart, paramsEnd);
		}
		else
		{
			parsed[1] = s.substring(commandStart, commandSpace);
			parsed[2] = s.substring(commandSpace+1, paramsEnd);
		}
		return parsed;	
	}
}
