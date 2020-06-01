package dev.minemalox.advancedskyblock.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.events.SkyblockJoinedEvent;
import dev.minemalox.advancedskyblock.utils.events.SkyblockLeftEvent;
import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import dev.minemalox.advancedskyblock.utils.nifty.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    /**
     * Used for web requests.
     */
    public static final String USER_AGENT = "AdvancedSkyblockGui/" + AdvancedSkyblock.VERSION;
    /**
     * Added to the beginning of messages.
     */
    private static final String MESSAGE_PREFIX =
            ChatFormatting.GRAY + "[" + ChatFormatting.AQUA + AdvancedSkyblock.MOD_NAME + ChatFormatting.GRAY + "] ";
    /**
     * Enchantments listed by how good they are. May or may not be subjective lol.
     */
    private static final List<String> ORDERED_ENCHANTMENTS = Collections.unmodifiableList(Arrays.asList(
            "smite", "bane of arthropods", "knockback", "fire aspect", "venomous", // Sword Bad
            "thorns", "growth", "protection", "depth strider", "respiration", "aqua affinity", // Armor
            "lure", "caster", "luck of the sea", "blessing", "angler", "frail", "magnet", "spiked hook", // Fishing
            "dragon hunter", "power", "snipe", "piercing", "aiming", "infinite quiver", // Bow Main
            "sharpness", "critical", "first strike", "giant killer", "execute", "lethality", "ender slayer", "cubism", "impaling", // Sword Damage
            "vampirism", "life steal", "looting", "luck", "scavenger", "experience", "cleave", "thunderlord", // Sword Others
            "punch", "flame", // Bow Others
            "telekinesis"
    ));
    private static final Pattern SERVER_REGEX = Pattern.compile("([0-9]{2}/[0-9]{2}/[0-9]{2}) (mini[0-9]{1,3}[A-Za-z])");
    /**
     * In English, Chinese Simplified.
     */
    private static final Set<String> SKYBLOCK_IN_ALL_LANGUAGES = Sets.newHashSet("SKYBLOCK", "\u7A7A\u5C9B\u751F\u5B58");
    // I know this is messy af, but frustration led me to take this dark path - said someone not biscuit
    public static boolean blockNextClick = false;
    /**
     * This is the item checker that makes sure items being dropped or sold are allowed to be dropped or sold.
     */
    private final ItemDropChecker itemDropChecker;
    /**
     * Get a player's attributes. This includes health, mana, and defence.
     */
    private Map<Attribute, MutableInt> attributes = new EnumMap<>(Attribute.class);
    /**
     * List of enchantments that the player is looking to find.
     */
    private List<String> enchantmentMatches = new LinkedList<>();

    /**
     * List of enchantment substrings that the player doesn't want to match.
     */
    private List<String> enchantmentExclusions = new LinkedList<>();

    private Backpack backpackToRender = null;

    /**
     * Whether the player is on skyblock.
     */
    private boolean onSkyblock = false;

    /**
     * The player's current location in Skyblock
     */
    private Location location = Location.UNKNOWN;

    /**
     * The skyblock profile that the player is currently on. Ex. "Grapefruit"
     */
    private String profileName = null;

    /**
     * Whether or not a loud sound is being played by the mod.
     */
    private boolean playingSound = false;

    /**
     * The current serverID that the player is on.
     */
    private String serverID = "";
    private int lastHoveredSlot = -1;

    /**
     * Whether the player is using the old style of bars packaged into Imperial's Skyblock Pack.
     */
    private boolean usingOldSkyBlockTexture = false;

    /**
     * Whether the player is using the default bars packaged into the mod.
     */
    private boolean usingDefaultBarTextures = true;

    private SkyblockDate currentDate = new SkyblockDate(SkyblockDate.SkyblockMonth.EARLY_WINTER, 1, 1, 1, "am");
    private double purse = 0;
    private int jerryWave = -1;

    private boolean fadingIn;

    // Featured link
    private boolean lookedOnline = false;
    private URI featuredLink = null;

    private long lastDamaged = -1;

    private AdvancedSkyblock main;
    private Logger logger;

    public Utils(AdvancedSkyblock main) {
        this.main = main;
        logger = AdvancedSkyblock.getInstance().getLogger();
        addDefaultStats();
        itemDropChecker = new ItemDropChecker(main);
    }

    private void addDefaultStats() {
        for (Attribute attribute : Attribute.values()) {
            attributes.put(attribute, new MutableInt(attribute.getDefaultValue()));
        }
    }

    public void sendMessage(String text, boolean prefix) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent(ChatType.SYSTEM, new TextComponentString((prefix ? MESSAGE_PREFIX : "") + text));
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().player.sendMessage(event.getMessage()); // addChatMessage(event.message); // Just for logs
        }
    }

    public void sendMessage(String text) {
        sendMessage(text, true);
    }

    private void sendMessage(TextComponentString text) {
        sendMessage(text.getFormattedText());
    }

    private void sendMessage(TextComponentString text, boolean prefix) {
        sendMessage(text.getFormattedText(), prefix);
    }

    public void sendErrorMessage(String errorText) {
        sendMessage(ChatFormatting.RED + "Error: " + errorText);
    }

    /**
     * Checks if the player is on the Hypixel Network
     *
     * @return {@code true} if the player is on Hypixel, {@code false} otherwise
     */
    public boolean isOnHypixel() {
        final Pattern SERVER_BRAND_PATTERN = Pattern.compile("(.+) <- (?:.+)");
        final String HYPIXEL_SERVER_BRAND = "BungeeCord (Hypixel)";

        Minecraft mc = Minecraft.getMinecraft();

        if (!mc.isSingleplayer()) {
            Matcher matcher = SERVER_BRAND_PATTERN.matcher(mc.player.getServerBrand()/*TODO: getClientBrand()*/);

            if (matcher.find()) {
                // Group 1 is the server brand.
                return matcher.group(1).equals(HYPIXEL_SERVER_BRAND);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void checkGameLocationDate() {
        boolean foundLocation = false;
        boolean foundJerryWave = false;
        Minecraft mc = Minecraft.getMinecraft();

        if (mc != null && mc.world != null && !mc.isSingleplayer() && isOnHypixel()) {
            Scoreboard scoreboard = mc.world.getScoreboard();
            ScoreObjective sidebarObjective = mc.world.getScoreboard().getObjectiveInDisplaySlot(1);

            if (sidebarObjective != null) {
                String objectiveName = TextUtils.stripColor(sidebarObjective.getDisplayName());
                boolean isSkyblockScoreboard = false;

                for (String skyblock : SKYBLOCK_IN_ALL_LANGUAGES) {
                    if (objectiveName.startsWith(skyblock)) {
                        isSkyblockScoreboard = true;
                        break;
                    }
                }

                // Copied from SkyblockLib, should be removed when we switch to use that
                if (isSkyblockScoreboard) {
                    // If it's a Skyblock scoreboard and the player has not joined Skyblock yet,
                    // this indicates that he did so.
                    if (!this.isOnSkyblock()) {
                        MinecraftForge.EVENT_BUS.post(new SkyblockJoinedEvent());
                    }
                } else {
                    // If it's not a Skyblock scoreboard, the player must have left Skyblock and
                    // be in some other Hypixel lobby or game.
                    if (this.isOnSkyblock()) {
                        MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
                    }
                }

                Collection<Score> scoreboardLines = scoreboard.getSortedScores(sidebarObjective);
                List<Score> list = scoreboardLines.stream().filter(p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")).collect(Collectors.toList());
                if (list.size() > 15) {
                    scoreboardLines = Lists.newArrayList(Iterables.skip(list, scoreboardLines.size() - 15));
                } else {
                    scoreboardLines = list;
                }
                String timeString = null;
                String dateString = null;
                for (Score line : scoreboardLines) {
                    ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(line.getPlayerName());
                    String strippedLine = TextUtils.stripColor(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, line.getPlayerName()));
                    String locationString = TextUtils.keepLettersAndNumbersOnly(strippedLine);

                    if (locationString.endsWith("am") || locationString.endsWith("pm")) {
                        timeString = locationString;
                    }
                    if (locationString.endsWith("st")
                            || locationString.endsWith("nd")
                            || locationString.endsWith("rd")
                            || locationString.endsWith("th")) {
                        dateString = locationString;
                    }

                    if (strippedLine.startsWith("Purse") || strippedLine.startsWith("Piggy")) {
                        try {
                            purse = Double.parseDouble(TextUtils.keepFloatCharactersOnly(strippedLine));
                        } catch (NumberFormatException ignored) {
                            purse = 0;
                        }
                    }
                    if (locationString.contains("mini")) {
                        Matcher matcher = SERVER_REGEX.matcher(locationString);
                        if (matcher.matches()) {
                            serverID = matcher.group(2);
                            continue; // skip to next line
                        }
                    }
                    for (Location loopLocation : Location.values()) {
                        if (locationString.endsWith(loopLocation.getScoreboardName())) {
                            if (loopLocation == Location.BLAZING_FORTRESS &&
                                    location != Location.BLAZING_FORTRESS) {
                                sendPostRequest(EnumUtils.MagmaEvent.PING); // going into blazing fortress
                                fetchEstimateFromServer();
                            }

                            if (location != loopLocation) {
                                location = loopLocation;
                            }

                            foundLocation = true;
                            break;
                        }
                    }
                    if (location == Location.JERRYS_WORKSHOP || location == Location.JERRY_POND) {
                        if (strippedLine.startsWith("Wave")) {
                            foundJerryWave = true;

                            int newJerryWave;
                            try {
                                newJerryWave = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(strippedLine));
                            } catch (NumberFormatException ignored) {
                                newJerryWave = 0;
                            }
                            if (jerryWave != newJerryWave) {
                                jerryWave = newJerryWave;
                            }
                        }
                    }
                }
                currentDate = SkyblockDate.parse(dateString, timeString);
            }
        }
        if (!foundLocation) {
            location = Location.UNKNOWN;
        }
        if (!foundJerryWave) {
            jerryWave = -1;
        }
    }

    @Deprecated
    public void checkUpdates() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/MineMalox/AdvancedSkyblock/1.12/build.gradle");
                URLConnection connection = url.openConnection();
                connection.setReadTimeout(5000);
                connection.addRequestProperty("User-Agent", "AdvancedSkyblockGui update checker");
                connection.setDoOutput(true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String currentLine;
                String newestVersion = "";
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.contains("version = \"")) {
                        String[] newestVersionSplit = currentLine.split(Pattern.quote("version = \""));
                        newestVersionSplit = newestVersionSplit[1].split(Pattern.quote("\""));
                        newestVersion = newestVersionSplit[0];
                        break;
                    }
                }
                main.getRenderListener().getDownloadInfo().setNewestVersion(newestVersion);
                reader.close();
                List<Integer> newestVersionNumbers = new ArrayList<>();
                List<Integer> thisVersionNumbers = new ArrayList<>();
                try {
                    for (String s : newestVersion.split(Pattern.quote("."))) {
                        if (s.contains("-b")) {
                            String[] splitBuild = s.split(Pattern.quote("-b"));
                            newestVersionNumbers.add(Integer.parseInt(splitBuild[0]));
                            newestVersionNumbers.add(Integer.parseInt(splitBuild[1]));
                        } else {
                            newestVersionNumbers.add(Integer.parseInt(s));
                        }
                    }
                    for (String s : AdvancedSkyblock.VERSION.split(Pattern.quote("."))) {
                        if (s.contains("-b")) {
                            String[] splitBuild = s.split(Pattern.quote("-b"));
                            thisVersionNumbers.add(Integer.parseInt(splitBuild[0]));
                            thisVersionNumbers.add(Integer.parseInt(splitBuild[1]));
                        } else {
                            thisVersionNumbers.add(Integer.parseInt(s));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                for (int i = 0; i < 4; i++) {
                    if (i >= newestVersionNumbers.size()) {
                        newestVersionNumbers.add(i, 0);
                    }
                    if (i >= thisVersionNumbers.size()) {
                        thisVersionNumbers.add(i, 0);
                    }
                }

                boolean outOfBeta = newestVersionNumbers.get(1).equals(thisVersionNumbers.get(1)) &&
                        newestVersionNumbers.get(2).equals(thisVersionNumbers.get(2)) && // Update message when either: the version numbers are the same, but its longer a build.
                        newestVersionNumbers.get(3).equals(thisVersionNumbers.get(3)) && AdvancedSkyblock.VERSION.contains("b") && !newestVersion.contains("b");

                for (int i = 0; i < 4; i++) {
                    if (newestVersionNumbers.get(i) > thisVersionNumbers.get(i) || // OR: one of the version numbers is higher.
                            outOfBeta) {
                        String link = "https://hypixel.net/";
                        try {
                            url = new URL("https://raw.githubusercontent.com/MineMalox/AdvancedSkyblock/1.12/updatelink.txt");
                            connection = url.openConnection();
                            connection.setReadTimeout(5000);
                            connection.addRequestProperty("User-Agent", "AdvancedSkyblockGui");
                            connection.setDoOutput(true);
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            while ((currentLine = reader.readLine()) != null) {
                                link = currentLine;
                            }
                            reader.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            main.getRenderListener().getDownloadInfo().setDownloadLink(link);
                            if (i == 2 || i == 3 || outOfBeta) { // 0.0.x or 0.0.0-bx
                                main.getRenderListener().getDownloadInfo().setPatch(true);
                                main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.PATCH_AVAILABLE);
                                sendUpdateMessage(true, true);
                            } else {
                                main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.MAJOR_AVAILABLE);
                                sendUpdateMessage(true, false);
                            }
                        }
                        break;
                    } else if (thisVersionNumbers.get(i) > newestVersionNumbers.get(i)) {
                        main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.DEVELOPMENT);
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    @Deprecated
    void sendUpdateMessage(boolean showDownload, boolean showAutoDownload) {
        String newestVersion = main.getRenderListener().getDownloadInfo().getNewestVersion();
        sendMessage(TextUtils.color("&7&m------------&7[&b&l AdvancedSkyblockGui &7]&7&m------------"), false);
        if (main.getRenderListener().getDownloadInfo().getMessageType() == EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED) {
            TextComponentString deleteOldFile = new TextComponentString(ChatFormatting.RED + Message.MESSAGE_DELETE_OLD_FILE.getMessage() + "\n");
            sendMessage(deleteOldFile, false);
        } else {
            TextComponentString newUpdate = new TextComponentString(ChatFormatting.AQUA + Message.UPDATE_MESSAGE_NEW_UPDATE.getMessage(newestVersion) + "\n");
            sendMessage(newUpdate, false);
        }

        TextComponentString buttonsMessage = new TextComponentString("");
        if (showDownload) {
            buttonsMessage = new TextComponentString(ChatFormatting.AQUA.toString() + ChatFormatting.BOLD + '[' + Message.MESSAGE_DOWNLOAD_LINK.getMessage(newestVersion) + ']');
            buttonsMessage.setStyle(buttonsMessage.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, main.getRenderListener().getDownloadInfo().getDownloadLink())));
            buttonsMessage.appendSibling(new TextComponentString(" "));
        }

        if (showAutoDownload) {
            TextComponentString downloadAutomatically = new TextComponentString(ChatFormatting.GREEN.toString() + ChatFormatting.BOLD + '[' + Message.MESSAGE_DOWNLOAD_AUTOMATICALLY.getMessage(newestVersion) + ']');
            downloadAutomatically.setStyle(downloadAutomatically.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba update")));
            buttonsMessage.appendSibling(downloadAutomatically);
            buttonsMessage.appendSibling(new TextComponentString(" "));
        }

        TextComponentString openModsFolder = new TextComponentString(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + '[' + Message.MESSAGE_OPEN_MODS_FOLDER.getMessage(newestVersion) + ']');
        openModsFolder.setStyle(openModsFolder.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba folder")));
        buttonsMessage.appendSibling(openModsFolder);

        sendMessage(buttonsMessage, false);
        if (main.getRenderListener().getDownloadInfo().getMessageType() != EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED) {
            TextComponentString discord = new TextComponentString(ChatFormatting.AQUA + Message.MESSAGE_VIEW_PATCH_NOTES.getMessage() + " " +
                    ChatFormatting.BLUE.toString() + ChatFormatting.BOLD + '[' + Message.MESSAGE_JOIN_DISCORD.getMessage() + ']');
            discord.setStyle(discord.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/PqTAEek")));
            sendMessage(discord);
        }
        sendMessage(TextUtils.color("&7&m----------------------------------------------"), false);
    }

    public void checkDisabledFeatures() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/MineMalox/AdvancedSkyblock/1.12/disabledFeatures.txt");
                URLConnection connection = url.openConnection();
                connection.setReadTimeout(5000);
                connection.addRequestProperty("User-Agent", "AdvancedSkyblockGui");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String currentLine;
                Set<Feature> disabledFeatures = main.getConfigValues().getRemoteDisabledFeatures();
                while ((currentLine = reader.readLine()) != null) {
                    String[] splitLine = currentLine.split(Pattern.quote("|"));
                    if (!currentLine.startsWith("all|")) {
                        if (!AdvancedSkyblock.VERSION.equals(splitLine[0])) {
                            continue;
                        }
                    }
                    if (splitLine.length > 1) {
                        for (int i = 1; i < splitLine.length; i++) {
                            String part = splitLine[i];
                            Feature feature = Feature.fromId(Integer.parseInt(part));
                            if (feature != null) {
                                disabledFeatures.add(feature);
                            }
                        }
                    }
                }
                reader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public int getDefaultColor(float alphaFloat) {
        int alpha = (int) alphaFloat;
        return new Color(150, 236, 255, alpha).getRGB();
    }

    /**
     * When you use this function, any sound played will bypass the player's
     * volume setting, so make sure to only use this for like warnings or stuff like that.
     */
    public void playLoudSound(SoundEvent sound, double pitch) {
        playingSound = true;
        Minecraft.getMinecraft().player.playSound(sound, 1, (float) pitch);
        playingSound = false;
    }

    /**
     * This one plays the sound normally. See {@link Utils#playLoudSound(SoundEvent, double)} for playing
     * a sound that bypasses the user's volume settings.
     */
    public void playSound(SoundEvent sound, double pitch) {
        Minecraft.getMinecraft().player.playSound(sound, 1, (float) pitch);
    }

    public void playSound(SoundEvent sound, double volume, double pitch) {
        Minecraft.getMinecraft().player.playSound(sound, (float) volume, (float) pitch);
    }

    public boolean enchantReforgeMatches(String text) {
        text = text.toLowerCase();
        for (String enchant : enchantmentMatches) {
            enchant = enchant.trim().toLowerCase();
            if (StringUtil.notEmpty(enchant) && text.contains(enchant)) {
                boolean foundExclusion = false;
                for (String exclusion : enchantmentExclusions) {
                    exclusion = exclusion.trim().toLowerCase();
                    if (StringUtil.notEmpty(exclusion) && text.contains(exclusion)) {
                        foundExclusion = true;
                        break;
                    }
                }
                if (!foundExclusion) {
                    return true;
                }
            }
        }
        return false;
    }

    public void fetchEstimateFromServer() {
        new Thread(() -> {
            final boolean magmaTimerEnabled = main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER);
            if (!magmaTimerEnabled) {
                logger.info("Getting magma boss spawn estimate from server...");
            }
            try {
                URL url = new URL("https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/estimatedSpawn");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                if (!magmaTimerEnabled) {
                    logger.info("Got response code " + connection.getResponseCode());
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                connection.disconnect();
                JsonObject responseJson = new Gson().fromJson(response.toString(), JsonObject.class);
                long estimate = responseJson.get("estimate").getAsLong();
                long currentTime = responseJson.get("queryTime").getAsLong();
                int magmaSpawnTime = (int) ((estimate - currentTime) / 1000);

                if (!magmaTimerEnabled) {
                    logger.info("Query time was " + currentTime + ", server time estimate is " +
                            estimate + ". Updating magma boss spawn to be in " + magmaSpawnTime + " seconds.");
                }

                main.getPlayerListener().setMagmaTime(magmaSpawnTime);
                main.getPlayerListener().setMagmaAccuracy(EnumUtils.MagmaTimerAccuracy.ABOUT);
            } catch (IOException ex) {
                if (!magmaTimerEnabled) {
                    logger.warn("Failed to get magma boss spawn estimate from server");
                }
            }
        }).start();
    }

    public void sendPostRequest(EnumUtils.MagmaEvent event) {
        new Thread(() -> {
            final boolean magmaTimerEnabled = main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER);
            if (!magmaTimerEnabled) {
                logger.info("Posting event " + event.getInventiveTalentEvent() + " to InventiveTalent API");
            }

            try {
                String urlString = "https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/addEvent";
                if (event == EnumUtils.MagmaEvent.PING) {
                    urlString = "https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/ping";
                }
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null && mc.player != null) {
                    String postString;
                    if (event == EnumUtils.MagmaEvent.PING) {
                        postString = "isModRequest=true&minecraftUser=" + mc.player.getName() + "&lastFocused=" + System.currentTimeMillis() / 1000 + "&serverId=" + serverID;
                    } else {
                        postString = "type=" + event.getInventiveTalentEvent() + "&isModRequest=true&minecraftUser=" + mc.player.getName() + "&serverId=" + serverID;
                    }
                    connection.setDoOutput(true);
                    try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                        out.writeBytes(postString);
                        out.flush();
                    }

                    if (!magmaTimerEnabled) {
                        logger.info("Got response code " + connection.getResponseCode());
                    }
                    connection.disconnect();
                }
            } catch (IOException ex) {
                if (!magmaTimerEnabled) {
                    logger.warn("Failed to post event to server");
                }
            }
        }).start();
    }

    public boolean isMaterialForRecipe(ItemStack item) {
        final List<String> tooltip = item.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL);
        for (String s : tooltip) {
            if ("§5§o§eRight-click to view recipes!".equals(s)) {
                return true;
            }
        }
        return false;
    }

    public String getReforgeFromItem(ItemStack item) {
        if (item.hasTagCompound()) {
            NBTTagCompound extraAttributes = item.getTagCompound();
            if (extraAttributes.hasKey("ExtraAttributes")) {
                extraAttributes = extraAttributes.getCompoundTag("ExtraAttributes");
                if (extraAttributes.hasKey("modifier")) {
                    String reforge = WordUtils.capitalizeFully(extraAttributes.getString("modifier"));

                    reforge = reforge.replace("_sword", ""); //fixes reforges like "Odd_sword"
                    reforge = reforge.replace("_bow", "");

                    return reforge;
                }
            }
        }
        return null;
    }

    // TODO: Replace this in new update checker implementation
    @Deprecated
    public void downloadPatch(String version) {
        File sbaFolder = getSBAFolder();
        if (sbaFolder != null) {
            main.getUtils().sendMessage(ChatFormatting.YELLOW + Message.MESSAGE_DOWNLOADING_UPDATE.getMessage());
            new Thread(() -> {
                try {
                    String fileName = "AdvancedSkyblockGui-" + version + "-for-MC-1.12.2.jar";
                    URL url = new URL("https://github.com/MineMalox/AdvancedSkyblock/releases/download/v" + version + "/" + fileName);
                    File outputFile = new File(sbaFolder.toString() + File.separator + fileName);
                    URLConnection connection = url.openConnection();
                    long totalFileSize = connection.getContentLengthLong();
                    main.getRenderListener().getDownloadInfo().setTotalBytes(totalFileSize);
                    main.getRenderListener().getDownloadInfo().setOutputFileName(fileName);
                    main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.DOWNLOADING);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        main.getRenderListener().getDownloadInfo().setDownloadedBytes(main.getRenderListener().getDownloadInfo().getDownloadedBytes() + bytesRead);
                    }
                    main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED);
                } catch (IOException e) {
                    main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.FAILED);
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Returns the folder that AdvancedSkyblockGui is located in.
     *
     * @return the folder the AdvancedSkyblockGui jar is located in
     */
    public File getSBAFolder() {
        return Loader.instance().activeModContainer().getSource().getParentFile();
    }

    public int getNBTInteger(ItemStack item, String... path) {
        if (item != null && item.hasTagCompound()) {
            NBTTagCompound tag = item.getTagCompound();
            for (String tagName : path) {
                if (path[path.length - 1].equals(tagName)) continue;
                if (tag.hasKey(tagName)) {
                    tag = tag.getCompoundTag(tagName);
                } else {
                    return -1;
                }
            }
            return tag.getInteger(path[path.length - 1]);
        }
        return -1;
    }

    /**
     * Checks if it is currently Halloween according to the system calendar.
     *
     * @return {@code true} if it is Halloween, {@code false} otherwise
     */
    public boolean isHalloween() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == Calendar.OCTOBER && calendar.get(Calendar.DAY_OF_MONTH) == 31;
    }

    /**
     * Checks if the given item is a pickaxe.
     *
     * @param item the item to check
     * @return {@code true} if this item is a pickaxe, {@code false} otherwise
     */
    public boolean isPickaxe(Item item) {
        return Items.WOODEN_PICKAXE.equals(item) || Items.STONE_PICKAXE.equals(item) || Items.GOLDEN_PICKAXE.equals(item) || Items.IRON_PICKAXE.equals(item) || Items.DIAMOND_PICKAXE.equals(item);
    }

    /**
     * This retrieves the featured link for the banner in the top left of the GUI.
     *
     * @return the featured link or {@code null} if the link could not be read
     */
    public URI getFeaturedURL() {
        String featuredLinkFilePath = "featuredlink.txt";

        if (featuredLink != null) return featuredLink;

        InputStream featuredLinkStream;
        BufferedReader reader;
        featuredLinkStream = getClass().getClassLoader().getResourceAsStream(featuredLinkFilePath);

        if (featuredLinkStream != null) {
            reader = new BufferedReader(new InputStreamReader(featuredLinkStream));

            try {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    featuredLink = new URI(currentLine);
                }
                reader.close();
            } catch (IOException | URISyntaxException e) {
                logger.error("Failed to read featured link!");
                logger.catching(e);
            }
        } else {
            logger.warn("Resource not found: " + featuredLinkFilePath);
        }

        return logger.exit(featuredLink);
    }

    public void getFeaturedURLOnline() {
        if (!lookedOnline) {
            lookedOnline = true;
            new Thread(() -> {
                try {
                    URL url = new URL("https://raw.githubusercontent.com/MineMalox/AdvancedSkyblock/1.12/src/main/resources/featuredlink.txt");
                    URLConnection connection = url.openConnection(); // try looking online
                    connection.setReadTimeout(5000);
                    connection.addRequestProperty("User-Agent", "AdvancedSkyblockGui");
                    connection.setDoOutput(true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String currentLine;
                    while ((currentLine = reader.readLine()) != null) {
                        featuredLink = new URI(currentLine);
                    }
                    reader.close();
                } catch (IOException | URISyntaxException ignored) {
                }
            }).start();
        }
    }

    public void drawTextWithStyle(String text, int x, int y, ChatFormatting color) {
        drawTextWithStyle(text, x, y, color.getRGB(), 1);
    }

    public void drawTextWithStyle(String text, int x, int y, int color) {
        drawTextWithStyle(text, x, y, color, 1);
    }

    public void drawTextWithStyle(String text, int x, int y, int color, float textAlpha) {
        if (main.getConfigValues().getTextStyle() == EnumUtils.TextStyle.STYLE_TWO) {
            int colorBlack = new Color(0, 0, 0, textAlpha > 0.016 ? textAlpha : 0.016F).getRGB();
            String strippedText = TextUtils.stripColor(text);
            Minecraft.getMinecraft().fontRenderer.drawString(strippedText, x + 1, y, colorBlack);
            Minecraft.getMinecraft().fontRenderer.drawString(strippedText, x - 1, y, colorBlack);
            Minecraft.getMinecraft().fontRenderer.drawString(strippedText, x, y + 1, colorBlack);
            Minecraft.getMinecraft().fontRenderer.drawString(strippedText, x, y - 1, colorBlack);
            Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color);
        } else {
            Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color, true);
        }
    }

    public int getDefaultBlue(int alpha) {
        return new Color(160, 225, 229, alpha).getRGB();
    }

    public void reorderEnchantmentList(List<String> enchantments) {
        SortedMap<Integer, String> orderedEnchants = new TreeMap<>();
        for (int i = 0; i < enchantments.size(); i++) {
            int nameEnd = enchantments.get(i).lastIndexOf(' ');
            if (nameEnd < 0) nameEnd = enchantments.get(i).length();

            int key = ORDERED_ENCHANTMENTS.indexOf(enchantments.get(i).substring(0, nameEnd).toLowerCase());
            if (key < 0) key = 100 + i;
            orderedEnchants.put(key, enchantments.get(i));
        }

        enchantments.clear();
        enchantments.addAll(orderedEnchants.values());
    }

    public float denormalizeScale(float value, float min, float max, float step) {
        return snapToStepClamp(min + (max - min) *
                MathHelper.clamp(value, 0.0F, 1.0F), min, max, step);
    }

    private float snapToStepClamp(float value, float min, float max, float step) {
        value = step * (float) Math.round(value / step);
        return MathHelper.clamp(value, min, max);
    }

    public void bindRGBColor(int color) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >> 24 & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);
    }

    public void bindColorInts(int r, int g, int b, int a) {
        GlStateManager.color(r / 255F, g / 255F, b / 255F, a / 255F);
    }

    public String[] wrapSplitText(String text, int wrapLength) {
        return WordUtils.wrap(text, wrapLength).replace("\r", "").split(Pattern.quote("\n"));
    }

    public boolean itemIsInHotbar(ItemStack itemStack) {
        ItemStack[] inventory = Minecraft.getMinecraft().player.inventory.mainInventory.toArray(new ItemStack[0]);

        for (int slot = 0; slot < 9; slot++) {
            if (inventory[slot] == itemStack) {
                return true;
            }
        }
        return false;
    }

    public int getColorWithAlpha(int color, int alpha) {
        return color + ((alpha << 24) & 0xFF000000);
    }

    public ItemDropChecker getItemDropChecker() {
        return this.itemDropChecker;
    }

    public Map<Attribute, MutableInt> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<Attribute, MutableInt> attributes) {
        this.attributes = attributes;
    }

    public List<String> getEnchantmentMatches() {
        return this.enchantmentMatches;
    }

    public void setEnchantmentMatches(List<String> enchantmentMatches) {
        this.enchantmentMatches = enchantmentMatches;
    }

    public List<String> getEnchantmentExclusions() {
        return this.enchantmentExclusions;
    }

    public void setEnchantmentExclusions(List<String> enchantmentExclusions) {
        this.enchantmentExclusions = enchantmentExclusions;
    }

    public Backpack getBackpackToRender() {
        return this.backpackToRender;
    }

    public void setBackpackToRender(Backpack backpackToRender) {
        this.backpackToRender = backpackToRender;
    }

    public boolean isOnSkyblock() {
        return this.onSkyblock;
    }

    public void setOnSkyblock(boolean onSkyblock) {
        this.onSkyblock = onSkyblock;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getProfileName() {
        return this.profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public boolean isPlayingSound() {
        return this.playingSound;
    }

    public void setPlayingSound(boolean playingSound) {
        this.playingSound = playingSound;
    }

    public String getServerID() {
        return this.serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public int getLastHoveredSlot() {
        return this.lastHoveredSlot;
    }

    public void setLastHoveredSlot(int lastHoveredSlot) {
        this.lastHoveredSlot = lastHoveredSlot;
    }

    public boolean isUsingOldSkyBlockTexture() {
        return this.usingOldSkyBlockTexture;
    }

    public void setUsingOldSkyBlockTexture(boolean usingOldSkyBlockTexture) {
        this.usingOldSkyBlockTexture = usingOldSkyBlockTexture;
    }

    public boolean isUsingDefaultBarTextures() {
        return this.usingDefaultBarTextures;
    }

    public void setUsingDefaultBarTextures(boolean usingDefaultBarTextures) {
        this.usingDefaultBarTextures = usingDefaultBarTextures;
    }

    public SkyblockDate getCurrentDate() {
        return this.currentDate;
    }

    public void setCurrentDate(SkyblockDate currentDate) {
        this.currentDate = currentDate;
    }

    public double getPurse() {
        return this.purse;
    }

    public void setPurse(double purse) {
        this.purse = purse;
    }

    public int getJerryWave() {
        return this.jerryWave;
    }

    public void setJerryWave(int jerryWave) {
        this.jerryWave = jerryWave;
    }

    public boolean isFadingIn() {
        return this.fadingIn;
    }

    public void setFadingIn(boolean fadingIn) {
        this.fadingIn = fadingIn;
    }

    public boolean isLookedOnline() {
        return this.lookedOnline;
    }

    public void setLookedOnline(boolean lookedOnline) {
        this.lookedOnline = lookedOnline;
    }

    public URI getFeaturedLink() {
        return this.featuredLink;
    }

    public void setFeaturedLink(URI featuredLink) {
        this.featuredLink = featuredLink;
    }

    public long getLastDamaged() {
        return this.lastDamaged;
    }

    public void setLastDamaged(long lastDamaged) {
        this.lastDamaged = lastDamaged;
    }

    public AdvancedSkyblock getMain() {
        return this.main;
    }

    public void setMain(AdvancedSkyblock main) {
        this.main = main;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
