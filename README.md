Változások az eredetihez képest:
-
- Ha ki lettél bannolva, és új fiókkal akarsz felmenni, akkor nem fogja kiírni csatlakozásnál, hogy "regisztrálj a fyremc.hu weboldalon", és nem kell bezárnod a proxyt és újra megnyitni, hogy megint fel menj.
### Bekerült új parancsok:
- spi (SelectedProfileId) -  Ez generál egy új random számot a SelectedProfileIdhez, ez abban fog segíteni amit az előbb megemlítettem. 
- login [accessToken | uuid | username] - Generál egy új random számot a SelectedProfileIdhez, és talán kicsit könnyedén be tudsz lépni. Mindegy milyen sorrendben írod be a dolgokat, egy szűrővel megoldottam, hogy ne kelljen ezzel se foglalkoznod. Pl: login [accessToken, username, uuid], login [username, uuid, accessToken]
- fy - Ez lefuttatja a fyre parancsot és beállítja ipnek a play.fyremc.hu-t
- tutorial - Proxy használatának bemutása, illetve ha az se menne, akkor discordon segítséget lehet kérni
- help - Kiírja a parancsokat
- staffteam [admin] - Kiír néhány dolgot egy fmc adminról

### A módosított proxyban később kerülhetnek be olyan parancsok amik nem segítik a gyorsabb belépést!

Módosított fájl: [InteractiveProxy.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/standalone/src/main/java/me/marvin/proxy/InteractiveProxy.java), [Proxy.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/api/src/main/java/me/marvin/proxy/Proxy.java), [GameProfile.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/api/src/main/java/me/marvin/proxy/utils/GameProfile.java), [SessionService.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/api/src/main/java/me/marvin/proxy/utils/SessionService.java)

# Hozzáadott dolgok:
## Random SelectedProfileId, ServerId

\- Generál egy új SPI-t, így nem fogja új account csatlakozásnál kiírni hogy "regisztrálj a fyremc.hu weboldalon"
```java
String RandSelectedPid = String.valueOf(Math.random()).substring(2) ;
String serverId = proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2);
```
## Új parancsok:

\- fy
```java
commandTree.register(args -> {
    commandTree.execute("fyre");
    ServerAddress prev = proxy.address();
    proxy.address("play.fyremc.hu");
    logger.info("Changed address: '{}' -> '{}'", prev, proxy.addres());

    return true;
}, "fy");
```

\- login [username | uuid | accessToken]
```java
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
```
    
\- tutorial
```java
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
```

\- help
```java
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
```

\- spi
```java
commandTree.register(args -> {
    String prev = proxy.selectedProfileId();
    String serverPrev = proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2);
    String RandSelectedPid = String.valueOf(Math.random()).substring(2);
    proxy.selectedProfileId(RandSelectedPid);
    logger.info("Changed SelectedProfileId: {} -> {}", prev, proxy.selectedProfileId());
    logger.info("Changed ServerId: {} -> {}", serverPrev, proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2));

    return true;
}, "spi");
```

\-staffteam
```
java
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

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String Today = now.format(formatter);
    WeekFields weekFields = WeekFields.of(Locale.getDefault());
    String weekNumber = String.valueOf(now.get(weekFields.weekOfWeekBasedYear()));
    String getYear = String.valueOf(now.getYear());

    JsonArray onlineStatArray = JsonData.getAsJsonArray("onlinestat");
    String wasOnlineStr = "";
    boolean wasOnlineBool = false;
    JsonObject onlineStat;
    Iterator onlineStatK = onlineStatArray.iterator();
    JsonObject onlineStatParse = (JsonObject) JsonParser.parseString(onlineStatK.next().toString());
    int onlineTime;

    if (onlineStatParse.toString().contains(Today)) {
        onlineStat = onlineStatParse.get(Today).getAsJsonObject();
        onlineTime = onlineStat.get("online").getAsInt();
        wasOnlineStr = "True (" + onlineTime + " minute)";
        wasOnlineBool = true;
    } else {
        wasOnlineStr = "False";
    }

    JsonObject WeekOnlineStatParse = (JsonObject) JsonParser.parseString(onlineStatK.next().toString());
    JsonObject WeekOnlineStat;
    int WeekOnlineTime = 0;
    String WasActiveWeekStr = "";
    if (WeekOnlineStatParse.toString().contains(getYear + "-" + weekNumber)) {
        WeekOnlineStat = WeekOnlineStatParse.get(getYear + "-" + weekNumber).getAsJsonObject();
        WeekOnlineTime = WeekOnlineStat.get("online").getAsInt() / 60;
        if(WeekOnlineTime > 20) {
            WasActiveWeekStr += "True (20 < " + WeekOnlineTime + " hour)";
        } else {
            WasActiveWeekStr += "False (20 > " + WeekOnlineTime + " hour)";
        }
    }
        
    logger.info("StaffTeam");
    logger.info("Username: {}", name);
    logger.info("Rank: {}", rank);
    logger.info("Was today online? {}", wasOnlineStr);
    logger.info("Was active in this week? {}", WasActiveWeekStr);

    return true;
}, "staffteam");
```

## Módosított parancsok
\- credentials
```java
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
```


## Kotlin scriptek a módosított proxyhoz a discord szeron: https://discord.gg/qAn2TXtYFv
Eredeti: (https://github.com/marvintheskid/mc-reverse-proxy)
