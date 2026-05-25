package com.duy.project_cuoiki_calories;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.duy.project_cuoiki_calories.databinding.ActivityLoginBinding;
import com.duy.project_cuoiki_calories.databinding.DialogForgotPasswordBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra tự động đăng nhập - Chỉ vào nếu email đã được xác minh
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (currentUser.isEmailVerified()) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            });
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

            // Giai đoạn 2: Gọi Firebase
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText("ĐĂNG NHẬP");
                        
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    // Đã xác thực -> Vào app
                                    Log.d("LoginSuccess", "Đăng nhập thành công");
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    // Chưa xác thực -> Hiện thông báo và cho phép gửi lại link
                                    showVerificationWarningDialog(user);
                                }
                            }
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Lỗi kết nối";
                            Log.e("LoginError", error);
                            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Xử lý sự kiện Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void showVerificationWarningDialog(FirebaseUser user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác thực tài khoản")
                .setMessage("Tài khoản của bạn chưa được kích hoạt qua Gmail. Vui lòng kiểm tra hộp thư (bao gồm cả Thư rác/Spam).\n\nBạn có muốn gửi lại link xác nhận không?")
                .setCancelable(false)
                .setPositiveButton("GỬI LẠI LINK", (dialog, which) -> {
                    user.sendEmailVerification().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đã gửi lại link xác thực thành công!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        mAuth.signOut();
                    });
                })
                .setNegativeButton("ĐÓNG", (dialog, which) -> {
                    mAuth.signOut();
                    dialog.dismiss();
                })
                .show();
    }

    private void showForgotPasswordDialog() {
        DialogForgotPasswordBinding dialogBinding = DialogForgotPasswordBinding.inflate(getLayoutInflater());
        
        // Sử dụng MaterialAlertDialogBuilder để có giao diện Material 3 đồng bộ với app
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("GỬI LINK", null) // Handle manually to prevent auto-dismiss
                .setNegativeButton("HỦY", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            // Tùy chỉnh màu sắc nút bấm cho nổi bật (Màu Neon Cyan của app)
            int primaryColor = ContextCompat.getColor(this, R.color.primary);
            int secondaryColor = ContextCompat.getColor(this, R.color.text_secondary);
            
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(primaryColor);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(secondaryColor);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String email = dialogBinding.etResetEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    dialogBinding.etResetEmail.setError("Vui lòng nhập email");
                    return;
                }

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Đã gửi link khôi phục mật khẩu! Vui lòng kiểm tra email của bạn.", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            } else {
                                String error = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                                Toast.makeText(LoginActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        });

        dialog.show();
    }
}
