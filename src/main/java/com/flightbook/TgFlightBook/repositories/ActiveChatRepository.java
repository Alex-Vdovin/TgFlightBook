package com.flightbook.TgFlightBook.repositories;

import com.flightbook.TgFlightBook.entities.ActiveChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ActiveChatRepository extends JpaRepository<ActiveChat, Long> {
    Optional<ActiveChat> findAllByChatId(Long chatId);

    @Transactional
    void deleteByChatId(Long chatId);
}
