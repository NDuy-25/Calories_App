package com.duy.project_cuoiki_calories.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.duy.project_cuoiki_calories.databinding.ItemFoodBinding;
import com.duy.project_cuoiki_calories.models.FoodModel;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodModel> foods;
    private OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public FoodAdapter(List<FoodModel> foods, OnQuantityChangeListener listener) {
        this.foods = foods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodBinding binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel food = foods.get(position);
        holder.binding.tvFoodName.setText(food.getName());
        holder.binding.tvFoodCalories.setText(String.format("%.1f kcal", food.getCalories()));

        updateQuantityViews(holder, food);

        holder.binding.btnPlus.setOnClickListener(v -> {
            food.setQuantity(food.getQuantity() + 1);
            updateQuantityViews(holder, food);
            if (listener != null) listener.onQuantityChanged();
        });

        holder.binding.btnMinus.setOnClickListener(v -> {
            if (food.getQuantity() > 0) {
                food.setQuantity(food.getQuantity() - 1);
                updateQuantityViews(holder, food);
                if (listener != null) listener.onQuantityChanged();
            }
        });
    }

    private void updateQuantityViews(FoodViewHolder holder, FoodModel food) {
        int qty = food.getQuantity();
        holder.binding.tvQuantity.setText(String.valueOf(qty));

        if (qty > 0) {
            holder.binding.btnMinus.setVisibility(View.VISIBLE);
            holder.binding.tvQuantity.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnMinus.setVisibility(View.GONE);
            holder.binding.tvQuantity.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return foods != null ? foods.size() : 0;
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ItemFoodBinding binding;
        FoodViewHolder(ItemFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
