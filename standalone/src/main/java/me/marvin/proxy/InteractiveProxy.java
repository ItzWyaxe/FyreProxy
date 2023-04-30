package me.marvin.proxy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.epoll.Epoll;
import me.marvin.proxy.addon.ProxyAddonHandler;
import me.marvin.proxy.commands.impl.CommandTree;
import me.marvin.proxy.utils.ServerAddress;
import me.marvin.proxy.utils.Tristate;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
public class InteractiveProxy extends SimpleTerminalConsole {
    private volatile boolean isRunning;
    private final Proxy proxy;
    private final ProxyAddonHandler addonHandler;
    private final Logger logger;
    private final CommandTree commandTree;

    public InteractiveProxy(int port, String targetAddr) throws IOException {
        proxy = new Proxy(port, targetAddr);
        logger = proxy.logger();
        if (Epoll.isAvailable()) {
            proxy.logger().info("Using epoll...");
        }
        logger.info("Resolving address... ({})", targetAddr);
        logger.info("Resolved server address: {}", proxy.address());
        commandTree = new CommandTree();
        registerBuiltinCommands();
        addonHandler = new ProxyAddonHandler(proxy, commandTree);
        logger.info("This program is modified by Wyaxe!");
        logger.info("Use the 'help' command to list the commands");
        String RandSelectedPid = String.valueOf(Math.random()).substring(2);
        proxy.selectedProfileId(RandSelectedPid);
    }

    private void registerBuiltinCommands() {
        commandTree.register(args -> {
            if (args.length != 1) {
                logger.info("Usage: setip [ip]");
                return false;
            }

            ServerAddress prev = proxy.address();
            proxy.address(args[0]);
            logger.info("Changed address: '{}' -> '{}'", prev, proxy.address());
            return true;
        }, "setip", "ip");

        commandTree.register(args -> {
            shutdown();
            return true;
        }, "shutdown", "goodbye", "stop");

        commandTree.register(args -> {
            if (args.length != 1) {
                logger.info("Usage: setname [name]");
                return false;
            }

            proxy.name(args[0]);
            logger.info("Set name to: '{}'", proxy.name());
            return true;
        }, "setname");

        commandTree.register(args -> {
            String prev = proxy.selectedProfileId();
            String serverPrev = proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2);
            String RandSelectedPid = String.valueOf(Math.random()).substring(2);
            proxy.selectedProfileId(RandSelectedPid);
            logger.info("Changed SelectedProfileId: {} -> {}", prev, proxy.selectedProfileId());
            logger.info("Changed ServerId: {} -> {}", serverPrev, proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2));

            return true;
        }, "spi");

        commandTree.register(args -> {
            if (args.length != 3) {
                logger.info("Usage: login [accessToken | uuid | username]");
                return false;
            }
            String uuid = "";
            String username = "";
            String accessToken = "";

            int a = 0;
            while(a != 3) {
                if(args[a].contains("-") && !args[a].contains(".")) uuid = args[a];
                if(args[a].contains(".") && !args[a].contains("-")) accessToken = args[a];
                if(!args[a].contains("-") && !args[a].contains(".")) username = args[a];
                a++;
            }

            proxy.name(username);
            proxy.accessToken(accessToken);
            proxy.uuid(uuid.replace("-", ""));
            String prev = proxy.selectedProfileId();
            String serverPrev = proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2);
            String RandSelectedPid = String.valueOf(Math.random()).substring(2);
            proxy.selectedProfileId(RandSelectedPid);

            logger.info("--------------------");
            logger.info("Username: {}", proxy.name());
            logger.info("AccessToken: {}", proxy.accessToken());
            logger.info("UUID: {}", proxy.uuid());
            logger.info("SelectedProfileId changed to: {} -> {}", prev, proxy.selectedProfileId());
            logger.info("ServerId changed to: {} -> {}", serverPrev, proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2));
            logger.info("--------------------");

            return true;
        }, "login");

        commandTree.register(args -> {
            commandTree.execute("fyre");
            ServerAddress prev = proxy.address();
            proxy.address("play.fyremc.hu");
            logger.info("Changed address: '{}' -> '{}'", prev, proxy.address());

            return true;
        }, "fy");

        commandTree.register(args -> {
            logger.info("YT tutorial: https://www.youtube.com/watch?v=TrDlr-hEDmA");
            logger.info("For more help: https://discord.gg/qAn2TXtYFv");
            Desktop desktop = Desktop.getDesktop();
            try {
                URI discord = new URI("https://discord.gg/qAn2TXtYFv");
                desktop.browse(discord);
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }, "tutorial");
        
        commandTree.register(args -> {
            if(args.length != 1) {
                logger.info("Usage: staffteam [admin]");
                return false;
            }

            URL url = new URL("https://account.fyremc.hu/api/player/"+ args[0]);
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null, data = "";
            while ((line = reader.readLine()) != null) {
                data += line;
            }

            JsonObject StaffTeamJson = (JsonObject) JsonParser.parseString(data);
            if(StaffTeamJson.get("error").getAsBoolean()) {
                logger.info("Admin not found");
                return false;
            }

            JsonObject JsonData = StaffTeamJson.get("data").getAsJsonObject();
            String name = JsonData.get("username").getAsString();
            String rank = JsonData.get("rank").getAsString();

            String AdminRanks = "Admin Admin+ Veteran Team Owner Moderator Builder Builder+ Jr.Moderator";

            if(!AdminRanks.contains(rank)) {
                logger.info("This user is not admin");
                return false;
            }

            if(StaffTeamJson.toString().contains("cached")) {
                logger.info("StaffTeam");
                logger.info("Username: {}", name);
                logger.info("Rank: {}", rank);
                return true;
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String Today = now.format(formatter);
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            String weekNumber = String.valueOf(now.get(weekFields.weekOfWeekBasedYear()));
            String getYear = String.valueOf(now.getYear());

            JsonArray onlineStatArray = JsonData.getAsJsonArray("onlinestat");
            String wasOnlineStr;
            JsonObject onlineStat;
            Iterator onlineStatK = onlineStatArray.iterator();
            JsonObject onlineStatParse = (JsonObject) JsonParser.parseString(onlineStatK.next().toString());
            int onlineTime;

            if (onlineStatParse.toString().contains(Today)) {
                onlineStat = onlineStatParse.get(Today).getAsJsonObject();
                onlineTime = onlineStat.get("online").getAsInt();
                wasOnlineStr = "True (" + onlineTime + " minute)";
            } else {
                wasOnlineStr = "False";
            }

            JsonObject WeekOnlineStatParse = (JsonObject) JsonParser.parseString(onlineStatK.next().toString());
            JsonObject WeekOnlineStat;
            int WeekOnlineTime;
            String WasActiveWeekStr = "";
            if (WeekOnlineStatParse.toString().contains(getYear + "-" + weekNumber)) {
                WeekOnlineStat = WeekOnlineStatParse.get(getYear + "-" + weekNumber).getAsJsonObject();
                WeekOnlineTime = WeekOnlineStat.get("online").getAsInt() / 60;
                if(WeekOnlineTime >= 20) {
                    WasActiveWeekStr += "True (20 <= " + WeekOnlineTime + " hour)";
                } else {
                    WasActiveWeekStr += "False (20 > " + WeekOnlineTime + " hour)";
                }
            } else {
                WasActiveWeekStr += "False (20 > 0 hour)";
            }

            JsonObject MonthOnlineStat;
            String WasActiveMonthStr = "";
            int MonthOnlineTime = 0;
            int WasNoActiveWeek = 0;
            int weekcm = now.get(weekFields.weekOfWeekBasedYear())-4;
            while (weekcm < now.get(weekFields.weekOfWeekBasedYear())) {
                if (WeekOnlineStatParse.toString().contains(getYear + "-" + weekcm)) {
                    MonthOnlineStat = WeekOnlineStatParse.get(getYear + "-" + weekcm).getAsJsonObject();
                    MonthOnlineTime += MonthOnlineStat.get("online").getAsInt() / 60;
                    if(MonthOnlineTime >= 150) {
                        WasActiveMonthStr = "True (150 <= " + MonthOnlineTime + " hour)";
                    } else {
                        WasActiveMonthStr = "False (150 > " + MonthOnlineTime +" hour)";
                    }
                } else {
                    WasNoActiveWeek++;
                }
                weekcm++;
            }

            logger.info("StaffTeam");
            logger.info("Username: {}", name);
            logger.info("Rank: {}", rank);
            logger.info("Was today online? {}", wasOnlineStr);
            logger.info("Was active in this week? {}", WasActiveWeekStr);
            logger.info("Was active in the last 30 days? {}", WasActiveMonthStr);
            if (WasNoActiveWeek > 0) {
                logger.info("He/She was inactive for {} weeks in the last 30 days", WasNoActiveWeek);
            }

            return true;
        }, "staffteam");

        commandTree.register(args -> {
            if (args.length != 1) {
                logger.info("Usage: setuuid [uuid]");
                return false;
            }

            proxy.uuid(args[0].replace("-", ""));
            logger.info("Set uuid to: '{}'", proxy.uuid());
            return true;
        }, "setuuid");

        commandTree.register(args -> {
            if (args.length != 1) {
                logger.info("Usage: settoken [access token]");
                return false;
            }

            proxy.accessToken(args[0]);
            logger.info("Set access token to: '{}'", proxy.accessToken());
            return true;
        }, "settoken");

        commandTree.register(args -> {
            logger.info("Current credentials:");
            logger.info("Session Service: {}", proxy.sessionService());
            logger.info("Name: '{}'", proxy.name());
            logger.info("UUID: '{}'", proxy.uuid());
            logger.info("Token: '{}'", proxy.accessToken());
            logger.info("SelectedProfileId: '{}'", proxy.selectedProfileId());
            logger.info("ServerId: '{}'", proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2));
            return true;
        }, "credentials");

        commandTree.register(args -> {
            logger.info("----------------");
            logger.info("fy --> Session service, set server address to play.fyremc.hu");
            logger.info("fyre --> Session service");
            logger.info("setip [play.fyremc.hu] --> Set server address to [...]");
            logger.info("settoken [accessToken] --> Set accessToken to [...]");
            logger.info("setuuid [uuid] --> Set uuid to [...]");
            logger.info("setname [username] --> Set username to [...]");
            logger.info("login [accessToken | uuid | username] --> Set accessToken, uuid, username to [...], generate new SPI, ServerId");
            logger.info("spi --> Generate new SelectedProfileId, ServerId");
            logger.info("credentials --> Current credentials");
            logger.info("tutorial --> For more help");
            logger.info("----------------");

            return true;
        }, "help");

        commandTree.register(args -> {
            StringBuilder threadDump = new StringBuilder(System.lineSeparator());
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            for (ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true)) {
                threadDump.append(threadInfo.toString());
            }
            logger.info(threadDump.toString());
            return true;
        }, "threaddump");
    }

    @Override
    public void start() {
        CountDownLatch lock = new CountDownLatch(1);

        new Thread(() -> {
            try {
                proxy.start(f -> {
                    if (f.isSuccess()) {
                        logger.info("Listening on {}", f.channel().localAddress());
                        isRunning = true;
                        lock.countDown();
                    } else {
                        logger.fatal("Failed to bind on :{}", proxy.port(), f.cause());
                        lock.countDown();
                    }
                });
            } catch (InterruptedException e) {
                logger.fatal("Interrupted while starting up the proxy", e);
                lock.countDown();
            }
        }).start();

        try {
            lock.await();
            if (isRunning()) {
                super.start();
            } else {
                shutdown();
            }
        } catch (InterruptedException e) {
            logger.fatal("Interrupted while starting up the console", e);
            shutdown();
        }
    }

    @Override
    protected boolean isRunning() {
        return isRunning;
    }

    @Override
    protected void runCommand(String command) {
        try {
            if (commandTree.execute(command) == Tristate.NOT_SET) {
                logger.info("Unknown command '{}'", command);
            }
        } catch (Exception ex) {
            logger.error("An error happened while executing '{}'", command, ex);
        }
    }

    @Override
    protected void shutdown() {
        logger.info("Shutting down...");
        isRunning = false;
        addonHandler.stop();

        try {
            proxy.shutdown();
        } catch (InterruptedException e) {
            logger.fatal("Interrupted while shutting down", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Goodbye!");
        System.exit(1);
    }
}
