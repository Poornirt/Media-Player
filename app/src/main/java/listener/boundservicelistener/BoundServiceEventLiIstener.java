package listener.boundservicelistener;

public interface BoundServiceEventLiIstener {
    void updateProgress(int position,int audioDuration);
    void onComplete();
}
