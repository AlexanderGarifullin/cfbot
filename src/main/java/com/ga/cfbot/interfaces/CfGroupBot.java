package com.ga.cfbot.interfaces;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class CfGroupBot extends TelegramLongPollingBot {

    @Value("${bot.telegram.username}")
    private String username;

    @Value("${bot.telegram.token}")
    private String token;

    private final @Lazy BotService botService;

    @Override public String getBotUsername() { return username; }
    @Override public String getBotToken()    { return token; }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                botService.handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));
                botService.handleCallback(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }
}
