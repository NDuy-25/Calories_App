package com.duy.project_cuoiki_calories.models;

import java.util.ArrayList;
import java.util.List;

public class DailyLog {
    public String date; // YYYY-MM-DD
    public double totalCaloriesConsumed;
    public double totalCaloriesBurned;
    public double totalCarbs;
    public double totalProtein;
    public double totalFat;
    public List<FoodEntry> foods = new ArrayList<>();
    public List<ExerciseEntry> exercises = new ArrayList<>();

    public DailyLog() {}
    public DailyLog(String date) {
        this.date = date;
    }

    public static class FoodEntry {
        public String name;
        public double calories;
        public double carbs;
        public double protein;
        public double fat;
        public String mealType; // Sáng, Trưa, Chiều, Phụ

        public FoodEntry() {}
    }

    public static class ExerciseEntry {
        public String name;
        public double caloriesBurned;
        public int durationMinutes;

        public ExerciseEntry() {}
    }
}