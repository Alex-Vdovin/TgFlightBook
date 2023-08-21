package com.flightbook.TgFlightBook.dto;

import com.flightbook.TgFlightBook.entities.Instructor;
import com.flightbook.TgFlightBook.entities.Student;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserDTO {
    private Long chatId;
    private String command;
    private Instructor promoInstructor;
    private Student promoStudent;


}
