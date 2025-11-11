package com.example.accizardlucban;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.text.DateFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class BarChartMarkerView extends MarkerView {

    private final Supplier<Boolean> isPerTypeSupplier;
    private final List<String> typeLabels;
    private final Map<String, Integer> typeCounts;
    private final Map<Integer, Integer> monthlyCounts;

    private final TextView titleLabel;
    private final TextView countLabel;
    private final TextView percentLabel;
    private final View colorSwatch;
    private final MPPointF offsetBuffer = new MPPointF();
    private final DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();

    public BarChartMarkerView(@NonNull Context context,
                              int layoutResource,
                              @NonNull Supplier<Boolean> isPerTypeSupplier,
                              @NonNull List<String> typeLabels,
                              @NonNull Map<String, Integer> typeCounts,
                              @NonNull Map<Integer, Integer> monthlyCounts) {
        super(context, layoutResource);
        this.isPerTypeSupplier = isPerTypeSupplier;
        this.typeLabels = typeLabels;
        this.typeCounts = typeCounts;
        this.monthlyCounts = monthlyCounts;

        titleLabel = findViewById(R.id.markerTitle);
        countLabel = findViewById(R.id.markerCount);
        percentLabel = findViewById(R.id.markerPercentage);
        colorSwatch = findViewById(R.id.markerColorSwatch);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        try {
            if (getChartView() == null) {
                clearMarkerText();
                super.refreshContent(e, highlight);
                return;
            }
            if (!(e instanceof BarEntry)) {
                super.refreshContent(e, highlight);
                return;
            }

            BarEntry barEntry = (BarEntry) e;
            float value = barEntry.getY();

            boolean perType = isPerTypeSupplier.get();
            String title = perType ? resolveTypeLabel(barEntry) : resolveMonthLabel(barEntry);
            float total = perType ? computeTypeTotal() : computeMonthlyTotal();

            if (total <= 0f) {
                total = computeDatasetTotal();
            }

            float percentage = total > 0f ? (value / total) * 100f : 0f;
            int color = resolveBarColor(highlight);

            titleLabel.setText(title);
            countLabel.setText(String.format(Locale.getDefault(), "Count: %.0f", value));
            percentLabel.setText(String.format(Locale.getDefault(), "Percent: %.1f%%", percentage));
            colorSwatch.setBackgroundColor(color);
        } catch (Exception ex) {
            clearMarkerText();
        }

        super.refreshContent(e, highlight);
    }

    private String resolveTypeLabel(BarEntry entry) {
        int index = Math.round(entry.getX());
        if (index >= 0 && index < typeLabels.size()) {
            return typeLabels.get(index);
        }
        return "Unknown";
    }

    private String resolveMonthLabel(BarEntry entry) {
        int index = Math.round(entry.getX());
        index = Math.max(0, Math.min(index, 11));
        return dateFormatSymbols.getShortMonths()[index];
    }

    private float computeTypeTotal() {
        if (typeCounts == null || typeCounts.isEmpty()) {
            return 0f;
        }
        float sum = 0f;
        for (Integer value : typeCounts.values()) {
            if (value != null) {
                sum += value;
            }
        }
        return sum;
    }

    private float computeMonthlyTotal() {
        if (monthlyCounts == null || monthlyCounts.isEmpty()) {
            return 0f;
        }
        float sum = 0f;
        for (Integer value : monthlyCounts.values()) {
            if (value != null) {
                sum += value;
            }
        }
        return sum;
    }

    private float computeDatasetTotal() {
        if (!(getChartView() instanceof BarChart)) {
            return 0f;
        }
        BarChart barChart = (BarChart) getChartView();
        BarData barData = barChart.getData();
        if (barData == null) {
            return 0f;
        }
        float sum = 0f;
        for (int i = 0; i < barData.getDataSetCount(); i++) {
            IBarDataSet dataSet = barData.getDataSetByIndex(i);
            if (dataSet == null) {
                continue;
            }
            for (int entryIndex = 0; entryIndex < dataSet.getEntryCount(); entryIndex++) {
                BarEntry barEntry = dataSet.getEntryForIndex(entryIndex);
                if (barEntry != null) {
                    sum += barEntry.getY();
                }
            }
        }
        return sum;
    }

    private int resolveBarColor(Highlight highlight) {
        try {
            if (getChartView() == null) {
                return Color.GRAY;
            }
            if (!(getChartView() instanceof BarChart)) {
                return Color.GRAY;
            }
            BarChart barChart = (BarChart) getChartView();
            BarData barData = barChart.getData();
            if (barData == null) {
                return Color.GRAY;
            }
            IBarDataSet dataSet = barData.getDataSetByIndex(highlight.getDataSetIndex());
            if (dataSet == null) {
                return Color.GRAY;
            }
            List<Integer> colors = dataSet.getColors();
            if (colors != null && !colors.isEmpty()) {
                int index = Math.abs(Math.round(highlight.getX())) % colors.size();
                return colors.get(index);
            }
            return Color.GRAY;
        } catch (Exception e) {
            return Color.GRAY;
        }
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
        if (getChartView() == null) {
            return new MPPointF(offset.x, offset.y);
        }
        float width = getWidth();
        float height = getHeight();

        float adjustedX = offset.x;
        float adjustedY = offset.y;

        if (posX + adjustedX < 0f) {
            adjustedX = -posX;
        } else if (posX + width + adjustedX > getChartView().getWidth()) {
            adjustedX = getChartView().getWidth() - posX - width;
        }

        if (posY + adjustedY < 0f) {
            adjustedY = -posY + Utils.convertDpToPixel(4f);
        } else if (posY + height + adjustedY > getChartView().getHeight()) {
            adjustedY = getChartView().getHeight() - posY - height - Utils.convertDpToPixel(4f);
        }

        return new MPPointF(adjustedX, adjustedY);
    }

    private void clearMarkerText() {
        titleLabel.setText("Details unavailable");
        countLabel.setText("");
        percentLabel.setText("");
        colorSwatch.setBackgroundColor(Color.GRAY);
    }
}