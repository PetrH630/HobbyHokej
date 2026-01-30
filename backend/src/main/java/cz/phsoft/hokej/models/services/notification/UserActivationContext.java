package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;

public record UserActivationContext(
        AppUserEntity user,
        String activationLink
) {}
