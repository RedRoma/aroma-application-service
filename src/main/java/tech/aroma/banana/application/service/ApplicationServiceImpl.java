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

package tech.aroma.banana.application.service;

import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.application.service.ApplicationAssertions.withMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
final class ApplicationServiceImpl implements ApplicationService.Iface
{

    private final static Logger LOG = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;

    private final ExecutorService executor;

    @Inject
    ApplicationServiceImpl(ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation,
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
        return ApplicationServiceConstants.API_VERSION;
    }

    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              TException
    {
        LOG.debug("Received request to send message: {}", request);

        checkThat(request)
            .throwing(withMessage("request is missing"))
            .is(notNull());

        return sendMessageOperation.process(request);
    }

    @Override
    public void sendMessageAsync(SendMessageRequest request) throws TException
    {
        executor.submit(() -> this.sendMessage(request));
    }

}
