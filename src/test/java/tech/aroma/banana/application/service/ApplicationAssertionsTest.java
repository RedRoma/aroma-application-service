/*
 * Copyright 2016 Aroma Tech.
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

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.VerifyTokenRequest;
import tech.aroma.banana.thrift.authentication.service.VerifyTokenResponse;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.ExceptionMapper;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.strings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class ApplicationAssertionsTest 
{
    @Mock
    private AuthenticationService.Iface authenticationService;
    
    @GeneratePojo
    private ApplicationToken appToken;
    
    @GenerateString(UUID)
    private String applicationId;
    
    private String tokenId;
    
    private VerifyTokenRequest expectedAuthRequest;
    
    @Before
    public void setUp() throws TException
    {
        setupData();
        setupMocks();
    }
    
    private void setupData()
    {
        appToken.applicationId = applicationId;
        
        tokenId = appToken.tokenId;
        
        expectedAuthRequest = new VerifyTokenRequest()
            .setTokenId(tokenId)
            .setOwnerId(applicationId);
    }
    
    private void setupMocks() throws TException
    {
        when(authenticationService.verifyToken(expectedAuthRequest))
            .thenReturn(new VerifyTokenResponse());
    }

    @Test
    public void testValidTokenIn()
    {
        AlchemyAssertion<ApplicationToken> instance = ApplicationAssertions.validTokenIn(authenticationService);
        assertThat(instance, notNullValue());
        
        instance.check(appToken);
    }
    
    @Test
    public void testValidTokenWithoutAppId()
    {
        AlchemyAssertion<ApplicationToken> instance = ApplicationAssertions.validTokenIn(authenticationService);
        
        appToken.unsetApplicationId();
    }

    @Test
    public void testValidTokenWithMissingToken()
    {
        AlchemyAssertion<ApplicationToken> instance = ApplicationAssertions.validTokenIn(authenticationService);
        
        appToken.unsetTokenId();
        
        assertThrows(() -> instance.check(appToken))
            .isInstanceOf(FailedAssertionException.class);
    }
    
    @Test
    public void testWithMessage()
    {
        String message = one(strings());
        
        ExceptionMapper<InvalidArgumentException> instance = ApplicationAssertions.withMessage(message);
        assertThat(instance, notNullValue());
        
        FailedAssertionException ex = new FailedAssertionException();
        InvalidArgumentException response = instance.apply(ex);
        assertThat(response, notNullValue());
        assertThat(response.getMessage(), is(message));
    }

}