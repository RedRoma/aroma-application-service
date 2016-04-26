/*
 * Copyright 2016 RedRoma.
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

package tech.aroma.application.service.reactions.actions;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class IgnoreInboxTest
{

    @GeneratePojo
    private Message message;
    
    @GenerateString(UUID)
    private String messageId;
    
    @GenerateString(UUID)
    private String appId;
    
    private IgnoreInbox instance;

    @Before
    public void setUp() throws Exception
    {
        message.applicationId = appId;
        message.messageId = messageId;
        
        instance = new IgnoreInbox();
    }

    @Test
    public void testActOnMessage() throws Exception
    {
        instance.actOnMessage(message);
    }
    
    @Test
    public void testActOnMessageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.actOnMessage(new Message()))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testToString()
    {
        assertThat(instance.toString(), not(isEmptyOrNullString()));
    }

}
