package com.gopang.orderservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final StreamBridge streamBridge;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionAfterCommit(OrderEvent event) {
        log.info("Recieved message to paymentRequest-topic : " + event.getMessage());
        streamBridge.send("paymentRequest-topic", MessageBuilder
                .withPayload(event.getPaymentRequest())
                .build()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTransactionBeforeCommit (OrderCancelEvent cancelEvent) {
        log.info("Recieved message to paymentCancelRequest-topic : " + cancelEvent.getMessage());
        streamBridge.send("paymentCancelRequest-topic", MessageBuilder
                .withPayload(cancelEvent.getPaymentRequest())
                .build()
        );
    }
}
