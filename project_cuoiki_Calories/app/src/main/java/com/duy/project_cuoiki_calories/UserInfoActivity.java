package com.duy.project_cuoiki_calories;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.duy.project_cuoiki_calories.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserInfoActivity extends AppCompatActivity {

    // 1. Khai báo các thành phần trên giao diện
    TextView tvTitle;
    EditText etAge, etHeight, etWeight;
    RadioButton rbMale, rbFemale, rbLoseWeight, rbMaintainWeight, rbGainWeight;
    Button btnSave;

    // Các công cụ Firebase
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    
    // Biến để kiểm tra xem là đăng ký mới hay chỉ là cập nhật cân nặng thứ 2
    boolean laCapNhatThuHai = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // 2. Ánh xạ (Tìm các thành phần trong file XML)
        tvTitle = findViewById(R.id.tvTitle);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        
        rbLoseWeight = findViewById(R.id.rbLoseWeight);
        rbMaintainWeight = findViewById(R.id.rbMaintainWeight);
        rbGainWeight = findViewById(R.id.rbGainWeight);
        
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra xem MainActivity có gửi yêu cầu cập nhật thứ 2 không
        laCapNhatThuHai = getIntent().getBooleanExtra("IS_MONDAY_UPDATE", false);

        if (laCapNhatThuHai == true) {
            // Nếu chỉ cập nhật cân nặng thì ẩn bớt các ô khác đi cho gọn
            tvTitle.setText("Cập nhật cân nặng hôm nay");
            etAge.setVisibility(View.GONE);
            etHeight.setVisibility(View.GONE);
            findViewById(R.id.rgGender).setVisibility(View.GONE);
            findViewById(R.id.rgGoal).setVisibility(View.GONE);
        }

        // 3. Lập trình nút Lưu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                luuDuLieuLenFirebase();
            }
        });
    }

    private void luuDuLieuLenFirebase() {
        String userId = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        if (laCapNhatThuHai == true) {
            // Trường hợp A: Chỉ cập nhật cân nặng vào thứ Hai
            String sCanNang = etWeight.getText().toString();
            if (sCanNang.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập cân nặng mới", Toast.LENGTH_SHORT).show();
                return;
            }
            double canNangMoi = Double.parseDouble(sCanNang);
            
            db.collection("users").document(userId)
                .update("weight", canNangMoi, "lastWeightUpdate", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật cân nặng!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình chính
                });

        } else {
            // Trường hợp B: Đăng ký thông tin lần đầu tiên
            String sTuoi = etAge.getText().toString();
            String sCao = etHeight.getText().toString();
            String sNang = etWeight.getText().toString();

            if (sTuoi.isEmpty() || sCao.isEmpty() || sNang.isEmpty()) {
                Toast.makeText(this, "Hãy nhập đủ tuổi, chiều cao, cân nặng", Toast.LENGTH_SHORT).show();
                return;
            }

            int tuoi = Integer.parseInt(sTuoi);
            double cao = Double.parseDouble(sCao);
            double nang = Double.parseDouble(sNang);
            
            // Xác định giới tính
            String gioiTinh = "Nam";
            if (rbFemale.isChecked()) gioiTinh = "Nữ";
            
            // Xác định mục tiêu
            String mucTieu = "Duy trì";
            if (rbLoseWeight.isChecked()) mucTieu = "Giảm cân";
            else if (rbGainWeight.isChecked()) mucTieu = "Tăng cân";

            // TÍNH TOÁN CALORIE HÀNG NGÀY (Công thức BMR)
            double bmr;
            if (gioiTinh.equals("Nam")) {
                bmr = (10 * nang) + (6.25 * cao) - (5 * tuoi) + 5;
            } else {
                bmr = (10 * nang) + (6.25 * cao) - (5 * tuoi) - 161;
            }

            double calorieNgay = bmr * 1.2; // Giả sử lười vận động (hệ số 1.2)
            if (mucTieu.equals("Giảm cân")) calorieNgay -= 500;
            else if (mucTieu.equals("Tăng cân")) calorieNgay += 500;

            // Tạo đối tượng User để lưu
            User nguoiDung = new User(userId, email, gioiTinh, tuoi, cao, nang, mucTieu, calorieNgay);

            // Lưu vào Firestore
            db.collection("users").document(userId).set(nguoiDung.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserInfoActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }
}
