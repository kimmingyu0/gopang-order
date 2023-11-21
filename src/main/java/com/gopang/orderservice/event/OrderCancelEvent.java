package com.gopang.orderservice.event;

import com.gopang.orderservice.message.payment.ReqPayOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderCancelEvent {
    private final ReqPayOrder reqPayOrder;

    private final String message;
}
