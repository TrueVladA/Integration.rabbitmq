package ru.bpmcons.sbi_elma.properties;

public enum PerecoderGroupNames {
    EADOC("EADOC"),
    SPHERE("SPHERE"),
    VIRTU("VIRTU");

    private String name;

    PerecoderGroupNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
