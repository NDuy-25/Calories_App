package com.duy.project_cuoiki_calories;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.duy.project_cuoiki_calories.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();
            String confirmPass = binding.etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnRegister.setEnabled(false);
            binding.btnRegister.setText("ĐANG TẠO TÀI KHOẢN...");

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        binding.btnRegister.setEnabled(true);
                        binding.btnRegister.setText("ĐĂNG KÝ");
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            // Vào thẳng màn hình chính
                            startActivity(new Intent(RegisterActivity.this, UserInfoActivity.class));
                            finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Lỗi đăng ký";
                            Toast.makeText(RegisterActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
