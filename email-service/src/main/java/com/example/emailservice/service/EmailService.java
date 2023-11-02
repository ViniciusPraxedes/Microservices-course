package com.example.emailservice.service;

import com.example.emailservice.DTO.EmailDTO;
import com.example.emailservice.DTO.OrderCreatedEvent;
import com.example.emailservice.DTO.OrderItem;
import com.example.emailservice.DTO.UserCreatedEvent;
import com.example.emailservice.model.Email;
import com.example.emailservice.model.EmailStatus;
import com.example.emailservice.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailService {
    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(EmailRepository emailRepository, JavaMailSender javaMailSender) {
        this.emailRepository = emailRepository;
        this.javaMailSender = javaMailSender;
    }

    @RabbitListener(queues = "orders.v1.order-created")
    public void onOrderCreated(OrderCreatedEvent order) {

        //Create an email object so it can be saved to the database
        Email email = new Email();
        email.setDateSent(LocalDateTime.now());
        email.setEmailTo(order.getEmail());
        email.setEmailFrom("vinipraxedes@hotmail.com");
        email.setSubject("Order: "+order.getOrderNumber()+" has been placed.");
        email.setOwnedRef("order-service order-created");
        email.setText("orderCreatedHtmlContent");

        try {
            //MimeMessage makes it possible to send an email with html + css
            MimeMessage message = javaMailSender.createMimeMessage();

            //Gets all the items within the order and puts them in a list
            List<OrderItem> orderItems = order.getOrderItems().stream().toList();

            //Sums upp the price of all items within the order so we can have a total order price
            BigDecimal totalOrderPrice = BigDecimal.ZERO;
            for (OrderItem item : orderItems) {
                totalOrderPrice = totalOrderPrice.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            //Generates the html content that is going to be sent within the email
            String orderCreatedHtmlContent = orderCreatedHtmlContent(order.getOrderNumber(), order.getEmail(), order.getFirstname(), order.getLastname(),
                    order.getAddress(), order.getCity(), order.getPostcode(), order.getPhoneNumber(), orderItems, totalOrderPrice);

            //Sets the parameters of the message(email)
            message.setFrom(email.getEmailFrom());
            message.setRecipients(MimeMessage.RecipientType.TO, email.getEmailTo());
            message.setSubject(email.getSubject());
            message.setContent(orderCreatedHtmlContent, "text/html; charset=utf-8");

            //Sends the email
            javaMailSender.send(message);

            //Sets the status of the email to SENT
            email.setEmailStatus(EmailStatus.SENT);

            //Saves the email to the database
            emailRepository.save(email);

        } catch (MailException e) {

            //If this exception is caught then set the status of the email to ERROR
            email.setEmailStatus(EmailStatus.ERROR);

            //Saves the email so we can check what went wrong later
            emailRepository.save(email);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "users.v1.user-created")
    public void onUserCreated(UserCreatedEvent user) {

        //Create an email object so it can be saved to the database
        Email email = new Email();
        email.setDateSent(LocalDateTime.now());
        email.setEmailTo(user.getEmail());
        email.setEmailFrom("vinipraxedes@hotmail.com");
        email.setSubject("Account created, welcome!");
        email.setOwnedRef("users-service user-created");
        email.setText("user created template");

        try {
            //MimeMessage makes it possible to send an email with html + css
            MimeMessage message = javaMailSender.createMimeMessage();

            //Generates the html content that is going to be sent within the email
            String htmlContent = userCreatedHtmlContent(user.getEmail(), user.getFirstname(), user.getLastname(),
                    user.getAddress(), user.getCity(), user.getPostcode(), user.getPhoneNumber());

            //Sets the parameters of the message(email)
            message.setFrom(email.getEmailFrom());
            message.setRecipients(MimeMessage.RecipientType.TO, email.getEmailTo());
            message.setSubject(email.getSubject());
            message.setContent(htmlContent, "text/html; charset=utf-8");

            //Sends the email
            javaMailSender.send(message);

            //Sets the status of the email to SENT
            email.setEmailStatus(EmailStatus.SENT);

            //Saves the email to the database
            emailRepository.save(email);

        } catch (MailException e) {

            //If this exception is caught then set the status of the email to ERROR
            email.setEmailStatus(EmailStatus.ERROR);

            //Saves the email so we can check what went wrong later
            emailRepository.save(email);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    public ResponseEntity<?> sendEmail(EmailDTO emailDTO) {
        Email email = new Email();
        BeanUtils.copyProperties(emailDTO, email);
        email.setDateSent(LocalDateTime.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(email.getEmailFrom());
            message.setTo(email.getEmailTo());
            message.setSubject(email.getSubject());
            message.setText(email.getText());
            javaMailSender.send(message);
            email.setEmailStatus(EmailStatus.SENT);
            emailRepository.save(email);
            return new ResponseEntity<>("Email successfully sent to: " + email.getEmailTo(), HttpStatus.OK);
        } catch (MailException e) {
            email.setEmailStatus(EmailStatus.ERROR);
            emailRepository.save(email);
            return new ResponseEntity<>("Failed to send email to: " + email.getEmailTo(), HttpStatus.BAD_REQUEST);
        }
    }

    public String orderCreatedHtmlContent(String orderNumber,String emailAddress, String firstName, String lastName, String address, String city, Integer postcode, String phoneNumber, List<OrderItem> orderItems, BigDecimal totalOrderPrice) {
        StringBuilder orderItemsHTML = new StringBuilder();
        for (OrderItem item : orderItems) {
            orderItemsHTML.append("<div class='item'>")
                    .append("Product: ").append(item.getProductName()).append("<br>")
                    .append("Quantity: ").append(item.getQuantity()).append("<br>")
                    .append("Price: $").append(item.getPrice()).append("<br>")
                    .append("</div>");
        }
        return "<html><head><style>"
                + "@font-face { font-family: 'Roboto'; src: url('https://fonts.googleapis.com/css2?family=Roboto:wght@400&display=swap'); }"
                + "body { font-family: 'Roboto', sans-serif; background-color: #f8f8f8; margin: 0; padding: 0; font-size: 16px; }"
                + ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }"
                + ".header { background-color: #ff7f50; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 10px; border-top-right-radius: 10px; font-family: 'Pacifico', cursive; }"
                + ".content { padding: 20px; }"
                + ".order-summary { border: 1px solid #ccc; padding: 10px; background-color: #ffffff; border-radius: 5px; }"
                + ".item { margin-bottom: 10px; background-color: #f0f0f0; padding: 15px; border-radius: 8px; }"
                + "h3.order-number { text-align: center; }"
                + "h4.order-items { text-align: center; }"
                + ".order-items-container { background-color: #ffcc80; padding: 15px; border-radius: 8px; margin-top: 20px; }"
                + "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Thank you for choosing us! Your order has been placed.</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<div class='order-summary'>"
                + "<h3 class='order-number'>Order: " + orderNumber + "</h3>"
                + "<div class='item'>Name: " + firstName + " " + lastName + "</div>"
                + "<div class='item'>Email: " + emailAddress + "</div>"
                + "<div class='item'>Address: " + address + "</div>"
                + "<div class='item'>City: " + city + "</div>"
                + "<div class='item'>Postcode: " + postcode + "</div>"
                + "<div class='item'>Phone Number: " + phoneNumber + "</div>"
                + "<div class='item'>Order Items: " + orderItemsHTML + "</div>"
                + "</div>"
                + "<h2 style='text-align: center;'>Total Order Price: $" + totalOrderPrice + "</h2>"  // Added this line
                + "</div>"
                + "</div>"
                + "</div>"
                + "</div>"
                + "</body></html>";
    }

    public String userCreatedHtmlContent(String emailAddress, String firstName, String lastName, String address, String city, Integer postcode, String phoneNumber) {
        return "<html><head><style>"
                + "@font-face { font-family: 'Roboto'; src: url('https://fonts.googleapis.com/css2?family=Roboto:wght@400&display=swap'); }"
                + "body { font-family: 'Roboto', sans-serif; background-color: #f8f8f8; margin: 0; padding: 0; font-size: 16px; }"
                + ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }"
                + ".header { background-color: #ff7f50; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 10px; border-top-right-radius: 10px; font-family: 'Pacifico', cursive; }"
                + ".content { padding: 20px; }"
                + ".order-summary { border: 1px solid #ccc; padding: 10px; background-color: #ffffff; border-radius: 5px; }"
                + ".item { margin-bottom: 10px; background-color: #f0f0f0; padding: 15px; border-radius: 8px; }"
                + "h3.order-number { text-align: center; }"
                + "h4.order-items { text-align: center; }"
                + ".order-items-container { background-color: #ffcc80; padding: 15px; border-radius: 8px; margin-top: 20px; }"
                + "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Welcome "+ firstName +" "+ lastName + "!" + " Your account has been created."+"</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<div class='order-summary'>"
                + "<h3 class='order-number'>Account information</h3>"
                + "<div class='item'>Name: " + firstName + " " + lastName + "</div>"
                + "<div class='item'>Email: " + emailAddress + "</div>"
                + "<div class='item'>Address: " + address + "</div>"
                + "<div class='item'>City: " + city + "</div>"
                + "<div class='item'>Postcode: " + postcode + "</div>"
                + "<div class='item'>Phone Number: " + phoneNumber + "</div>"
                + "</div>"
                + "</div>"
                + "</div>"
                + "</div>"
                + "</div>"
                + "</body></html>";

    }


}

