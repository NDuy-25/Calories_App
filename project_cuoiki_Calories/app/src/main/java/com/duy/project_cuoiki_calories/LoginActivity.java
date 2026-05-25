package com.duy.project_cuoiki_calories;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.duy.project_cuoiki_calories.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Tự động vào nếu đã đăng nhập
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Giai đoạn 1: Chặn nút và hiển thị trạng thái
            binding.btnLogin.setEnabled(false);
            binding.btnLogin.setText("ĐANG XỬ LÝ...");

            // Cơ chế timeout: Sau 20 giây sẽ tự động nhả nút
            new Handler().postDelayed(() -> {
                if (!binding.btnLogin.isEnabled()) {
                    binding.btnLogin.setEnabled(true);
                    binding.btnLogin.setText("ĐĂNG NHẬP");
                    Toast.makeText(this, "Hết thời gian kết nối. Kiểm tra mạng hoặc Firebase Console!", Toast.LENGTH_LONG).show();
                }
            }, 20000);

            // Giai đoạn 2: Gọi Firebase
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText("ĐĂNG NHẬP");
                        
                        if (task.isSuccessful()) {
                            Log.d("LoginSuccess", "Đăng nhập thành công");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Lỗi kết nối";
                            Log.e("LoginError", error);
                            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
