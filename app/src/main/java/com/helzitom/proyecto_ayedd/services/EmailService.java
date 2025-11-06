package com.helzitom.proyecto_ayedd.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//Servicio para el manejo de email
public class EmailService {

    private static final String TAG = "EmailService";
    private final ExecutorService executorService;
    private final Handler mainHandler;

    // Configuración del email el cuál enviará correos a cuentas, recuperación de contraseña, etc
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "margarita100101010@gmail.com";
    private static final String EMAIL_PASSWORD = "ssdt kjej qkkg vaun"; // Usa App Password de Google

    public interface EmailCallback {
        void onResult(boolean success);
    }

    public EmailService() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    //Método para enviar email de verificación
    public void sendVerificationEmail(String toEmail, String userName,
                                      String verificationCode, EmailCallback callback) {
        executorService.execute(() -> {
            boolean success = sendEmail(toEmail, userName, verificationCode);
            mainHandler.post(() -> callback.onResult(success));
        });
    }

    //Método para enviar email
    private boolean sendEmail(String toEmail, String userName, String verificationCode) {
        try {
            // Configurar propiedades
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            // Crear sesión (Autentificacón)
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            // Crear mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Código de Verificación - Tu App");

            String htmlContent = buildEmailContent(userName, verificationCode);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Envio
            Transport.send(message);
            Log.d(TAG, "Email enviado exitosamente a " + toEmail);
            return true;

        } catch (MessagingException e) {
            Log.e(TAG, "Error al enviar email: " + e.getMessage());
            return false;
        }
    }

    //Método que contiene el formato del correo de verificación de cuenta
    private String buildEmailContent(String userName, String verificationCode) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                "        .container { background-color: white; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }" +
                "        .code { font-size: 32px; font-weight: bold; color: #4CAF50; text-align: center; padding: 20px; background-color: #f0f0f0; border-radius: 5px; margin: 20px 0; }" +
                "        h1 { color: #333; }" +
                "        p { color: #666; line-height: 1.6; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <h1>¡Hola " + userName + "!</h1>" +
                "        <p>Gracias por registrarte en nuestra aplicación.</p>" +
                "        <p>Tu código de verificación es:</p>" +
                "        <div class='code'>" + verificationCode + "</div>" +
                "        <p>Este código es válido por 10 minutos.</p>" +
                "        <p>Si no solicitaste este código, puedes ignorar este mensaje.</p>" +
                "        <p>Saludos,<br>El equipo de Tu App</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

}