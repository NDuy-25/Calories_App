package com.duy.project_cuoiki_calories;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    // Khai báo các ô nhập và nút bấm
    EditText etEmail, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvLogin;

    // Công cụ đăng ký của Firebase
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Ánh xạ giao diện (Tìm ID trong file XML)
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        mAuth = FirebaseAuth.getInstance();

        // 2. Lập trình nút Đăng Ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();
                String confirm = etConfirmPassword.getText().toString().trim();

                // Kiểm tra xem đã nhập đủ chưa
                if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra mật khẩu có giống nhau không
                if (!pass.equals(confirm)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra độ dài mật khẩu (Firebase yêu cầu ít nhất 6 ký tự)
                if (pass.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Thực hiện tạo tài khoản trên Firebase
                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Gửi email xác thực
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                if (verifyTask.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, 
                                                            "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản trước khi đăng nhập.", 
                                                            Toast.LENGTH_LONG).show();
                                                    
                                                    // Đăng xuất ngay để bắt người dùng phải xác thực mới cho vào
                                                    mAuth.signOut();
                                                    
                                                    // Quay lại màn hình Login
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, 
                                                            "Lỗi gửi email xác thực: " + verifyTask.getException().getMessage(), 
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "Lỗi đăng ký: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // 3. Nhấn vào chữ "Đã có tài khoản" thì quay lại Login
        tvLogin.setOnClickListener(v -> {
            finish(); // Đóng trang đăng ký để quay về trang trước đó (Login)
        });
    }
}
