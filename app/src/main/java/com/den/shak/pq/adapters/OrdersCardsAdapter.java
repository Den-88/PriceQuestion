package com.den.shak.pq.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.den.shak.pq.models.Order;

import java.util.List;


// Определение класса OrdersCardsAdapter, расширяющего RecyclerView.Adapter
public class OrdersCardsAdapter extends RecyclerView.Adapter<OrdersCardsAdapter.ViewHolder>{
    private final List<Order> dataList; // Список данных
    private OnOrderClickListener mListener; // Слушатель событий нажатия на элемент списка

    // Конструктор класса
    public OrdersCardsAdapter(List<Order> dataList) {
        this.dataList = dataList;
    }
    @NonNull
    @Override
    // Метод для создания нового экземпляра ViewHolder
    public OrdersCardsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создание нового View для элемента списка
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // Метод для связывания данных с ViewHolder
    public void onBindViewHolder(@NonNull OrdersCardsAdapter.ViewHolder holder, int position) {
        // Получение данных о заказе
        String title = dataList.get(position).getTitle();
        holder.title.setText(title);
        int category = dataList.get(position).getCategoryID();
        String[] categoriesArray = holder.itemView.getContext().getResources().getStringArray(R.array.category);
        holder.category.setText(categoriesArray[category]);

        // Установка цены заказа (если указана)
        if (dataList.get(position).getPrice() != null) {
            String price = dataList.get(position).getPrice() + " руб.";
            holder.price.setText(price);
        } else {
            holder.priceCard.setVisibility(View.GONE); // Скрытие карточки цены, если цена не указана
        }

        // Установка слушателя событий для элемента списка
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onOrderClick(dataList.get(position));
            }
        });
    }

    @Override
    // Метод для получения общего количества элементов в списке
    public int getItemCount() {
        return dataList.size();
    }

    // Интерфейс для обработки событий нажатия на элемент списка
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    // Внутренний класс ViewHolder для отображения элементов списка
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title; // Заголовок заказа
        public TextView category; // Категория заказа
        public TextView price; // Цена заказа
        public CardView priceCard; // Карточка с ценой заказа


        // Конструктор класса ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.order_card_title);
            category = itemView.findViewById(R.id.order_card_catetgory);
            price = itemView.findViewById(R.id.order_card_price);
            priceCard = itemView.findViewById(R.id.order_card_price_card);
        }
    }

    // Метод для установки слушателя событий нажатия на элемент списка
    public void setOnOrderClickListener(OnOrderClickListener listener) {
        mListener = listener;
    }
}
