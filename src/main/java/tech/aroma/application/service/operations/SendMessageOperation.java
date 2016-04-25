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
import java.util.UUID;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.application.service.reactions.MessageReactor;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.functions.TokenFunctions;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.thrift.application.service.ApplicationServiceConstants.MAX_CHARACTERS_IN_BODY;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Checks.Internal.isNullOrEmpty;
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
    private final MessageReactor messageReactor;
    
    @Inject
    SendMessageOperation(AuthenticationService.Iface authenticationService, MessageReactor messageReactor)
    {
        checkThat(authenticationService, messageReactor)
            .are(notNull());
        
        this.authenticationService = authenticationService;
        this.messageReactor = messageReactor;
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
        
        messageReactor.reactToMessage(message);

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
    
    private void checkAppId(String applicationId) throws OperationFailedException
    {
        checkThat(applicationId)
            .throwing(OperationFailedException.class)
            .usingMessage("Could not get Application ID from Token")
            .is(nonEmptyString())
            .is(stringWithLengthGreaterThanOrEqualTo(10));
    }
    
}
