package com.den.shak.pq.activity;

import static com.den.shak.pq.ConfigReader.setMapKitApiKey;
import static com.den.shak.pq.activity.MapActivity.mapInitialized;
import static com.den.shak.pq.fragments.ListAdvertFragment.isFiltered;
import static com.den.shak.pq.fragments.ListAdvertFragment.ordersList;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.den.shak.pq.R;
import com.den.shak.pq.models.Order;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.VisibleRegionUtils;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session.SearchListener;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
public class MapSearchActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean userLocationSet = false;
    private MapView mapView;
    private Map map;
    private UserLocationLayer userLocationLayer;
    private MapObjectTapListener tapListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация MapKit, если необходимо
        if (!mapInitialized) {
            initializeMapKit();
            mapInitialized = true;
        }
        // Затем устанавливаем макет активности
        setContentView(R.layout.map_activity);

        // Получаем ссылку на MapView из макета
        mapView = findViewById(R.id.map_view);

        // Создаем экземпляр Map и устанавливаем его на MapView
        map = mapView.getMapWindow().getMap();

        // Проверка разрешения на местоположение
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Запрос разрешения, если оно не предоставлено
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Разрешение уже предоставлено, установка слоя пользовательского местоположения
            setUserLocationLayer();

        }

        // Настройка поиска
        setSearch();
        setMarkers();

        TextView toolbarTitle = findViewById(R.id.map_toolbar_text);
        toolbarTitle.setText("Выберите маркер");

        if (isFiltered) {
            MaterialCardView mapFilterCard = findViewById(R.id.map_filter_card);
            mapFilterCard.setVisibility(View.VISIBLE);
        }
    }

    @Override
    // Запуск MapKit при входе в активность
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    // Остановка MapKit при выходе из активности
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
    // Обработка ответа на запрос разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, установка слоя пользовательского местоположения
                setUserLocationLayer();
            }
        }
    }

    private void setMarkers() {
                    // Добавляем новый обработчик нажатия на маркер
        tapListener = (mapObject, point) -> {
                Order order = (Order) mapObject.getUserData();
                // Создаем диалоговое окно при нажатии на маркер
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                assert order != null;
                List<String> categories = Arrays.asList(getResources().getStringArray(R.array.category));
                builder.setTitle(categories.get(order.getCategoryID()));
                if (order.getPrice() != null) {
                    builder.setMessage(order.getTitle() + "\nОплата за выполнене: " + order.getPrice() + " руб.");
                } else {
                    builder.setMessage(order.getTitle());
                }

                builder.setPositiveButton("Открыть заявку", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(this, OrderActivity.class);
                    intent.putExtra("order_id", order.getId());
                    // Если необходимо, передайте информацию о заказе через Intent
                    startActivity(intent);
                });
                // Добавляем кнопку "Закрыть"
                builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

                // Показываем диалоговое окно
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);

                // Возвращаем true, чтобы сообщить, что событие обработано
                return true;
            };
        for (Order order : ordersList) {
            Point point = new Point(order.getLocation().latitude, order.getLocation().longitude);
            PlacemarkMapObject mark = map.getMapObjects().addPlacemark();
            mark.setGeometry(point);
            ImageProvider icon = ImageProvider.fromResource(this, R.drawable.marker);
            mark.setIcon(icon);
            mark.setIconStyle(new IconStyle().setScale(0.5f));

            mark.setUserData(order);
            mark.addTapListener(tapListener);
        }
    }

    // Обработка нажатия на кнопку "Назад"
    public void onClickBack(View view) {
        // Закрытие активности
        finish();
    }

    // Установка слоя пользовательского местоположения
    private void setUserLocationLayer() {
        // Включение отображения пользовательского местоположения
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(false);

        // Создание слушателя для отслеживания изменений состояния местоположения пользователя
        UserLocationObjectListener userLocationListener = new UserLocationObjectListener() {
            @Override
            public void onObjectAdded(@NonNull UserLocationView userLocationView) {
                if (userLocationLayer.cameraPosition() != null && !userLocationSet) {
                    onClickMyLocation(null);
                    userLocationSet = true;
                }
            }

            @Override
            public void onObjectRemoved(@NonNull UserLocationView userLocationView) {
            }

            @Override
            public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
                // Обработка изменения состояния местоположения пользователя
                if (userLocationLayer.cameraPosition() != null && !userLocationSet) {
                    onClickMyLocation(null);
                    userLocationSet = true;
                }
            }
        };

        // Устанавливаем слушатель для слоя местоположения пользователя
        userLocationLayer.setObjectListener(userLocationListener);

    }

    // Инициализация MapKit
    private void initializeMapKit() {
        // Сначала инициализируем Yandex MapKit и устанавливаем API-ключ
        MapKitFactory.setApiKey(Objects.requireNonNull(setMapKitApiKey(this)));
        MapKitFactory.initialize(this);
    }

    // Обработка нажатия на кнопку "Приблизить"
    public void onClickZoomIn(View view) {
        map.move(new CameraPosition(map.getCameraPosition().getTarget(), map.getCameraPosition().getZoom() + 1,
                        map.getCameraPosition().getAzimuth(), map.getCameraPosition().getTilt()),
                new Animation(Animation.Type.SMOOTH, 1),
                null);
    }
    // Обработка нажатия на кнопку "Отдалить"
    public void onClickZoomOut(View view) {
        map.move(new CameraPosition(map.getCameraPosition().getTarget(), map.getCameraPosition().getZoom() - 1,
                        map.getCameraPosition().getAzimuth(), map.getCameraPosition().getTilt()),
                new Animation(Animation.Type.SMOOTH, 1),
                null);
    }

    // Обработка нажатия на кнопку "Мое местоположение"
    public void onClickMyLocation(View view) {
        if (userLocationLayer != null && userLocationLayer.cameraPosition() != null) {
            map.move(
                    new CameraPosition(
                            Objects.requireNonNull(userLocationLayer.cameraPosition()).getTarget(),
                            17.0f,
                            map.getCameraPosition().getAzimuth(),
                            map.getCameraPosition().getTilt()),
                    new Animation(Animation.Type.SMOOTH, 1),
                    null);
        }
    }

    // Настройка поиска
    private void setSearch() {
        SearchManager searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        // Определение параметров поиска
        SearchOptions searchOptions = new SearchOptions();
        searchOptions.setResultPageSize(1);
        // Слушатель для результатов поиска
        SearchListener searchSessionListener = new SearchListener() {
            @Override
            // Перемещение карты к найденному объекту
            public void onSearchResponse(@NonNull Response response) {
                map.move(
                        new CameraPosition(
                                Objects.requireNonNull(Objects.requireNonNull(response.getCollection().getChildren().get(0).getObj()).getGeometry().get(0).getPoint()),
                                16.0f,
                                map.getCameraPosition().getAzimuth(),
                                map.getCameraPosition().getTilt()),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null);

                // Скрытие клавиатуры
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // Получение текущего фокуса ввода
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    // Скрытие клавиатуру для текущего фокуса ввода
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }
            }

            @Override
            public void onSearchError(@NonNull Error error) {
                // Обработка ошибки поиска
            }
        };

        TextInputEditText search_text = findViewById(R.id.map_search_text);
        // Задержка перед отправкой запроса поиска для уменьшения нагрузки
        final Handler search_handler = new Handler();
        final Runnable search_runnable = () -> searchManager.submit(
                Objects.requireNonNull(search_text.getText()).toString(),
                VisibleRegionUtils.toPolygon(map.getVisibleRegion()),
                searchOptions,
                searchSessionListener
        );

        // Слушатель изменений в тексте поиска
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search_handler.removeCallbacks(search_runnable);
                search_handler.postDelayed(search_runnable, 2000); // 2000 миллисекунд = 2 секунды
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}

