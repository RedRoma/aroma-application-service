
package tech.aroma.banana.application.service.server;

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



import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import java.net.SocketException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.application.service.ModuleApplicationService;
import tech.aroma.banana.application.service.operations.ModuleApplicationServiceOperations;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.ApplicationServiceConstants;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.NoOpAuthenticationService;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This Main Class runs the Authentication Service on a Server Socket.
 * 
 * @author SirWellington
 */
@Internal
public final class TcpServer
{

    private final static Logger LOG = LoggerFactory.getLogger(TcpServer.class);
    private static final int PORT = ApplicationServiceConstants.SERVICE_PORT;

    public static void main(String[] args) throws TTransportException, SocketException
    {
        Injector injector = Guice.createInjector(new AuthenticationProvider(),
                                                 new ModuleApplicationServiceOperations(),
                                                 new ModuleApplicationService());

        ApplicationService.Iface applicationService = injector.getInstance(ApplicationService.Iface.class);
        ApplicationService.Processor processor = new ApplicationService.Processor<>(applicationService);

        TServerSocket socket = new TServerSocket(PORT);
        socket.getServerSocket().setSoTimeout((int) SECONDS.toMillis(30));

        TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(socket)
            .protocolFactory(new TBinaryProtocol.Factory())
            .processor(processor)
            .requestTimeout(60)
            .requestTimeoutUnit(SECONDS)
            .minWorkerThreads(5)
            .maxWorkerThreads(100);
        
        LOG.info("Starting Application Service at port {}", PORT);
        
        TThreadPoolServer server = new TThreadPoolServer(serverArgs);
        server.serve();
        server.stop();
    }
    
    static class AuthenticationProvider extends AbstractModule
    {

        @Override
        protected void configure()
        {
        }
        
        @Provides
        AuthenticationService.Iface provideAuthenticationService()
        {
            return new NoOpAuthenticationService();
        }
        
    }
}
