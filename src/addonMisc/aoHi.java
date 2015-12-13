package addonMisc;
import java.io.IOException;

import base.AddOn;
import base.AmeBot;


public class aoHi implements AddOn
{
    String channel;
    AmeBot bot;
    String botName;

    public aoHi(String channel, AmeBot bot, String botName)
    {
        this.channel = channel;
        this.bot = bot;
        this.botName = botName;
        System.out.println("aoHi been installed in "+channel);
    }

    @Override
    //:<prefix> <command> <params> :<trailing>
    public void checkMessage(String[] message)
    {
        if (message[1].equals("JOIN") && message[3].equalsIgnoreCase(channel))
        {
            try
            {
                bot.sendPrivmsg(channel, "hi");
                return;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        if (message[2].equalsIgnoreCase(channel))
        {

            String[] array = message[3].split(" ");
            for (int i = 0; i < array.length; i++)
            {
                if (array[i].equalsIgnoreCase(botName))
                {
                    try
                    {
                        bot.sendPrivmsg(channel, "hi");
                        return;
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            // TODO Auto-generated method stub

        }
    }
}
