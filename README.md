## Kotlin scriptek a módosított proxyhoz a discord szeron: https://discord.gg/qAn2TXtYFv
## Tutorial a discord szeromon, vagy a youtube csatornámon!

> Változások az eredetihez képest:
- Ha ki lettél bannolva, és új fiókkal akarsz felmenni, akkor nem fogja kiírni csatlakozásnál, hogy "regisztrálj a fyremc.hu weboldalon", és nem kell bezárnod a proxyt és újra megnyitni, hogy megint fel menj.
- MultiAccount elérhető

> A módosított proxyban később kerülhetnek be olyan parancsok, amik nem segítik a gyorsabb belépést!

Módosított fájl: [InteractiveProxy.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/standalone/src/main/java/me/marvin/proxy/InteractiveProxy.java), [Proxy.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/api/src/main/java/me/marvin/proxy/Proxy.java), [GameProfile.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/api/src/main/java/me/marvin/proxy/utils/GameProfile.java), [SessionService.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/api/src/main/java/me/marvin/proxy/utils/SessionService.java), [ProxyBootstrap.java](https://github.com/ItzWyaxe/FyreProxy/blob/main/standalone/src/main/java/me/marvin/proxy/ProxyBootstrap.java)

\- Generál egy új SPI-t, így nem fogja új account csatlakozásnál kiírni hogy "regisztrálj a fyremc.hu weboldalon"
```java
String RandSelectedPid = String.valueOf(Math.random()).substring(2);
String serverId = proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2);
```

# Új paranacsok:

<details>
<summary>fy</summary>
Ez lefuttatja a fyre parancsot és beállítja ipnek a play.fyremc.hu-t

```java
commandTree.register(args -> {
    commandTree.execute("fyre");
    ServerAddress prev = proxy.address();
    proxy.address("play.fyremc.hu");
    logger.info("Changed address: '{}' -> '{}'", prev, proxy.address());
    logger.info("Join IP: localhost:{} (Copied)", proxy.port());
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(new StringSelection("localhost:"+proxy.port()), null);
    return true;
}, "fy");
```
</details>

<details>
<summary>login [username | uuid | accessToken]</summary>
Generál egy új random számot a SelectedProfileIdhez, és talán kicsit könnyedén be tudsz lépni. Mindegy milyen sorrendben írod be a dolgokat, egy szűrővel megoldottam, hogy ne kelljen ezzel se foglalkoznod. Pl: login [accessToken, username, uuid], login [username, uuid, accessToken]

```java
commandTree.register(args -> {
    String uuid = "";
    String username = "";
    String accessToken = "";
    if (!proxy.sessionService().toString().contains("FyreSession")) {
        commandTree.execute("fy");
    }
    if (args.length == 3) {
        int a = 0;
        while (a != 3) {
            if (args[a].contains("-") && !args[a].contains(".")) uuid = args[a];
            if (args[a].contains(".") && !args[a].contains("-")) accessToken = args[a];
            if (!args[a].contains("-") && !args[a].contains(".")) username = args[a];
            a++;
        }
    } else if (args.length == 1) {
        try {
            JsonObject FmcJsonParse = (JsonObject) JsonParser.parseString(args[0]);
            String FmcJsonStr =  FmcJsonParse.toString();
            if(!FmcJsonStr.contains("accessToken") || !FmcJsonStr.contains("id") || !FmcJsonStr.contains("name")) {
                logger.info("accessToken, UUID or username not found");
                return false;
            }
            accessToken = FmcJsonParse.get("accessToken").getAsString();
            JsonObject SelectedProfile = FmcJsonParse.get("selectedProfile").getAsJsonObject();
            username = SelectedProfile.get("name").getAsString();
            uuid = SelectedProfile.get("id").getAsString();
        } catch (JsonParseException e) {
            logger.info("Usage: login {\"accessToken\": \"access\", \"selectedProfile\":{\"name\": \"Username\"}}");
            return false;
        }
    } else {
        logger.info("Usage: login [accessToken | uuid | username] or login [json]");
        return false;
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
</details>

<details>
<summary>tutorial</summary>
Proxy használatának bemutása, illetve ha az se menne, akkor discordon segítséget lehet kérni

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
</details>

<details>
<summary>help</summary>
Kiírja a parancsokat

```java
commandTree.register(args -> {
    logger.info("----------------");
    logger.info("fy --> Session service, set server address to play.fyremc.hu");
    logger.info("fyre --> Session service");
    logger.info("setip [play.fyremc.hu] --> Set server address to [...]");
    logger.info("settoken [accessToken] --> Set accessToken to [...]");
    logger.info("setuuid [uuid] --> Set uuid to [...]");
    logger.info("setname [username] --> Set username to [...]");
    logger.info("login [accessToken | uuid | username] or [json] --> Set accessToken, uuid, username  to [...], generate new SPI, ServerId");
    logger.info("spi --> Generate new SelectedProfileId, ServerId");
    logger.info("credentials --> Current credentials");
    logger.info("player [player] --> Information about of a player");
    logger.info("tutorial --> For more help");
    logger.info("----------------");
    return true;
```
</details>

<details>
<summary>spi (SelectedProfileId)</summary>
Ez generál egy új random számot a SelectedProfileIdhez, ez abban fog segíteni amit az előbb megemlítettem. 

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
</details>

<details>
<summary>player [player]</summary>
Kiír néhány dolgot egy fyremc játékosról

```java
commandTree.register(args -> {
    if (args.length != 1) {
        logger.info("Usage: player [player]");
        return false;
    }
    URL FyremcPlayerAPI = new URL("https://account.fyremc.hu/api/player/"+ args[0]);
    URLConnection connection = FyremcPlayerAPI.openConnection();
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String line;
    StringBuilder data = new StringBuilder();
    while ((line = reader.readLine()) != null) {
        data.append(line);
    }
    JsonObject FmcPlayerJson = (JsonObject) JsonParser.parseString(data.toString());
    if (FmcPlayerJson.get("error").getAsBoolean()) {
        logger.info("Player not found");
        return false;
    }
    JsonObject JsonData = FmcPlayerJson.get("data").getAsJsonObject();
    String name = JsonData.get("username").getAsString();
    String rank = JsonData.get("rank").getAsString();
    if (FmcPlayerJson.toString().contains("\"onlinestat\":[[],[]]")) {
        logger.info("FyreMC player lookup");
        logger.info("Username: {}", name);
        logger.info("Rank: {}", rank);
        return true;
    }
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String Today = now.format(formatter);
    WeekFields weekFields = WeekFields.of(Locale.getDefault());
    int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
    int getYear = now.getYear();
    JsonArray onlineStatArray = JsonData.getAsJsonArray("onlinestat");
    String wasOnlineStr;
    Iterator<JsonElement> onlineStatK = onlineStatArray.iterator();
    JsonObject onlineStatParse = (JsonObject) JsonParser.parseString(onlineStatK.next().toString());
    if (onlineStatParse.toString().contains(Today)) {
        JsonObject onlineStat = onlineStatParse.get(Today).getAsJsonObject();
        int onlineTime = onlineStat.get("online").getAsInt();
            wasOnlineStr = "True (" + onlineTime + " minute)";
    } else {
        wasOnlineStr = "False";
    }
    JsonObject WeekOnlineStatParse = (JsonObject) JsonParser.parseString(onlineStatK.next().toString());
    String WasActiveWeekStr = "";
    if (WeekOnlineStatParse.toString().contains(getYear + "-" + weekNumber)) {
        JsonObject WeekOnlineStat = WeekOnlineStatParse.get(getYear + "-" + weekNumber).getAsJsonObject();
        int WeekOnlineTime = WeekOnlineStat.get("online").getAsInt() / 60;
        if (WeekOnlineTime >= 20) {
            WasActiveWeekStr += "True (" + WeekOnlineTime + " >= 20 hour)";
        } else {
            WasActiveWeekStr += "False (" + WeekOnlineTime + " < 20 hour)";
        }
    } else {
        WasActiveWeekStr += "False (0 < 20 hour)";
    }
    int weekOfMonth = now.get(weekFields.weekOfMonth());
    String WasActiveLast30DaysStr = "";
    double Last30DaysOnlineTimeDouble = 0;
    int WasNoActiveLast30Days = 0;
    int weekcm = now.get(weekFields.weekOfWeekBasedYear())-4;
    int weekOfWeekBasedYear = now.get(weekFields.weekOfWeekBasedYear());
    while (weekcm <= weekOfWeekBasedYear) {
        if (WeekOnlineStatParse.toString().contains(getYear + "-" + weekcm)) {
            JsonObject Last30DaysOnlineStat = WeekOnlineStatParse.get(getYear + "-" + weekcm).getAsJsonObject();
            Last30DaysOnlineTimeDouble += (double) Last30DaysOnlineStat.get("online").getAsInt() / 60;
            int Last30DaysOnlineTimeInt = (int) Math.round(Last30DaysOnlineTimeDouble);
            if (Last30DaysOnlineTimeInt >= 100) {
                WasActiveLast30DaysStr = "True (" + Last30DaysOnlineTimeInt + " >= 100 hour)";
            } else {
                WasActiveLast30DaysStr = "False (" + Last30DaysOnlineTimeInt + " < 100 hour)";
            }
        } else {
            WasNoActiveLast30Days++;
            if(WasNoActiveLast30Days == (weekOfMonth-1)) {
                WasActiveLast30DaysStr = "False (" + 0 + " < 100 hour)";
            }
        }
        weekcm++;
    }
    String WasActiveThisMonthStr = "";
    double ThisMonthOnlineTimeDouble = 0;
    int WasNoActiveThisMonth = 0;
    int weekOfMonthcm = now.get(weekFields.weekOfWeekBasedYear()) - (now.get(weekFields.weekOfMonth())-1);
    while (weekOfMonthcm <= weekOfWeekBasedYear) {
        if (WeekOnlineStatParse.toString().contains(getYear + "-" + weekOfMonthcm)) {
            JsonObject ThisMonthOnlineStat = WeekOnlineStatParse.get(getYear + "-" + weekOfMonthcm).getAsJsonObject();
            ThisMonthOnlineTimeDouble += (double) ThisMonthOnlineStat.get("online").getAsInt() / 60;
            int ThisMonthOnlineTimeInt = (int) Math.round(ThisMonthOnlineTimeDouble);
            if (ThisMonthOnlineTimeInt >= 100) {
                WasActiveThisMonthStr = "True (" + ThisMonthOnlineTimeInt + " >= 100 hour)";
            } else {
                WasActiveThisMonthStr = "False (" + ThisMonthOnlineTimeInt + " < 100 hour)";
            }
        } else {
            WasNoActiveThisMonth++;
            if(WasNoActiveThisMonth == (weekOfMonth-1)) {
                WasActiveThisMonthStr = "False (" + 0 + " < 100 hour)";
            }
        }
        weekOfMonthcm++;
    }
    logger.info("FyreMC player lookup");
    logger.info("Username: {}", name);
    logger.info("Rank: {}", rank);
    logger.info("Was today online? {}", wasOnlineStr);
    logger.info("Was active in this week? {}", WasActiveWeekStr);
    if (weekOfMonth != 4) {
        logger.info("Was active in the last 30 days? {}", WasActiveLast30DaysStr);
        if (WasNoActiveLast30Days > 0) {
            logger.info("He/She was inactive for {} weeks in the last 30 days", WasNoActiveLast30Days);
        }
    }
    if (weekOfMonth != 1) {
        logger.info("Was active in this month? {}", WasActiveThisMonthStr);
        if (WasNoActiveThisMonth > 0) {
            logger.info("He/She was inactive for {} weeks in this month", WasNoActiveThisMonth);
        }
    }
    return true;
}, "player");
```
</details>

## Módosított parancsok

<details>
<summary>credentials</summary>

```java
commandTree.register(args -> {
    logger.info("Current credentials:");
    logger.info("Session Service: {}", proxy.sessionService());
    logger.info("Name: '{}'", proxy.name());
    logger.info("UUID: '{}'", proxy.uuid());
    logger.info("Token: '{}'", proxy.accessToken());
    logger.info("SelectedProfileId: '{}'", proxy.selectedProfileId());
    logger.info("ServerId: '{}'", proxy.selectedProfileId().substring(proxy.selectedProfileId().length()-2));
    logger.info("Target address: '{}'", proxy.address());
    logger.info("Join IP: localhost:{}", proxy.port());
    return true;
}, "credentials");
```
</details>

<details>
<summary>setip [ip]</summary>

```java
commandTree.register(args -> {
    if (args.length > 1) {
        logger.info("Usage: setip [ip]");
        return false;
    } else if (args.length < 1) {
        logger.info("Target address: {}", proxy.address());
        logger.info("Join IP: localhost:{}", proxy.port());
        return true;
    }
    ServerAddress prev = proxy.address();
    proxy.address(args[0]);
    logger.info("Changed address: '{}' -> '{}'", prev, proxy.address());
    logger.info("Join IP: localhost:{} (Copied) ", proxy.port());
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(new StringSelection("localhost:"+proxy.port()), null);
    return true;
}, "setip", "ip");
```
</details>

Eredeti: (https://github.com/marvintheskid/mc-reverse-proxy)
