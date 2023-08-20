package com.flightbook.TgFlightBook.services;

import com.flightbook.TgFlightBook.DTO.UserDTO;
import com.flightbook.TgFlightBook.entities.ActiveChat;
import com.flightbook.TgFlightBook.entities.Instructor;
import com.flightbook.TgFlightBook.entities.Student;
import com.flightbook.TgFlightBook.repositories.ActiveChatRepository;
import com.flightbook.TgFlightBook.repositories.InstructorsRepository;
import com.flightbook.TgFlightBook.repositories.StudentsRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
    private String previousCommand;
    private UserDTO userDTO;
    private final HashMap<Long, UserDTO> userDTOHashMap = new HashMap<>();
    public final ActiveChatRepository activeChatRepository;
    public final InstructorsRepository instructorsRepository;
    public final StudentsRepository studentsRepository;
    private static final String TEST = "/test";
    private static final String REGISTRATION = "/registration";
    public static final String PROFILE = "/profile";
    private static final String INSTRUCTOR = "Инструктор";
    private static final String STUDENT = "Курсант";
    @PostConstruct
    public void start(){
        log.info("username: {}, token: {}", name, apiKey);
    }

    @Override
    public String getBotToken() {
        return apiKey;
    }
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && REGISTRATION.equalsIgnoreCase(update.getMessage().getText())){
           startingRegistration(update);
        } else if (update.hasCallbackQuery() && (update.getCallbackQuery().getData().equalsIgnoreCase(INSTRUCTOR) ||
                update.getCallbackQuery().getData().equalsIgnoreCase(STUDENT))) {
            instrOrStudentRegistration(update);
        }else if (update.hasMessage() && activeChatRepository.findAllByChatId(update.getMessage().getChatId()).isPresent() && userDTOHashMap.get(update.getMessage().getChatId()) != null && userDTOHashMap.get(update.getMessage().getChatId()).getPreviousCommand().equalsIgnoreCase("Введите Ваше имя")){
            nameRegistration(update);
        } else if (update.hasMessage() && activeChatRepository.findAllByChatId(update.getMessage().getChatId()).isPresent() && userDTOHashMap.get(update.getMessage().getChatId()) != null && userDTOHashMap.get(update.getMessage().getChatId()).getPreviousCommand().equalsIgnoreCase("Введите Вашу фамилию")) {
           lastNameRegistration(update);
        }else if(update.hasMessage() && activeChatRepository.findAllByChatId(update.getMessage().getChatId()).isPresent() && userDTOHashMap.get(update.getMessage().getChatId()) != null && userDTOHashMap.get(update.getMessage().getChatId()).getPreviousCommand().equalsIgnoreCase("Введите налёт")){
            flightTimeRegistration(update);
        }else if(update.hasCallbackQuery() && activeChatRepository.findAllByChatId(update.getCallbackQuery().getMessage().getChatId()).isPresent() && userDTOHashMap.get(update.getMessage().getChatId()) != null && userDTOHashMap.get(update.getCallbackQuery().getMessage().getChatId()).getPreviousCommand().equalsIgnoreCase("Проверьте Ваши данные")){
            endOfRegistration(update);
        }else if(update.hasMessage() && PROFILE.equalsIgnoreCase(update.getMessage().getText())){
            getProfile(update);
        }
    }

    @Override
    public String getBotUsername() {
        return name;
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
    public void startingRegistration(Update update){
        Message message = update.getMessage();
        System.out.println(message);
        if(activeChatRepository.findAllByChatId(message.getChatId()).isEmpty()){
            activeChatRepository.save(new ActiveChat(message.getChatId()));
            SendMessage sendMessage = new SendMessage();
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

            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Выберите должность");
            sendMessage.setReplyMarkup(markup);
            execute(sendMessage);
        }else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
            sendMessage.setText("Такой пользователь уже существует");
            execute(sendMessage);
        }
    }
    @SneakyThrows
    public void instrOrStudentRegistration(Update update){
        Message message = update.getCallbackQuery().getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if(update.getCallbackQuery().getData().equalsIgnoreCase(INSTRUCTOR)){
            sendMessage.setText("Выбрана должность инструктор\nВведите Ваше имя");
            userDTO = new UserDTO();
            userDTO.setPromoInstructor(new Instructor());
            userDTO.getPromoInstructor().setChatId(message.getChatId());
            userDTO.setPreviousCommand("Введите Ваше имя");
            userDTOHashMap.put(message.getChatId(), userDTO);
            execute(sendMessage);
        }else if(update.getCallbackQuery().getData().equalsIgnoreCase(STUDENT)){
            sendMessage.setText("Выбрана должность курсант\nВведите Ваше имя");
            userDTO = new UserDTO();
            userDTO.setPromoStudent(new Student());
            userDTO.getPromoStudent().setChatId(message.getChatId());
            userDTO.setPreviousCommand("Введите Ваше имя");
            userDTOHashMap.put(message.getChatId(), userDTO);
            System.out.println(userDTOHashMap.get(message.getChatId()).getPreviousCommand());
            execute(sendMessage);
        }
    }
    @SneakyThrows
    public void nameRegistration(Update update){
        Message message = update.getMessage();
        UserDTO userDtoForName = userDTOHashMap.get(message.getChatId());
        SendMessage sendMessage = new SendMessage(String.valueOf(message.getChatId()), "Введите Вашу фамилию");
        if(userDtoForName.getPromoInstructor() != null){
            userDtoForName.getPromoInstructor().setFirstName(message.getText());
        }else {
            userDtoForName.getPromoStudent().setFirstName(message.getText());
        }
        userDtoForName.setPreviousCommand("Введите Вашу фамилию");
        userDTOHashMap.replace(message.getChatId(), userDtoForName);
        execute(sendMessage);
    }
    @SneakyThrows
    public void lastNameRegistration(Update update){
        Message message = update.getMessage();
        UserDTO userDtoForLastName = userDTOHashMap.get(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        if(userDtoForLastName.getPromoInstructor() != null){
            userDtoForLastName.getPromoInstructor().setLastName(message.getText());
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Введите оставшееся время от сан-нормы");
        }else {
            userDtoForLastName.getPromoStudent().setLastName(message.getText());
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Введите Ваш налёт");
        }
        userDtoForLastName.setPreviousCommand("Введите налёт");
        userDTOHashMap.replace(message.getChatId(), userDtoForLastName);
        execute(sendMessage);
    }
    @SneakyThrows
    public void flightTimeRegistration(Update update){
        Message message = update.getMessage();
        UserDTO userDtoForFlightTime = userDTOHashMap.get(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        try{
            dateFormat.parse(message.getText());
            if(userDtoForFlightTime.getPromoInstructor() != null){
                userDtoForFlightTime.getPromoInstructor().setFlightTimeLeft(message.getText());
            }else {
                userDtoForFlightTime.getPromoStudent().setTotalFlightTime(message.getText());
            }
            userDtoForFlightTime.setPreviousCommand("Проверьте Ваши данные");
            userDTOHashMap.replace(message.getChatId(), userDtoForFlightTime);
            if (userDtoForFlightTime.getPromoInstructor() != null){
                Instructor promoInstructor = userDtoForFlightTime.getPromoInstructor();
                sendMessage.setText("Должность: Инструктор\n" +
                        "Имя: " + promoInstructor.getFirstName() + "\n" +
                        "Фамилия: " + promoInstructor.getLastName() + "\n" +
                        "Остаток сан-нормы: "  + promoInstructor.getFlightTimeLeft() + "\n\n" +
                        "Данные указаны верно?");
            }else {
                Student promoStudent = userDTO.getPromoStudent();
                sendMessage.setText("Должность: Курсант\n" +
                        "Имя: " + promoStudent.getFirstName() + "\n" +
                        "Фамилия: " + promoStudent.getLastName() + "\n" +
                        "Часы налета: "  + promoStudent.getTotalFlightTime() + "\n\n" +
                        "Данные указаны верно?");
            }
            InlineKeyboardMarkup finalCheckMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            InlineKeyboardButton yesButton = new InlineKeyboardButton();
            yesButton.setText("Да");
            yesButton.setCallbackData("ДА");
            InlineKeyboardButton noButton = new InlineKeyboardButton();
            noButton.setText("Нет");
            noButton.setCallbackData("Нет");
            rows.add(List.of(yesButton, noButton));
            finalCheckMarkup.setKeyboard(rows);
            sendMessage.setReplyMarkup(finalCheckMarkup);
            execute(sendMessage);
        }catch (DateTimeParseException | ParseException e){
            sendMessage.setText("Введено неверное значение\nВведите значение в формате HH:mm");
            execute(sendMessage);
        }
    }
    @SneakyThrows
    public void endOfRegistration(Update update){
        Message message = update.getCallbackQuery().getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if(update.getCallbackQuery().getData().equalsIgnoreCase("ДА")){
            sendMessage.setText("Данные введены корректно");
            if(userDTOHashMap.get(message.getChatId()).getPromoInstructor() != null){
                instructorsRepository.save(userDTOHashMap.get(message.getChatId()).getPromoInstructor());
            }else if(userDTOHashMap.get(message.getChatId()).getPromoStudent() != null){
                studentsRepository.save(userDTOHashMap.get(message.getChatId()).getPromoStudent());
            }
            sendMessage.setText("\nДанные внесены в таблицу");
        }else if (update.getCallbackQuery().getData().equalsIgnoreCase("НЕТ")){
            sendMessage.setText("Данные введены некорректно)))))");
        }
        execute(sendMessage);
    }
    @SneakyThrows
    public void getProfile(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        if(activeChatRepository.findAllByChatId(update.getMessage().getChatId()).isPresent()){
            if(instructorsRepository.findAllByChatId(update.getMessage().getChatId()).isPresent()){
                sendMessage.setText(instructorsRepository.findAllByChatId(update.getMessage().getChatId()).get().toString());
            }else if(studentsRepository.findAllByChatId(update.getMessage().getChatId()).isPresent()){
                sendMessage.setText(studentsRepository.findAllByChatId(update.getMessage().getChatId()).get().toString());
            }else {
                sendMessage.setText("Данного пользователя не существует\nДля регистрации пользователя введите /registration");
            }
            execute(sendMessage);
        }else {
            sendMessage.setText("Данного пользователя не существует\nДля регистрации пользователя введите /registration");
            execute(sendMessage);
        }
    }

}
