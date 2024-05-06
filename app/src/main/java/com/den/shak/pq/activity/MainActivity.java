package com.den.shak.pq.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.den.shak.pq.R;
import com.den.shak.pq.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    // Флаг для отслеживания запроса на выход из приложения
    private boolean exitRequested = false;
    public static User current_user;


    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        current_user = (User) getIntent().getSerializableExtra("user");
        // Находим кнопку меню и назначаем обработчик нажатия
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(this::onClickMenu);

        // Находим нижнюю панель навигации и настраиваем навигацию
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        navController.setGraph(R.navigation.nav_graph);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Устанавливаем обработчик для пунктов нижней панели навигации
        bottomNavigationView.setOnItemSelectedListener(item -> {
            TextView toolbarText = findViewById(R.id.toolbar_text);
            int itemId = item.getItemId();
            if (itemId == R.id.list_advert_fragment) {
                // Переход к фрагменту списка заявок и обновление заголовка
                navController.navigate(R.id.action_to_list_advert_fragment);
                toolbarText.setText(R.string.list_advert_fragment_list_advert);
                return true;
            } else if (itemId == R.id.new_advert) {
                // Переход к фрагменту создания заявки и обновление заголовка
                navController.navigate(R.id.action_to_new_advert);
                toolbarText.setText(R.string.new_advert_fragment_new_advert);
                return true;
            } else if (itemId == R.id.chat) {
                // Переход к фрагменту чата и обновление заголовка
                navController.navigate(R.id.action_to_chat);
                toolbarText.setText(R.string.chat_fragment_chat);
                return true;
            }
            return false;
        });

        // Устанавливаем обработчик для кнопки "Назад"
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (exitRequested) {
                    // Завершение приложения, если пользователь уже запросил выход
                    finishAffinity();
                } else {
                    // Запрос на подтверждение выхода
                    exitRequested = true;
                    View rootView = findViewById(android.R.id.content);
                    // Отображение сообщения пользователю о необходимости повторного нажатия для выхода
                    Snackbar.make(rootView, R.string.press_to_exit_text, Snackbar.LENGTH_SHORT)
                            .show();
                    // Сброс запроса на выход через некоторое время (например, через 2 секунды)
                    new android.os.Handler().postDelayed(() -> exitRequested = false, 2000); // Устанавливаем таймер на сброс запроса выхода
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    // Обработчик нажатия на кнопку меню
    private void onClickMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_settings) {
                // Открываем активность настроек приложения
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.menu_about) {
                // Отображаем диалог "О приложении"
                showDialogAbout();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // Отображаем диалог "О приложении"
    private void showDialogAbout() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            // Создаем диалог с информацией о версии приложения и его разработчике
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.menu_about_app);
            builder.setMessage(getString(R.string.menu_version) + versionName + getString(R.string.menu_developer));
            builder.setPositiveButton(R.string.ok, null);
            builder.show();
        } catch (PackageManager.NameNotFoundException e) {
            // Обработка ошибки, если не удалось получить информацию о пакете
            Log.e("Error", "PackageManager exception", e);
        }
    }
}
