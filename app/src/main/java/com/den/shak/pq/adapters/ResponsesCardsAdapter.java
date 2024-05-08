package com.den.shak.pq.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.den.shak.pq.models.Order;
import com.den.shak.pq.models.Response;
import com.den.shak.pq.models.User;

import java.util.List;

// Определение класса OrdersCardsAdapter, расширяющего RecyclerView.Adapter
public class ResponsesCardsAdapter extends RecyclerView.Adapter<ResponsesCardsAdapter.ViewHolder>{
    private final List<Response> responseList;
    private final List<Order> orderList;
    private final List<User> userList;

    private OnResponsesClickListener mListener; // Слушатель событий нажатия на элемент списка

    // Конструктор класса
    public ResponsesCardsAdapter(List<Response> responseList, List<Order> orderList, List<User> userList) {
        this.responseList = responseList;
        this.orderList = orderList;
        this.userList = userList;
    }
    @NonNull
    @Override
    // Метод для создания нового экземпляра ViewHolder
    public ResponsesCardsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создание нового View для элемента списка
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // Метод для связывания данных с ViewHolder
    public void onBindViewHolder(@NonNull ResponsesCardsAdapter.ViewHolder holder, int position) {
        // Получение данных о заказе
        holder.title.setText(orderList.get(position).getTitle());
        int category = orderList.get(position).getCategoryID();
        String[] categoriesArray = holder.itemView.getContext().getResources().getStringArray(R.array.category);
        holder.category.setText(categoriesArray[category]);
        // Установка цены заказа (если указана)
        if (responseList.get(position).getPrice() != null) {
            String price = responseList.get(position).getPrice() + " руб.";
            holder.price.setText(price);
        } else {
            holder.priceCard.setVisibility(View.GONE); // Скрытие карточки цены, если цена не указана
        }
        // Установка цвета карточки заказа
        if (responseList.get(position).getAccepted() != null) {
            if (responseList.get(position).getAccepted()) {
                holder.order_card.setCardBackgroundColor(Color.argb(50, 0, 255, 0));
            } else {
                holder.order_card.setCardBackgroundColor(Color.argb(50, 255, 0, 0));
            }
        }

        // Установка слушателя событий для элемента списка
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onResponsesClick(responseList.get(position), orderList.get(position), userList.get(position));
            }
        });
    }

    @Override
    // Метод для получения общего количества элементов в списке
    public int getItemCount() {
        return responseList.size();
    }

    // Интерфейс для обработки событий нажатия на элемент списка
    public interface OnResponsesClickListener {
        void onResponsesClick(Response response, Order order, User user);
    }

    // Внутренний класс ViewHolder для отображения элементов списка
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title; // Заголовок заказа
        public TextView category; // Категория заказа
        public TextView price; // Цена заказа
        public CardView priceCard; // Карточка с ценой заказа
        public CardView order_card;

        // Конструктор класса ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.order_card_title);
            category = itemView.findViewById(R.id.order_card_catetgory);
            price = itemView.findViewById(R.id.order_card_price);
            priceCard = itemView.findViewById(R.id.order_card_price_card);
            order_card = itemView.findViewById(R.id.order_card);
        }
    }

    // Метод для установки слушателя событий нажатия на элемент списка
    public void setOnResponsesClickListener(OnResponsesClickListener listener) {
        mListener = listener;
    }
}
