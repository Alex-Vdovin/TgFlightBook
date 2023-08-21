package com.flightbook.TgFlightBook.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "instructors")
@Getter
@Setter
@RequiredArgsConstructor
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "instructor_chat_id")
    Long chatId;
    @Column(name = "instructor_first_name")
    String firstName;
    @Column(name = "instructor_last_name")
    String lastName;
    @Column(name = "instructor_patronymic")
    String patronymic;
    @Column(name = "flight_time_left")
    String flightTimeLeft;

    @Override
    public String toString() {
        if (patronymic == null) {
            return "Инструктор\n" +
                    "Имя: " + firstName + "\n" +
                    "Фамилия: " + lastName + "\n" +
                    "Отчество: Не указано" + "\n" +
                    "Остаток сан-нормы: " + flightTimeLeft;
        } else {
            return "Инструктор\n" +
                    "Имя: " + firstName + "\n" +
                    "Фамилия: " + lastName + "\n" +
                    "Отчество: " + patronymic + "\n" +
                    "Остаток сан-нормы: " + flightTimeLeft;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Instructor that = (Instructor) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
