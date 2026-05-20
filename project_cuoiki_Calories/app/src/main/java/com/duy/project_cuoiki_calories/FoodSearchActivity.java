package com.duy.project_cuoiki_calories;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.duy.project_cuoiki_calories.adapters.FoodAdapter;
import com.duy.project_cuoiki_calories.databinding.ActivityFoodSearchBinding;
import com.duy.project_cuoiki_calories.models.FoodModel;
import com.duy.project_cuoiki_calories.utils.VietnameseFoodData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class FoodSearchActivity extends AppCompatActivity {

    private ActivityFoodSearchBinding binding;
    private FoodAdapter adapter;
    private ArrayList<FoodModel> foodList = new ArrayList<>();
    private String mealType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoodSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mealType = getIntent().getStringExtra("MEAL_TYPE");
        if (mealType == null) mealType = "Khác";

        adapter = new FoodAdapter(foodList, this::updateConfirmButton);
        binding.rvFoodResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFoodResults.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnConfirm.setOnClickListener(v -> addSelectedFoodsToDiary());

        showVietnameseFoods("");

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString().trim();
                showVietnameseFoods(query);
                return true;
            }
            return false;
        });
    }

    private void updateConfirmButton() {
        int totalItems = 0;
        for (FoodModel food : foodList) {
            totalItems += food.getQuantity();
        }

        if (totalItems > 0) {
            binding.btnConfirm.setVisibility(View.VISIBLE);
            binding.btnConfirm.setText("Thêm vào nhật ký (" + totalItems + ")");
        } else {
            binding.btnConfirm.setVisibility(View.GONE);
        }
    }

    private void showVietnameseFoods(String query) {
        List<FoodModel> allVietFoods = VietnameseFoodData.getVietnameseFoods();
        foodList.clear();
        
        if (query.isEmpty()) {
            foodList.addAll(allVietFoods);
        } else {
            String normalizedQuery = removeAccent(query.toLowerCase());
            for (FoodModel food : allVietFoods) {
                String normalizedName = removeAccent(food.getName().toLowerCase());
                if (normalizedName.contains(normalizedQuery)) {
                    foodList.add(food);
                }
            }
        }
        
        if (foodList.isEmpty() && !query.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy món ăn phù hợp", Toast.LENGTH_SHORT).show();
        }
        
        adapter.notifyDataSetChanged();
        updateConfirmButton();
    }

    private String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private void addSelectedFoodsToDiary() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId == null) return;

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        double totalCalVal = 0, totalProtVal = 0, totalFatVal = 0, totalCarbVal = 0;
        List<Map<String, Object>> foodEntries = new ArrayList<>();

        for (FoodModel food : foodList) {
            if (food.getQuantity() > 0) {
                for (int i = 0; i < food.getQuantity(); i++) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("name", food.getName());
                    entry.put("calories", food.getCalories());
                    entry.put("carbs", food.getCarbs());
                    entry.put("protein", food.getProtein());
                    entry.put("fat", food.getFat());
                    entry.put("mealType", mealType);
                    foodEntries.add(entry);
                }
                totalCalVal += (food.getCalories() * food.getQuantity());
                totalProtVal += (food.getProtein() * food.getQuantity());
                totalFatVal += (food.getFat() * food.getQuantity());
                totalCarbVal += (food.getCarbs() * food.getQuantity());
            }
        }

        if (foodEntries.isEmpty()) return;

        final double totalCal = totalCalVal;
        final double totalProt = totalProtVal;
        final double totalFat = totalFatVal;
        final double totalCarb = totalCarbVal;

        db.collection("users").document(userId).collection("logs").document(date)
                .update("foods", FieldValue.arrayUnion(foodEntries.toArray()),
                        "totalCaloriesConsumed", FieldValue.increment(totalCal),
                        "totalCarbs", FieldValue.increment(totalCarb),
                        "totalProtein", FieldValue.increment(totalProt),
                        "totalFat", FieldValue.increment(totalFat))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật nhật ký ăn uống", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Nếu chưa có document cho ngày hôm nay, tạo mới
                    Map<String, Object> newLog = new HashMap<>();
                    newLog.put("date", date);
                    newLog.put("totalCaloriesConsumed", totalCal);
                    newLog.put("totalCarbs", totalCarb);
                    newLog.put("totalProtein", totalProt);
                    newLog.put("totalFat", totalFat);
                    newLog.put("foods", foodEntries);
                    db.collection("users").document(userId).collection("logs").document(date).set(newLog)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Đã tạo nhật ký mới cho hôm nay", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });
    }
}
