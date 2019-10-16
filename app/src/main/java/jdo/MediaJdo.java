package jdo;

import java.io.Serializable;

public class MediaJdo implements Serializable {
    private String mAudioname;
    private String mImgUrl;
    private String mPath;
    private long mDuration;

    public String getmAudioname() {
        return mAudioname;
    }

    public void setmAudioname(String mAudioname) {
        this.mAudioname = mAudioname;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public String getmPath() {
        return mPath;
    }

    public void setmPath(String mPath) {
        this.mPath = mPath;
    }

    public long getmDuration() {
        return mDuration;
    }

    public void setmDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    @Override
    public String toString() {
        return "MediaJdo{" +
                "mAudioname='" + mAudioname + '\'' +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mPath='" + mPath + '\'' +
                ", mDuration='" + mDuration + '\'' +
                '}';
    }
}
