package com.duy.project_cuoiki_calories.models;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String uid;
    public String email;
    public String gender;
    public int age;
    public double height;
    public double weight;
    public String goal;
    public double dailyCalorieGoal;
    public long lastWeightUpdate; // Timestamp

    public User() {}

    public User(String uid, String email, String gender, int age, double height, double weight, String goal, double dailyCalorieGoal) {
        this.uid = uid;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.goal = goal;
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.lastWeightUpdate = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("email", email);
        result.put("gender", gender);
        result.put("age", age);
        result.put("height", height);
        result.put("weight", weight);
        result.put("goal", goal);
        result.put("dailyCalorieGoal", dailyCalorieGoal);
        result.put("lastWeightUpdate", lastWeightUpdate);
        return result;
    }
}