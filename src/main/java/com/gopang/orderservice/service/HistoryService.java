package com.gopang.orderservice.service;

import com.gopang.orderservice.domain.OrderHistory;
import com.gopang.orderservice.domain.OrderState;
import com.gopang.orderservice.domain.Orders;
import com.gopang.orderservice.domain.PaymentStatus;
import com.gopang.orderservice.dto.CancelStatus;
import com.gopang.orderservice.dto.History;
import com.gopang.orderservice.dto.Order;
import com.gopang.orderservice.dto.StatusUpdate;
import com.gopang.orderservice.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    @Bean
    public Consumer<StatusUpdate> consumerBinding() {
        return pay -> stateUpdate(pay.getOrderId(), pay.getPaymentStatus());
    }

    @Bean
    public Consumer<CancelStatus> cancelBinding() {
        return cancel -> System.out.println("cancel 토픽 : " + cancel);
    }

    public void register (Orders orders, Order.OrderRequest orderRequest) {
        OrderHistory history = OrderHistory.builder()
                .orders(orders)
                .user_id(orderRequest.user_id)
                .item_id(orderRequest.cartitem_id)
                .order_amount(orderRequest.amount)
                .order_state(OrderState.DELIVERYREADY)
                .build();

        historyRepository.save(history);
    }

    public void stateUpdate (Long orderId, PaymentStatus status) {
        System.out.println(status);
        Optional<OrderHistory> history = historyRepository.findById(orderId);

        OrderHistory orderHistory;

        if (history.isPresent()) {
            orderHistory = history.get();
            if (status == PaymentStatus.PAYCOMPLETE) {
                orderHistory.setOrder_state(OrderState.PAYCOMPLETE);
            } else {
                orderHistory.setOrder_state(OrderState.CANCEL);
            }

            historyRepository.save(orderHistory);
        }
    }

    // 전체 주문 내역 조회
    // 주문 내역의 아이템 각 항목당 링크 추가 필요
    @Transactional(readOnly = true)
    public List<History.HistoryResponse> getAllHistory (Long userId) {
        List<OrderHistory> allByUserId = historyRepository.findAllByUserId(userId);
        return allByUserId.stream().map(orderHistory ->
                History.HistoryResponse.builder()
                        .id(orderHistory.getId())
                        .user_id(orderHistory.getUser_id())
                        .item_id(orderHistory.getItem_id())
                        .order_state(orderHistory.getOrder_state())
                        .order_amount(orderHistory.getOrder_amount())
                        .orders(orderHistory.getOrders())
                        .build()
        ).toList();
    }

}
