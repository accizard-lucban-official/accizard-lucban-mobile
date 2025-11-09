package com.example.accizardlucban;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Use the Report class from models package
import com.example.accizardlucban.models.Report;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private List<Report> reportList;

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reportList != null ? reportList.size() : 0;
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private TextView typeTextView;
        private TextView descriptionTextView;
        private TextView locationTextView;
        private TextView timestampTextView;
        private TextView statusTextView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.reportTypeTextView);
            descriptionTextView = itemView.findViewById(R.id.reportDescriptionTextView);
            locationTextView = itemView.findViewById(R.id.reportLocationTextView);
            timestampTextView = itemView.findViewById(R.id.reportTimestampTextView);
            statusTextView = itemView.findViewById(R.id.reportStatusTextView);
        }

        public void bind(Report report) {
            if (report == null) {
                setDefaultValues();
                return;
            }

            try {
                // Set report type safely (using category from models.Report)
                String reportType = report.getCategory();
                typeTextView.setText(reportType != null ? reportType : "Unknown Type");

                // Set description safely
                String description = report.getDescription();
                descriptionTextView.setText(description != null ? description : "No description");

                // Set location safely
                String location = report.getLocation();
                locationTextView.setText(location != null ? location : "Unknown Location");

                // Set timestamp safely - handle both Date and formatted string approaches
                String timestampString = getFormattedTimestampSafely(report);
                timestampTextView.setText(timestampString);

                // Set status safely
                String status = report.getStatus();
                statusTextView.setText(status != null ? status : "Unknown Status");

            } catch (Exception e) {
                // Log error and set default values
                android.util.Log.e("ReportAdapter", "Error binding report data", e);
                setDefaultValues();
            }
        }

        private String getFormattedTimestampSafely(Report report) {
            try {
                // Get the timestamp as long from the report
                long timestamp = report.getTimestamp();
                if (timestamp > 0) {
                    // Convert long timestamp to formatted string
                    Date date = new Date(timestamp);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                    return sdf.format(date);
                }
            } catch (Exception e) {
                android.util.Log.w("ReportAdapter", "Could not get timestamp", e);
            }

            // If all else fails, return a default value
            return "No timestamp";
        }

        private void setDefaultValues() {
            typeTextView.setText("Error loading report");
            descriptionTextView.setText("Error");
            locationTextView.setText("Error");
            timestampTextView.setText("Error");
            statusTextView.setText("Error");
        }
    }
}