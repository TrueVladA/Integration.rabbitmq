package ru.bpmcons.sbi_elma.utils;

public class Utils {
    public static String createMarkdown(String categories) {
        if (categories == null) {
            return null;
        }
        String[] split = categories.split("/");
        StringBuilder markDown = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            builder.append(split[i]);
            if (i != split.length - 1) {
                builder.append(" \n")
                        .append(markDown)
                        .append("- ");
                markDown.append("  ");
            }
        }
        return builder.toString();
    }

    public static String getStringFromMarkdown(String categories) {
        String[] split = categories.split("\n");
        StringBuilder markDown = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            categories = categories.replace(" \n" + markDown + "- ", "/");
            markDown.append("  ");
        }
        return categories;
    }

}
