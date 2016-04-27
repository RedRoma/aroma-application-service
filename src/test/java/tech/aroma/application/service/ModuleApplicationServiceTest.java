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

package tech.aroma.application.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import tech.aroma.application.service.operations.ModuleApplicationServiceOperations;
import tech.aroma.data.memory.ModuleMemoryDataRepositories;
import tech.aroma.thrift.application.service.ApplicationService;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.notification.service.NotificationService;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class ModuleApplicationServiceTest 
{
    private ModuleMemoryDataRepositories dataModule;
    private ModuleApplicationService instance;
    private ModuleApplicationServiceOperations operationsModule;
    private Module mockModule = new AbstractModule()
    {
        @Override
        protected void configure()
        {
        }
        
        @Provides
        AuthenticationService.Iface provideAuthService()
        {
            return Mockito.mock(AuthenticationService.Iface.class);
        }
        
        @Provides
        NotificationService.Iface provideNotificationService()
        {
            return Mockito.mock(NotificationService.Iface.class);
        }
        
    };
    
    @Before
    public void setUp()
    {
        dataModule = new ModuleMemoryDataRepositories();
        operationsModule = new ModuleApplicationServiceOperations();
        instance = new ModuleApplicationService();
    }

    @Test
    public void testConfigure()
    {
        Injector injector = Guice.createInjector(dataModule, mockModule, operationsModule, instance);
        
        ApplicationService.Iface service = injector.getInstance(ApplicationService.Iface.class);
        assertThat(service, notNullValue());
    }

    @Test
    public void testProvideHttpClient()
    {
        AlchemyHttp client = instance.provideHttpClient();
        assertThat(client, notNullValue());
    }

}
