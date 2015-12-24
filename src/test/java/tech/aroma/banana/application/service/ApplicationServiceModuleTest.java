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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.banana.application.service.operations.ApplicationServiceOperationsModule;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ApplicationServiceModuleTest
{

    @Mock
    private AuthenticationService.Iface authenticationService;

    @Mock
    private ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;

    private ApplicationServiceModule module;
    private FakeModule fakeModule;
    private ApplicationServiceOperationsModule operationModule;
    
    @GeneratePojo
    private SendMessageRequest request;

    @Before
    public void setUp()
    {
        module = new ApplicationServiceModule();
        fakeModule = new FakeModule(authenticationService, sendMessageOperation);
        operationModule = new ApplicationServiceOperationsModule();
    }

    @Test
    public void testConfigure() throws TException
    {
        Injector injector = Guice.createInjector(fakeModule, module);
        ApplicationService.Iface service = injector.getInstance(ApplicationService.Iface.class);
        assertThat(service, notNullValue());
        
        service.sendMessage(request);
        verify(authenticationService).verifyToken(Mockito.any());
    }
    
    @Test
    public void testConfigureWithRealModules() throws Exception
    {
        Injector injector = Guice.createInjector(operationModule, module, new AuthenticationModule());
        assertThat(injector, notNullValue());
    }

    private static class FakeModule extends AbstractModule
    {

        private final AuthenticationService.Iface authenticationService;
        private final ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;

        FakeModule(AuthenticationService.Iface authenticationService,
                   ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation)
        {
            this.authenticationService = authenticationService;
            this.sendMessageOperation = sendMessageOperation;
        }

        @Override
        protected void configure()
        {
            bind(AuthenticationService.Iface.class).toInstance(authenticationService);
            
            bind(new TypeLiteral<ThriftOperation<SendMessageRequest, SendMessageResponse>>() {})
                .toInstance(sendMessageOperation);
        }

    }
    
    private static class AuthenticationModule extends AbstractModule
    {

        @Override
        protected void configure()
        {
            AuthenticationService.Iface authenticationService;
            authenticationService = Mockito.mock(AuthenticationService.Iface.class);
            
            bind(AuthenticationService.Iface.class).toInstance(authenticationService);
        }
        
    }

}
