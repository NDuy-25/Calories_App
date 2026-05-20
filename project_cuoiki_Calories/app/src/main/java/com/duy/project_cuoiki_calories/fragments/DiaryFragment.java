package com.duy.project_cuoiki_calories.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.duy.project_cuoiki_calories.ExerciseActivity;
import com.duy.project_cuoiki_calories.FoodSearchActivity;
import com.duy.project_cuoiki_calories.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiaryFragment extends Fragment {

    TextView tvCalorieGoal, tvCalorieConsumed, tvCalorieBurned, tvCalorieRemaining;
    TextView tvCarbs, tvProtein, tvFat;
    MaterialButton btnAddBreakfast, btnAddLunch, btnAddDinner, btnAddSnack, btnAddExercise;
    CircularProgressIndicator calorieProgress;

    FirebaseFirestore db;
    String userId;
    String ngayHomNay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_diary, container, false);

        tvCalorieGoal = root.findViewById(R.id.tvCalorieGoal);
        tvCalorieConsumed = root.findViewById(R.id.tvCalorieConsumed);
        tvCalorieBurned = root.findViewById(R.id.tvCalorieBurned);
        tvCalorieRemaining = root.findViewById(R.id.tvCalorieRemaining);
        
        tvCarbs = root.findViewById(R.id.tvCarbs);
        tvProtein = root.findViewById(R.id.tvProtein);
        tvFat = root.findViewById(R.id.tvFat);

        btnAddBreakfast = root.findViewById(R.id.btnAddBreakfast);
        btnAddLunch = root.findViewById(R.id.btnAddLunch);
        btnAddDinner = root.findViewById(R.id.btnAddDinner);
        btnAddSnack = root.findViewById(R.id.btnAddSnack);
        btnAddExercise = root.findViewById(R.id.btnAddExercise);
        
        calorieProgress = root.findViewById(R.id.calorieProgress);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        taiDuLieuNguoiDung();

        btnAddBreakfast.setOnClickListener(v -> moManHinhTimDoAn("Sáng"));
        btnAddLunch.setOnClickListener(v -> moManHinhTimDoAn("Trưa"));
        btnAddDinner.setOnClickListener(v -> moManHinhTimDoAn("Chiều"));
        btnAddSnack.setOnClickListener(v -> moManHinhTimDoAn("Phụ"));
        
        btnAddExercise.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ExerciseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Luôn cập nhật ngày mới nhất khi người dùng quay lại Fragment
        ngayHomNay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        taiNhatKyHangNgay();
    }

    private void taiDuLieuNguoiDung() {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded() && documentSnapshot.exists()) {
                Double mucTieu = documentSnapshot.getDouble("dailyCalorieGoal");
                if (mucTieu != null) {
                    tvCalorieGoal.setText(String.format(Locale.US, "%,d", mucTieu.intValue()));
                    tinhToanConLai();
                }
            }
        });
    }

    private void taiNhatKyHangNgay() {
        db.collection("users").document(userId).collection("logs").document(ngayHomNay)
            .get().addOnSuccessListener(documentSnapshot -> {
                if (!isAdded()) return;

                if (documentSnapshot.exists()) {
                    Double daAn = documentSnapshot.getDouble("totalCaloriesConsumed");
                    Double tieuHao = documentSnapshot.getDouble("totalCaloriesBurned");
                    Double carbs = documentSnapshot.getDouble("totalCarbs");
                    Double protein = documentSnapshot.getDouble("totalProtein");
                    Double fat = documentSnapshot.getDouble("totalFat");

                    tvCalorieConsumed.setText(String.valueOf(daAn != null ? daAn.intValue() : 0));
                    tvCalorieBurned.setText(String.valueOf(tieuHao != null ? tieuHao.intValue() : 0));
                    
                    tvCarbs.setText(String.format(Locale.getDefault(), "%.0fg", carbs != null ? carbs : 0));
                    tvProtein.setText(String.format(Locale.getDefault(), "%.0fg", protein != null ? protein : 0));
                    tvFat.setText(String.format(Locale.getDefault(), "%.0fg", fat != null ? fat : 0));
                } else {
                    // Nếu chưa có dữ liệu cho ngày mới, reset toàn bộ về 0
                    tvCalorieConsumed.setText("0");
                    tvCalorieBurned.setText("0");
                    tvCarbs.setText("0g");
                    tvProtein.setText("0g");
                    tvFat.setText("0g");
                }
                tinhToanConLai();
            });
    }

    private void tinhToanConLai() {
        try {
            String goalStr = tvCalorieGoal.getText().toString().replace(",", "");
            int mucTieu = Integer.parseInt(goalStr);
            int daAn = Integer.parseInt(tvCalorieConsumed.getText().toString());
            int tieuHao = Integer.parseInt(tvCalorieBurned.getText().toString());
            
            int conLai = mucTieu - daAn + tieuHao;
            tvCalorieRemaining.setText(String.format(Locale.US, "%,d", conLai));
            
            // Cập nhật thanh tiến trình
            if (mucTieu > 0) {
                int progress = (int) ((float) daAn / (mucTieu + tieuHao) * 100);
                calorieProgress.setProgress(Math.min(progress, 100), true);
            }
        } catch (Exception e) {
            // Tránh lỗi khi dữ liệu chưa tải kịp
        }
    }

    private void moManHinhTimDoAn(String buoiAn) {
        Intent intent = new Intent(getContext(), FoodSearchActivity.class);
        intent.putExtra("MEAL_TYPE", buoiAn);
        startActivity(intent);
    }
}
