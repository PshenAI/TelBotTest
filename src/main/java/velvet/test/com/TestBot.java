package velvet.test.com;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TestBot extends TelegramLongPollingBot {

    private final CurrencyModeService currencyModeService = CurrencyModeService.getInstance();

    @Override
    public String getBotUsername() {
        return "--";
    }

    @Override
    public String getBotToken() {
        return "--";
    }


    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if(update.hasCallbackQuery()){
            handleCallback(update.getCallbackQuery());
        } else if(update.hasMessage()){
            handleMessage(update.getMessage());
        }
    }

    @SneakyThrows
    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String[] data = callbackQuery.getData().split(":");
        System.out.println(Arrays.toString(data));
        String action = data[0];
        Currency newCurrency = Currency.valueOf(data[1]);
        switch(action){
            case "ORIGINAL":
                currencyModeService.setOriginalCurrency(message.getChatId(), newCurrency);
                break;
            case "TARGET" :
                currencyModeService.setTargetCurrency(message.getChatId(), newCurrency);
                break;
        }
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
        for (Currency currency : Currency.values()) {
            buttons.add(
                    Arrays.asList(
                            InlineKeyboardButton.builder().text(getCurrencyButton(originalCurrency, currency))
                                    .callbackData("ORIGINAL:" + currency).build(),
                            InlineKeyboardButton.builder().text(getCurrencyButton(targetCurrency, currency))
                                    .callbackData("TARGET:" + currency).build()));
        }
        execute(EditMessageReplyMarkup.builder()
                .chatId(message.getChatId().toString())
                .messageId(message.getMessageId())
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                .build());
    }

    @SneakyThrows
    private void handleMessage(Message message) {
        //handle command
        if(message.hasText() && message.hasEntities()){
            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(a -> "bot_command".equals(a.getType())).findFirst();
            if(commandEntity.isPresent()){
                String command =
                        message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch(command){
                    case "/set_currency":
                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
                        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
                        for (Currency currency : Currency.values()) {
                            buttons.add(
                                    Arrays.asList(
                                            InlineKeyboardButton.builder().text(getCurrencyButton(originalCurrency, currency))
                                                    .callbackData("ORIGINAL:" + currency).build(),
                                            InlineKeyboardButton.builder().text(getCurrencyButton(targetCurrency, currency))
                                                    .callbackData("TARGET:" + currency).build()));
                        }
                        execute(SendMessage.builder()
                                .text("Please choose Original and Target currencies")
                                .chatId(message.getChatId().toString())
                                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                .build());
                        return;
                }
            }
        }
    }

    public String getCurrencyButton(Currency saved, Currency current){
        return saved == current ? current + "  ✅" : current.name();
    }


    @SneakyThrows
    public static void main(String[] args) {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TestBot());
    }
}
