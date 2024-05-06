package com.den.shak.pq.fragments;

import static com.den.shak.pq.activity.MainActivity.current_user;
import static com.den.shak.pq.cloud.GetOrders.getOrdersData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.den.shak.pq.activity.FilterActivity;
import com.den.shak.pq.activity.MapSearchActivity;
import com.den.shak.pq.activity.OrderActivity;
import com.den.shak.pq.adapters.OrdersCardsAdapter;
import com.den.shak.pq.cloud.GetOrders;
import com.den.shak.pq.models.Order;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Определение класса ListAdvertFragment, расширяющего Fragment
public class ListAdvertFragment extends Fragment implements GetOrders.GetOrdersCallback, OrdersCardsAdapter.OnOrderClickListener {
    // Статическое поле, указывающее, нужно ли отображать только мои заказы
    public static boolean onlyMyOrders = false;
    // Адаптер для списка заказов
    private OrdersCardsAdapter cardsAdapter;
    // Список заказов
    public static List<Order> ordersList;
    // Переменные для макетов с информацией о загрузке и отсутствии данных
    LinearLayout unavalibleLayout;
    LinearLayout waitLayout;
    // Объект SharedPreferences для сохранения выбранных категорий
    private SharedPreferences sharedPreferences;
    // Список категорий и список выбранных категорий
    List<String> categories;
    private List<Integer> selectedCategories;

    // Переменная, указывающая, применен ли фильтр
    public static boolean isFiltered = false;

    @Nullable
    @Override
    // Метод для создания и настройки макета фрагмента
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_advert_fragment, container, false);

        // Инициализация SharedPreferences и списка выбранных категорий
        sharedPreferences = requireContext().getSharedPreferences("filter_selected_items", Context.MODE_PRIVATE);
        selectedCategories = new ArrayList<>();
        categories = Arrays.asList(getResources().getStringArray(R.array.category));

        // Инициализация RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.list_fragment_orders_cards);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Создание списка данных
        ordersList = new ArrayList<>();

        // Создание адаптера и установка его в RecyclerView
        cardsAdapter = new OrdersCardsAdapter(ordersList);
        recyclerView.setAdapter(cardsAdapter);

        // Установка слушателей на кнопки фильтрации и перехода к карте
        FloatingActionButton button_filter = view.findViewById(R.id.button_filter);
        FloatingActionButton button_map = view.findViewById(R.id.button_map);
        button_filter.setOnClickListener(v -> openFilter());
        button_map.setOnClickListener(v -> openMap());

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    // Метод для обновления данных при возврате к фрагменту из состояния паузы или остановки
    public void onResume() {
        super.onResume();
        ordersList.clear();
        cardsAdapter.notifyDataSetChanged();
        waitLayout.setVisibility(View.VISIBLE);
        TextView toolbarText = requireActivity().findViewById(R.id.toolbar_text);
        TextView filterText = requireActivity().findViewById(R.id.list_fragment_filter_text);
        if (onlyMyOrders) {
            toolbarText.setText("Мои заявки");
            getOrdersData(null,null, current_user.getId(), null, ListAdvertFragment.this, requireContext());
            filterText.setVisibility(View.GONE);
            onlyMyOrders = false;
        } else {
            loadSelectedCategories();
            Log.d("selectedCategories", selectedCategories.toString());
            toolbarText.setText("Список заявок");
            if (!selectedCategories.isEmpty()) {
                getOrdersData(null,null, null, selectedCategories, ListAdvertFragment.this, requireContext());
                filterText.setVisibility(View.VISIBLE);
                isFiltered = true;
            } else {
                getOrdersData(null,null, null, null, ListAdvertFragment.this, requireContext());
                filterText.setVisibility(View.GONE);
                isFiltered = false;
            }
        }
    }

    @Override
    // Метод, вызываемый после того, как фрагмент полностью создан
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSearch();
        unavalibleLayout = requireView().findViewById(R.id.list_fragment_unavalible);
        waitLayout = requireView().findViewById(R.id.list_fragment_wait);
        cardsAdapter.setOnOrderClickListener(this);
    }

    // Метод для настройки функциональности поиска
    private void setSearch() {
        TextInputEditText search_text = requireView().findViewById(R.id.list_fragment_search_text);

        // Слушатель изменений в тексте поиска
        search_text.addTextChangedListener(new TextWatcher() {
            private final Handler handler = new Handler();
            private final Runnable delayedAction = () -> getOrdersData(String.valueOf(search_text.getText()),null, null, null, ListAdvertFragment.this, requireContext());
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Удаляем все предыдущие вызовы Runnable из очереди
                handler.removeCallbacks(delayedAction);

                // Добавляем новый вызов Runnable через 2 секунды
                handler.postDelayed(delayedAction, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    // Метод, вызываемый после получения данных о заказах
    public void onGetOrdersResult(List<Order> orders) {
        requireActivity().runOnUiThread(() -> {
            if (orders != null) {
                // Обновление списка заказов и уведомление адаптера об изменениях
                ordersList.clear();
                ordersList.addAll(orders);
                cardsAdapter.notifyDataSetChanged();
                unavalibleLayout.setVisibility(View.GONE);
            } else {
                ordersList.clear();
                cardsAdapter.notifyDataSetChanged();
                unavalibleLayout.setVisibility(View.VISIBLE);
            }
            waitLayout.setVisibility(View.GONE);
        });
    }

    @Override
    // Метод, вызываемый при клике на заказ
    public void onOrderClick(Order order) {
        // Откройте активность для просмотра выбранного заказа
        Intent intent = new Intent(getActivity(), OrderActivity.class);
        intent.putExtra("order_id", order.getId());
        // Если необходимо, передайте информацию о заказе через Intent
        startActivity(intent);
    }
    // Метод для открытия активности фильтра
    private void openFilter() {
        Intent intent = new Intent(requireContext(), FilterActivity.class);
        // Запуск активности карты
        startActivity(intent);
    }
    // Метод для открытия активности карты
    private void openMap() {
        Intent intent = new Intent(requireContext(), MapSearchActivity.class);
        // Запуск активности карты
        startActivity(intent);
    }
    // Метод для загрузки ранее выбранных категорий
    private void loadSelectedCategories() {
        // Загрузка ранее выбранных элементов из SharedPreferences
        selectedCategories.clear(); // Очищаем список перед загрузкой
        for (int i = 0; i < categories.size(); i++) {
            boolean isSelected = sharedPreferences.getBoolean(categories.get(i), false);
            if (isSelected) {
                selectedCategories.add(i);
            }
        }
    }

}
