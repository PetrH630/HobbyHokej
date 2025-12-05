package cz.phsoft.hokej.models.services.sms;

public interface SmsService {
    void sendSms(String phoneNumber, String message);
}
