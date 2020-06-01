package dev.minemalox.advancedskyblock.utils;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;

@Deprecated
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

    public AdvancedSkyblock getMain() {
        return this.main;
    }

    public void setMain(AdvancedSkyblock main) {
        this.main = main;
    }

    public boolean isPatch() {
        return this.patch;
    }

    public void setPatch(boolean patch) {
        this.patch = patch;
    }

    public EnumUtils.UpdateMessageType getMessageType() {
        return this.messageType;
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

    public long getDownloadedBytes() {
        return this.downloadedBytes;
    }

    public void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public long getTotalBytes() {
        return this.totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getOutputFileName() {
        return this.outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getNewestVersion() {
        return this.newestVersion;
    }

    public void setNewestVersion(String newestVersion) {
        this.newestVersion = newestVersion;
    }

    public String getDownloadLink() {
        return this.downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }
}
