package com.den.shak.pricequestion.activity;

import static com.den.shak.pricequestion.cloud.GetUser.getUserData;
import static com.den.shak.pricequestion.cloud.VoiceAuth.sendVoiceAuth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.den.shak.pricequestion.R;
import com.den.shak.pricequestion.cloud.GetUser;
import com.den.shak.pricequestion.cloud.VoiceAuth;
import com.den.shak.pricequestion.models.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AuthActivity extends AppCompatActivity implements VoiceAuth.VoiceAuthCallback, GetUser.GetUserCallback {
    private String auth_code = "";
    private String phone;

    private boolean exitRequested = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);

        // Получение экземпляра SharedPreferences для сохранения данных пользователя
        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        // Проверка наличия сохранённого ID пользователя в SharedPreferences
        String preferencesUserID = sharedPreferences.getString("id", "");
        if (!preferencesUserID.isEmpty()) {
            // Если ID сохранён, получаем данные пользователя
            Thread thread = new Thread(() -> runOnUiThread(() -> getUserData(preferencesUserID, null, AuthActivity.this, AuthActivity.this)));
            thread.start();
        } else {
            // Если ID пользователя не сохранён, отображаем форму ввода номера телефона
            LinearLayout layout_wait = findViewById(R.id.auth_layout_wait);
            layout_wait.setVisibility(View.GONE);
            LinearLayout layout_phone = findViewById(R.id.auth_layout_phone);
            layout_phone.setVisibility(View.VISIBLE);
        }

        // Настройка обработчика изменений в поле ввода номера телефона
        EditText phoneText = findViewById(R.id.phone_input);
        phoneText.addTextChangedListener(new TextWatcher()
                // Методы TextWatcher, которые реализуют обработку изменений в поле ввода
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s)
            {
                // Обработка введенного текста для форматирования номера телефона
                String text = phoneText.getText().toString();
                int  textLength = phoneText.getText().length();
                if (text.endsWith("-") || text.endsWith(" ") && textLength >= 3)
                    return;
                if (textLength < 3) {
                    phoneText.setText("+7 ");
                    phoneText.setSelection(phoneText.getText().length());
                }
                if (textLength == 4) {
                    if (!text.contains("("))
                    {
                        phoneText.setText(new StringBuilder(text).insert(text.length() - 1, "(").toString());
                        phoneText.setSelection(phoneText.getText().length());
                    }
                }
                else if (textLength == 8)
                {
                    if (!text.contains(")"))
                    {
                        phoneText.setText(new StringBuilder(text).insert(text.length() - 1, ")").toString());
                        phoneText.setSelection(phoneText.getText().length());
                    }
                }
                else if (textLength == 9)
                {
                    phoneText.setText(new StringBuilder(text).insert(text.length() - 1, " ").toString());
                    phoneText.setSelection(phoneText.getText().length());
                }
                else if (textLength == 13)
                {
                    if (!text.contains("-"))
                    {
                        phoneText.setText(new StringBuilder(text).insert(text.length() - 1, "-").toString());
                        phoneText.setSelection(phoneText.getText().length());
                    }
                }
                else if (textLength == 18)
                {
                    if (text.contains("-"))
                    {
                        phoneText.setText(new StringBuilder(text).insert(text.length() - 1, "-").toString());
                        phoneText.setSelection(phoneText.getText().length());
                    }
                }
                else if (textLength == 21)
                {
                    if (text.contains("-"))
                    {
                        phoneText.setText(new StringBuilder(text).insert(text.length() - 1, "-").toString());
                        phoneText.setSelection(phoneText.getText().length());
                    }
                }
            }
        });

        // Настройка обработчика нажатия кнопки "Назад"
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Обработка нажатия кнопки "Назад"
                if (exitRequested) {
                    // Если пользователь уже запросил выход, завершить приложение
                    finishAffinity();
                } else {
                    // Запросить подтверждение выхода
                    exitRequested = true;
                    View rootView = findViewById(android.R.id.content);
                    Snackbar.make(rootView, "Нажмите еще раз для выхода", Snackbar.LENGTH_SHORT)
                            .show();

                    // Через какое-то время сбросьте состояние запроса на выход (например, через 2 секунды)
                    new android.os.Handler().postDelayed(() -> exitRequested = false, 2000); // Задержка в миллисекундах (2 секунды)
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    // Метод для обработки нажатия кнопки "Войти"
    public void onClickLogIn(View view) {
        // Получение номера телефона из поля ввода и удаление лишних символов
        Button login_button_1 = findViewById(R.id.login_button_1);
        login_button_1.setEnabled(false);
        TextInputEditText inputPhone = findViewById(R.id.phone_input);
        phone = String.valueOf(inputPhone.getText()).replaceAll("[\\s\\-+()]", "");

        // Создаём новый поток для выполнения кода совершения звонка
        Thread thread = new Thread(() -> runOnUiThread(() -> sendVoiceAuth(phone, AuthActivity.this, AuthActivity.this)));
        thread.start();
    }
    // Метод для обработки нажатия кнопки "Войти"(Проверить код)
    public void onClickCheckCode(View view) {
        TextInputEditText inputCode = findViewById(R.id.code_input);
        final String input_code = String.valueOf(inputCode.getText());
        if (input_code.equals(auth_code)) {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("phone", phone);
            editor.apply();

            // Создаём новый поток для выполнения кода получения данных пользователя
            Thread thread = new Thread(() -> runOnUiThread(() -> getUserData(null, phone, AuthActivity.this, AuthActivity.this)));
            thread.start();
        } else {
            TextInputLayout codeLayout = findViewById(R.id.code_layout);
            codeLayout.setError("Неверный код! Повторите попытку!");

        }
    }

    @Override
    public void onVoiceAuthResult(final String status, final String code) {
        runOnUiThread(() -> handleVoiceAuthResult(status, code));
    }

    private void handleVoiceAuthResult(String status, String code) {
        // Обработайте результаты авторизации по звонку здесь
        if ("OK".equals(status)) {
            // Звонок отправлен успешно
            Log.d("YourActivity", "Звонок отправлен успешно. Код: " + code);
            auth_code = code;
            TextView call_text = findViewById(R.id.call_text);
            String formattedNumber = phone;
            if (formattedNumber.length() == 11) {
                formattedNumber = "+" + formattedNumber.charAt(0) + " (" +
                        formattedNumber.substring(1, 4) + ") " +
                        formattedNumber.substring(4, 7) + " " +
                        formattedNumber.substring(7, 9) + " " +
                        formattedNumber.substring(9);
            }
            call_text.setText(String.format("%s%s.", getString(R.string.auth_activity_robot_wait_with_phone), formattedNumber));
            LinearLayout layout_phone = findViewById(R.id.auth_layout_phone);
            layout_phone.setVisibility(View.GONE);
            LinearLayout layout_code = findViewById(R.id.auth_layout_code);
            layout_code.setVisibility(View.VISIBLE);
        } else {
            // Произошла ошибка при отправке звонка
            Log.e("YourActivity", "Ошибка при отправке звонка. Статус: " + status);
            TextInputLayout phoneLayout = findViewById(R.id.phone);
            phoneLayout.setError("Ошибка! Проверьте введенный номер!");
            Button login_button_1 = findViewById(R.id.login_button_1);
            login_button_1.setEnabled(true);
        }
    }

    @Override
    public void onGetUserResult(User user) {
        runOnUiThread(() -> handleGetUserResult(user));
    }

    private void handleGetUserResult(User user) {
        if (user != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("id", user.getId());
            editor.apply();
            Log.d("GetUserResult", "+++");
        }
        //TODO Если пользователь не null, то переходим на главный экран, иначе к регистрации
    }
}

