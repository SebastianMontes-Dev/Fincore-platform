package com.fincore.platform.infrastructure.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConfigurarWebhookRequest {
    @NotBlank @Size(max = 500)
    private String urlWebhook;
}
