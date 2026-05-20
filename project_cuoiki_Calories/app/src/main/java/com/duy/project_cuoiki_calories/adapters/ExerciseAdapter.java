package com.duy.project_cuoiki_calories.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.duy.project_cuoiki_calories.databinding.ItemExerciseBinding;
import com.duy.project_cuoiki_calories.models.ExerciseModel;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<ExerciseModel> exercises;
    private OnDurationChangeListener listener;

    public interface OnDurationChangeListener {
        void onDurationChanged();
    }

    public ExerciseAdapter(List<ExerciseModel> exercises, OnDurationChangeListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseBinding binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ExerciseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseModel exercise = exercises.get(position);
        holder.binding.tvExerciseName.setText(exercise.getName());
        holder.binding.tvExerciseInfo.setText(String.format("MET: %.1f", exercise.getMetValue()));

        updateDurationViews(holder, exercise);

        holder.binding.btnPlus.setOnClickListener(v -> {
            // Increment by 5 minutes for convenience
            exercise.setQuantity(exercise.getQuantity() + 5);
            updateDurationViews(holder, exercise);
            if (listener != null) listener.onDurationChanged();
        });

        holder.binding.btnMinus.setOnClickListener(v -> {
            if (exercise.getQuantity() > 0) {
                exercise.setQuantity(Math.max(0, exercise.getQuantity() - 5));
                updateDurationViews(holder, exercise);
                if (listener != null) listener.onDurationChanged();
            }
        });
    }

    private void updateDurationViews(ExerciseViewHolder holder, ExerciseModel exercise) {
        int duration = exercise.getQuantity();
        holder.binding.tvDuration.setText(duration + "m");

        if (duration > 0) {
            holder.binding.btnMinus.setVisibility(View.VISIBLE);
            holder.binding.tvDuration.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnMinus.setVisibility(View.GONE);
            holder.binding.tvDuration.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ItemExerciseBinding binding;
        ExerciseViewHolder(ItemExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
