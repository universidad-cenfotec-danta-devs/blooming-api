package com.blooming.api.controller;

import com.blooming.api.request.PayPalRequest;
import com.blooming.api.service.paypal.PayPalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/paypal")
public class PayPalController {

    @Autowired
    private PayPalService payPalService;

    @PostMapping("/pay")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'DESIGNER_USER', 'SIMPLE_USER', 'NURSERY_USER')")
    public ResponseEntity<?> pay(@RequestBody PayPalRequest request) {
        try {
            Payment payment = payPalService.createPayment(
                    request.getTotal(),
                    "USD",
                    "paypal",
                    "sale",
                    "Pago de prueba en sandbox",
                    "http://localhost:4200/paypal/cancel",
                    "http://localhost:4200/paypal/success");

            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    System.out.println(link.getHref());
                    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.getHref())).build();
                }
            }

        } catch (PayPalRESTException e) {
            return ResponseEntity.internalServerError().body("Error creando el pago");
        }

        return ResponseEntity.badRequest().body("No se pudo crear el pago");
    }

    @GetMapping("/success")
    public String success(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            return "Pago exitoso. ID: " + payment.getId();
        } catch (PayPalRESTException e) {
            return "Error al ejecutar el pago";
        }
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "Pago cancelado.";
    }
}