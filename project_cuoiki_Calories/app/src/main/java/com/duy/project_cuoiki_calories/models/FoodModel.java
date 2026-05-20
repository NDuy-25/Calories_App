package com.duy.project_cuoiki_calories.models;

public class FoodModel {
    public String name;
    public double calories;
    public double protein;
    public double fat;
    public double carbs;
    private int quantity = 0; // Thêm trường số lượng

    public FoodModel() {
    }

    public FoodModel(String name, double calories, double protein, double fat, double carbs) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    public String getName() {
        return name;
    }

    public double getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    public double getCarbs() {
        return carbs;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
