package ru.bpmcons.sbi_elma;

import lombok.experimental.UtilityClass;
import ru.bpmcons.sbi_elma.infra.version.Version;

/**
 * Класс для хранения версий АПИ и быстрого перехода на все места, где они используются
 */
@UtilityClass
public class Versions {
    public static Version V_1_1_18 = new Version(1, 1, 18);
    public static Version V_1_1_20 = new Version(1, 1, 20);
}
