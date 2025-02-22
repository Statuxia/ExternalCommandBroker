package cc.spherix.internal.dto;

import java.util.UUID;

public record MessageDTO(String message, UUID token) {
}
