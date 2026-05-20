package com.duy.project_cuoiki_calories.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.duy.project_cuoiki_calories.LoginActivity;
import com.duy.project_cuoiki_calories.databinding.FragmentProfileBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            loadProfile();
            loadChartData();
        }

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void loadProfile() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        binding.tvProfileEmail.setText(mAuth.getCurrentUser().getEmail());

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (isAdded() && documentSnapshot.exists()) {
                String gender = documentSnapshot.getString("gender");
                Long age = documentSnapshot.getLong("age");
                Double height = documentSnapshot.getDouble("height");
                Double weight = documentSnapshot.getDouble("weight");

                String info = String.format("%s | %d tuổi | %.0fcm | %.1fkg", 
                        gender, age, height, weight);
                binding.tvProfileInfo.setText(info);
            }
        });
    }

    private void loadChartData() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        String sevenDaysAgo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        db.collection("users").document(userId).collection("logs")
                .whereGreaterThanOrEqualTo("date", sevenDaysAgo)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    List<Entry> entries = new ArrayList<>();
                    int i = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double consumed = doc.getDouble("totalCaloriesConsumed");
                        if (consumed != null) {
                            entries.add(new Entry(i++, consumed.floatValue()));
                        }
                    }

                    setupChart();

                    if (entries.isEmpty()) {
                        binding.lineChart.setNoDataText("Chưa có dữ liệu 7 ngày qua");
                        binding.lineChart.setNoDataTextColor(Color.WHITE);
                        binding.lineChart.invalidate();
                        return;
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "Calories nạp vào");
                    dataSet.setColor(Color.parseColor("#00F0FF"));
                    dataSet.setCircleColor(Color.parseColor("#00F0FF"));
                    dataSet.setLineWidth(3f);
                    dataSet.setCircleRadius(4f);
                    dataSet.setDrawCircleHole(true);
                    dataSet.setCircleHoleColor(Color.parseColor("#151A2E"));
                    dataSet.setValueTextColor(Color.WHITE);
                    dataSet.setValueTextSize(10f);
                    dataSet.setDrawFilled(true);
                    dataSet.setFillAlpha(50);
                    dataSet.setFillColor(Color.parseColor("#00F0FF"));
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                    LineData lineData = new LineData(dataSet);
                    binding.lineChart.setData(lineData);
                    binding.lineChart.invalidate();
                });
    }

    private void setupChart() {
        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.setDrawGridBackground(false);
        binding.lineChart.setBackgroundColor(Color.TRANSPARENT);
        
        Legend legend = binding.lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(Color.WHITE);

        YAxis leftAxis = binding.lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setGridColor(Color.parseColor("#1A1F3C"));
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setDrawZeroLine(false);

        binding.lineChart.getAxisRight().setEnabled(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}