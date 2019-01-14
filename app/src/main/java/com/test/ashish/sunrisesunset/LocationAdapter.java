package com.test.ashish.sunrisesunset;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LocationAdapter extends ListAdapter<Location,LocationAdapter.LocationHolder> {


    private OnItemClickListener onItemClickListener;

    protected LocationAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Location>DIFF_CALLBACK = new DiffUtil.ItemCallback<Location>(){

        @Override
        public boolean areItemsTheSame(Location oldItem, Location newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Location oldItem, Location newItem) {
            return oldItem.getLat() == newItem.getLat() &&
                    oldItem.getLng() == newItem.getLng() &&
                    oldItem.getAddress() == newItem.getAddress();
        }
    };
    @NonNull
    @Override
    public LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item,parent,false);
        return new LocationHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHolder holder, int position) {
        Location note = getItem(position);
        holder.noteAddressTextView.setText(note.getAddress());
    }



    public Location getNoteAt(int index){
        return getItem(index);
    }

    class LocationHolder extends RecyclerView.ViewHolder{

        private TextView noteAddressTextView;

        public LocationHolder(View itemView) {
            super(itemView);
            noteAddressTextView = itemView.findViewById(R.id.address);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(getItem(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(Location note);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
