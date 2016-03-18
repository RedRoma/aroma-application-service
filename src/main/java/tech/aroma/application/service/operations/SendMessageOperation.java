/*
 * Copyright 2015 Aroma Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.application.service.operations;

import com.datastax.driver.core.utils.UUIDs;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.InboxRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.thrift.events.ApplicationSentMessage;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.events.EventType;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.functions.TokenFunctions;
import tech.aroma.thrift.message.service.MessageServiceConstants;
import tech.aroma.thrift.notification.service.NotificationService;
import tech.aroma.thrift.notification.service.SendNotificationRequest;
import tech.aroma.thrift.service.AromaServiceConstants;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
final class SendMessageOperation implements ThriftOperation<SendMessageRequest, SendMessageResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(SendMessageOperation.class);

    private final AuthenticationService.Iface authenticationService;
    private final ApplicationRepository appRepo;
    private final InboxRepository inboxRepo;
    private final MessageRepository messageRepo;
    private final FollowerRepository followerRepo;
    private final UserRepository userRepo;
    private final NotificationService.Iface notificationService;

    @Inject
    SendMessageOperation(AuthenticationService.Iface authenticationService,
                         ApplicationRepository appRepo,
                         FollowerRepository followerRepo,
                         InboxRepository inboxRepo,
                         MessageRepository messageRepo,
                         UserRepository userRepo,
                         NotificationService.Iface notificationService)
    {
        checkThat(authenticationService, appRepo, followerRepo, inboxRepo, messageRepo, userRepo, notificationService)
            .are(notNull());

        this.authenticationService = authenticationService;
        this.appRepo = appRepo;
        this.messageRepo = messageRepo;
        this.followerRepo = followerRepo;
        this.inboxRepo = inboxRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    /*
     * TODO: Add Rate Limiting
     */
    @Override
    public SendMessageResponse process(SendMessageRequest request) throws TException
    {
        checkThat(request)
            .throwing(InvalidArgumentException.class)
            .usingMessage("request missing")
            .is(notNull());

        checkThat(request.applicationToken)
            .throwing(InvalidTokenException.class)
            .usingMessage("missing Application Token")
            .is(notNull());

        GetTokenInfoResponse tokenInfo = tryToGetTokenInfo(request.applicationToken);

        ApplicationToken appToken = TokenFunctions.authTokenToAppTokenFunction().apply(tokenInfo.token);

        String applicationId = appToken.applicationId;
        checkAppId(applicationId);

        Message message = createMessageFrom(request, appToken);

        messageRepo.saveMessage(message, MessageServiceConstants.DEFAULT_MESSAGE_LIFETIME);
        LOG.debug("Message successfully stored in repository");

        storeInFollowerInboxes(message);
        SendNotificationRequest sendNotificationRequest = createNotificationRequestFor(message);

        tryToSendNotification(sendNotificationRequest);

        SendMessageResponse response = new SendMessageResponse()
            .setMessageId(message.messageId);

        return response;
    }

    private GetTokenInfoResponse tryToGetTokenInfo(ApplicationToken applicationToken) throws InvalidTokenException,
                                                                                             OperationFailedException
    {

        GetTokenInfoRequest getTokenInfoRequest = new GetTokenInfoRequest()
            .setTokenId(applicationToken.tokenId)
            .setTokenType(TokenType.APPLICATION);

        GetTokenInfoResponse tokenInfo;
        try
        {
            tokenInfo = authenticationService.getTokenInfo(getTokenInfoRequest);
        }
        catch (InvalidTokenException ex)
        {
            LOG.warn("Application Token is Invalid: [{}]", applicationToken, ex);
            throw ex;
        }
        catch (TException ex)
        {
            LOG.error("Failed to get info for Token [{}]", applicationToken, ex);
            throw new OperationFailedException("Could not get token info: " + ex.getMessage());
        }

        checkThat(tokenInfo, tokenInfo.token)
            .throwing(OperationFailedException.class)
            .usingMessage("AuthenticationService Response is missing Token Info")
            .are(notNull());

        checkThat(tokenInfo.token.ownerId)
            .throwing(OperationFailedException.class)
            .usingMessage("missing Token Info")
            .is(nonEmptyString());

        return tokenInfo;
    }

    private Message createMessageFrom(SendMessageRequest request, ApplicationToken token)
    {
        UUID messageId = UUIDs.timeBased();

        Message message = new Message()
            .setApplicationId(token.applicationId)
            .setApplicationName(token.applicationName)
            .setMessageId(messageId.toString())
            .setBody(request.body)
            .setTitle(request.title)
            .setUrgency(request.urgency)
            .setTimeOfCreation(request.timeOfMessage)
            .setTimeMessageReceived(Instant.now().toEpochMilli())
            .setHostname(request.hostname)
            .setMacAddress(request.macAddress);

        return message;
    }

    private SendNotificationRequest createNotificationRequestFor(Message message)
    {
        ApplicationSentMessage applicationSentMessage = new ApplicationSentMessage()
            .setMessage(message.messageId)
            .setMessage(message.body);

        EventType eventType = new EventType();
        eventType.setApplicationSentMessage(applicationSentMessage);

        String appId = message.applicationId;
        Application app = new Application().setApplicationId(appId);
        
        Event event = new Event()
            .setApplication(app)
            .setApplicationId(appId)
            .setTimestamp(Instant.now().toEpochMilli())
            .setEventId("")
            .setEventType(eventType);

        return new SendNotificationRequest().setEvent(event);
    }

    private void tryToSendNotification(SendNotificationRequest sendNotificationRequest)
    {
        try
        {
            notificationService.sendNotification(sendNotificationRequest);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to send Notification request: {}", sendNotificationRequest, ex);
        }
    }

    private void checkAppId(String applicationId) throws OperationFailedException
    {
        checkThat(applicationId)
            .throwing(OperationFailedException.class)
            .usingMessage("Could not get Application ID from Token")
            .is(nonEmptyString())
            .is(stringWithLengthGreaterThanOrEqualTo(10));
    }

    private void storeInFollowerInboxes(Message message) throws TException
    {
        String appId = message.applicationId;

        Set<User> followers = Sets.toSet(followerRepo.getApplicationFollowers(appId));

        followers.parallelStream()
            .forEach(user -> this.tryToSaveInInbox(message, user));

        LOG.debug("Store Message in the Inboxes of {} users", followers.size());
    }

    private Set<User> getOwnerForApp(String appId) throws TException
    {
        Application app = appRepo.getById(appId);

        return app.owners
            .stream()
            .map(this::toUser)
            .collect(Collectors.toSet());
    }

    private void tryToSaveInInbox(Message message, User user)
    {
        LengthOfTime lifetime = AromaServiceConstants.DEFAULT_INBOX_LIFETIME;
        
        try
        {
            inboxRepo.saveMessageForUser(user, message, lifetime);
        }
        catch (TException ex)
        {
            LOG.error("Failed to save message {} in Inbox of User {}", message, user, ex);
        }
    }

    private User toUser(String userId)
    {
        try
        {
            return userRepo.getUser(userId);
        }
        catch (TException ex)
        {
            LOG.warn("Could not find user With ID [{}]", userId, ex);
            return new User().setUserId(userId);
        }
    }

}
