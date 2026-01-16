package cz.phsoft.hokej.data.enums;

public enum PlayerMatchStatus {
    REGISTERED, // registrován
    UNREGISTERED, // odhlášen
    EXCUSED,  // omluven
    RESERVED, // náhradník
    NO_RESPONSE, // bez odpovědi
    NO_EXCUSED, // neomluven - byl registrován a nepřišel

}
