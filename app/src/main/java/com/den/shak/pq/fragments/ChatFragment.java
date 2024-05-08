package com.den.shak.pq.fragments;

import static com.den.shak.pq.activity.MainActivity.current_user;
import static com.den.shak.pq.cloud.GetResponses.getResponsesData;
import static com.den.shak.pq.cloud.SetResponse.setResponse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.den.shak.pq.activity.OrderActivity;
import com.den.shak.pq.adapters.ResponsesCardsAdapter;
import com.den.shak.pq.cloud.GetResponses;
import com.den.shak.pq.cloud.SetResponse;
import com.den.shak.pq.models.Order;
import com.den.shak.pq.models.Response;
import com.den.shak.pq.models.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ChatFragment extends Fragment implements GetResponses.GetResponsesCallback, ResponsesCardsAdapter.OnResponsesClickListener, SetResponse.SetResponseCallback {
    private ResponsesCardsAdapter myCardsAdapter;
    private ResponsesCardsAdapter forMeCardsAdapter;
    public static List<Response> myResponses;
    public static List<Response> forMeResponses;
    public static List<Order> myOrders;
    public static List<Order> forMeOrders;
    public static List<User> myUsers;
    public static List<User> forMeUsers;
    AlertDialog myOrdersDialog;


    @Nullable
    @Override
    // Метод onCreateView вызывается при создании представления фрагмента
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Надуваем макет для фрагмента
        View view = inflater.inflate(R.layout.chat_fragment, container, false);

        // Инициализация RecyclerView для моих ответов
        RecyclerView myRecyclerView = view.findViewById(R.id.chat_my_responses);
        myRecyclerView.setHasFixedSize(true);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Инициализация RecyclerView для ответов для меня
        RecyclerView forMeRecyclerView = view.findViewById(R.id.chat_for_me_responses);
        forMeRecyclerView.setHasFixedSize(true);
        forMeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Создание списков данных для моих и для ответов для меня
        myResponses = new ArrayList<>();
        forMeResponses = new ArrayList<>();
        myOrders = new ArrayList<>();
        forMeOrders = new ArrayList<>();
        myUsers = new ArrayList<>();
        forMeUsers = new ArrayList<>();

        // Создание адаптеров для RecyclerView и установка их
        myCardsAdapter = new ResponsesCardsAdapter(myResponses, myOrders, myUsers);
        myRecyclerView.setAdapter(myCardsAdapter);
        myCardsAdapter.setOnResponsesClickListener(this);

        forMeCardsAdapter = new ResponsesCardsAdapter(forMeResponses, forMeOrders, forMeUsers);
        forMeRecyclerView.setAdapter(forMeCardsAdapter);
        forMeCardsAdapter.setOnResponsesClickListener(this);

        // Получение данных о ответах
        getResponsesData(current_user.getId(), ChatFragment.this, requireContext());
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    // Метод onGetResponsesResult вызывается при получении ответов
    public void onGetResponsesResult(List<Order> ordersMy, List<Response> responsesMy, List<User> usersMy, List<Order> ordersForMe, List<Response> responsesForMe, List<User> usersForMe) {
        myResponses.clear();
        myResponses.addAll(responsesMy);
        myOrders.clear();
        myOrders.addAll(ordersMy);
        myUsers.clear();
        myUsers.addAll(usersMy);
        myCardsAdapter.notifyDataSetChanged();

        forMeResponses.clear();
        forMeResponses.addAll(responsesForMe);
        forMeOrders.clear();
        forMeOrders.addAll(ordersForMe);
        forMeUsers.clear();
        forMeUsers.addAll(usersForMe);
        forMeCardsAdapter.notifyDataSetChanged();

        // Скрытие элементов интерфейса в случае отсутствия ответов
        LinearLayout wait_layout = requireView().findViewById(R.id.chat_fragment_wait);
        wait_layout.setVisibility(View.GONE);
        if (!responsesMy.isEmpty()) {
            TextView my_responses_text = requireView().findViewById(R.id.chat_my_responses_text);
            my_responses_text.setVisibility(View.VISIBLE);
        }
        if (!responsesForMe.isEmpty()) {
            TextView for_me_responses_text = requireView().findViewById(R.id.chat_for_me_responses_text);
            for_me_responses_text.setVisibility(View.VISIBLE);
        }
        if (responsesMy.isEmpty() && responsesForMe.isEmpty()) {
            LinearLayout no_responses_layout = requireView().findViewById(R.id.chat_fragment_unavalible);
            no_responses_layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    // Метод onResponsesClick вызывается при нажатии на ответ в списке
    public void onResponsesClick(Response response, Order order, User user) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        if (Objects.equals(response.getIdPerformer(), current_user.getId())) {
            builder.setTitle(R.string.chat_my_response);
            String status;

            if (response.getAccepted() != null) {
                if (response.getAccepted()) {
                    status = getString(R.string.chat_allow);
                    builder.setMessage(getString(R.string.chat_response_text) + response.getText() + getString(R.string.chat_my_price) + response.getPrice() + getString(R.string.chat_rub_and_status) + status + getString(R.string.chat_name) + user.getName());
                } else {
                    status = getString(R.string.chat_decline);
                    builder.setMessage(getString(R.string.chat_response_text) + response.getText() + getString(R.string.chat_my_price) + response.getPrice() + getString(R.string.chat_rub_and_status) + status);
                }
            } else {
                status = getString(R.string.chat_on_wait);
                builder.setMessage(getString(R.string.chat_response_text) + response.getText() + getString(R.string.chat_my_price) + response.getPrice() + getString(R.string.chat_rub_and_status) + status);
            }
            builder.setNegativeButton(R.string.chat_see_order, (dialog, which) -> {
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                intent.putExtra("order_id", order.getId());
                // Если необходимо, передайте информацию о заказе через Intent
                startActivity(intent);
            });
            builder.setNeutralButton(R.string.chat_cancel, (dialog, which) -> dialog.dismiss());
            if (response.getAccepted() != null) {
                if (response.getAccepted()) {
                    builder.setPositiveButton(R.string.chat_call, (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:+" + user.getPhone()));
                        startActivity(intent);

                    });
                }
            }

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#008000")); // Зеленый цвет в формате RGB
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.RED);

        } else {
            builder.setTitle(R.string.chat_response_for_me);
            builder.setNegativeButton(R.string.chat_see_order, (dialog, which) -> {
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                intent.putExtra("order_id", order.getId());
                // Если необходимо, передайте информацию о заказе через Intent
                startActivity(intent);
            });
            if (response.getAccepted() == null) {
                builder.setMessage(getString(R.string.chat_response_text) + response.getText() + getString(R.string.chat_price) + response.getPrice() + getString(R.string.chat_status_on_wait));

                builder.setPositiveButton(R.string.chat_allow_response, (dialog, which) -> {
                    response.setAccepted(true);
                    setResponse(response, ChatFragment.this, requireContext());
                    myOrdersDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    myOrdersDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                    myOrdersDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                    myOrdersDialog.setCancelable(false);
                });
                builder.setNeutralButton(R.string.chat_decline_response, (dialog, which) -> {
                    response.setAccepted(false);
                    setResponse(response, ChatFragment.this, requireContext());
                    myOrdersDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    myOrdersDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                    myOrdersDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                    myOrdersDialog.setCancelable(false);
                });
            } else {
                String status;
                if (response.getAccepted()) {
                    status = getString(R.string.chat_allowed);
                } else {
                    status = getString(R.string.chat_declined);
                }
                builder.setMessage(getString(R.string.chat_response_text) + response.getText() + getString(R.string.chat_price) + response.getPrice() + getString(R.string.chat_rub_and_status) + status);
            }

            myOrdersDialog = builder.create();
            myOrdersDialog.show();
            myOrdersDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#008000")); // Зеленый цвет в формате RGB
            myOrdersDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.RED);
        }
    }

    @Override
    // Метод onSetResponseResult вызывается при установке ответа через API
    public void onSetResponseResult() {
        // Закрытие диалогового окна после установки ответа и обновление данных
        myOrdersDialog.dismiss();
        getResponsesData(current_user.getId(), ChatFragment.this, requireContext());
    }
}