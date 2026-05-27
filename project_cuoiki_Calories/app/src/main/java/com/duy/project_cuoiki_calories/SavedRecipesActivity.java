package com.duy.project_cuoiki_calories;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.duy.project_cuoiki_calories.adapters.SavedRecipesAdapter;
import com.duy.project_cuoiki_calories.databinding.ActivitySavedRecipesBinding;
import com.duy.project_cuoiki_calories.models.SavedRecipeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SavedRecipesActivity extends AppCompatActivity {

    private ActivitySavedRecipesBinding binding;
    private SavedRecipesAdapter adapter;
    private List<SavedRecipeModel> recipeList;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        recipeList = new ArrayList<>();
        adapter = new SavedRecipesAdapter(recipeList, this::deleteRecipe);

        binding.rvSavedRecipes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSavedRecipes.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());

        loadSavedRecipes();
    }

    private void loadSavedRecipes() {
        if (userId == null) return;

        db.collection("users").document(userId).collection("saved_recipes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        SavedRecipeModel recipe = new SavedRecipeModel(
                                doc.getId(),
                                doc.getString("content"),
                                doc.getLong("timestamp")
                        );
                        recipeList.add(recipe);
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (recipeList.isEmpty()) {
                        binding.tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoData.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteRecipe(SavedRecipeModel recipe, int position) {
        db.collection("users").document(userId).collection("saved_recipes")
                .document(recipe.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    recipeList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Đã xóa thực đơn", Toast.LENGTH_SHORT).show();
                    
                    if (recipeList.isEmpty()) {
                        binding.tvNoData.setVisibility(View.VISIBLE);
                    }
                });
    }
}
