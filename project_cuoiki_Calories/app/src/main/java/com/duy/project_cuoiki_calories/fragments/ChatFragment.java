package com.duy.project_cuoiki_calories.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private GenerativeModelFutures model;

    private final Executor executor = Executors.newSingleThreadExecutor();

    // API KEY
    private final String API_KEY = "API Key for you";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initGemini();

        messageList = new ArrayList<>();

        adapter = new ChatAdapter(messageList);
        
        // Cài đặt sự kiện Lưu (Like) thực đơn
        adapter.setOnSaveClickListener((message, position) -> {
            saveRecipeToFirestore(message, position);
        });

        binding.rvChat.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        binding.rvChat.setAdapter(adapter);

        // Tin nhắn chào
        if (messageList.isEmpty()) {
            addBotMessage("Xin chào! Tôi là trợ lý AI dinh dưỡng 😊");
        }

        binding.btnSend.setOnClickListener(v -> {

            String message = binding.etMessage
                    .getText()
                    .toString()
                    .trim();

            if (!message.isEmpty()) {

                addUserMessage(message);

                binding.etMessage.setText("");

                addBotMessage("AI đang suy nghĩ...");

                askGeminiAI(message);
            }
        });
    }

    private void saveRecipeToFirestore(ChatMessage message, int position) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.isSaved()) {
            Toast.makeText(getContext(), "Thực đơn này đã được lưu rồi", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("content", message.getMessage());
        recipe.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(userId).collection("saved_recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    message.setSaved(true);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Đã lưu thực đơn vào danh sách yêu thích ❤️", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initGemini() {

        try {

            // Generation Config
            GenerationConfig.Builder configBuilder =
                    new GenerationConfig.Builder();

            configBuilder.temperature = 0.7f;

            GenerationConfig config = configBuilder.build();

            // FIX REQUEST OPTIONS
            RequestOptions requestOptions =
                    new RequestOptions(
                            30000L,     // timeout 30s
                            "v1beta"    // api version
                    );

            // Gemini Model
            GenerativeModel gm = new GenerativeModel(
                    "gemini-2.5-flash-lite",
                    API_KEY,
                    config,
                    null,
                    requestOptions
            );

            model = GenerativeModelFutures.from(gm);

        } catch (Exception e) {

            Log.e("GeminiInit", "Lỗi khởi tạo: " + e.getMessage());

        }
    }

    private void askGeminiAI(String userPrompt) {

        if (model == null) {

            updateLastBotMessage("Lỗi: AI chưa được khởi tạo.");

            return;
        }

        Content content = new Content.Builder()
                .addText(
                        "Bạn là chuyên gia dinh dưỡng. "
                                + "Hãy trả lời ngắn gọn bằng tiếng Việt: "
                                + userPrompt
                )
                .build();

        ListenableFuture<GenerateContentResponse> response =
                model.generateContent(content);

        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {

                    @Override
                    public void onSuccess(
                            GenerateContentResponse result
                    ) {

                        if (getActivity() != null) {

                            getActivity().runOnUiThread(() -> {

                                String botReply =
                                        (result != null)
                                                ? result.getText()
                                                : null;

                                updateLastBotMessage(
                                        botReply != null
                                                ? botReply
                                                : "AI không có phản hồi."
                                );
                            });
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Throwable t
                    ) {

                        if (getActivity() != null) {

                            getActivity().runOnUiThread(() -> {

                                String errorMsg = t.getMessage();

                                Log.e(
                                        "GeminiError",
                                        "FULL ERROR: " + errorMsg
                                );

                                if (errorMsg != null
                                        && errorMsg.contains("503")) {

                                    updateLastBotMessage(
                                            "Hệ thống đang quá tải (503). "
                                                    + "Hãy thử lại sau vài giây."
                                    );

                                } else {

                                    updateLastBotMessage(
                                            "Lỗi: " + errorMsg
                                    );
                                }
                            });
                        }
                    }
                },
                executor
        );
    }

    private void updateLastBotMessage(String newMessage) {

        if (!messageList.isEmpty()) {

            int lastIndex = messageList.size() - 1;

            if (messageList.get(lastIndex).getType()
                    == ChatMessage.TYPE_BOT) {

                messageList.set(
                        lastIndex,
                        new ChatMessage(
                                newMessage,
                                ChatMessage.TYPE_BOT
                        )
                );

                adapter.notifyItemChanged(lastIndex);

                binding.rvChat.smoothScrollToPosition(lastIndex);
            }
        }
    }

    private void addUserMessage(String message) {

        messageList.add(
                new ChatMessage(
                        message,
                        ChatMessage.TYPE_USER
                )
        );

        adapter.notifyItemInserted(messageList.size() - 1);

        binding.rvChat.smoothScrollToPosition(
                messageList.size() - 1
        );
    }

    private void addBotMessage(String message) {

        messageList.add(
                new ChatMessage(
                        message,
                        ChatMessage.TYPE_BOT
                )
        );

        adapter.notifyItemInserted(messageList.size() - 1);

        binding.rvChat.smoothScrollToPosition(
                messageList.size() - 1
        );
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        binding = null;
    }
}
