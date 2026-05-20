package com.duy.project_cuoiki_calories;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister, tvForgotPassword;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đủ Email và Mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    kiemTraThongTin();
                                } else {
                                    // Hiển thị thông báo và nút gửi lại email
                                    showResendEmailDialog(user);
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    // Hàm hiển thị khi chưa xác thực email, cho phép gửi lại
    private void showResendEmailDialog(FirebaseUser user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Email chưa được xác thực")
                .setMessage("Bạn cần nhấn vào link trong Email để đăng nhập. Bạn có muốn chúng tôi gửi lại email xác thực mới không?")
                .setPositiveButton("Gửi lại Email", (dialog, which) -> {
                    if (user != null) {
                        user.sendEmailVerification().addOnSuccessListener(aVoid -> {
                            Toast.makeText(LoginActivity.this, "Đã gửi lại Email xác thực. Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(LoginActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                    mAuth.signOut();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    mAuth.signOut();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showForgotPasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText etResetEmail = view.findViewById(R.id.etResetEmail);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Quên mật khẩu?")
                .setMessage("Nhập Email để nhận liên kết đặt lại mật khẩu.")
                .setView(view)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String mail = etResetEmail.getText().toString().trim();
                    if (mail.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "Vui lòng nhập Email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(aVoid -> {
                        Toast.makeText(LoginActivity.this, "Liên kết đặt lại mật khẩu đã được gửi về Email của bạn.", Toast.LENGTH_LONG).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void kiemTraThongTin() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, UserInfoActivity.class));
            }
            finish();
        });
    }
}
