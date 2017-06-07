/*
 * Copyright 2017 RedRoma, Inc.
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

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;

import com.datastax.driver.core.utils.UUIDs;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.application.service.reactions.MessageReactor;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.authentication.*;
import tech.aroma.thrift.authentication.service.*;
import tech.aroma.thrift.exceptions.*;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.thrift.application.service.ApplicationServiceConstants.MAX_CHARACTERS_IN_BODY;
import static tech.aroma.thrift.application.service.ApplicationServiceConstants.MAX_TITLE_LENGTH;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.Checks.isNullOrEmpty;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
final class SendMessageOperation implements ThriftOperation<SendMessageRequest, SendMessageResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(SendMessageOperation.class);

    private final AuthenticationService.Iface authenticationService;
    private final MessageReactor messageReactor;
    private final Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @Inject
    SendMessageOperation(AuthenticationService.Iface authenticationService,
                         MessageReactor messageReactor,
                         Function<AuthenticationToken, ApplicationToken> tokenMapper)
    {
        checkThat(authenticationService, tokenMapper, messageReactor)
            .are(notNull());

        this.authenticationService = authenticationService;
        this.messageReactor = messageReactor;
        this.tokenMapper = tokenMapper;

    }

    /*
     * TODO: Add Rate Limiting
     */
    @Override
    public SendMessageResponse process(SendMessageRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        ApplicationToken appToken = tryToGetTokenInfo(request.applicationToken);

        String applicationId = appToken.applicationId;
        checkAppId(applicationId);

        Message message = createMessageFrom(request, appToken);

        messageReactor.reactToMessage(message);

        SendMessageResponse response = new SendMessageResponse()
            .setMessageId(message.messageId);

        return response;
    }

    private ApplicationToken tryToGetTokenInfo(ApplicationToken applicationToken) throws InvalidTokenException,
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

        
        ApplicationToken appToken;
        try
        {
            appToken = tokenMapper.apply(tokenInfo.token);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to map Auth Token {} to App Token", tokenInfo.token, ex);
            throw new OperationFailedException("Could not map Auth Token to App Token: " + ex.getMessage());
        }

        checkThat(appToken)
            .throwing(OperationFailedException.class)
            .usingMessage("Could not map Auth Token to App Token")
            .is(notNull());
        
        return appToken;
    }

    private Message createMessageFrom(SendMessageRequest request, ApplicationToken token)
    {
        //Time-Based UUIDs to optimize Storage in Cassandra.
        UUID messageId = UUIDs.timeBased();

        if (request.title.length() > MAX_TITLE_LENGTH)
        {
            request.setTitle(request.title.substring(0, MAX_TITLE_LENGTH));
        }

        String body = request.body;
        if (!isNullOrEmpty(body) && body.length() > MAX_CHARACTERS_IN_BODY)
        {
            body = body.substring(0, MAX_CHARACTERS_IN_BODY);
        }

        Message message = new Message()
            .setApplicationId(token.applicationId)
            .setApplicationName(token.applicationName)
            .setMessageId(messageId.toString())
            .setBody(body)
            .setTitle(request.title)
            .setUrgency(request.urgency)
            .setTimeOfCreation(request.timeOfMessage)
            .setTimeMessageReceived(Instant.now().toEpochMilli())
            .setHostname(request.hostname)
            .setMacAddress(request.macAddress);

        return message;
    }

    private AlchemyAssertion<SendMessageRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            checkThat(request.applicationToken)
                .usingMessage("Missing Application Token")
                .is(notNull());
            
            checkThat(request.title)
                .usingMessage("Missing Message Title")
                .is(nonEmptyString());
        };
    }

    private void checkAppId(String applicationId) throws OperationFailedException
    {
        checkThat(applicationId)
            .throwing(OperationFailedException.class)
            .usingMessage("Could not get Application ID from Token")
            .is(validApplicationId());
    }

}
