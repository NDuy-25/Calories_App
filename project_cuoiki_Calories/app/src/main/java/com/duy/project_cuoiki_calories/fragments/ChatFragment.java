package com.duy.project_cuoiki_calories.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.duy.project_cuoiki_calories.adapters.ChatAdapter;
import com.duy.project_cuoiki_calories.databinding.FragmentChatBinding;
import com.duy.project_cuoiki_calories.models.ChatMessage;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private GenerativeModelFutures model;

    // Sử dụng một Executor duy nhất cho toàn bộ Fragment để tránh lãng phí tài nguyên
    private final Executor executor = Executors.newSingleThreadExecutor();

    // Lưu ý: Sau này nên để API_KEY trong local.properties hoặc BuildConfig
    private final String API_KEY = "AIzaSyCpAgj60xrCeOQgsQgjKp728Wv5EHcOJzA";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Gemini AI
        initGemini();

        // Cấu hình RecyclerView
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        binding.rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChat.setAdapter(adapter);

        // Lời chào mặc định
        if (messageList.isEmpty()) {
            addBotMessage("Xin chào! Tôi là Trợ lý AI về dinh dưỡng. Hãy hỏi tôi về thực đơn hoặc sức khỏe nhé! 😊");
        }

        // Xử lý sự kiện gửi tin nhắn
        binding.btnSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addUserMessage(message);
                binding.etMessage.setText("");

                // Thêm dòng trạng thái chờ
                addBotMessage("AI đang suy nghĩ...");
                askGeminiAI(message);
            }
        });
    }

    private void initGemini() {
        try {
            // Không cần thêm "models/" ở đầu
            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
            model = GenerativeModelFutures.from(gm);
            Log.d("GeminiInit", "Gemini AI đã khởi tạo thành công.");
        } catch (Exception e) {
            Log.e("GeminiInit", "Lỗi khởi tạo: " + e.getMessage());
        }
    }


    private void askGeminiAI(String userPrompt) {
        if (model == null) {
            updateLastBotMessage("Lỗi: AI chưa được khởi tạo. Kiểm tra lại API Key.");
            return;
        }

        // Tạo nội dung gửi đi với hướng dẫn cụ thể cho AI
        Content content = new Content.Builder()
                .addText("Bạn là một chuyên gia dinh dưỡng Việt Nam. Hãy trả lời ngắn gọn, thân thiện bằng tiếng Việt: " + userPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (isAdded() && getActivity() != null) { // Kiểm tra Fragment còn tồn tại không
                    getActivity().runOnUiThread(() -> {
                        String botReply = result.getText();
                        if (botReply != null && !botReply.isEmpty()) {
                            updateLastBotMessage(botReply);
                        } else {
                            updateLastBotMessage("Tôi không tìm thấy câu trả lời phù hợp. Bạn hãy thử hỏi cách khác nhé.");
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e("GeminiError", "Lỗi: " + t.getMessage());
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String errorMsg = t.getMessage();
                        if (errorMsg != null && errorMsg.contains("403")) {
                            updateLastBotMessage("Lỗi: API Key không hợp lệ hoặc hết hạn.");
                        } else if (errorMsg != null && errorMsg.contains("404")) {
                            updateLastBotMessage("Lỗi: Model không tìm thấy. Kiểm tra lại tên model.");
                        } else {
                            updateLastBotMessage("Lỗi kết nối: " + errorMsg);
                        }
                    });
                }
            }
        }, executor);
    }

    private void updateLastBotMessage(String newMessage) {
        if (!messageList.isEmpty()) {
            int lastIndex = messageList.size() - 1;
            // Chỉ cập nhật nếu tin nhắn cuối cùng là của Bot (dòng "AI đang suy nghĩ...")
            if (messageList.get(lastIndex).getType() == ChatMessage.TYPE_BOT) {
                messageList.set(lastIndex, new ChatMessage(newMessage, ChatMessage.TYPE_BOT));
                adapter.notifyItemChanged(lastIndex);
                binding.rvChat.smoothScrollToPosition(lastIndex);
            }
        }
    }

    private void addUserMessage(String message) {
        messageList.add(new ChatMessage(message, ChatMessage.TYPE_USER));
        adapter.notifyItemInserted(messageList.size() - 1);
        binding.rvChat.smoothScrollToPosition(messageList.size() - 1);
    }

    private void addBotMessage(String message) {
        messageList.add(new ChatMessage(message, ChatMessage.TYPE_BOT));
        adapter.notifyItemInserted(messageList.size() - 1);
        binding.rvChat.smoothScrollToPosition(messageList.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}