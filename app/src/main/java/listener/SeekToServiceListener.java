package listener;

public interface SeekToServiceListener {
    void seekTo(int pCurrentPosition);
    void updateProgress(int position,int audioDuration);
}
