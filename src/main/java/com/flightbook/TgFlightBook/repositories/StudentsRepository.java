package com.flightbook.TgFlightBook.repositories;

import com.flightbook.TgFlightBook.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentsRepository extends JpaRepository<Student, Long> {
    Optional<Student> findAllByChatId(Long chatId);
}
