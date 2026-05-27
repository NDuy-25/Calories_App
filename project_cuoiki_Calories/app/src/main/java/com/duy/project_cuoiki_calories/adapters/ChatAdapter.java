package com.duy.project_cuoiki_calories.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.duy.project_cuoiki_calories.R;
import com.duy.project_cuoiki_calories.models.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatMessages;
    private OnSaveClickListener saveClickListener;

    public interface OnSaveClickListener {
        void onSaveClick(ChatMessage message, int position);
    }

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void setOnSaveClickListener(OnSaveClickListener listener) {
        this.saveClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        String message = chatMessage.getMessage();
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMessage.setText(message);
        } else {
            BotViewHolder botHolder = (BotViewHolder) holder;
            botHolder.tvMessage.setText(message);
            
            // Cập nhật icon dựa trên trạng thái đã lưu
            if (chatMessage.isSaved()) {
                botHolder.btnSave.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                botHolder.btnSave.setImageResource(android.R.drawable.btn_star_big_off);
            }

            botHolder.btnSave.setOnClickListener(v -> {
                if (saveClickListener != null) {
                    saveClickListener.onSaveClick(chatMessage, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        ImageButton btnSave;
        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}
