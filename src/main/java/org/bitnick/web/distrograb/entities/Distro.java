package org.bitnick.web.distrograb.entities;

public class Distro {
    private String distroName = "";
    private String distroDownloadUrl = "";
    private String distroMainUrl = "";

    public void setDistroName(String distroName) {
        this.distroName = distroName;
    }

    public void setDistroDownloadUrl(String distroDownloadUrl) {
        this.distroDownloadUrl = distroDownloadUrl;
    }

    public void setDistroMainUrl(String distroMainUrl) {
        this.distroMainUrl = distroMainUrl;
    }

    public String getDistroName() {
        return distroName;
    }

    public String getDistroDownloadUrl() {
        return distroDownloadUrl;
    }

    public String getDistroMainUrl() {
        return distroMainUrl;
    }
}
