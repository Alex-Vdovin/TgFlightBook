package com.flightbook.TgFlightBook.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="students")
@Getter
@Setter
@RequiredArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "student_chat_id")
    Long chatId;
    @Column(name = "student_first_name")
    String firstName;
    @Column(name = "student_last_name")
    String lastName;
    @Column(name = "student_patronymic")
    String patronymic;
    @Column(name = "total_flight_time")
    String totalFlightTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Student student = (Student) o;
        return id != null && Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        if(patronymic == null){
            return "Студент\n" +
                    "Имя: " + firstName + "\n" +
                    "Фамилия: " + lastName + "\n" +
                    "Отчество: Не указано" + "\n" +
                    "Общий налет: " + totalFlightTime;
        }else {
            return "Студент\n" +
                    "Имя: " + firstName + "\n" +
                    "Фамилия: " + lastName + "\n" +
                    "Отчество" + patronymic + "\n" +
                    "Общий налет: " + totalFlightTime;
        }
    }
}
