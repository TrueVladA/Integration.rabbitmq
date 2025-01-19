package ru.bpmcons.sbi_elma.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class RandomString {
    private final String DOC_CHARS = "йцукенгшщзхъфывапролджэячсмитьбю1234567890".toUpperCase();

    public String documentId(int length) {
        Random random = new Random();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < length; i++) {
            str.append(DOC_CHARS.charAt(random.nextInt(DOC_CHARS.length())));
        }
        return str.toString();
    }
}
