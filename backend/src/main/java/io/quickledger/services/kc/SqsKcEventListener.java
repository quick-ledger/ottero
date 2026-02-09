package io.quickledger.services.kc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import io.quickledger.entities.KcEvent;
import io.quickledger.entities.User;
import io.quickledger.mappers.KcEventMapper;
import io.quickledger.repositories.KcEventRepository;
import io.quickledger.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class SqsKcEventListener {
    // SLF4J Logger
    private static final Logger logger = LoggerFactory.getLogger(SqsKcEventListener.class);

    @Autowired
    private KcEventRepository kcEventRepository;

    @Autowired
    private KcEventMapper kcEventMapper;

    @Autowired
    private UserService userService;

    private ObjectMapper objectMapper = new ObjectMapper();

    /*
     * Sample event message from Keycloak:
     * {
     * auth_method=openid-connect,
     * auth_type=code,
     * register_method=form,
     * last_name=baradaran Hosseini,
     * redirect_uri=http://localhost:8085/,
     * first_name=barad,
     * code_id=133c426d-85da-43ff-8dee-84a60b45a6b9,
     * email=barad4@gmail.com,
     * username=barad4@gmail.com
     * }'
     */
    // @SqsListener(queueNames = "kc-events", acknowledgementMode = "MANUAL") //
    // Listen to the "kc-events" SQS queue
    public void receiveMessage(String message, Acknowledgement acknowledgement) {
        logger.debug("===> Received message started");
        try {
            // Parse the message to a Map
            Map<String, Object> messageData = objectMapper.readValue(message, Map.class);
            logger.debug("===> Received message: {}", messageData);

            // Map the message to a KcEvent object
            KcEvent kcEvent = kcEventMapper.mapToKcEvent(messageData);
            /*
             * Can not use VERIFY_EMAIL event as it doesn't have name and surname and other
             * details in it, only has email in it.
             * Later we can use this to activate user. But they can't login anyway!
             * 
             * These are the list of possible KC events: REGISTER, LOGIN, LOGOUT,
             * UPDATE_PROFILE, UPDATE_PASSWORD, RESET_PASSWORD, VERIFY_EMAIL,
             * VERIFY_EMAIL_UPDATE_PROFILE, VERIFY_EMAIL_UPDATE_PASSWORD,
             * VERIFY_EMAIL_RESET_PASSWORD, DELETE_USER
             * 
             * DELETE_USER event works only when user delete his profile in KC not when we
             * delete them.
             * When we delete them in admin it's admin event and we will only have KC user
             * ID not user email in that event.
             * We need to check user status going forward, Also when company owner remove
             * employee we have to remove group membership of the user.
             */
            if (kcEvent.getEventType().equals("REGISTER")) {
                createUser(messageData);
            }

            kcEventRepository.save(kcEvent);
            logger.debug("===> Saved message to DB: {}", kcEvent);
            acknowledgement.acknowledge();
        } catch (Exception e) {
            // Handle the exception
            logger.error("===> Error processing message: {}", e.getMessage());
        }
    }

    private void createUser(Map<String, Object> messageData) {
        logger.debug("===> Handling REGISTER event");
        Map<String, Object> eventDetails = (Map<String, Object>) messageData.get("eventDetails");
        // Extract the required fields from the eventDetails
        String firstName = (String) eventDetails.get("first_name");
        String lastName = (String) eventDetails.get("last_name");
        String email = (String) eventDetails.get("email");
        String username = (String) eventDetails.get("username");
        String registerDateTimeStr = (String) messageData.get("eventDateTime");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime registerDateTime = LocalDateTime.parse(registerDateTimeStr, formatter);
        // Create a new User object using UserService
        User user = userService.createUserFromKcEvent(firstName, lastName, email, username, registerDateTime);
        logger.debug("===> Created user: {}", user);
    }
}
