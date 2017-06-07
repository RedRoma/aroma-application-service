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


import java.util.function.Function;

import com.google.inject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.functions.TokenFunctions;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

/**
 *
 * @author SirWellington
 */
public final class ModuleApplicationServiceOperations extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(ModuleApplicationServiceOperations.class);

    @Override
    protected void configure()
    {
        bind(new TypeLiteral<ThriftOperation<SendMessageRequest, SendMessageResponse>>() {})
            .to(SendMessageOperation.class);
    }

    @Provides
    Function<AuthenticationToken, ApplicationToken> provideAuthToAppTokenMapper()
    {
        return TokenFunctions.authTokenToAppTokenFunction();
    }

}
