package dev.minemalox.advancedskyblock.utils;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.apache.logging.log4j.Logger;

/**
 * This class is the AdvancedSkyblockGui updater. It reads the Forge Update Checker results for AdvancedSkyblockGui.
 */
public class Updater {

    private AdvancedSkyblock main;
    private Logger logger;

    private ComparableVersion current;
    private ComparableVersion latest;
    private String message;
    private boolean hasUpdate;

    public Updater(AdvancedSkyblock main) {
        this.main = main;
        logger = main.getLogger();
        current = new ComparableVersion(AdvancedSkyblock.VERSION);
        hasUpdate = false;
    }

    /**
     * Returns whether there is an update available
     *
     * @return {@code true} if there is an update available, false otherwise.
     */
    public boolean hasUpdate() {
        return hasUpdate;
    }

    /**
     * Processes the update checker result from the Forge Update Checker and sets the correct message to be displayed.
     */
    public void processUpdateCheckResult() {
        ForgeVersion.CheckResult result = ForgeVersion.getResult(Loader.instance().activeModContainer());
        ForgeVersion.Status status = result.status;
        ComparableVersion target = result.target;

        if (status == ForgeVersion.Status.OUTDATED || status == ForgeVersion.Status.BETA_OUTDATED) {
            hasUpdate = true;
            latest = target;

            if (status == ForgeVersion.Status.OUTDATED) {
                message = Message.UPDATE_MESSAGE_NEW_UPDATE.getMessage().replace("%version%", latest.toString());
            } else {
                message = Message.UPDATE_MESSAGE_NEW_BETA.getMessage().replace("%version%", latest.toString());
            }
        } else if (status == ForgeVersion.Status.PENDING) {
            // The update checker hasn't finished yet. Check back later.
            main.getScheduler().schedule(Scheduler.CommandType.PROCESS_UPDATE_CHECK_RESULT, 10);
        }
    }

    public String getMessage() {
        return this.message;
    }
}
