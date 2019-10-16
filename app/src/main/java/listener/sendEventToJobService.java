package listener;

public interface sendEventToJobService {
    void playorpauseEvent();
    void sendClickEvent(boolean pForwardEventOccured, boolean pRewindEventOccured,int pLoopMode);
    void shuffleClick();
}
