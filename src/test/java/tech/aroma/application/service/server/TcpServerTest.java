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

package tech.aroma.application.service.server;

 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;

 import com.google.common.collect.Lists;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
 import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

 import static java.util.concurrent.TimeUnit.SECONDS;
 import static org.junit.Assert.fail;

/**
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class TcpServerTest
{
    
    private ExecutorService async;
    
    private List<Throwable> exceptions;
    
    @Before
    public void setUp()
    {
        async = Executors.newSingleThreadExecutor();
        exceptions = Lists.newArrayList();
    }
    
    @Test
    public void testMain() throws Exception
    {
        async.submit(this::testRun);
        //Give it a few seconds, then kill it.
        Thread.sleep(SECONDS.toMillis(5));
        async.shutdownNow();
        
        for(Throwable ex : exceptions)
        {
            fail("Main Method failed: \n"+ ex);
        }
    }
    
    private void testRun()
    {
        try
        {
            TcpServer.main(new String[0]);
        }
        catch(Exception ex)
        {
            exceptions.add(ex);
        }
    }
    
}
