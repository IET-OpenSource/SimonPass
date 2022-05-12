package it.iet.interfaces.facade;

public interface EmailService {

    void sendEmail(String to, String subject, String body);
}
