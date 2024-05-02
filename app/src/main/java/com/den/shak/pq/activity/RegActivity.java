package com.den.shak.pq.activity;


import static com.den.shak.pq.cloud.SetUser.setUser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;

import com.den.shak.pq.models.Order;
import com.den.shak.pq.models.User;
import com.den.shak.pq.R;
import com.den.shak.pq.cloud.SetUser;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.UUID;

public class RegActivity extends androidx.appcompat.app.AppCompatActivity implements SetUser.SetUserCallback {
    // Объявление переменных класса
    private String phone;
    private TextInputLayout nameEditTextLayout;
    private EditText nameEditText;
    private MaterialSwitch materialSwitch;
    private boolean exitRequested = false;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_activity);

        // Получение экземпляра SharedPreferences для сохранения данных пользователя
        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        // Получение переданного из предыдущей активности телефонного номера
        Intent intent = getIntent();
        phone = (String) intent.getSerializableExtra("phone");

        // Инициализация элементов пользовательского интерфейса
        materialSwitch = findViewById(R.id.is_performer_switch);
        nameEditTextLayout = findViewById(R.id.name_input_layout);
        nameEditText = findViewById(R.id.name_input);

        // Установка обработчика для кнопки "Назад"
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Обработка нажатия кнопки "Назад"
                if (exitRequested) {
                    // Завершение приложения, если пользователь уже запросил выход
                    finishAffinity();
                } else {
                    // Запрос на подтверждение выхода
                    exitRequested = true;
                    View rootView = findViewById(android.R.id.content);
                    Snackbar.make(rootView, R.string.press_to_exit_text, Snackbar.LENGTH_SHORT)
                            .show();

                    // Сброс запроса на выход через некоторое время (например, через 2 секунды)
                    new android.os.Handler().postDelayed(() -> exitRequested = false, 2000); // 2 секунды
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Установка обработчика для переключателя
        switchChecker();
    }

    // Метод для установки значений иконки при изменении состояния переключателя
    private void switchChecker() {
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                materialSwitch.setThumbIconResource(R.drawable.ic_done);
            } else {
                materialSwitch.setThumbIconDrawable(null);
            }
        });
    }

    // Обработчик клика по кнопке справки
    public void onClickHelp(View view) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.reg_activity_help);
        builder.setMessage(R.string.reg_activity_help_text);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // Обработчик клика по кнопке регистрации
    public void onClickRegistration(View view) {
        // Отключение кнопки регистрации, чтобы избежать повторных нажатий
        Button regButton = findViewById(R.id.reg_button);
        regButton.setEnabled(false);

        // Получение имени и значения переключателя
        String name = nameEditText.getText().toString();
        Boolean isPerformer = materialSwitch.isChecked();

        // Проверка заполненности поля с именем
        if (name.isEmpty()) {
            nameEditTextLayout.setError(getString(R.string.reg_activity_user_name_error));
        } else {
            // Создание нового объекта пользователя и запись его данных в базу данных
            User user = new User();
            UUID uuid = UUID.randomUUID();
            user.setId(uuid.toString());
            user.setPhone(phone);
            user.setName(name);
            user.setPerformer(isPerformer);

            // Создание нового потока для выполнения операции записи в БД
            Thread thread = new Thread(() -> runOnUiThread(() -> setUser(user, RegActivity.this, RegActivity.this)));
            thread.start();
        }
    }

    // Метод обратного вызова, вызываемый после завершения операции записи пользователя в БД
    @Override
    public void onSetUserResult() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("id", user.getId());
        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra("order", order);
        startActivity(intent);
    }
}
