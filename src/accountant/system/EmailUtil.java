package accountant.system;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {

    public static void sendEmail(String toEmail, String subject, String body) {
        final String fromEmail = ""; // requires valid Gmail id
        final String appPassword = ""; // app-specific password

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
        props.put("mail.smtp.port", "587"); // TLS Port
        props.put("mail.smtp.auth", "true"); // enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        };

        Session session = Session.getInstance(props, auth);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject(subject);
            msg.setText(body);

            Transport.send(msg);
            System.out.println("Email sent successfully");
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            System.out.println("Authentication failed: Check your username and password.");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Email sending failed");
        }
    }
}
