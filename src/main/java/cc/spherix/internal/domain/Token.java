package cc.spherix.internal.domain;

public record Token(Long id, String username, String token, boolean disabled) {
}
