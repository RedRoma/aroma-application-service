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

import decorice.DecoratedBy;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;

import static tech.aroma.banana.application.service.ApplicationAssertions.validTokenIn;
import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.CONCRETE_DECORATOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Decorates an Application Service, providing Authentication mechanisms against an
 * {@linkplain AuthenticationService.Iface Authentication Service}.
 *
 * @author SirWellington
 */
@Internal
@DecoratorPattern(role = CONCRETE_DECORATOR)
final class AuthenticationDecorator implements ApplicationService.Iface
{

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationDecorator.class);

    private final AuthenticationService.Iface authenticationService;
    private final ApplicationService.Iface delegate;

    @Inject
    AuthenticationDecorator(AuthenticationService.Iface authenticationService,
                            @DecoratedBy(AuthenticationDecorator.class) ApplicationService.Iface delegate)
    {
        checkThat(delegate, authenticationService)
            .are(notNull());

        this.authenticationService = authenticationService;
        this.delegate = delegate;
    }

    @Override
    public double getApiVersion() throws TException
    {
        return delegate.getApiVersion();
    }

    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) throws OperationFailedException, InvalidArgumentException,
                                                                              InvalidCredentialsException, TException
    {
        checkThat(request)
            .is(notNull());

        checkThat(request.applicationToken)
            .throwing(InvalidTokenException.class)
            .is(validTokenIn(authenticationService));

        return delegate.sendMessage(request);
    }

    @Override
    public void sendMessageAsync(SendMessageRequest request) throws TException
    {
        checkThat(request)
            .is(notNull());

        checkThat(request.applicationToken)
            .throwing(InvalidTokenException.class)
            .is(validTokenIn(authenticationService));

        delegate.sendMessageAsync(request);
    }

}
