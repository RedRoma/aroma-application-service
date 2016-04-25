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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ActionTest 
{

    @GeneratePojo
    private Message goodMessage;
    
    @GeneratePojo
    private Message badMessage;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    @GenerateString(UUID)
    private String goodId;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
    }


    private void setupData() throws Exception
    {
        goodMessage.messageId = goodId;
        goodMessage.applicationId = goodId;
        
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
    }

}