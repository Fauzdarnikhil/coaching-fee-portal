
package com.iscode.coaching_fee_portal.service;

import com.iscode.coaching_fee_portal.entity.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class ReminderMessageService {

    public String generateMessage(
            Parent parent,
            Instalment instalment,
            ReminderType reminderType,
            BigDecimal outstandingAmount
    ) {
        Student student = instalment.getFeePlan().getStudent();

        String studentName = student.getName();
        String mobileNo = parent.getMobileNo();
        String amount = outstandingAmount.toPlainString();

        String instalmentNumber = String.valueOf(
                instalment.getInstalmentNumber()
        );

        String dueDate = instalment.getDueDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );

        if (parent.getPreferredLanguage() == ReminderLanguage.HINDI) {
            return generateHindiMessage(
                    studentName,
                    amount,
                    instalmentNumber,
                    dueDate,
                    reminderType
            );
        }

        return generateEnglishMessage(
                studentName,
                amount,
                instalmentNumber,
                dueDate,
                reminderType
        );
    }

    private String generateEnglishMessage(
            String studentName,
            String amount,
            String instalmentNumber,
            String dueDate,
            ReminderType reminderType
    ) {
        return switch (reminderType) {

            case UPCOMING ->
                    "Dear Parent, the fee instalment number "
                            + instalmentNumber + " for student "
                            + studentName + " is due on " + dueDate
                            + ". Outstanding amount: Rs. " + amount
                            + ". Please make the payment on time.";

            case DUE_TODAY ->
                    "Dear Parent, today's fee due date for student "
                            + studentName + " (instalment "
                            + instalmentNumber + ") is " + dueDate
                            + ". Outstanding amount: Rs. " + amount
                            + ". Please complete the payment today.";

            case OVERDUE ->
                    "Dear Parent, the fee instalment number "
                            + instalmentNumber + " for student "
                            + studentName + " was due on " + dueDate
                            + ". Outstanding amount: Rs. " + amount
                            + ". Please clear the pending payment.";
        };
    }

    private String generateHindiMessage(
            String studentName,
            String amount,
            String instalmentNumber,
            String dueDate,
            ReminderType reminderType
    ) {
        return switch (reminderType) {

            case UPCOMING ->
                    "प्रिय अभिभावक, विद्यार्थी "
                            + studentName + " की फीस की किस्त संख्या "
                            + instalmentNumber + " की अंतिम तिथि "
                            + dueDate + " है। बकाया राशि: ₹" + amount
                            + "। कृपया समय पर भुगतान करें।";

            case DUE_TODAY ->
                    "प्रिय अभिभावक, विद्यार्थी "
                            + studentName + " की फीस की किस्त संख्या "
                            + instalmentNumber + " का भुगतान आज "
                            + dueDate + " तक करना है। बकाया राशि: ₹"
                            + amount + "। कृपया आज भुगतान करें।";

            case OVERDUE ->
                    "प्रिय अभिभावक, विद्यार्थी "
                            + studentName + " की फीस की किस्त संख्या "
                            + instalmentNumber + " की अंतिम तिथि "
                            + dueDate + " निकल चुकी है। बकाया राशि: ₹"
                            + amount + "। कृपया जल्द भुगतान करें।";
        };
    }
}
