package com.nrd.messagerie.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "blocked_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockedUser {
    @Id
    private String id;
    private Long blockerId;
    private Long blockedId;
    private String blockedAt;
}