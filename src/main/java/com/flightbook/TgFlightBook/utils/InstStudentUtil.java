package com.flightbook.TgFlightBook.utils;

import com.flightbook.TgFlightBook.dto.UserDTO;
import com.flightbook.TgFlightBook.entities.Instructor;
import com.flightbook.TgFlightBook.entities.Student;

public class InstStudentUtil {
    private InstStudentUtil(){

    }
    public static UserDTO InstrToStudent(UserDTO userDTO){
        Student student = new Student();
        Instructor incorrectInst = userDTO.getPromoInstructor();
        student.setChatId(incorrectInst.getChatId());
        student.setFirstName(incorrectInst.getFirstName());
        student.setLastName(incorrectInst.getLastName());
        student.setPatronymic(incorrectInst.getPatronymic());
        student.setTotalFlightTime(incorrectInst.getFlightTimeLeft());
        userDTO.setPromoStudent(student);
        userDTO.setPromoInstructor(null);
        return userDTO;
    }
    public static UserDTO StudentToInstructor(UserDTO userDTO){
        Instructor instructor = new Instructor();
        Student incorrectStudent = userDTO.getPromoStudent();
        instructor.setChatId(incorrectStudent.getChatId());
        instructor.setFirstName(incorrectStudent.getFirstName());
        instructor.setLastName(incorrectStudent.getLastName());
        instructor.setPatronymic(incorrectStudent.getPatronymic());
        instructor.setFlightTimeLeft(incorrectStudent.getTotalFlightTime());
        userDTO.setPromoInstructor(instructor);
        userDTO.setPromoStudent(null);
        userDTO.setCommand("Проверьте Ваши данные");
        return userDTO;
    }
}
