package ru.bpmcons.sbi_elma.elma.dto.common.filter.tf.operator;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class TimestampBetweenTfOperator extends TfOperator {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final ZonedDateTime min;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final ZonedDateTime max;

    public static TimestampBetweenTfOperator between(ZonedDateTime from, ZonedDateTime to) {
        return new TimestampBetweenTfOperator(from, to);
    }

    public static TimestampBetweenTfOperator from(ZonedDateTime from) {
        return new TimestampBetweenTfOperator(from, null);
    }

    public static TimestampBetweenTfOperator to(ZonedDateTime to) {
        return new TimestampBetweenTfOperator(null, to);
    }
}
