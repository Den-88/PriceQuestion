package com.den.shak.pq.activity;

import static com.den.shak.pq.ConfigReader.setMapKitApiKey;
import static com.den.shak.pq.activity.MainActivity.current_user;
import static com.den.shak.pq.activity.MapActivity.mapInitialized;
import static com.den.shak.pq.cloud.DeleteOrder.deleteOrder;
import static com.den.shak.pq.cloud.GetOrders.getOrdersData;
import static com.den.shak.pq.cloud.GetPhotos.getPhotosData;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.den.shak.pq.adapters.PhotoAdapterURL;
import com.den.shak.pq.cloud.DeleteOrder;
import com.den.shak.pq.cloud.GetOrders;
import com.den.shak.pq.cloud.GetPhotos;
import com.den.shak.pq.cloud.SetResponse;
import com.den.shak.pq.models.Order;
import com.den.shak.pq.models.Response;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Определение класса OrderActivity, расширяющего AppCompatActivity
public class OrderActivity extends AppCompatActivity implements GetOrders.GetOrdersCallback, GetPhotos.GetPhotosCallback, DeleteOrder.DeleteOrderCallback, SetResponse.SetResponseCallback {
    private MapView mapView;
    private Map map;
    private Order order;
    private Button order_delete_button;
    AlertDialog responseDialog;

    // Переопределение метода onCreate, вызываемого при создании активности
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация MapKit, если необходимо
        if (!mapInitialized) {
            initializeMapKit();
            mapInitialized = true;
        }
        setContentView(R.layout.order_activity);

        // Получаем Intent, который вызвал эту активность
        Intent intent = getIntent();
        // Получаем значение идентификатора заказа из Intent
        String orderId = intent.getStringExtra("order_id");

        // Получение данных о заказах и фотографиях
        getOrdersData(null,orderId, null, null, OrderActivity.this, this);
        getPhotosData(orderId, OrderActivity.this, this);

        // Получаем ссылку на MapView из макета
        mapView = findViewById(R.id.order_map);

        // Создаем экземпляр Map и устанавливаем его на MapView
        map = mapView.getMapWindow().getMap();
    }

    @Override
    // Переопределение метода onStart, вызываемого при входе в активность
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    // Переопределение метода onStop, вызываемого при выходе из активности
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    // Метод обработки нажатия кнопки "Назад"
    public void onClickBack(View view) {
        // Закрытие активности
        finish();
    }

    // Инициализация MapKit
    private void initializeMapKit() {
        // Сначала инициализируем Yandex MapKit и устанавливаем API-ключ
        MapKitFactory.setApiKey(Objects.requireNonNull(setMapKitApiKey(this)));
        MapKitFactory.initialize(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    // Переопределение метода обратного вызова получения данных о заказах
    public void onGetOrdersResult(List<Order> orders) {
        // Получение первого заказа из списка
        order = orders.get(0);
        // Получение ссылок на элементы интерфейса
        TextView toolbarTitle = findViewById(R.id.order_toolbar_text);
        TextView title = findViewById(R.id.order_title);
        TextView description = findViewById(R.id.order_description);
        TextView price = findViewById(R.id.order_price);
        CardView priceCard = findViewById(R.id.order_price_card);
        LinearLayout orderWait = findViewById(R.id.order_wait);

        // Получение массива категорий из ресурсов
        String[] categoriesArray = this.getResources().getStringArray(R.array.category);
        toolbarTitle.setText(categoriesArray[order.getCategoryID()]);
        title.setText(order.getTitle());
        description.setText(order.getDescription());
        if (order.getPrice() != null) {
            price.setText(order.getPrice() + " руб.");
        } else {
            priceCard.setVisibility(View.GONE);
        }
        orderWait.setVisibility(View.GONE);

        // Инициализация маркера на карте
        PlacemarkMapObject mark = map.getMapObjects().addPlacemark();
        LatLng position = order.getLocation();
        Point point = new Point(position.latitude, position.longitude);
        mark.setGeometry(point);
        ImageProvider icon = ImageProvider.fromResource(this, R.drawable.marker);
        mark.setIcon(icon);
        mark.setIconStyle(
                new IconStyle().setScale(0.5f)
        );
        map.move(
                new CameraPosition(
                        point,
                        17.0f,
                        map.getCameraPosition().getAzimuth(),
                        map.getCameraPosition().getTilt()),
                new Animation(Animation.Type.SMOOTH, 1),
                null
        );


        if (Objects.equals(order.getUserID(), current_user.getId())) {
            order_delete_button = findViewById(R.id.order_delete_button);
            order_delete_button.setVisibility(View.VISIBLE);
        } else if (current_user.isPerformer()) {
            Button order_respone_button = findViewById(R.id.order_respone_button);
            order_respone_button.setVisibility(View.VISIBLE);
        }
    }

    @Override
    // Переопределение метода обратного вызова получения фотографий
    public void onGetPhotosResult(List<String> photosUrls) {
        if (photosUrls != null ) {
            // Если есть фотографии, отображаем карточку фотографий
            CardView orderPhotoCard = findViewById(R.id.order_photo_card);
            orderPhotoCard.setVisibility(View.VISIBLE);
            // Инициализация RecyclerView для отображения фотографий
            RecyclerView recyclerView = findViewById(R.id.order_photo_viewer);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            PhotoAdapterURL photoAdapter = new PhotoAdapterURL(this, photosUrls);
            GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(photoAdapter);
        }
    }

    public void onClickDelete(View view) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.order_confirm_delete)
                .setMessage(R.string.order_confirm_delete_text)
                .setPositiveButton(R.string.order_delete_yes, (dialog, which) -> {
                    order_delete_button.setEnabled(false);
                    // Пользователь подтвердил удаление, вызываем метод для удаления заказа
                    deleteOrder(order, OrderActivity.this, OrderActivity.this);
                })
                .setNegativeButton(R.string.order_delete_cancel, (dialog, which) -> {
                    // Пользователь отменил удаление, закрываем диалог
                    dialog.dismiss();
                });

        // Показываем диалоговое окно
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
    }

    @Override
    public void onDeleteOrderResult() {
        finish();
    }

    public void onClickResponse(View view) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Откликнуться на заявку");
        View dialogView = getLayoutInflater().inflate(R.layout.response_dialog, null);
        builder.setView(dialogView);
        builder.setPositiveButton("Откликнуться", null); // Сохраняем ссылку на кнопку
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(false);

        // Показываем диалоговое окно
        responseDialog = builder.create();
        responseDialog.show();
        responseDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        responseDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            TextInputLayout textLayout = dialogView.findViewById(R.id.response_dialog_text_layout);
            TextInputLayout priceLayout = dialogView.findViewById(R.id.response_dialog_price_layout);
            textLayout.setError(null);
            priceLayout.setError(null);
            TextInputEditText text = dialogView.findViewById(R.id.response_dialog_text);
            TextInputEditText price = dialogView.findViewById(R.id.response_dialog_price);

            if (Objects.requireNonNull(text.getText()).toString().isEmpty()) {
                textLayout.setError("Поле не может быть пустым");
            }
            if (Objects.requireNonNull(price.getText()).toString().isEmpty()) {
                priceLayout.setError("Поле не может быть пустым");
            }

            if (!Objects.requireNonNull(text.getText()).toString().isEmpty() && !Objects.requireNonNull(price.getText()).toString().isEmpty()) {
                responseDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                responseDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                Response response = new Response();
                UUID uuid = UUID.randomUUID();
                response.setId(uuid.toString());
                response.setIdOrder(order.getId());
                response.setIdPerformer(current_user.getId());
                response.setText(text.getText().toString());
                response.setPrice(Integer.parseInt(price.getText().toString()));
                SetResponse.setResponse(response, OrderActivity.this, OrderActivity.this);
            }
        });

    }

    @Override
    public void onSetResponseResult() {
        runOnUiThread(() -> {
            responseDialog.dismiss();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Отклик отправлен");
            builder.setMessage("Ваш отклик успешно отправлен! Его можно посмотреть в разделе откликов.");
            builder.setNegativeButton("ОК", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

    }
}
