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
import decorice.DecoratorModule;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.application.service.ApplicationService;

/**
 *
 * @author SirWellington
 */
public final class ApplicationServiceModule extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(ApplicationServiceModule.class);

    @Override
    protected void configure()
    {
        install(new ServiceModule());
        
        bind(ExecutorService.class).toInstance(Executors.newWorkStealingPool(10));
    }
    
    private static class ServiceModule extends DecoratorModule
    {
        {
            bind(ApplicationService.Iface.class)
                .to(ApplicationServiceBase.class)
                .decoratedBy(AuthenticationLayer.class);
        }
    }
    
}
