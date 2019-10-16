package listener;

public interface SendClickEventListener {
    /**
     *
     * This
     * @param pForwardEventOccured
     * @param pRewindEventOccured
     */
    void sendClickEvent(boolean pForwardEventOccured, boolean pRewindEventOccured,boolean pLoopMode);

    void shuffleClick();
}
