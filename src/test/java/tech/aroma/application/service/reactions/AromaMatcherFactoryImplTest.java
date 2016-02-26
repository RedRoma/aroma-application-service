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
import tech.aroma.thrift.reactions.AromaMatcher;
import tech.aroma.thrift.reactions.MatcherAll;
import tech.aroma.thrift.reactions.MatcherBodyContains;
import tech.aroma.thrift.reactions.MatcherBodyIs;
import tech.aroma.thrift.reactions.MatcherTitleContains;
import tech.aroma.thrift.reactions.MatcherTitleIs;
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
public class AromaMatcherFactoryImplTest 
{

    private AromaMatcher matcher;
    
    @GeneratePojo
    private MatcherBodyContains bodyContains;
    
    @GeneratePojo
    private MatcherBodyIs bodyIs;
    
    @GeneratePojo
    private MatcherTitleContains titleContains;
    
    @GeneratePojo
    private MatcherTitleIs titleIs;
    
    @GeneratePojo
    private Message message;
    
    @GenerateString(HEXADECIMAL)
    private String randomString;
    
    
    private AromaMatcherFactoryImpl instance;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
        
        instance = new AromaMatcherFactoryImpl();
    }

    private void setupData() throws Exception
    {
        matcher = new AromaMatcher();

    }

    @Test
    public void testNull() throws Exception
    {
        ReactionMatcher result = instance.matcherFor(null);
        assertThat(result, notNullValue());
        
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testEmpty()
    {
        ReactionMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        
        assertThat(result.matches(message), is(false));
    }

    
    @Test
    public void testAll()
    {
        matcher.setAll(new MatcherAll());
        
        ReactionMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testBodyContainsWhenMatches()
    {
        String substring = message.body.substring(message.body.length() / 2);
        matcher.setBodyContains(new MatcherBodyContains(substring));
        
        ReactionMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testBodyContainsWhenNoMatch()
    {
        matcher.setBodyContains(new MatcherBodyContains(randomString));
        
        ReactionMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @Test
    public void testBodyIsWhenMatches()
    {
        String expected = message.body;
        matcher.setBodyIs(new MatcherBodyIs(expected));
        
        ReactionMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testBodyIsWhenNoMatch()
    {
        matcher.setBodyIs(new MatcherBodyIs(randomString));
        
        ReactionMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @Test
    public void testTitleContainsWhenMatch()
    {
        String substring = message.title.substring(message.title.length() / 2);
        matcher.setTitleContains(new MatcherTitleContains(substring));
        
        ReactionMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testTitleContainsWhenNoMatch()
    {
        matcher.setTitleContains(new MatcherTitleContains(randomString));
        
        ReactionMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
}