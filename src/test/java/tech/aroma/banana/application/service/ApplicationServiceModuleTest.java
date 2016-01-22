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
import tech.aroma.banana.application.service.operations.ModuleApplicationServiceOperations;
import tech.aroma.banana.data.cassandra.ModuleCassandraDataRepositories;
import tech.aroma.banana.data.cassandra.ModuleCassandraDevCluster;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.notification.service.NotificationService;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
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
    private NotificationService.Iface notificationService;

    @Mock
    private ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;

    private ApplicationServiceModule module;
    private ModuleMocks moduleFake;
    private ModuleApplicationServiceOperations moduleOperation;
    private ModuleCassandraDataRepositories moduleCassandraData;
    private ModuleCassandraDevCluster moduleCassandraDevCluster;

    @GeneratePojo
    private SendMessageRequest request;

    @Before
    public void setUp()
    {
        module = new ApplicationServiceModule();
        moduleFake = new ModuleMocks(authenticationService, sendMessageOperation);
        moduleOperation = new ModuleApplicationServiceOperations();
        moduleCassandraData = new ModuleCassandraDataRepositories();
        moduleCassandraDevCluster = new ModuleCassandraDevCluster();
    }

    @Test
    public void testConfigure() throws TException
    {
        Injector injector = Guice.createInjector(moduleFake, module);

        ApplicationService.Iface service = injector.getInstance(ApplicationService.Iface.class);
        assertThat(service, notNullValue());

        ensureAuthenticationServiceIsCalled(service);
    }

    private void ensureAuthenticationServiceIsCalled(ApplicationService.Iface service) throws TException
    {
        service.sendMessage(request);
        verify(authenticationService).verifyToken(Mockito.any());
    }

    @Test
    public void testConfigureWithRealModules() throws Exception
    {
        Injector injector = Guice.createInjector(moduleCassandraData,
                                                 moduleCassandraDevCluster,
                                                 moduleOperation,
                                                 module,
                                                 new ModuleAuthentication(),
                                                 new ModuleNotification());
        assertThat(injector, notNullValue());
    }

    private static class ModuleMocks extends AbstractModule
    {

        private final AuthenticationService.Iface authenticationService;
        private final ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;

        ModuleMocks(AuthenticationService.Iface authenticationService,
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

    private static class ModuleAuthentication extends AbstractModule
    {

        @Override
        protected void configure()
        {
            AuthenticationService.Iface authenticationService;
            authenticationService = Mockito.mock(AuthenticationService.Iface.class);

            bind(AuthenticationService.Iface.class).toInstance(authenticationService);
        }

    }
    
    private static class ModuleNotification extends AbstractModule
    {

        @Override
        protected void configure()
        {
            NotificationService.Iface notificationService;
            notificationService = mock(NotificationService.Iface.class);
            
            bind(NotificationService.Iface.class)
                .toInstance(notificationService);
        }
        
    }

}
