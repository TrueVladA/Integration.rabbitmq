package ru.bpmcons.sbi_elma.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import ru.bpmcons.sbi_elma.exceptions.BadRequestException;

import java.io.IOException;

/**
 * Класс создан для перехвата ошибок, возвращённых от public api elma
 */
public class BadRequestErrorHandler implements ResponseErrorHandler {
    Logger logger = LoggerFactory.getLogger(BadRequestErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode() == HttpStatus.BAD_REQUEST || response.getStatusCode() == HttpStatus.GONE;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String responseBody = new String(response.getBody().readAllBytes());
        /**
         * Возникает из-за того, что в метаданных остался id какой-либо карточки метаданных или создателя / редактора
         * Когда запрашиваем /get этих "несуществующих" данных, api elma выдаёт bad_request и сообщение not_found
         * Редка ошибка. Ранее прерывала crud операции, сейчас мы перехватываем ошибку и возвращаем понятный модулю ответ
         * содержащий json {"success": false}
         */
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST && responseBody.contains("not found")) {
            logger.error("Ресурс в документе был удалён, но остался в UI. Вместо этого объекта, в ответном сообщении, будет пустой объект.");
            logger.error(responseBody);
            logger.error(response.getStatusText());
            logger.error(response.getStatusCode().toString());
            throw new BadRequestException("{\"success\": false}",
                    new LinkedMultiValueMap<>(),
                    HttpStatus.resolve(response.getRawStatusCode()));
        }
        /**
         * deprecated
         * При рефакторе можно удалить
         */
        if (response.getStatusCode() == HttpStatus.GONE && responseBody.contains("Не найдена блокировка")) {
            logger.error(responseBody);
            logger.error(response.getStatusText());
            logger.error(response.getStatusCode().toString());
        }
        /**
         * deprecated
         * При рефакторе можно удалить
         */
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST && responseBody.contains("Field with Code:")) {
            logger.error("При использовании публичного api возникла ошибка: в json запросе лишняя переменная.");
            logger.error(responseBody);
            throw new IllegalArgumentException(responseBody);
        }
    }
}
