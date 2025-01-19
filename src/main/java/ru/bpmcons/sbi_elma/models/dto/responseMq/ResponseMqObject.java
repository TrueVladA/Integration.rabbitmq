package ru.bpmcons.sbi_elma.models.dto.responseMq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.amqp.core.Message;

@Data
@AllArgsConstructor
public class ResponseMqObject implements Cloneable {
//    private Channel channel;
    private String exchange;
    private String replyTo;
//    private AMQP.BasicProperties properties;
    private String payload;
    private Message message;
    private boolean testRequest;

    @Override
    public ResponseMqObject clone() {
        try {
            ResponseMqObject clone = (ResponseMqObject) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
