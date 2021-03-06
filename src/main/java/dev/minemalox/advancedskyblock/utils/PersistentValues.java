package dev.minemalox.advancedskyblock.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PersistentValues {

    private final File persistentValuesFile;
    private final Logger logger;
    private int kills;
    private int totalKills;
    private int summoningEyeCount;
    private boolean blockCraftingIncompletePatterns = true;
    private CraftingPattern selectedCraftingPattern = CraftingPattern.FREE;

    public PersistentValues(File configDir) {
        this.persistentValuesFile = new File(configDir.getAbsolutePath() + "/advancedskyblock_persistent.cfg");
        logger = AdvancedSkyblock.getInstance().getLogger();
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getSummoningEyeCount() {
        return summoningEyeCount;
    }

    public boolean isBlockCraftingIncompletePatterns() {
        return blockCraftingIncompletePatterns;
    }

    public void setBlockCraftingIncompletePatterns(boolean blockCraftingIncompletePatterns) {
        this.blockCraftingIncompletePatterns = blockCraftingIncompletePatterns;
        this.saveValues();
    }

    public CraftingPattern getSelectedCraftingPattern() {
        return selectedCraftingPattern;
    }

    public void setSelectedCraftingPattern(CraftingPattern selectedCraftingPattern) {
        this.selectedCraftingPattern = selectedCraftingPattern;
        this.saveValues();
    }

    public int getKills() {
        return kills;
    }

    public void loadValues() {
        if (this.persistentValuesFile.exists()) {
            try (FileReader reader = new FileReader(this.persistentValuesFile)) {
                JsonElement fileElement = new JsonParser().parse(reader);

                if (fileElement == null || fileElement.isJsonNull()) {
                    throw new JsonParseException("File is null!");
                }

                JsonObject valuesObject = fileElement.getAsJsonObject();

                this.kills = valuesObject.has("kills") ? valuesObject.get("kills").getAsInt() : 0;

                this.totalKills = valuesObject.has("totalKills") ? valuesObject.get("totalKills").getAsInt() : 0;

                this.summoningEyeCount = valuesObject.has("summoningEyeCount") ? valuesObject.get("summoningEyeCount").getAsInt() : 0;

            } catch (Exception ex) {
                logger.error("AdvancedSkyblockGui: There was an error while trying to load persistent values.");
                logger.error(ex.getMessage());
                this.saveValues();
            }

        } else {
            this.saveValues();
        }
    }

    private void saveValues() {
        JsonObject valuesObject = new JsonObject();

        try (FileWriter writer = new FileWriter(this.persistentValuesFile)) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            valuesObject.addProperty("kills", this.kills);
            valuesObject.addProperty("totalKills", this.totalKills);
            valuesObject.addProperty("summoningEyeCount", this.summoningEyeCount);

            bufferedWriter.write(valuesObject.toString());
            bufferedWriter.close();
        } catch (Exception ex) {
            logger.error("AdvancedSkyblockGui: An error occurred while attempting to save persistent values!");
            logger.error(ex.getMessage());
        }
    }

    public void addKill() {
        this.kills++;
        this.saveValues();
    }

    public void addEyeResetKills() {
        this.summoningEyeCount++;
        this.totalKills += this.kills;
        this.kills = -1; // This is triggered before the death of the killed zealot, so the kills are set to -1 to account for that.
        this.saveValues();
    }
}
