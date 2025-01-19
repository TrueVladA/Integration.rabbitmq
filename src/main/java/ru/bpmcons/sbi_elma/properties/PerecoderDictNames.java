package ru.bpmcons.sbi_elma.properties;

public enum PerecoderDictNames {
    ROLETYPES("RoleTypes"),
    PARTYTYPES("PartyTypes"),
    FILETYPES("FileTypes"),
    DOCTYPES("DocTypes"),
    CONTRACTTYPES("ContractTypes"),
    PRODUCTLINE("ProductLineTypes"),
    TYPEIDENTITYDOC("DulTypes"),
    PRODUCT("InsuranceProducts");

    private String name;

    PerecoderDictNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
