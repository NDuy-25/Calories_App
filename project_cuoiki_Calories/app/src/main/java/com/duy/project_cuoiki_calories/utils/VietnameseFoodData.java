package com.duy.project_cuoiki_calories.utils;

import com.duy.project_cuoiki_calories.models.FoodModel;
import java.util.ArrayList;
import java.util.List;

public class VietnameseFoodData {
    public static List<FoodModel> getVietnameseFoods() {
        List<FoodModel> list = new ArrayList<>();

        list.add(new FoodModel("Phở bò", 450, 20, 15, 50));
        list.add(new FoodModel("Phở gà", 400, 18, 12, 45));
        list.add(new FoodModel("Cơm tấm sườn", 600, 25, 20, 70));
        list.add(new FoodModel("Bánh mì thịt", 350, 12, 15, 40));
        list.add(new FoodModel("Bún bò Huế", 500, 22, 18, 60));
        list.add(new FoodModel("Bún chả", 450, 15, 20, 50));
        list.add(new FoodModel("Gỏi cuốn (1 cuốn)", 50, 3, 1, 8));
        list.add(new FoodModel("Cơm chiên dương châu", 550, 15, 20, 75));
        list.add(new FoodModel("Hủ tiếu Nam Vang", 400, 15, 12, 55));
        list.add(new FoodModel("Bánh xèo", 350, 10, 15, 35));
        list.add(new FoodModel("Bún riêu", 400, 15, 12, 50));
        list.add(new FoodModel("Bánh cuốn", 300, 8, 10, 45));
        list.add(new FoodModel("Xôi mặn", 450, 10, 15, 65));
        list.add(new FoodModel("Cá kho tộ", 250, 20, 10, 5));
        list.add(new FoodModel("Canh chua cá lóc", 150, 15, 5, 10));
        list.add(new FoodModel("Thịt kho hột vịt", 400, 20, 25, 5));
        list.add(new FoodModel("Rau muống xào tỏi", 100, 3, 7, 5));

        return list;
    }
}
