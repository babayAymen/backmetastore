package com.example.meta.store.werehouse.Entities;

import java.io.Serializable;

import com.example.meta.store.Base.Entity.BaseEntity;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Enums.PrivacySetting;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="message")
@Builder
public class Message extends BaseEntity<Long> implements Serializable {

    private static final long serialVersionUID = 12345678110L;
    
//    @ManyToOne
//    private User senderUser;
//    @ManyToOne
//    private User receiverUser;
//    
//    @ManyToOne
//    private Company senderCompany;
//    
//    @ManyToOne
//    private Company receiverCompany;
    
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "conversationId")
    private Conversation conversation;
}
