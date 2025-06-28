package com.ga.cfbot.interfaces;

import com.ga.cfbot.infrastructure.codeforces.CodeforcesApiService;
import com.ga.cfbot.application.service.GroupService;
import com.ga.cfbot.application.service.MembershipService;
import com.ga.cfbot.domain.model.Group;
import com.ga.cfbot.domain.model.GroupMembership;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotService {

    private final ObjectProvider<CfGroupBot> botProvider;
    private final GroupService groupService;
    private final MembershipService membershipService;
    private final CodeforcesApiService cfApi;

    private enum State { NONE, WAIT_GROUP_NAME, WAIT_CF_HANDLE }
    private final Map<Long, State> userState   = new HashMap<>();
    private final Map<Long, Long>  targetGroup = new HashMap<>();

    public void handleMessage(Message msg) throws Exception {
        Long chatId = msg.getChatId();
        String text  = msg.getText();
        log.info("Message from {} ({}): {}", msg.getFrom().getUserName(), chatId, text);

        State st = userState.getOrDefault(chatId, State.NONE);

        if (st == State.WAIT_GROUP_NAME) {
            groupService.create(text, chatId);
            send(chatId, "Группа '" + text + "' создана", mainMenu());
            userState.put(chatId, State.NONE);
            return;
        }
        if (st == State.WAIT_CF_HANDLE) {
            Long gid = targetGroup.remove(chatId);
            membershipService.add(gid, text);
            send(chatId, "Участник '" + text + "' добавлен", groupMenu(gid));
            userState.put(chatId, State.NONE);
            return;
        }

        switch (text) {
            case "/start" ->
                    send(chatId, "Привет! Выберите действие:", mainMenu());
            case "Создать группу" -> {
                send(chatId, "Введите название группы:", null);
                userState.put(chatId, State.WAIT_GROUP_NAME);
            }
            case "Мои группы" -> {
                List<Group> groups = groupService.list(chatId);
                send(chatId, "Ваши группы:", groupsList(groups));
            }
            default ->
                    send(chatId, "Неизвестная команда", mainMenu());
        }
    }

    public void handleCallback(CallbackQuery cq) throws Exception {
        Long chatId = cq.getMessage().getChatId();
        String data  = cq.getData();
        log.info("Callback from {}: {}", chatId, data);

        if (data.startsWith("group:")) {
            Long gid = Long.parseLong(data.split(":")[1]);
            send(chatId, "Меню группы:", groupMenu(gid));
        }
        else if (data.startsWith("adduser:")) {
            Long gid = Long.parseLong(data.split(":")[1]);
            targetGroup.put(chatId, gid);
            userState.put(chatId, State.WAIT_CF_HANDLE);
            send(chatId, "Введите handle Codeforces:", null);
        }
        else if (data.startsWith("remuser:")) {
            Long gid = Long.parseLong(data.split(":")[1]);
            send(chatId, "Выберите участника для удаления:", deleteUserKeyboard(gid));
        }
        else if (data.startsWith("dodel:")) {
            String[] p = data.split(":");
            Long mid = Long.valueOf(p[1]), gid = Long.valueOf(p[2]);
            membershipService.remove(mid);
            send(chatId, "Участник удалён", groupMenu(gid));
        }
        else if (data.startsWith("showrating:")) {
            Long gid = Long.parseLong(data.split(":")[1]);
            StringBuilder sb = new StringBuilder("Рейтинги группы:\n");
            List<GroupMembership> members = membershipService.list(gid);
            List<Pair> ratings = new ArrayList<>();

            for (GroupMembership m : members) {
                String handle = m.getMembershipCodeforcesName();
                Integer rating = cfApi.fetchRating(handle);
                if (rating != null) ratings.add(new Pair(handle, rating));
                else membershipService.remove(m.getId());
            }

            ratings.sort((a, b) -> Integer.compare(b.rating, a.rating));

            for (Pair p : ratings) {
                sb.append(p.handle)
                        .append(" — ")
                        .append(p.rating)
                        .append("\n");
            }

            send(chatId, sb.toString(), groupMenu(gid));
        }
        else if (data.startsWith("deletegroup:")) {
            Long gid = Long.parseLong(data.split(":")[1]);
            groupService.delete(gid);
            List<Group> groups = groupService.list(chatId);
            send(chatId, "Группа удалена", groupsList(groups));
        }
        else if (data.equals("mainmenu")) {
            send(chatId, "Привет! Выберите действие:", mainMenu());
            userState.put(chatId, State.NONE);
        }
    }

    private void send(Long chatId, String text,
                      org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard kb) throws Exception {
        SendMessage sm = new SendMessage(chatId.toString(), text);
        if (kb != null) sm.setReplyMarkup(kb);
        botProvider.getObject().execute(sm);
    }

    private ReplyKeyboardMarkup mainMenu() {
        ReplyKeyboardMarkup mk = new ReplyKeyboardMarkup();
        mk.setResizeKeyboard(true);
        mk.setOneTimeKeyboard(true);
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Создать группу"));
        row.add(new KeyboardButton("Мои группы"));
        mk.setKeyboard(Collections.singletonList(row));
        return mk;
    }

    private InlineKeyboardMarkup groupsList(List<Group> groups) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Group g : groups) {
            rows.add(List.of(btn(g.getName(), "group:" + g.getId())));
        }
        rows.add(List.of(btn("Назад", "mainmenu")));
        InlineKeyboardMarkup mk = new InlineKeyboardMarkup();
        mk.setKeyboard(rows);
        return mk;
    }

    private InlineKeyboardMarkup groupMenu(Long gid) {
        List<List<InlineKeyboardButton>> rows = List.of(
                List.of(
                        btn("Добавить участника", "adduser:" + gid),
                        btn("Удалить участника",  "remuser:" + gid)
                ),
                List.of(
                        btn("Показать рейтинги",   "showrating:" + gid),
                        btn("Удалить группу",      "deletegroup:" + gid)
                ),
                List.of(
                        btn("Назад",               "mainmenu")
                )
        );
        InlineKeyboardMarkup mk = new InlineKeyboardMarkup();
        mk.setKeyboard(rows);
        return mk;
    }

    private InlineKeyboardMarkup deleteUserKeyboard(Long gid) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (GroupMembership m : membershipService.list(gid)) {
            rows.add(List.of(btn(m.getMembershipCodeforcesName(),
                    "dodel:" + m.getId() + ":" + gid)));
        }
        rows.add(List.of(btn("Отмена", "group:" + gid)));
        InlineKeyboardMarkup mk = new InlineKeyboardMarkup();
        mk.setKeyboard(rows);
        return mk;
    }

    private InlineKeyboardButton btn(String text, String data) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(data);
        return b;
    }

    private static class Pair {
        String handle;
        Integer rating;

        Pair(String handle, Integer rating) {
            this.handle = handle;
            this.rating = rating;
        }
    }
}
