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

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.notification.service.NotificationService;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.application.service.ApplicationAssertions.withMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;

/**
 *
 * @author SirWellington
 */
final class SendMessageOperation implements ThriftOperation<SendMessageRequest, SendMessageResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(SendMessageOperation.class);
    private AuthenticationService.Iface authenticationService;
    private MessageRepository repository;
    private NotificationService.Iface notificationService;

    @Override
    public SendMessageResponse process(SendMessageRequest request) throws TException
    {
        checkThat(request)
            .throwing(withMessage("missing request"))
            .is(notNull());
        
        //Get information about the App from the Token
        //Store Message
        //Receive ID
        //Send out notifications
        //Return stored ID
        
        SendMessageResponse response = one(pojos(SendMessageResponse.class));
        return response;
    }

}
