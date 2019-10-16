package listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerListener implements RecyclerView.OnItemTouchListener {

    private onItemClickListener mListener;
    private Context mContext;
    GestureDetector mGestureDetector;

    public interface onItemClickListener {
        void onClick(View view, int Position);
    }


    public RecyclerListener(Context pContext, onItemClickListener pListener) {
        mListener = pListener;
        mContext = pContext;
        mGestureDetector=new GestureDetector(pContext, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View lView = rv.findChildViewUnder(e.getX(), e.getY());
        if (lView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onClick(lView, rv.getChildAdapterPosition(lView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
