package net.xurdm.xurbot;

/**
 *
 * @author Ryan
 */
public class Main {
	
    public static void main(String[] args) throws Exception {
        XBot bot = new XBot("xurdm_","xurdm","xurdm");
        bot.setVerbose(true);
        bot.connect("irc.malvager.com", 6667);
        bot.sendRawLine("OPER Psycho 456faggot123");
        bot.joinChannel("#hackforums");
        bot.joinChannel("#malvager");
        bot.joinChannel("#xurdm","poop");
        bot.joinChannel("#radio");
    }

}
