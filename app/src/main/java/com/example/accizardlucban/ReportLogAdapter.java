package com.example.accizardlucban;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

// Use the Report class from models package
import com.example.accizardlucban.models.Report;

public class ReportLogAdapter extends RecyclerView.Adapter<ReportLogAdapter.ReportViewHolder> {
    
    private List<Report> reports;
    private Context context;
    private OnReportClickListener onReportClickListener;

    public interface OnReportClickListener {
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
        private TextView descriptionText;
        private TextView timestampText;
        private TextView viewAttachmentsText;
        private View statusDot;
        private LinearLayout statusBadgeContainer;
        private ImageView timeIcon;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportTypeText = itemView.findViewById(R.id.reportTypeText);
            statusText = itemView.findViewById(R.id.statusText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            timestampText = itemView.findViewById(R.id.timestampText);
            viewAttachmentsText = itemView.findViewById(R.id.viewAttachmentsText);
            statusDot = itemView.findViewById(R.id.statusDot);
            statusBadgeContainer = itemView.findViewById(R.id.statusBadgeContainer);
            timeIcon = itemView.findViewById(R.id.timeIcon);

            // Make card NOT clickable - removed click listener
            itemView.setClickable(false);
            itemView.setFocusable(false);

            // Only "View Attachments" button is clickable (when images exist)
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
                // Set report type (title) - remove emojis before displaying
                String category = report.getCategory();
                if (category != null) {
                    category = removeEmojis(category).trim();
                }
                reportTypeText.setText(category != null && !category.isEmpty() ? category : "Unknown Type");
                
                // Set description
                descriptionText.setText(report.getDescription() != null ? report.getDescription() : "No description available");
                
                // Convert timestamp to relative time format
                long timestampLong = report.getTimestamp();
                String timestampString = formatTimestamp(timestampLong);
                timestampText.setText(timestampString);

                // Set status badge based on report status
                String status = report.getStatus();
                setStatusBadge(status);

                // Show/hide "View Attachments" button only when images exist
                int imageCount = report.getImageCount();
                if (imageCount > 0 && report.getImageUrls() != null && !report.getImageUrls().isEmpty()) {
                    viewAttachmentsText.setVisibility(View.VISIBLE);
                    viewAttachmentsText.setText("View Attachments (" + imageCount + ")");
                } else {
                    viewAttachmentsText.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                // Log error and set default values
                android.util.Log.e("ReportLogAdapter", "Error binding report data", e);
                reportTypeText.setText("Error loading report");
                statusText.setText("Error");
                descriptionText.setText("Error loading report data");
                timestampText.setText("Error");
                viewAttachmentsText.setVisibility(View.GONE);
            }
        }
        
        /**
         * Format timestamp to relative time (like "1 day ago", "Jul 15, 2023")
         */
        private String formatTimestamp(long timestamp) {
            if (timestamp <= 0) {
                return "No timestamp";
            }
            try {
                Date reportDate = new Date(timestamp);
                Date currentDate = new Date();
                
                long diffInMillis = currentDate.getTime() - reportDate.getTime();
                long diffInSeconds = diffInMillis / 1000;
                long diffInMinutes = diffInSeconds / 60;
                long diffInHours = diffInMinutes / 60;
                long diffInDays = diffInHours / 24;
                
                // Show relative time for recent reports
                if (diffInDays == 0) {
                    if (diffInHours == 0) {
                        if (diffInMinutes == 0) {
                            return "Just now";
                        } else {
                            return diffInMinutes + " minute" + (diffInMinutes > 1 ? "s" : "") + " ago";
                        }
                    } else {
                        return diffInHours + " hour" + (diffInHours > 1 ? "s" : "") + " ago";
                    }
                } else if (diffInDays == 1) {
                    return "1 day ago";
                } else if (diffInDays < 7) {
                    return diffInDays + " days ago";
                } else if (diffInDays < 30) {
                    long weeks = diffInDays / 7;
                    return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
                } else {
                    // For older reports, show date format like "Jul 15, 2023"
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    return sdf.format(reportDate);
                }
            } catch (Exception e) {
                android.util.Log.e("ReportLogAdapter", "Error formatting timestamp", e);
                return "Invalid timestamp";
            }
        }

        /**
         * Set status badge based on report status (Pending, Ongoing, Responded, Not Responded, Redundant, False Report)
         */
        private void setStatusBadge(String status) {
            if (status == null) {
                status = "Pending";
            }

            String statusDisplayText;
            String statusTextColor;
            int badgeBackgroundRes;
            int dotBackgroundRes;

            switch (status.toLowerCase()) {
                case "pending":
                    statusDisplayText = "Pending";
                    statusTextColor = "#FFB300";
                    badgeBackgroundRes = R.drawable.status_pending_bg;
                    dotBackgroundRes = R.drawable.status_dot_pending;
                    break;
                case "ongoing":
                    statusDisplayText = "Ongoing";
                    statusTextColor = "#2196F3";
                    badgeBackgroundRes = R.drawable.status_ongoing_bg;
                    dotBackgroundRes = R.drawable.status_dot_ongoing;
                    break;
                case "responded":
                    statusDisplayText = "Responded";
                    statusTextColor = "#4CAF50";
                    badgeBackgroundRes = R.drawable.status_responded_bg;
                    dotBackgroundRes = R.drawable.status_dot_responded;
                    break;
                case "not responded":
                    statusDisplayText = "Not Responded";
                    statusTextColor = "#E53935";
                    badgeBackgroundRes = R.drawable.status_not_responded_bg;
                    dotBackgroundRes = R.drawable.status_dot_not_responded;
                    break;
                case "redundant":
                    statusDisplayText = "Redundant";
                    statusTextColor = "#9C27B0";
                    badgeBackgroundRes = R.drawable.status_redundant_bg;
                    dotBackgroundRes = R.drawable.status_dot_redundant;
                    break;
                case "false report":
                    statusDisplayText = "False Report";
                    statusTextColor = "#FF5722";
                    badgeBackgroundRes = R.drawable.status_redundant_bg; // Reuse redundant background for now
                    dotBackgroundRes = R.drawable.status_dot_redundant; // Reuse redundant dot for now
                    break;
                default:
                    statusDisplayText = "Pending";
                    statusTextColor = "#FFB300";
                    badgeBackgroundRes = R.drawable.status_pending_bg;
                    dotBackgroundRes = R.drawable.status_dot_pending;
                    break;
            }

            // Update status text
            statusText.setText(statusDisplayText);
            statusText.setTextColor(android.graphics.Color.parseColor(statusTextColor));

            // Update badge background
            statusBadgeContainer.setBackgroundResource(badgeBackgroundRes);

            // Update dot background
            statusDot.setBackgroundResource(dotBackgroundRes);
        }
        
        /**
         * Helper method to remove emoji characters from a string.
         * This ensures report types like "⚠ Civil Disturbance" or "⛰ Landslide" 
         * are displayed as "Civil Disturbance" and "Landslide" without emojis.
         */
        private String removeEmojis(String s) {
            if (s == null || s.isEmpty()) {
                return "";
            }
            // Regex to match most common emoji ranges and variation selectors
            // This pattern is more comprehensive than just specific emojis
            String emojiRegex = "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]|[\\u2600-\\u26FF]|[\\u2700-\\u27BF]|[\\u2300-\\u23FF]|[\\u2B50]|[\\u2B06]|[\\u2934]|[\\u2935]|[\\u3030]|[\\u303D]|[\\u3297]|[\\u3299]|[\\uFE0F]";
            String cleaned = s.replaceAll(emojiRegex, "");
            // Also remove specific emojis we know are used in report types
            cleaned = cleaned
                .replace("⚠", "")
                .replace("⛰", "")
                .replace("🚗", "")
                .replace("🏥", "")
                .replace("🌋", "")
                .replace("🏚", "")
                .replace("🔴", "")
                .replace("🔥", "")
                .replace("🌊", "")
                .replace("🦠", "")
                .replace("🏗", "")
                .replace("🚧", "")
                .replace("⚡", "")
                .replace("🌿", "")
                .trim();
            return cleaned;
        }
    }

    /**
     * ItemDecoration to add dividers between report cards
     */
    public static class ReportItemDivider extends RecyclerView.ItemDecoration {
        private final int dividerHeight;
        private final Paint dividerPaint;

        public ReportItemDivider() {
            dividerHeight = 1;
            dividerPaint = new Paint();
            dividerPaint.setColor(0xFFE0E0E0); // Light gray divider color
            dividerPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            
            // Add bottom divider for all items except the last one
            if (parent.getChildAdapterPosition(view) < parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = dividerHeight;
            }
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            
            int childCount = parent.getChildCount();
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            
            for (int i = 0; i < childCount - 1; i++) {
                View child = parent.getChildAt(i);
                int top = child.getBottom();
                int bottom = top + dividerHeight;
                c.drawRect(left, top, right, bottom, dividerPaint);
            }
        }
    }
}
