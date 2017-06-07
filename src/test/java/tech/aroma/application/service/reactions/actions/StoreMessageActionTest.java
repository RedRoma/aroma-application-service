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

package tech.aroma.application.service.reactions.actions;

import java.util.List;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.emptyList;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class StoreMessageActionTest
{

    @Mock
    private MessageRepository messageRepo;
    
    private Message message;
    
    private StoreMessageAction instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new StoreMessageAction(messageRepo);
        verifyZeroInteractions(messageRepo);
    }
    
    private void setupData() throws Exception
    {
        message = one(messages());
    }
    
    private void setupMocks() throws Exception
    {
    }
    
    @Test
    public void testActOnMessage() throws Exception
    {
        List<Action> result = instance.actOnMessage(message);
        checkThat(result)
            .is(notNull())
            .is(emptyList());
        
        verify(messageRepo).saveMessage(eq(message), any());
    }
    
    @DontRepeat
    @Test
    public void testWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.actOnMessage(null))
            .isInstanceOf(TException.class);
        
        verifyZeroInteractions(messageRepo);
    }
    
    @Test
    public void testToString()
    {
        assertThat(instance.toString(), not(isEmptyOrNullString()));
    }
    
}
