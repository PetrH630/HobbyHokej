package cz.phsoft.hokej.data.enums;

public enum NotificationType {

    // REGISTRATION
    MATCH_REGISTRATION_CREATED(NotificationCategory.REGISTRATION, true),
    MATCH_REGISTRATION_UPDATED(NotificationCategory.REGISTRATION, true),
    MATCH_REGISTRATION_CANCELED(NotificationCategory.REGISTRATION, true),
    MATCH_REGISTRATION_RESERVED(NotificationCategory.REGISTRATION, true),
    MATCH_REGISTRATION_SUBSTITUTE(NotificationCategory.REGISTRATION, false),
    MATCH_WAITING_LIST_MOVED_UP(NotificationCategory.REGISTRATION, true),
    MATCH_REGISTRATION_NO_RESPONSE(NotificationCategory.REGISTRATION, false),
    PLAYER_EXCUSED(NotificationCategory.REGISTRATION, true),
    PLAYER_NO_EXCUSED(NotificationCategory.REGISTRATION, true),

    // MATCH_INFO
    MATCH_REMINDER(NotificationCategory.MATCH_INFO, true),
    MATCH_CANCELED(NotificationCategory.MATCH_INFO, true),
    MATCH_UNCANCELED(NotificationCategory.MATCH_INFO, true),
    MATCH_TIME_CHANGED(NotificationCategory.MATCH_INFO, true),

    // SYSTEM – hráčské/uživatelské události
    PLAYER_CREATED(NotificationCategory.SYSTEM, true),
    PLAYER_UPDATED(NotificationCategory.SYSTEM, false),
    PLAYER_APPROVED(NotificationCategory.SYSTEM, true),
    PLAYER_REJECTED(NotificationCategory.SYSTEM, true),
    PLAYER_DELETED(NotificationCategory.SYSTEM, true),
    PLAYER_CHANGE_USER(NotificationCategory.SYSTEM, true),

    //USER
    USER_CREATED(NotificationCategory.SYSTEM, true),
    USER_ACTIVATED(NotificationCategory.SYSTEM, true),
    USER_DEACTIVATED(NotificationCategory.SYSTEM, true),
    USER_UPDATED(NotificationCategory.SYSTEM, true),


    // SYSTEM – SECURITY
    PASSWORD_RESET(NotificationCategory.SYSTEM, true),
    USER_CHANGE_PASSWORD(NotificationCategory.SYSTEM, true),
    FORGOTTEN_PASSWORD_RESET_REQUEST(NotificationCategory.SYSTEM, true),
    FORGOTTEN_PASSWORD_RESET_COMPLETED(NotificationCategory.SYSTEM, true),
    SECURITY_ALERT(NotificationCategory.SYSTEM, true);

    private final NotificationCategory category;
    private final boolean important;

    NotificationType(NotificationCategory category, boolean important) {
        this.category = category;
        this.important = important;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public boolean isImportant() {
        return important;
    }
}
