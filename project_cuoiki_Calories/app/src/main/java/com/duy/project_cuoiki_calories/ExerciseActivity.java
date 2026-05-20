package com.duy.project_cuoiki_calories;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.duy.project_cuoiki_calories.adapters.ExerciseAdapter;
import com.duy.project_cuoiki_calories.databinding.ActivityExerciseBinding;
import com.duy.project_cuoiki_calories.models.ExerciseModel;
import com.duy.project_cuoiki_calories.utils.ExerciseData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class ExerciseActivity extends AppCompatActivity {

    private ActivityExerciseBinding binding;
    private ExerciseAdapter adapter;
    private ArrayList<ExerciseModel> exerciseList = new ArrayList<>();
    private double userWeight = 60.0; // Mặc định nếu không lấy được

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExerciseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fetchUserWeight();

        adapter = new ExerciseAdapter(exerciseList, this::updateConfirmButton);
        binding.rvExerciseResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExerciseResults.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnConfirm.setOnClickListener(v -> saveSelectedExercises());

        showExercises("");

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString().trim();
                showExercises(query);
                return true;
            }
            return false;
        });
    }

    private void fetchUserWeight() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("weight")) {
                        userWeight = documentSnapshot.getDouble("weight");
                    }
                });
    }

    private void updateConfirmButton() {
        int totalMinutes = 0;
        int count = 0;
        for (ExerciseModel exercise : exerciseList) {
            if (exercise.getQuantity() > 0) {
                totalMinutes += exercise.getQuantity();
                count++;
            }
        }

        if (count > 0) {
            binding.btnConfirm.setVisibility(View.VISIBLE);
            binding.btnConfirm.setText("Lưu bài tập (" + count + " bài, " + totalMinutes + " phút)");
        } else {
            binding.btnConfirm.setVisibility(View.GONE);
        }
    }

    private void showExercises(String query) {
        List<ExerciseModel> allExercises = ExerciseData.getExerciseList();
        exerciseList.clear();

        if (query.isEmpty()) {
            exerciseList.addAll(allExercises);
        } else {
            String normalizedQuery = removeAccent(query.toLowerCase());
            for (ExerciseModel ex : allExercises) {
                String normalizedName = removeAccent(ex.getName().toLowerCase());
                if (normalizedName.contains(normalizedQuery)) {
                    exerciseList.add(ex);
                }
            }
        }

        if (exerciseList.isEmpty() && !query.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy bài tập phù hợp", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
        updateConfirmButton();
    }

    private String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private void saveSelectedExercises() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        double totalCaloriesBurnedVal = 0;
        List<Map<String, Object>> exerciseEntries = new ArrayList<>();

        for (ExerciseModel exercise : exerciseList) {
            if (exercise.getQuantity() > 0) {
                // Công thức tính calo tiêu hao: Calories = MET * Weight * Time(hours)
                double calories = exercise.getMetValue() * userWeight * (exercise.getQuantity() / 60.0);
                
                Map<String, Object> entry = new HashMap<>();
                entry.put("name", exercise.getName());
                entry.put("duration", exercise.getQuantity());
                entry.put("caloriesBurned", calories);
                entry.put("metValue", exercise.getMetValue());
                
                exerciseEntries.add(entry);
                totalCaloriesBurnedVal += calories;
            }
        }

        if (exerciseEntries.isEmpty()) return;

        final double totalCaloriesBurned = totalCaloriesBurnedVal;

        db.collection("users").document(userId).collection("logs").document(date)
                .update("exercises", FieldValue.arrayUnion(exerciseEntries.toArray()),
                        "totalCaloriesBurned", FieldValue.increment(totalCaloriesBurned))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật nhật ký luyện tập", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Nếu chưa có document cho ngày hôm nay, tạo mới
                    Map<String, Object> newLog = new HashMap<>();
                    newLog.put("date", date);
                    newLog.put("totalCaloriesBurned", totalCaloriesBurned);
                    newLog.put("exercises", exerciseEntries);
                    db.collection("users").document(userId).collection("logs").document(date).set(newLog)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Đã tạo nhật ký mới và lưu bài tập", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });
    }
}
