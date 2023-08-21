package com.flightbook.TgFlightBook.services;

import com.flightbook.TgFlightBook.dto.UserDTO;
import com.flightbook.TgFlightBook.entities.ActiveChat;
import com.flightbook.TgFlightBook.entities.Instructor;
import com.flightbook.TgFlightBook.entities.Student;
import com.flightbook.TgFlightBook.repositories.ActiveChatRepository;
import com.flightbook.TgFlightBook.repositories.InstructorsRepository;
import com.flightbook.TgFlightBook.repositories.StudentsRepository;
import com.flightbook.TgFlightBook.utils.InstStudentUtil;
import com.flightbook.TgFlightBook.utils.MarkupUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {
    @Value("${bot.api.key}")
    private String apiKey;
    @Value(("{bot.api.name"))
    private String name;
    private final HashMap<Long, UserDTO> userDTOHashMap = new HashMap<>();
    public final ActiveChatRepository activeChatRepository;
    public final InstructorsRepository instructorsRepository;
    public final StudentsRepository studentsRepository;
    private static final String REGISTRATION = "/registration";
    public static final String PROFILE = "/profile";
    private static final String DELETE = "/delete";
    private static final String INSTRUCTOR = "Инструктор";
    private static final String STUDENT = "Курсант";
    private static final String CHECK_DATA = "Проверьте Ваши данные";

    @PostConstruct
    public void start() {
        log.info("username: {}, token: {}", name, apiKey);
    }

    @Override
    public String getBotToken() {
        return apiKey;
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (REGISTRATION.equalsIgnoreCase(messageText)) {
                startingRegistration(update);
            } else if (activeChatRepository.findAllByChatId(chatId).isPresent() && userDTOHashMap.get(chatId) != null) {
                if (userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Введите Ваше имя")
                        || userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Исправлено имя")) {
                    nameRegistration(update);
                } else if (userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Введите Вашу фамилию")
                        || userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Исправлена фамилия")) {
                    lastNameRegistration(update);
                } else if (userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Введите Ваше отчество")
                        || userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Исправлено отчество")) {
                    patronymicRegistration(update);
                } else if (userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Введите налёт")
                        || userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Исправлен налет")) {
                    flightTimeRegistration(update);
                }
            } else if (PROFILE.equalsIgnoreCase(messageText)) {
                getProfile(update);
            } else if (DELETE.equalsIgnoreCase(messageText)) {
                deleteUser(update);
            }
        } else if (update.hasCallbackQuery()) {
            String callBackQueryData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackQueryData.equalsIgnoreCase(INSTRUCTOR) || callBackQueryData.equalsIgnoreCase(STUDENT)) {
                if (userDTOHashMap.get(chatId) == null) {
                    instrOrStudentRegistration(update);
                } else if (userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Исправленная должность")) {
                    UserDTO userDTO = userDTOHashMap.get(chatId);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    if (callBackQueryData.equalsIgnoreCase(INSTRUCTOR)) {
                        if (userDTO.getPromoInstructor() != null) {
                            sendMessage.setText("Ранее была выбрана должность инструктор" + "\n\n" + dataChecker(update));
                            userDTO.setCommand(CHECK_DATA);
                            userDTOHashMap.replace(chatId, userDTO);
                        } else {
                            userDTOHashMap.replace(chatId, InstStudentUtil.StudentToInstructor(userDTO));
                            sendMessage.setText(dataChecker(update));
                        }
                    } else if (callBackQueryData.equalsIgnoreCase(STUDENT)) {
                        if (userDTO.getPromoStudent() != null) {
                            sendMessage.setText("Ранее была выбрана должность курсант" + "\n\n" + dataChecker(update));
                            userDTO.setCommand(CHECK_DATA);
                            userDTOHashMap.replace(chatId, userDTO);
                        } else {
                            userDTOHashMap.replace(chatId, InstStudentUtil.InstrToStudent(userDTO));
                            userDTO.setCommand(CHECK_DATA);
                            sendMessage.setText(dataChecker(update));
                        }
                    }
                    sendMessage.setReplyMarkup(MarkupUtil.getYesNoMarkup());
                    execute(sendMessage);
                }

            } else if (activeChatRepository.findAllByChatId(chatId).isPresent() && userDTOHashMap.get(chatId) != null) {
                if (userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase(CHECK_DATA)) {
                    endOfRegistration(update);
                } else if (userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Исправление")) {
                    amendField(update);
                } else if (userDTOHashMap.get(chatId).getCommand().equalsIgnoreCase("Исправленный")) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    UserDTO userDTO = userDTOHashMap.get(chatId);
                    if (callBackQueryData.equalsIgnoreCase("Имя")) {
                        userDTO.setCommand("Исправлено имя");
                        userDTOHashMap.replace(chatId, userDTO);
                        nameRegistration(update);
                    } else if (callBackQueryData.equalsIgnoreCase("Фамилия")) {
                        userDTO.setCommand("Исправлена фамилия");
                        userDTOHashMap.replace(chatId, userDTO);
                        lastNameRegistration(update);
                    } else if (callBackQueryData.equalsIgnoreCase("Отчество")) {
                        userDTO.setCommand("Исправлено отчество");
                        userDTOHashMap.replace(chatId, userDTO);
                        patronymicRegistration(update);
                    } else if (callBackQueryData.equalsIgnoreCase("Налет")) {
                        userDTO.setCommand("Исправлен налет");
                        userDTOHashMap.replace(chatId, userDTO);
                        flightTimeRegistration(update);
                    }
                }
            } else if (callBackQueryData.equalsIgnoreCase("Удалить пользователя")) {
                activeChatRepository.deleteByChatId(chatId);
                SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Пользователь удален");
                execute(sendMessage);
            } else if (callBackQueryData.equalsIgnoreCase("Не удалять пользователя")) {
                SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Пользователь не удален");
                execute(sendMessage);
            }
        }
    }


    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @SneakyThrows
    public void startingRegistration(Update update) {
        Message message = update.getMessage();
        if (activeChatRepository.findAllByChatId(message.getChatId()).isEmpty()) {
            activeChatRepository.save(new ActiveChat(message.getChatId()));
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Выберите должность");
            sendMessage.setReplyMarkup(MarkupUtil.getInstrOrStudentMarkup());
            if (userDTOHashMap.get(message.getChatId()) != null && userDTOHashMap.get(message.getChatId()).getCommand().equalsIgnoreCase("Исправленный")) {
                sendMessage.setText(dataChecker(update));
                execute(sendMessage);
            }
            execute(sendMessage);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
            sendMessage.setText("Такой пользователь уже существует");
            execute(sendMessage);
        }
    }

    @SneakyThrows
    public void instrOrStudentRegistration(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        UserDTO userDTO;
        if (update.getCallbackQuery().getData().equalsIgnoreCase(INSTRUCTOR)) {
            sendMessage.setText("Выбрана должность инструктор\n\nВведите Ваше имя");
            userDTO = new UserDTO();
            userDTO.setPromoInstructor(new Instructor());
            userDTO.getPromoInstructor().setChatId(message.getChatId());
            userDTO.setCommand("Введите Ваше имя");
            userDTOHashMap.put(message.getChatId(), userDTO);
            execute(sendMessage);
        } else if (update.getCallbackQuery().getData().equalsIgnoreCase(STUDENT)) {
            sendMessage.setText("Выбрана должность курсант\n\nВведите Ваше имя");
            userDTO = new UserDTO();
            userDTO.setPromoStudent(new Student());
            userDTO.getPromoStudent().setChatId(message.getChatId());
            userDTO.setCommand("Введите Ваше имя");
            userDTOHashMap.put(message.getChatId(), userDTO);
            execute(sendMessage);
        }
    }

    @SneakyThrows
    public void nameRegistration(Update update) {
        Message message = update.getMessage();
        UserDTO userDtoForName = userDTOHashMap.get(message.getChatId());
        SendMessage sendMessage = new SendMessage(String.valueOf(message.getChatId()), "Введите Вашу фамилию");
        if (userDtoForName.getPromoInstructor() != null) {
            userDtoForName.getPromoInstructor().setFirstName(message.getText());
        } else {
            userDtoForName.getPromoStudent().setFirstName(message.getText());
        }
        if (userDtoForName.getCommand().equalsIgnoreCase("Исправлено имя")) {
            sendMessage.setText(dataChecker(update));
            sendMessage.setReplyMarkup(MarkupUtil.getYesNoMarkup());
            userDtoForName.setCommand(CHECK_DATA);
        } else {
            userDtoForName.setCommand("Введите Вашу фамилию");
        }
        userDTOHashMap.replace(message.getChatId(), userDtoForName);
        execute(sendMessage);
    }

    @SneakyThrows
    public void lastNameRegistration(Update update) {
        Message message = update.getMessage();
        UserDTO userDtoForLastName = userDTOHashMap.get(message.getChatId());
        SendMessage sendMessage = new SendMessage(String.valueOf(message.getChatId()), "Введите Ваше отчество");
        if (userDtoForLastName.getPromoInstructor() != null) {
            userDtoForLastName.getPromoInstructor().setLastName(message.getText());
        } else {
            userDtoForLastName.getPromoStudent().setLastName(message.getText());
        }
        if (userDtoForLastName.getCommand().equalsIgnoreCase("Исправлена фамилия")) {
            sendMessage.setText(dataChecker(update));
            sendMessage.setReplyMarkup(MarkupUtil.getYesNoMarkup());
            userDtoForLastName.setCommand(CHECK_DATA);
        } else {
            userDtoForLastName.setCommand("Введите Ваше отчество");
        }
        userDTOHashMap.replace(message.getChatId(), userDtoForLastName);
        execute(sendMessage);
    }

    @SneakyThrows
    public void patronymicRegistration(Update update) {
        Message message = update.getMessage();
        UserDTO userDTOForPatronymic = userDTOHashMap.get(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if (userDTOForPatronymic.getPromoInstructor() != null) {
            userDTOForPatronymic.getPromoInstructor().setPatronymic(message.getText());
            sendMessage.setText("Введите оставшееся время от сан-нормы");
        } else if (userDTOForPatronymic.getPromoStudent() != null) {
            userDTOForPatronymic.getPromoStudent().setPatronymic(message.getText());
            sendMessage.setText("Введите Ваш налёт");
        }
        if (userDTOForPatronymic.getCommand().equalsIgnoreCase("Исправлено отчество")) {
            sendMessage.setText(dataChecker(update));
            sendMessage.setReplyMarkup(MarkupUtil.getYesNoMarkup());
            userDTOForPatronymic.setCommand(CHECK_DATA);
        } else {
            userDTOForPatronymic.setCommand("Введите налёт");
        }
        userDTOHashMap.replace(message.getChatId(), userDTOForPatronymic);
        execute(sendMessage);
    }

    @SneakyThrows
    public void flightTimeRegistration(Update update) {
        Message message = update.getMessage();
        UserDTO userDtoForFlightTime = userDTOHashMap.get(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if (checkIfTime(message.getText())) {
            if (userDtoForFlightTime.getPromoInstructor() != null) {
                userDtoForFlightTime.getPromoInstructor().setFlightTimeLeft(message.getText());
            } else {
                userDtoForFlightTime.getPromoStudent().setTotalFlightTime(message.getText());
            }
            userDtoForFlightTime.setCommand(CHECK_DATA);
            userDTOHashMap.replace(message.getChatId(), userDtoForFlightTime);
            if (userDtoForFlightTime.getPromoInstructor() != null) {
                sendMessage.setText(dataChecker(update));
            } else {
                sendMessage.setText(dataChecker(update));
            }
            if (userDtoForFlightTime.getCommand().equalsIgnoreCase("Исправлен налет")) {
                sendMessage.setText(dataChecker(update));
                sendMessage.setReplyMarkup(MarkupUtil.getYesNoMarkup());
                userDtoForFlightTime.setCommand(CHECK_DATA);
                userDTOHashMap.replace(message.getChatId(), userDtoForFlightTime);
                execute(sendMessage);
            }
            sendMessage.setReplyMarkup(MarkupUtil.getYesNoMarkup());
            execute(sendMessage);
        } else {
            sendMessage.setText("Введено неверное значение\nВведите значение в формате HH:mm");
            execute(sendMessage);
        }
    }

    @SneakyThrows
    public void endOfRegistration(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if (update.getCallbackQuery().getData().equalsIgnoreCase("ДА")) {
            if (userDTOHashMap.get(message.getChatId()).getPromoInstructor() != null) {
                instructorsRepository.save(userDTOHashMap.get(message.getChatId()).getPromoInstructor());
            } else if (userDTOHashMap.get(message.getChatId()).getPromoStudent() != null) {
                studentsRepository.save(userDTOHashMap.get(message.getChatId()).getPromoStudent());
            }
            sendMessage.setText("\nДанные внесены");
            userDTOHashMap.remove(message.getChatId());
        } else if (update.getCallbackQuery().getData().equalsIgnoreCase("НЕТ")) {
            sendMessage.setReplyMarkup(MarkupUtil.getAmendMarkup());
            UserDTO userDTOAmend = userDTOHashMap.get(message.getChatId());
            userDTOAmend.setCommand("Исправление");
            userDTOHashMap.replace(message.getChatId(), userDTOAmend);
            sendMessage.setText("Выберите поле, которое нужно исправить");
        }
        execute(sendMessage);
    }

    @SneakyThrows
    public void amendField(Update update) {
        String callBack = update.getCallbackQuery().getData();
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(String.valueOf(chatId));
        UserDTO amendedUserDTO = userDTOHashMap.get(chatId);
        if (callBack.equalsIgnoreCase("Должность")) {
            sendMessage.setReplyMarkup(MarkupUtil.getInstrOrStudentMarkup());
            amendedUserDTO.setCommand("Исправленная должность");
            sendMessage.setText("Выберите должность");
        } else if (callBack.equalsIgnoreCase("Имя")) {
            sendMessage.setText("Введите Ваше имя");
            amendedUserDTO.setCommand("Исправлено имя");
        } else if (callBack.equalsIgnoreCase("Фамилия")) {
            sendMessage.setText("Введите Вашу фамилию");
            amendedUserDTO.setCommand("Исправлена фамилия");
        } else if (callBack.equalsIgnoreCase("Отчество")) {
            sendMessage.setText("Введите Ваше отчество");
            amendedUserDTO.setCommand("Исправлено отчество");
        } else if (callBack.equalsIgnoreCase("Налет")) {
            if (userDTOHashMap.get(chatId).getPromoInstructor() != null) {
                sendMessage.setText("Введите остаток сан-нормы");
            } else if (userDTOHashMap.get(chatId).getPromoStudent() != null) {
                sendMessage.setText("Введите налет");
            }
            amendedUserDTO.setCommand("Исправлен налет");
        }
        userDTOHashMap.replace(chatId, amendedUserDTO);
        execute(sendMessage);
    }

    @SneakyThrows
    public void getProfile(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        if (activeChatRepository.findAllByChatId(update.getMessage().getChatId()).isPresent()) {
            if (instructorsRepository.findAllByChatId(update.getMessage().getChatId()).isPresent()) {
                sendMessage.setText(instructorsRepository.findAllByChatId(update.getMessage().getChatId()).get().toString());
            } else if (studentsRepository.findAllByChatId(update.getMessage().getChatId()).isPresent()) {
                sendMessage.setText(studentsRepository.findAllByChatId(update.getMessage().getChatId()).get().toString());
            } else {
                sendMessage.setText("Данного пользователя не существует\nДля регистрации пользователя введите /registration");
            }
            execute(sendMessage);
        } else {
            sendMessage.setText("Данного пользователя не существует\nДля регистрации пользователя введите /registration");
            execute(sendMessage);
        }
    }

    @SneakyThrows
    public String dataChecker(Update update) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }
        UserDTO userDtoChecker = userDTOHashMap.get(chatId);
        String data = null;
        if (userDtoChecker.getPromoInstructor() != null) {
            Instructor promoInstructor = userDtoChecker.getPromoInstructor();
            data = "Проверьте Ваши данные: " + "\n" +
                    "Должность: Инструктор\n" +
                    "Имя: " + promoInstructor.getFirstName() + "\n" +
                    "Фамилия: " + promoInstructor.getLastName() + "\n" +
                    "Отчество: " + promoInstructor.getPatronymic() + "\n" +
                    "Остаток сан-нормы: " + promoInstructor.getFlightTimeLeft() + "\n\n" +
                    "Данные указаны верно?";
        } else if (userDtoChecker.getPromoStudent() != null) {
            Student promoStudent = userDtoChecker.getPromoStudent();
            data = "Проверьте Ваши данные: " + "\n" +
                    "Должность: Курсант\n" +
                    "Имя: " + promoStudent.getFirstName() + "\n" +
                    "Фамилия: " + promoStudent.getLastName() + "\n" +
                    "Отчество: " + promoStudent.getPatronymic() + "\n" +
                    "Часы налета: " + promoStudent.getTotalFlightTime() + "\n\n" +
                    "Данные указаны верно?";
        }
        return data;
    }

    public boolean checkIfTime(String flightTime) {
        String[] hoursAndMinutes = flightTime.split(":");
        if (hoursAndMinutes.length > 2 || hoursAndMinutes.length == 0) return false;
        if (flightTime.equals(hoursAndMinutes[0])) return false;
        if (Integer.parseInt(hoursAndMinutes[1]) >= 60 || Integer.parseInt(hoursAndMinutes[1]) < 0) return false;
        return Integer.parseInt(hoursAndMinutes[0]) >= 0;
    }

    @SneakyThrows
    public void deleteUser(Update update) {
        Long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (activeChatRepository.findAllByChatId(chatId).isPresent()) {
            if (instructorsRepository.findAllByChatId(chatId).isPresent()) {
                sendMessage.setText("Вы уверены, что хотите удалить данного пользователя?" + "\n" + instructorsRepository.findAllByChatId(chatId).get());
            } else if (studentsRepository.findAllByChatId(chatId).isPresent()) {
                sendMessage.setText("Вы уверены, что хотите удалить данного пользователя?" + "\n" + studentsRepository.findAllByChatId(chatId).get());
            }
            sendMessage.setReplyMarkup(MarkupUtil.getDeleteUserMarkup());
            execute(sendMessage);
        } else {
            sendMessage.setText("Пользователя не найдено");
            execute(sendMessage);
        }
    }


}
