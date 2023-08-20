package com.flightbook.TgFlightBook.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="activechatid")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class ActiveChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name="chat_id")
    Long chatId;
    @Column(name="command")
    String previousCommand;
    public ActiveChat(Long userId) {
        this.chatId = userId;
    }

    public Long getId() {
        return id;
    }

    public ActiveChat setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getChatId() {
        return chatId;
    }

    public ActiveChat setChatId(Long chatId) {
        this.chatId = chatId;
        return this;
    }

    public String getPreviousCommand() {
        return previousCommand;
    }

    public ActiveChat setPreviousCommand(String previousCommand) {
        this.previousCommand = previousCommand;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ActiveChat that = (ActiveChat) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
