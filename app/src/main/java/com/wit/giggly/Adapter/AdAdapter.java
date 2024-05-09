package com.wit.giggly.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wit.giggly.AdHelper;
import com.wit.giggly.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class AdAdapter extends RecyclerView.Adapter<AdAdapter.ViewHolder> {
    private List<String> data;
    ViewGroup viewGroup;
    AdHelper adHelper;
    Context context;

    public AdAdapter(List<String> data, AdHelper adHelper, Context context) {
        this.data = data;
        this.adHelper = adHelper;
        this.context = context;
    }

    @NonNull
    @Override
    public AdAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adview_layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AdAdapter.ViewHolder holder, int position) {
        setFrameLayoutId(holder.frameLayout);
        adHelper.loadAndDisplayNativeAd(context,holder.frameLayout);

    }
    public void setFrameLayoutId(ViewGroup id){
        viewGroup = id;
    }


    public ViewGroup getFrameLayoutId(){
        return viewGroup;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frameLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.fl_adplaceholder);


        }
    }
}
