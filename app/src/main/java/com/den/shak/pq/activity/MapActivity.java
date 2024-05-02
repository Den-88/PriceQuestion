package com.den.shak.pq.activity;

import static com.den.shak.pq.ConfigReader.setMapKitApiKey;
import static com.den.shak.pq.fragments.NewAdvert.order;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.den.shak.pq.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
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

import java.util.Objects;

public class MapActivity extends AppCompatActivity implements InputListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static boolean mapInitialized = false;
    private boolean userLocationSet = false;
    private MapView mapView;
    private Map map;
    private UserLocationLayer userLocationLayer;
    private PlacemarkMapObject mark;

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

        // Устанавливаем слушатель нажатий на карту
        map.addInputListener(this);

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

        // Инициализация маркера
        mark = map.getMapObjects().addPlacemark();

        // Настройка поиска
        setSearch();
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

    // Обработка нажатия на карту
    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        // Установка маркера на точку нажатия
        mark.setGeometry(point);
        ImageProvider icon = ImageProvider.fromResource(this, R.drawable.marker);
        mark.setIcon(icon);
        mark.setIconStyle(
                new IconStyle().setScale(0.5f)
        );

        // Установка местоположения заказа
        order.setLocation(new LatLng(point.getLatitude(), point.getLongitude()));
        Button done_button = findViewById(R.id.map_done_button);
        done_button.setVisibility(View.VISIBLE);
    }
    // Обработка долгого нажатия на карту
    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
    }

    // Обработка нажатия на кнопку "Назад"
    public void onClickBack(View view) {
        // Закрытие активности
        finish();
    }
    // Обработка нажатия на кнопку "Готово"
    public void onClickDone(View view) {
        order.setLocation(new LatLng(mark.getGeometry().getLatitude(), mark.getGeometry().getLatitude()));
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
