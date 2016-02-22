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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.data.memory.ModuleMemoryDataRepositories;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.notification.service.NotificationService;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class ModuleApplicationServiceOperationsTest
{

    private ModuleMocks moduleMocks;
    private ModuleMemoryDataRepositories moduleData;
    private ModuleApplicationServiceOperations module;

    @Before
    public void setUp()
    {
        moduleMocks = new ModuleMocks();
        moduleData = new ModuleMemoryDataRepositories();
        module = new ModuleApplicationServiceOperations();
    }

    @Test
    public void testConfigure()
    {
        Injector injector = Guice.createInjector(module, moduleMocks, moduleData);

        assertThat(injector, notNullValue());
    }

    private static class ModuleMocks extends AbstractModule
    {

        @Override
        protected void configure()
        {

        }

        @Provides
        AuthenticationService.Iface provideMockAuthenticationService()
        {
            return mock(AuthenticationService.Iface.class);
        }

        @Provides
        NotificationService.Iface provideMockNotificationService()
        {
            return mock(NotificationService.Iface.class);
        }
    }

}
