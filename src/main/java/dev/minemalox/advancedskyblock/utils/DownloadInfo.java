package dev.minemalox.advancedskyblock.utils;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import lombok.Getter;
import lombok.Setter;

@Deprecated
@Getter
@Setter
public class DownloadInfo {

    private AdvancedSkyblock main;

    private boolean patch = false;
    private EnumUtils.UpdateMessageType messageType = null;
    private long downloadedBytes = 0;
    private long totalBytes = 0;
    private String outputFileName = "";
    private String newestVersion = "";
    private String downloadLink = "";

    public DownloadInfo(AdvancedSkyblock main) {
        this.main = main;
    }

    void setMessageType(EnumUtils.UpdateMessageType messageType) {
        this.messageType = messageType;
/*        if (messageType != EnumUtils.UpdateMessageType.DOWNLOADING) {
            main.getScheduler().schedule(Scheduler.CommandType.RESET_UPDATE_MESSAGE, 10, messageType);
        }*/
        if (messageType == EnumUtils.UpdateMessageType.FAILED) {
            main.getUtils().sendUpdateMessage(true, false);
        } else if (messageType == EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED) {
            main.getUtils().sendUpdateMessage(false, false);
        }
    }
}
