package com.example.accizardlucban;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ADMIN = 2;

    private List<ChatMessage> messagesList;

    public ChatAdapter(List<ChatMessage> messagesList) {
        this.messagesList = messagesList;
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).isUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_ADMIN;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_admin, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messagesList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timestampText;
        private ImageView messageImage;
        private LinearLayout imageContainer;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
            messageImage = itemView.findViewById(R.id.messageImage);
            imageContainer = itemView.findViewById(R.id.imageContainer);
        }

        public void bind(ChatMessage message) {
            if (messageText != null) {
                messageText.setText(message.getContent());
            }
            if (timestampText != null) {
                timestampText.setText(message.getTimestamp());
            }
            
            // Handle image display
            if (message.hasImage() && messageImage != null && imageContainer != null) {
                imageContainer.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.VISIBLE);
                
                if (message.getImageBitmap() != null) {
                    messageImage.setImageBitmap(message.getImageBitmap());
                }
            } else if (imageContainer != null) {
                imageContainer.setVisibility(View.GONE);
            }
        }
    }
}