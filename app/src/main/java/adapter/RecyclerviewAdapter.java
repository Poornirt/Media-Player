package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaplayer.R;

import java.util.ArrayList;

import jdo.MediaJdo;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<MediaJdo> mMediaJdoArrayList;

    public RecyclerviewAdapter(Context pContext, ArrayList<MediaJdo> pMediaJdoArrayList) {
        mContext = pContext;
        mMediaJdoArrayList = pMediaJdoArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.media_row_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.lTextviewName.setText(String.valueOf(mMediaJdoArrayList.get(position).getmAudioname()));
        Glide.with(mContext).load(mMediaJdoArrayList.get(position).getmImgUrl()).placeholder(R.drawable.musicplaceholder).into(holder.lAudioImage);
    }

    @Override
    public int getItemCount() {
        return mMediaJdoArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView lTextviewName;
        ImageView lAudioImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lTextviewName = itemView.findViewById(R.id.audio_name);
            lAudioImage = itemView.findViewById(R.id.audio_image);
        }
    }
}
