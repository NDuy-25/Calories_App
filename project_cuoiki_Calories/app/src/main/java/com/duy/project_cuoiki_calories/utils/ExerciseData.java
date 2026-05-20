package com.duy.project_cuoiki_calories.utils;

import com.duy.project_cuoiki_calories.models.ExerciseModel;

import java.util.ArrayList;
import java.util.List;

public class ExerciseData {
    public static List<ExerciseModel> getExerciseList() {
        List<ExerciseModel> list = new ArrayList<>();
        list.add(new ExerciseModel("Đi bộ (chậm)", 2.0));
        list.add(new ExerciseModel("Đi bộ (vừa)", 3.5));
        list.add(new ExerciseModel("Đi bộ (nhanh)", 4.5));
        list.add(new ExerciseModel("Chạy bộ (chậm)", 6.0));
        list.add(new ExerciseModel("Chạy bộ (vừa)", 8.0));
        list.add(new ExerciseModel("Chạy bộ (nhanh)", 11.5));
        list.add(new ExerciseModel("Đạp xe (thong thả)", 4.0));
        list.add(new ExerciseModel("Đạp xe (vừa)", 8.0));
        list.add(new ExerciseModel("Đạp xe (nhanh)", 12.0));
        list.add(new ExerciseModel("Bơi lội (vừa)", 6.0));
        list.add(new ExerciseModel("Bơi lội (nhanh)", 10.0));
        list.add(new ExerciseModel("Nhảy dây", 11.0));
        list.add(new ExerciseModel("Yoga", 2.5));
        list.add(new ExerciseModel("Aerobics", 6.5));
        list.add(new ExerciseModel("Gym (tập tạ)", 5.0));
        list.add(new ExerciseModel("Bóng đá", 8.0));
        list.add(new ExerciseModel("Cầu lông", 4.5));
        list.add(new ExerciseModel("Bóng rổ", 6.0));
        return list;
    }
}
