package com.duy.project_cuoiki_calories.models;

public class ExerciseModel {
    private String name;
    private double metValue;
    private int quantity; // minutes

    public ExerciseModel(String name, double metValue) {
        this.name = name;
        this.metValue = metValue;
        this.quantity = 0;
    }

    public String getName() {
        return name;
    }

    public double getMetValue() {
        return metValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
