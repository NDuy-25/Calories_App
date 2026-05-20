package com.duy.project_cuoiki_calories;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Bước 1: Kiểm tra xem người dùng đã đăng nhập và xác thực email chưa
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null || !currentUser.isEmailVerified()) {
            // Nếu chưa đăng nhập hoặc chưa xác thực email, chuyển về màn hình Login
            if (currentUser != null && !currentUser.isEmailVerified()) {
                Toast.makeText(this, "Vui lòng xác thực Email trước!", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); 
            return;
        }

        // Bước 2: Thiết lập giao diện
        setContentView(R.layout.activity_main);
        
        // Bước 3: Cài đặt Menu điều hướng ở dưới (Bottom Navigation)
        setupMenu();

        // Bước 4: Kiểm tra xem hôm nay có phải thứ Hai để cập nhật cân nặng không
        kiemTraCapNhatThuHai();
    }

    private void setupMenu() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            
            if (bottomNav != null) {
                NavigationUI.setupWithNavController(bottomNav, navController);
            }
        }
    }

    private void kiemTraCapNhatThuHai() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long lastUpdate = documentSnapshot.getLong("lastWeightUpdate");
                
                if (lastUpdate != null) {
                    Calendar lichCu = Calendar.getInstance();
                    lichCu.setTimeInMillis(lastUpdate);

                    Calendar homNay = Calendar.getInstance();
                    
                    int thuMay = homNay.get(Calendar.DAY_OF_WEEK);
                    if (thuMay == Calendar.MONDAY) {
                        boolean laCungNgay = (homNay.get(Calendar.YEAR) == lichCu.get(Calendar.YEAR)) &&
                                           (homNay.get(Calendar.DAY_OF_YEAR) == lichCu.get(Calendar.DAY_OF_YEAR));
                        
                        if (!laCungNgay) {
                            Toast.makeText(MainActivity.this, "Đến thứ Hai rồi, cập nhật cân nặng thôi!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                            intent.putExtra("IS_MONDAY_UPDATE", true);
                            startActivity(intent);
                        }
                    }
                }
            } else {
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivity(intent);
            }
        });
    }
}
