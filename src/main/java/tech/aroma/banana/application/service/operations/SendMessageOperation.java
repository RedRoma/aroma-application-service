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

package tech.aroma.banana.application.service.operations;

import com.datastax.driver.core.utils.UUIDs;
import java.time.Instant;
import java.util.UUID;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.banana.thrift.events.ApplicationSentMessage;
import tech.aroma.banana.thrift.events.Event;
import tech.aroma.banana.thrift.events.EventType;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.functions.TokenFunctions;
import tech.aroma.banana.thrift.message.service.MessageServiceConstants;
import tech.aroma.banana.thrift.notification.service.NotificationService;
import tech.aroma.banana.thrift.notification.service.SendNotificationRequest;
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
    private final MessageRepository repository;
    private final NotificationService.Iface notificationService;

    @Inject
    SendMessageOperation(AuthenticationService.Iface authenticationService,
                         MessageRepository repository,
                         NotificationService.Iface notificationService)
    {
        checkThat(authenticationService, repository, notificationService)
            .are(notNull());
        
        this.authenticationService = authenticationService;
        this.repository = repository;
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

        repository.saveMessage(message, MessageServiceConstants.DEFAULT_MESSAGE_LIFETIME);
        LOG.debug("Message successfully stored in repository");

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
            LOG.debug("Obtaining info on Token from Authentication Service for [{}]", applicationToken);
            tokenInfo = authenticationService.getTokenInfo(getTokenInfoRequest);
        }
        catch (InvalidTokenException ex)
        {
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
            .setBody(request.message)
            .setUrgency(request.urgency)
            .setTimeOfCreation(request.timeOfMessage)
            .setTimeMessageReceived(Instant.now().getEpochSecond());

        return message;
    }

    private SendNotificationRequest createNotificationRequestFor(Message message)
    {
        ApplicationSentMessage applicationSentMessage = new ApplicationSentMessage()
            .setApplicationId(message.applicationId)
            .setApplicationName(message.applicationName)
            .setMessage(message.body);

        EventType eventType = new EventType();
        eventType.setApplicationSentMessage(applicationSentMessage);

        Event event = new Event()
            .setTimestamp(Instant.now().getEpochSecond())
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

}
