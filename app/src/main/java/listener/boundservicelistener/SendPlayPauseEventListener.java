package listener.boundservicelistener;

public interface SendPlayPauseEventListener {
    void sendPlayorPauseEvent();
    void event(boolean forward,boolean rewind,int loopMode,boolean shuffle);
    void seekToevent(int pCurrentPosition);
}
