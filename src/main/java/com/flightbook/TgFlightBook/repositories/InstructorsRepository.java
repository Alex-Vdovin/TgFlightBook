package com.flightbook.TgFlightBook.repositories;

import com.flightbook.TgFlightBook.entities.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorsRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findAllByChatId(Long chatId);
}
