package net.xurdm.xurbot;

import com.google.api.translate.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.*;

/**
 * 
 * @author Ryan
 */
public class XBot extends Poop {// Notice:(\\*){3} Notice
														// --
	private final Pattern botPattern = Pattern
			.compile(".*Client connecting on port (\\d+): (\\{.*\\|.*\\}.*)\\((.*)@(.*?)\\) \\[clients\\]");
	private final Pattern orgyPattern = Pattern
			.compile(".*Client connecting on port (\\d+): (.*OrgyBot\\|.*)\\((.*)@(.*?)\\) \\[clients\\]");
	private final Pattern strictPattern = Pattern
			.compile(":(\\*){3} Notice -- Client connecting on port (\\d+): ([a-z]){5,6} \\(([a-z]){5,6}@(.*?)\\) \\[clients\\]");
	private final String botMessage = " :You resemble a bot, contact an administrator if this is not the case. -psycho";
	private String sender, channel, login, hostname, message;
	private Database db;
	private Map<String, Method> methods;
	private Map<String, Method> adminMethods;
	private Map<String, String> history;
	private boolean strict = false;
	private boolean spassive = true;
	private Matcher botMatcher;
	private Date date = new Date();
	private DateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss yyyy");
	private Calendar calendar;

	private void timeAdd() {
		String timezone;
		if (countOccurrences(message, ' ') >= 1) {
			if ((timezone = message.split(" ")[1])
					.matches("GMT[+-]\\d{2}:\\d{2}"))
				db.add("time", sender, timezone);
			else
				sendMessage(
						channel,
						String.format(
								"%s: Please enter a timezone such that: timezone =~ /GMT[+\\-](?:[0][0-9]|[1][0-2])/",
								sender));
			db.save();
			return;
		}
		sendMessage(channel, sender + ": wrong syntax to add timezone.");
	}

	private void timeGet() {
		if (!db.containsKey("time", sender)) {
			sendMessage(
					channel,
					String.format(
							"%s: you have not set a timezone. Use .time.add GMT-/+xx:xx to set one for your nick.",
							sender));
			return;
		}
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("h:mm a MM/dd/yyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone(db.getValue("time", sender)
				.toString()));
		sendMessage(channel,
				String.format("%s: %s", sender, dateFormat.format(date)));
	}

	private void addBan() {// .ban Haunter *
		if (message.replaceAll("[^ ]", "").length() >= 1) {
			db.add("bans", message.split(" ")[1], message.split(" ")[2]);
			db.save();
		} else
			sendMessage(channel, String.format("%s: invalid syntax for ban."));
	}

	private void removeBan() {
		if (message.replaceAll("[^ ]", "").length() >= 2) {
			String nick = message.split(" ")[1];
			if (db.getTable("bans").containsKey(nick)) {
				db.getTable("bans").remove(message.split(" ")[1]);
				sendMessage(
						channel,
						String.format("ban on %s@%s has been removed.",
								message.split(" ")[1]));
			}
		} else
			sendMessage(channel,
					String.format("%s: wrong syntax for unban command."));
	}

	private void addVoice() {// .addv mask
		db.add("voice", message.split(" ")[1].replace("*", ".*"),
				message.split(" ")[2]);
	}

	private void removeVoice() {// .remv mask
		db.getTable("voice").remove(message.split(" ")[1]);
	}

	private void translate() {
		try {
			String translation = Translate.execute(
					message.substring(11, message.length()),
					Language.AUTO_DETECT, Language.ENGLISH);
			sendMessage(channel, String.format("%s: %s", sender, translation));
		} catch (Exception e) {
			Logger.Log(e, Logger.Level.USER);
		}
	}

	private void whatcd() {
		if (checkWhat())
			sendMessage(channel, sender + ": what.cd is up");
		else
			sendMessage(channel, sender + ": what.cd is down");
	}

	private void down() {
		String site = message.substring(6, message.length());
		if (checkSite(site))
			sendMessage(channel,
					String.format("%s: %s appears to be up", sender, site));
		else
			sendMessage(channel,
					String.format("%s: %s appears to be down", sender, site));
	}

	private void rape() {
		String person = message.substring(6, message.length());
		sendAction(channel, String.format("rapes %s", person));
	}

	private void reload() {
		db.load("db.txt");
	}

	private void save() {
		db.save();
	}

	private void sload() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				message.split(" ")[1])));
		String line, bot;
		while ((line = br.readLine()) != null)
			addSuspicious(line, "*");
		br.close();
		db.save();
	}

	private void mgline() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				message.split(" ")[1])));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"glinelog.txt")));
		String line, bot;
		while ((line = br.readLine()) != null) {
			bot = "*@" + line.split("\\(")[1].split("\\)")[0].split("@")[1];
			sendRawLine("GLINE " + bot + botMessage);
			bw.append(bot + "\r\n");
		}
		br.close();
		bw.close();
	}

	private void glineLog(String host) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"glinelog.txt")));
		bw.append(host + "\r\n");
		bw.close();
	}

	private void parseBots() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				message.split(" ")[1])));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"bots.txt")));
		String line, bot;
		log("Attempting to parse " + message.split(" ")[1]);
		while ((line = br.readLine()) != null) {
			if (line.contains("connecting on")) {
				if ((bot = line.split(":\\s")[1].split("\\s\\(")[0]).length() == 5) {
					bw.write("*@"
							+ line.split("\\(")[1].split("\\)")[0].split("@")[1]
							+ "\r\n");
					log("Adding " + bot + "...");
				}
			}
			if (line.contains("Too many connections")) {
				bot = line.split("\\[")[1].split("\\]")[0];
				bw.write(String.format("*%s%s\r\n", bot.startsWith("@") ? ""
						: "@", bot));
				log("Adding " + bot + "...");
			}
		}
		br.close();
		bw.close();
	}

	private void sgline() throws IOException {
		Object host;
		while ((host = db.popKey("suspicious")) != null) {
			sendRawLine("GLINE " + host + botMessage);
			glineLog(host.toString());
		}
		db.save();
	}

	private void addSuspicious(String host, String nick) {
		if (db.containsTable("suspicious"))
			db.add("suspicious", host, nick);
		else {
			db.addTable("suspicious");
			db.add("suspcious", host, nick);
		}
		db.save();
	}

	private void slist() {
		Object[] hosts = db.getKeys("suspicious").toArray();
		for (int i = 0; i < 10; ++i)
			sendNotice(channel, String.format("%d: %s", i, hosts[i]));
	}

	private void setStrict() {
		strict = Boolean.parseBoolean(message.split(" ")[1]);
	}

	private void setPassive() {
		spassive = Boolean.parseBoolean(message.split(" ")[1]);
	}

	private void getTimes() {
		User[] users;
		users = getUsers(message.split(" ")[1]);
		for (User user : users)
			sendMessage("NickServ", "info " + user.getNick());
	}

	private void saveActivity() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					"activity.txt")));
			for (Map.Entry<String, Object> entry : db.getTable("users")
					.entrySet())
				bw.write(entry.getValue() + " " + entry.getKey() + "\r\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addInvite() {
		String msg[] = { message.split(" ")[1], message.split(" ")[2] };
		String chans[] = msg[1].split(",");
		for (String chan : chans)
			db.add("malvager", msg[0], db.getValue("malvager", msg[0]) + ","
					+ chan.replace("#", "@"));
		sendMessage(channel,
				String.format("Added %s to %s's invite list.", msg[0], msg[1]));
	}

	public XBot() {
		this("xurbot", "xurbot", "xurbot");
	}

	public XBot(String nick, String login, String version) {
		setName(nick);
		setLogin(login);
		setVersion(version);
		Translate.setHttpReferrer("irc://irc.malvager.com");
		db = new Database();
		db.load("db.txt");

		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));

		methods = new HashMap<String, Method>();
		adminMethods = new HashMap<String, Method>();
		history = new HashMap<String, String>();
		db.addTable("history");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				db.save();
			}
		});

		try {
			// methods.put(".time", XBot.class.getDeclaredMethod("timeGet"));
			// methods.put(".time.add",
			// XBot.class.getDeclaredMethod("timeAdd"));

			// methods.put(".translate",
			// XBot.class.getDeclaredMethod("translate"));
			// methods.put(".down", XBot.class.getDeclaredMethod("down"));
			// methods.put(".rape", XBot.class.getDeclaredMethod("rape"));
			// methods.put(".whatcd", XBot.class.getDeclaredMethod("whatcd"));

			adminMethods.put(".ban", XBot.class.getDeclaredMethod("addBan"));
			adminMethods.put(".unban",
					XBot.class.getDeclaredMethod("removeBan"));
			adminMethods.put(".addv", XBot.class.getDeclaredMethod("addVoice"));
			adminMethods.put(".reload", XBot.class.getDeclaredMethod("reload"));
			adminMethods.put(".reload", XBot.class.getDeclaredMethod("reload"));
			adminMethods.put(".save", XBot.class.getDeclaredMethod("save"));
			adminMethods.put(".massgline",
					XBot.class.getDeclaredMethod("mgline"));
			adminMethods.put(".parsebots",
					XBot.class.getDeclaredMethod("parseBots"));
			adminMethods.put(".sload", XBot.class.getDeclaredMethod("sload"));
			adminMethods.put(".sgline", XBot.class.getDeclaredMethod("sgline"));
			adminMethods.put(".slist", XBot.class.getDeclaredMethod("slist"));
			adminMethods.put(".spassive",
					XBot.class.getDeclaredMethod("setPassive"));
			adminMethods.put(".sstrict",
					XBot.class.getDeclaredMethod("setStrict"));
			adminMethods.put(".gettimes",
					XBot.class.getDeclaredMethod("getTimes"));
			adminMethods.put(".saveact",
					XBot.class.getDeclaredMethod("saveActivity"));
			adminMethods.put(".addinv",
					XBot.class.getDeclaredMethod("addInvite"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean match(AbstractList<String> list, String x) {
		for (String item : list)
			if (x.matches(item)) {
				return true;
			} else
				log(String.format(">>>%s != %s", item, x));

		return false;
	}

	private boolean checkWhat() {
		try {
			WebFile fat = new WebFile("http://what.cd");
			if (fat.getResponseCode() == 200) {
				if (fat.getContent()
						.toString()
						.contains(
								"Quit speculating and don't panic. We are still here and will be back up shortly. We don't have an ETA but we are working on the issue"))
					return false;
				log(fat.getContent().toString());
				return true;
			}
		} catch (Exception e) {
			Logger.Log(e, Logger.Level.SEVERE);
		}
		return false;
	}

	private boolean checkSite(String url) {
		try {
			WebFile site = new WebFile(url.contains("http://") ? url
					: String.format("http://%s", url));
			if (site.getResponseCode() == 200)
				return true;
		} catch (Exception e) {
			Logger.Log(e, Logger.Level.USER);
		}
		return false;
	}

	private int countOccurrences(String haystack, char needle) {
		return haystack.replaceAll(String.format("[^%c]", needle), "").length();
	}

	@Override
	protected void onPrivateMessage(String sender, String login,
			String hostname, String message) {
		if (db.containsKey("malvager", sender)) {
			String value = db.getValue("malvager", sender).toString();
			String chans[] = value.split(",");
			if (chans != null)
				for (String chan : chans)
					sendRawLine(String.format("SAJOIN %s %s", sender,
							value.replace("@", "#")));
			// sendRawLine("SAJOIN " + sender + " " + db.getValue("malvager",
			// sender).toString().replace("@","#"));
		} else
			sendMessage(sender, "NO!!!!!!!!!1111111");
	}

	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		String pChannel, pSender, pLogin, pHostname, pMessage;
		pChannel = this.channel;
		pSender = this.sender;
		pLogin = this.login;
		pHostname = this.hostname;
		pMessage = this.message;
		db.add("history", pSender, pMessage);
		this.channel = channel;
		this.sender = sender;
		this.login = login;
		this.hostname = hostname;
		this.message = message;
		String command = message;

		try {
			if (db.containsKey("users", sender))
				db.add("users",
						sender,
						Long.parseLong(db.getValue("users", sender).toString()) + 1);
			else
				db.add("users", sender, "0");
			if (message.contains(" "))
				command = message.split(" ")[0];
			if (message.matches("s\\/.*\\/.*\\/")) {
				if (history.containsKey(sender)) {
					String old = history.get(sender);
					String replace = message.split("\\/")[1];
					String replaceWith = message.split("\\/")[2];
					sendMessage(
							channel,
							String.format("%s: %s", sender,
									old.replace(replace, replaceWith)));
					return;
				}
			} else if (message.matches("s\\/.*\\/.*\\/\\s.*")) {
				if (history.containsKey(message.split("\\s")[1])) {
					String old = history.get(message.split("\\s")[1])
							.toString();
					String replace = message.split("\\/")[1];
					String replaceWith = message.split("\\/")[2];
					sendMessage(
							channel,
							String.format("%s: %s", sender,
									old.replace(replace, replaceWith)));
					return;
				}
			}
			if (!message.startsWith("."))
				return;
			if (adminMethods.containsKey(command)
					&& db.getTable("admins").containsKey(sender)) {
				if (db.getTable("admins").get(sender).equals(hostname))
					adminMethods.get(command).invoke(this);
			} else if (methods.containsKey(command)) {
				if (db.getTable("bans").containsKey(sender)) {
					String host = db.getTable("bans").get(sender).toString()
							.replace("*", ".*");
					if (hostname.matches(host))
						sendNotice(sender,
								String.format("You are blacklisted.", sender));
					return;
				}
				methods.get(command).invoke(this);
			} else
				log(">>>" + message.split(" ")[0] + " not found.");
		} catch (Exception e) {
			e.printStackTrace();
			// Logger.Log(e, Logger.Level.USER);
		}

	}

	@Override
	protected void onJoin(String channel, String sender, String login,
			String hostname) {
		if (db.containsKey("users", sender))
			db.add("users", sender,
					Long.parseLong(db.getValue("users", sender).toString()) + 2);
		else
			db.add("users", sender, "0");

		if (db.containsKey("voice", sender)) {
			if ((login + "@" + hostname).matches(db.getValue("voice", sender)
					.toString().replace("*", ".*")))
				voice(channel, sender);
		}
	}

	@Override
	protected void onNotice(String sourceNick, String sourceLogin,
			String sourceHostname, String target, String notice) {
		try {
			log("Notice: " + notice);
			if (sourceNick.equals("NickServ")) {

			}
			if (strict) {
				botMatcher = strictPattern.matcher(notice);
				if (botMatcher.find()) {
					sendRawLine("GLINE *@" + botMatcher.group(5) + botMessage);
					glineLog("*@" + botMatcher.group(5));
					return;
				}
			}
			botMatcher = botPattern.matcher(notice);
			if (botMatcher.find()) {
				addSuspicious("*@" + botMatcher.group(4), botMatcher.group(2));
				log(String.format("Adding %s@%s to suspicious list.",
						botMatcher.group(2), botMatcher.group(4).trim()));
				if (!spassive)
					sgline();
			}
			botMatcher = orgyPattern.matcher(notice);
			if (botMatcher.find()) {
				addSuspicious("*@" + botMatcher.group(4), botMatcher.group(2));
				log(String.format("Adding %s@%s to suspicious list.",
						botMatcher.group(2), botMatcher.group(4).trim()));
				if (!spassive)
					sgline();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}