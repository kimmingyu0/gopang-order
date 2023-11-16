package com.gopang.orderservice.service;

import com.gopang.orderservice.domain.OrderHistory;
import com.gopang.orderservice.domain.Orders;
import com.gopang.orderservice.domain.PayloadType;
import com.gopang.orderservice.dto.Order;
import com.gopang.orderservice.dto.PaymentRequest;
import com.gopang.orderservice.event.OrderCancelEvent;
import com.gopang.orderservice.event.OrderEvent;
import com.gopang.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ApplicationEventPublisher eventPublisher;

    // 주문 요청
    @Transactional
    public Orders register(Order.OrderRequest request) throws Exception {
        Orders orders= null;
        OrderHistory history = null;

        // 회원 서비스에 요청하기
        // 회원 아이디, 이름, 주소

        try {
            orders = Orders.builder()
                    .user_id(request.user_id)
                    .cartitem_id(request.cartitem_id)
                    .reciever_name(request.reciever_name)
                    .reciever_phone(request.reciever_phone)
                    .reciever_addr(request.reciever_addr)
                    .amount(request.amount)
                    .deleteYn("N")
                    .build();

            orders = orderRepository.save(orders);

            PaymentRequest payment = PaymentRequest.builder()
                    .order_id(orders.getId().toString())
                    .amount(orders.amount.intValue())
                    .build();

            String message = payment.getOrder_id()+"번 주문 결제 요청됨.";

            eventPublisher.publishEvent(new OrderEvent(payment, message));

        } catch (Exception e) {
            throw new Exception("잘못된 요청입니다.");
        }
        return orders;
    }

    @Transactional
    public void orderCancel (Long orderId) {
        Orders orders = orderRepository.findById(orderId).get();

        PaymentRequest payment = PaymentRequest.builder()
                .order_id(orders.getId().toString())
                .amount(orders.amount.intValue())
                .build();

        String message = payment.getOrder_id()+"번 주문 결제취소 요청됨.";

        // 결제 요청
        eventPublisher.publishEvent(new OrderCancelEvent(payment, message));

        orderRepository.deleteById(orderId);
    }
}
