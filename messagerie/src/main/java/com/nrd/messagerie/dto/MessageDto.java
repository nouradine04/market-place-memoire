package com.nrd.messagerie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String id; // MongoDB utilise des String IDs
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private Long annonceId;
    private String content;
    private String timestamp;
    private boolean read;
}