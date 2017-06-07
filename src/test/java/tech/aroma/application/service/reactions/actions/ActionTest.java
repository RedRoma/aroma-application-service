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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class ActionTest 
{

    private Message goodMessage;
    
    @GeneratePojo
    private Message badMessage;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
    }


    private void setupData() throws Exception
    {
        goodMessage = one(messages());
        
        badMessage.messageId = badId;
    }

    @Test
    public void testCheckMessageWhenGood() throws Exception
    {
        Action.checkMessage(goodMessage);
    }
    
    @Test
    public void testCheckMessageWhenBad() throws Exception
    {
        assertThrows(() -> Action.checkMessage(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> Action.checkMessage(badMessage))
            .isInstanceOf(InvalidArgumentException.class);
        
        Message messageWithBadId = new Message(goodMessage)
            .setMessageId(one(alphabeticStrings()));
        assertThrows(() -> Action.checkMessage(messageWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
    }

}