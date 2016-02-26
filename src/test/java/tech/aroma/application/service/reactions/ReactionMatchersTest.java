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

package tech.aroma.application.service.reactions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ReactionMatchersTest 
{

    @GeneratePojo
    private Message message;
    
    private Message emptyMessage;
    
    @GenerateString(HEXADECIMAL)
    private String randomString;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
    }

    private void setupData() throws Exception
    {
        emptyMessage = new Message();
    }

    @Test
    public void testMatchesAll()
    {
        ReactionMatcher matcher = ReactionMatchers.matchesAll();
        assertThat(matcher, notNullValue());
        
        assertThat(matcher.matches(null), is(true));
        assertThat(matcher.matches(emptyMessage), is(true));
        assertThat(matcher.matches(message), is(true));
    }

    @Test
    public void testMatchesNone()
    {
        ReactionMatcher matcher = ReactionMatchers.matchesNone();
        assertThat(matcher, notNullValue());
        
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(emptyMessage), is(false));
        assertThat(matcher.matches(message), is(false));
    }

    @Test
    public void testTitleContains()
    {
    }

    @Test
    public void testTitleEquals()
    {
    }

    @Test
    public void testBodyContains()
    {
    }

    @Test
    public void testBodyEquals()
    {
    }

    @Test
    public void testHostnameEquals()
    {
    }

    @Test
    public void testUrgencyEquals()
    {
    }

}