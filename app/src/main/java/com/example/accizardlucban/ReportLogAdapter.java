package com.example.accizardlucban;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// Use the Report class from models package
import com.example.accizardlucban.models.Report;

public class ReportLogAdapter extends RecyclerView.Adapter<ReportLogAdapter.ReportViewHolder> {
    
    private List<Report> reports;
    private Context context;
    private OnReportClickListener onReportClickListener;

    public interface OnReportClickListener {
        void onReportClick(Report report);
        void onViewAttachmentsClick(Report report);
    }

    public ReportLogAdapter(Context context, List<Report> reports) {
        this.context = context;
        this.reports = reports != null ? reports : new ArrayList<>();
    }

    public void setOnReportClickListener(OnReportClickListener listener) {
        this.onReportClickListener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_log, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void updateReports(List<Report> newReports) {
        this.reports = newReports != null ? newReports : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void filterReports(List<Report> filteredReports) {
        this.reports = filteredReports != null ? filteredReports : new ArrayList<>();
        notifyDataSetChanged();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private TextView reportTypeText;
        private TextView statusText;
        private TextView locationText;
        private TextView descriptionText;
        private TextView timestampText;
        private TextView viewAttachmentsText;
        private ImageView locationIcon;
        private ImageView timeIcon;
        private ImageView attachmentIcon;
        private LinearLayout attachmentsContainer;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportTypeText = itemView.findViewById(R.id.reportTypeText);
            statusText = itemView.findViewById(R.id.statusText);
            locationText = itemView.findViewById(R.id.locationText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            timestampText = itemView.findViewById(R.id.timestampText);
            viewAttachmentsText = itemView.findViewById(R.id.viewAttachmentsText);
            locationIcon = itemView.findViewById(R.id.locationIcon);
            timeIcon = itemView.findViewById(R.id.timeIcon);
            attachmentIcon = itemView.findViewById(R.id.attachmentIcon);
            attachmentsContainer = itemView.findViewById(R.id.attachmentsContainer);

            // Remove click listener from itemView - reports are no longer clickable
            // Only the "View Attachments" button remains clickable
            itemView.setClickable(false);
            itemView.setFocusable(false);

            // Keep "View Attachments" clickable
            viewAttachmentsText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onReportClickListener != null) {
                        onReportClickListener.onViewAttachmentsClick(reports.get(getAdapterPosition()));
                    }
                }
            });
        }

        public void bind(Report report) {
            try {
                // Safely set text with null checks
                // Use getCategory() instead of getReportType()
                reportTypeText.setText(report.getCategory() != null ? report.getCategory() : "Unknown Type");
                statusText.setText(report.getStatus() != null ? report.getStatus() : "Unknown Status");
                locationText.setText(report.getLocation() != null ? report.getLocation() : "Unknown Location");
                descriptionText.setText(report.getDescription() != null ? report.getDescription() : "No description available");
                
                // Convert long timestamp to formatted string
                long timestampLong = report.getTimestamp();
                String timestampString = formatTimestamp(timestampLong);
                timestampText.setText(timestampString);

                // Set status text color and background based on status
                setStatusStyle(report.getStatus());

                // Show/hide view attachments based on image count
                int imageCount = report.getImageCount();
                if (imageCount > 0) {
                    viewAttachmentsText.setVisibility(View.VISIBLE);
                    attachmentIcon.setVisibility(View.VISIBLE);
                    viewAttachmentsText.setText("View Attachments (" + imageCount + ")");
                } else {
                    viewAttachmentsText.setVisibility(View.GONE);
                    attachmentIcon.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                // Log error and set default values
                android.util.Log.e("ReportLogAdapter", "Error binding report data", e);
                reportTypeText.setText("Error loading report");
                statusText.setText("Error");
                locationText.setText("Error");
                descriptionText.setText("Error loading report data");
                timestampText.setText("Error");
                viewAttachmentsText.setVisibility(View.GONE);
            }
        }
        
        private String formatTimestamp(long timestamp) {
            if (timestamp <= 0) {
                return "No timestamp";
            }
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault());
                return sdf.format(new java.util.Date(timestamp));
            } catch (Exception e) {
                return "Invalid timestamp";
            }
        }

        private void setStatusStyle(String status) {
            int textColor;
            int backgroundRes;

            if (status == null) {
                status = "Unknown";
            }

            switch (status.toLowerCase()) {
                case "pending":
                    textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                    backgroundRes = R.drawable.status_pending_bg;
                    break;
                case "ongoing":
                    textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                    backgroundRes = R.drawable.status_ongoing_bg;
                    break;
                case "responded":
                    textColor = context.getResources().getColor(android.R.color.holo_green_dark);
                    backgroundRes = R.drawable.status_responded_bg;
                    break;
                case "not responded":
                    textColor = context.getResources().getColor(android.R.color.holo_red_dark);
                    backgroundRes = R.drawable.status_not_responded_bg;
                    break;
                case "redundant":
                    textColor = context.getResources().getColor(android.R.color.holo_purple);
                    backgroundRes = R.drawable.status_redundant_bg;
                    break;
                default:
                    textColor = context.getResources().getColor(android.R.color.darker_gray);
                    backgroundRes = R.drawable.status_pending_bg;
                    break;
            }

            statusText.setTextColor(textColor);
            statusText.setBackgroundResource(backgroundRes);
        }
    }
}
