package com.flightbook.TgFlightBook.DTO;

import com.flightbook.TgFlightBook.entities.Instructor;
import com.flightbook.TgFlightBook.entities.Student;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@RequiredArgsConstructor
public class UserDTO {
    private Long chatId;
    private String previousCommand;
    private Instructor promoInstructor;
    private Student promoStudent;


}
