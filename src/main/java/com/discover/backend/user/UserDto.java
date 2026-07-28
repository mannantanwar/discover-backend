package com.discover.backend.user;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserDto {

    private UUID publicId;
    private String email;
    private String displayName;
    private String username;
    private String avatarUrl;
    private Boolean emailVerified;
    private Instant createdAt;
}
