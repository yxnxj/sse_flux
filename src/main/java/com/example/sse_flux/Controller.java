package com.example.sse_flux;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class Controller {
    private final NotificationService notificationService;



    @GetMapping("/subscribe/{id}")
    public Flux<ServerSentEvent<Notification>> subscribe(@PathVariable Integer id) {
        return notificationService.subscribe(id);
    }
}
