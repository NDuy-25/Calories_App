package com.duy.project_cuoiki_calories;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.duy.project_cuoiki_calories.databinding.ActivityRegisterBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

            if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnRegister.setEnabled(false);
            binding.btnRegister.setText("ĐANG XỬ LÝ...");

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Gửi email xác thực
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            binding.btnRegister.setEnabled(true);
                                            binding.btnRegister.setText("ĐĂNG KÝ");

                                            if (verifyTask.isSuccessful()) {
                                                // Hiện thông báo thành công và yêu cầu xác thực
                                                new MaterialAlertDialogBuilder(RegisterActivity.this)
                                                        .setTitle("Đăng ký thành công")
                                                        .setMessage("Link xác nhận đã được gửi vào Gmail: " + email + "\n\nVui lòng kiểm tra hộp thư (bao gồm cả thư rác/spam) và nhấn vào link để kích hoạt tài khoản.")
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK, ĐÃ HIỂU", (dialog, which) -> {
                                                            mAuth.signOut(); // Đăng xuất để yêu cầu xác thực ở màn hình Login
                                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                            finish();
                                                        })
                                                        .show();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Lỗi gửi mail: " + verifyTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        } else {
                            binding.btnRegister.setEnabled(true);
                            binding.btnRegister.setText("ĐĂNG KÝ");
                            Toast.makeText(RegisterActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
