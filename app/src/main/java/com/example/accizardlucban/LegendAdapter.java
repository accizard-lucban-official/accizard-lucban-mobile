package com.example.accizardlucban;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.LegendViewHolder> {
    
    private List<String> legendItems;
    private Context context;
    private LegendColorProvider colorProvider;
    
    public interface LegendColorProvider {
        int getColorForType(String type);
    }
    
    public LegendAdapter(Context context, List<String> legendItems, LegendColorProvider colorProvider) {
        this.context = context;
        this.legendItems = legendItems;
        this.colorProvider = colorProvider;
    }
    
    @NonNull
    @Override
    public LegendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_legend, parent, false);
        return new LegendViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LegendViewHolder holder, int position) {
        String label = legendItems.get(position);
        holder.bind(label, colorProvider);
    }
    
    @Override
    public int getItemCount() {
        return legendItems != null ? legendItems.size() : 0;
    }
    
    public void updateLegendItems(List<String> newItems) {
        this.legendItems = newItems;
        notifyDataSetChanged();
    }
    
    static class LegendViewHolder extends RecyclerView.ViewHolder {
        private View colorDot;
        private TextView labelView;
        
        LegendViewHolder(@NonNull View itemView) {
            super(itemView);
            colorDot = itemView.findViewById(R.id.legendColorDot);
            labelView = itemView.findViewById(R.id.legendLabel);
        }
        
        void bind(String label, LegendColorProvider colorProvider) {
            if (labelView != null) {
                labelView.setText(label);
                
                // Set font
                Typeface dmSansTypeface = ResourcesCompat.getFont(itemView.getContext(), R.font.dmsans);
                if (dmSansTypeface != null) {
                    labelView.setTypeface(dmSansTypeface);
                }
            }
            
            if (colorDot != null && colorProvider != null) {
                int color = colorProvider.getColorForType(label);
                colorDot.getBackground().setTint(color);
            }
        }
    }
}

