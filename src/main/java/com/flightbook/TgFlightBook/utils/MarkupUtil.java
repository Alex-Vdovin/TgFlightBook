package com.flightbook.TgFlightBook.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class MarkupUtil {
    private MarkupUtil() {

    }

    public static InlineKeyboardMarkup getYesNoMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData("ДА");
        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("Нет");
        rows.add(List.of(yesButton, noButton));
        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup getInstrOrStudentMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton instructorInlineKeyboardButton = new InlineKeyboardButton();
        instructorInlineKeyboardButton.setText("Инструктор");
        instructorInlineKeyboardButton.setCallbackData("Инструктор");
        InlineKeyboardButton studentInlineKeyboardButton = new InlineKeyboardButton();
        studentInlineKeyboardButton.setText("Курсант");
        studentInlineKeyboardButton.setCallbackData("Курсант");
        rowInline1.add(instructorInlineKeyboardButton);
        rowInline1.add(studentInlineKeyboardButton);
        rowsInline.add(rowInline1);
        markup.setKeyboard(rowsInline);
        return markup;
    }

    public static InlineKeyboardMarkup getAmendMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton must = new InlineKeyboardButton();
        must.setText("Должность");
        must.setCallbackData("Должность");
        InlineKeyboardButton name = new InlineKeyboardButton();
        name.setText("Имя");
        name.setCallbackData("Имя");
        InlineKeyboardButton lastName = new InlineKeyboardButton();
        lastName.setText("Фамилия");
        lastName.setCallbackData("Фамилия");
        InlineKeyboardButton patronymic = new InlineKeyboardButton();
        patronymic.setText("Отчество");
        patronymic.setCallbackData("Отчество");
        InlineKeyboardButton flightTime = new InlineKeyboardButton();
        flightTime.setText("Налет");
        flightTime.setCallbackData("Налет");
        rows.add(List.of(must));
        rows.add(List.of(name));
        rows.add(List.of(lastName));
        rows.add(List.of(patronymic));
        rows.add(List.of(flightTime));
        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup getDeleteUserMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData("Удалить пользователя");
        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("Не удалять пользователя");
        rows.add(List.of(yesButton, noButton));
        markup.setKeyboard(rows);
        return markup;
    }
}
