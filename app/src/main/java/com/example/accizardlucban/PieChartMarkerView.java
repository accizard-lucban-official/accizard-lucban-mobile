package com.example.accizardlucban;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

/**
 * Custom marker view for the report type pie chart. Shows the slice color, type,
 * count and percentage when a slice is tapped.
 */
public class PieChartMarkerView extends MarkerView {

    private final TextView typeLabel;
    private final TextView countLabel;
    private final TextView percentLabel;
    private final View colorSwatch;
    private final PieChart pieChart;
    private final MPPointF offsetBuffer = new MPPointF();

    public PieChartMarkerView(@NonNull Context context, int layoutResource, @NonNull PieChart pieChart) {
        super(context, layoutResource);
        this.pieChart = pieChart;
        typeLabel = findViewById(R.id.reportTypeLabel);
        countLabel = findViewById(R.id.reportCount);
        percentLabel = findViewById(R.id.reportPercentage);
        colorSwatch = findViewById(R.id.colorSwatch);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (!(e instanceof PieEntry)) {
            super.refreshContent(e, highlight);
            return;
        }

        PieEntry pieEntry = (PieEntry) e;
        PieData pieData = pieChart.getData();
        if (pieData == null) {
            super.refreshContent(e, highlight);
            return;
        }

        float total = pieData.getYValueSum();
        float value = pieEntry.getValue();
        float percentage = total > 0 ? (value / total) * 100f : 0f;

        typeLabel.setText(pieEntry.getLabel());
        countLabel.setText(String.format("Count: %.0f", value));
        percentLabel.setText(String.format("Percent: %.1f%%", percentage));

        int color = resolveSliceColor(highlight);
        colorSwatch.setBackgroundColor(color);

        super.refreshContent(e, highlight);
    }

    private int resolveSliceColor(Highlight highlight) {
        PieData pieData = pieChart.getData();
        if (pieData == null) {
            return Color.GRAY;
        }

        PieDataSet dataSet = (PieDataSet) pieData.getDataSetByIndex(highlight.getDataSetIndex());
        if (dataSet == null || dataSet.getEntryCount() == 0) {
            return Color.GRAY;
        }

        int colorIndex = Math.round(highlight.getX());
        colorIndex = Math.max(0, Math.min(colorIndex, dataSet.getColors().size() - 1));

        int baseColor = dataSet.getColors().get(colorIndex);
        return ColorUtils.setAlphaComponent(baseColor, 255);
    }

    @Override
    public MPPointF getOffset() {
        offsetBuffer.x = -(getWidth() / 2f);
        offsetBuffer.y = -getHeight() - Utils.convertDpToPixel(12f);
        return offsetBuffer;
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        MPPointF offset = getOffset();
        float width = getWidth();
        float height = getHeight();

        float adjustedX = offset.x;
        float adjustedY = offset.y;

        if (posX + adjustedX < 0f) {
            adjustedX = -posX;
        } else if (posX + width + adjustedX > pieChart.getWidth()) {
            adjustedX = pieChart.getWidth() - posX - width;
        }

        if (posY + adjustedY < 0f) {
            adjustedY = -posY + Utils.convertDpToPixel(4f);
        } else if (posY + height + adjustedY > pieChart.getHeight()) {
            adjustedY = pieChart.getHeight() - posY - height - Utils.convertDpToPixel(4f);
        }

        return new MPPointF(adjustedX, adjustedY);
    }
}

