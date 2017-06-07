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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.service.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.arguments.*;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
@NonInstantiable
@Internal
public final class ApplicationAssertions 
{
    private final static Logger LOG = LoggerFactory.getLogger(ApplicationAssertions.class);
    
    private ApplicationAssertions() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }

    public static AlchemyAssertion<ApplicationToken> validTokenIn(@Required AuthenticationService.Iface authenticationService) 
    {
        checkThat(authenticationService)
            .usingMessage("authentication service is null")
            .is(notNull());
        
        return token ->
        {
            checkThat(token)
                .usingMessage("token is null")
                .is(notNull());
            
            checkThat(token.tokenId)
                .usingMessage("tokenId is missing")
                .is(nonEmptyString());
            
            VerifyTokenRequest request = new VerifyTokenRequest()
                .setTokenId(token.getTokenId())
                .setOwnerId(token.applicationId);
            
            VerifyTokenResponse response;
            try
            {
                response = authenticationService.verifyToken(request);
            }
            catch (InvalidTokenException ex)
            {
                throw new FailedAssertionException("Token is not valid");
            }
            catch (Exception ex)
            {
                throw new FailedAssertionException("Could not contact Authentication Service", ex);
            }
        };
    }
    
    
    public static ExceptionMapper<InvalidArgumentException> withMessage(String message)
    {
        return ex -> new InvalidArgumentException(message);
    }
}
