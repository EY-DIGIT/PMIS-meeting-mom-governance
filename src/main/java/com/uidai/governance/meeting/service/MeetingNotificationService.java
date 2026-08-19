package com.uidai.governance.meeting.service;

import com.uidai.governance.external.notification.NotificationClient;
import com.uidai.governance.external.notification.dto.EmailNotificationRequest;
import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingParticipant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Sends the meeting invite email to every attendee via the external notification
 * API ({@code app.notification.url}).
 *
 * <p>Recipients come straight from the meeting's participants: internal attendees
 * carry the mandatory {@code email} supplied on the create request, and
 * external attendees carry their own email address. No User-service lookup is
 * needed.</p>
 *
 * <p>The whole operation is best-effort — a failure is logged and swallowed so
 * that notifications never affect meeting creation.</p>
 */
@Service
public class MeetingNotificationService {

    private static final Logger log = LoggerFactory.getLogger(MeetingNotificationService.class);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    private final NotificationClient notificationClient;

    public MeetingNotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    /**
     * Invites all attendees to a newly created meeting. The mail body carries the
     * meeting details including the joining link. Never throws.
     *
     * <p>When called inside a transaction the send is deferred until after commit,
     * so attendees are only mailed about a meeting that was actually persisted and
     * the outbound HTTP call does not hold the database connection open.</p>
     */
    public void notifyMeetingCreated(Meeting meeting) {
        if (!notificationClient.isEnabled()) {
            log.debug("Notification API disabled - not inviting attendees of meeting {}", meeting.getId());
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(meeting);
                }
            });
            return;
        }
        send(meeting);
    }

    private void send(Meeting meeting) {
        try {
            List<String> recipients = resolveRecipients(meeting);
            if (recipients.isEmpty()) {
                log.info("No attendee email addresses on meeting {} - nothing to notify", meeting.getId());
                return;
            }
            notificationClient.sendEmail(EmailNotificationRequest.html(
                    recipients, buildSubject(meeting), buildBody(meeting)));
        } catch (Exception ex) {
            // Best-effort: the meeting is already recorded, so a notification
            // failure must not surface to the caller.
            log.error("Failed to invite attendees of meeting {}", meeting.getId(), ex);
        }
    }

    /**
     * Every attendee's email address, de-duplicated and in attendee order. Falls
     * back to the participant id for external attendees recorded before
     * {@code email} existed (their id is the address).
     */
    private static List<String> resolveRecipients(Meeting meeting) {
        Set<String> emails = new LinkedHashSet<>();
        for (MeetingParticipant participant : meeting.getParticipants()) {
            String email = participant.getEmail();
            if ((email == null || email.isBlank()) && participant.isExternal()) {
                email = participant.getUserId();
            }
            if (email != null && !email.isBlank()) {
                emails.add(email.trim());
            }
        }
        return List.copyOf(emails);
    }

    private static String buildSubject(Meeting meeting) {
        return "Meeting Invite: %s (%s)".formatted(
                meeting.getTitle(), DATE_FORMAT.format(meeting.getMeetingDate()));
    }

    /**
     * Builds the HTML mail body: meeting code, title, schedule, description and —
     * when one was recorded — the joining link, rendered as a clickable anchor.
     */
    private static String buildBody(Meeting meeting) {
        StringBuilder body = new StringBuilder(512);
        body.append("<p>Dear Attendee,</p>")
                .append("<p>You have been invited to the following meeting.</p>")
                .append("<table cellpadding=\"6\" cellspacing=\"0\">")
                .append(row("Title", escape(meeting.getTitle())))
                .append(row("Date", DATE_FORMAT.format(meeting.getMeetingDate())))
                .append(row("Time", "%s - %s (IST)".formatted(
                        TIME_FORMAT.format(meeting.getStartTime()),
                        TIME_FORMAT.format(meeting.getEndTime()))));

        if (meeting.getMeetingCode() != null && !meeting.getMeetingCode().isBlank()) {
            body.append(row("Meeting Code", escape(meeting.getMeetingCode())));
        }
        if (meeting.getProjectName() != null && !meeting.getProjectName().isBlank()) {
            body.append(row("Project", escape(meeting.getProjectName())));
        }
        if (meeting.getDescription() != null && !meeting.getDescription().isBlank()) {
            body.append(row("Description", escape(meeting.getDescription())));
        }

        boolean hasLink = meeting.getMeetingLink() != null && !meeting.getMeetingLink().isBlank();
        if (hasLink) {
            String link = escape(meeting.getMeetingLink());
            body.append(row("Meeting Link", "<a href=\"%s\">%s</a>".formatted(link, link)));
        }

        body.append("</table>");
        if (hasLink) {
            body.append("<p>Please use the meeting link above to join at the scheduled time.</p>");
        }
        body.append("<p>Regards,<br/>UIDAI PMIS</p>");
        return body.toString();
    }

    private static String row(String label, String value) {
        return "<tr><td><b>%s</b></td><td>%s</td></tr>".formatted(label, value);
    }

    /** Escapes user-supplied text so it cannot break out of the HTML body. */
    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
