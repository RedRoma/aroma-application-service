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

package tech.aroma.application.service;

import java.util.concurrent.ExecutorService;
import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.AromaConstants;
import tech.aroma.thrift.application.service.*;
import tech.aroma.thrift.exceptions.*;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.application.service.ApplicationAssertions.withMessage;
import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.CONCRETE_COMPONENT;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * The  Application Service Base is the Concrete Component. It is fully functional
 * on its own, but can be decorated to add additional features.
 * 
 * @see AuthenticationLayer
 * @author SirWellington
 */
@Internal
@DecoratorPattern(role = CONCRETE_COMPONENT)
final class ApplicationServiceBase implements ApplicationService.Iface
{
    
    private final static Logger LOG = LoggerFactory.getLogger(ApplicationServiceBase.class);

    private final ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;
    private final ExecutorService executor;
    
    @Inject
    ApplicationServiceBase(ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation,
                           ExecutorService executor)
    {
        checkThat(executor, sendMessageOperation)
            .are(notNull());
        
        this.sendMessageOperation = sendMessageOperation;
        this.executor = executor;
    }
    
    @Override
    public double getApiVersion() throws TException
    {
        return AromaConstants.API_VERSION;
    }
    
    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              TException
    {
        checkThat(request)
            .throwing(withMessage("request is missing"))
            .is(notNull());
        
        SendMessageRequest requestWithoutBody = new SendMessageRequest(request).setBody(null);
        LOG.debug("Received request to send message: {}", requestWithoutBody);
        
        return sendMessageOperation.process(request);
    }
    
    @Override
    public void sendMessageAsync(SendMessageRequest request) throws TException
    {
        if (request == null)
        {
            LOG.warn("Received null request");
            return;
        }
        
        executor.submit(() -> this.sendMessage(request));
    }
    
}
