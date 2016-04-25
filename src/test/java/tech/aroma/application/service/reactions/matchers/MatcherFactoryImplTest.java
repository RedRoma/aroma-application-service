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

package tech.aroma.application.service.reactions.matchers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Urgency;
import tech.aroma.thrift.reactions.AromaMatcher;
import tech.aroma.thrift.reactions.MatcherAll;
import tech.aroma.thrift.reactions.MatcherBodyContains;
import tech.aroma.thrift.reactions.MatcherBodyDoesNotContain;
import tech.aroma.thrift.reactions.MatcherBodyIs;
import tech.aroma.thrift.reactions.MatcherHostnameIs;
import tech.aroma.thrift.reactions.MatcherTitleContains;
import tech.aroma.thrift.reactions.MatcherTitleIs;
import tech.aroma.thrift.reactions.MatcherUrgencyIs;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class MatcherFactoryImplTest 
{

    private AromaMatcher matcher;

    @GeneratePojo
    private Message message;

    @GenerateString(HEXADECIMAL)
    private String randomString;

    
    private MatcherFactoryImpl instance;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
        
        instance = new MatcherFactoryImpl();
    }

    private void setupData() throws Exception
    {
        matcher = new AromaMatcher();

    }

    @Test
    public void testNull() throws Exception
    {
        MessageMatcher result = instance.matcherFor(null);
        assertThat(result, notNullValue());
        
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testEmpty()
    {
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        
        assertThat(result.matches(message), is(false));
    }

    
    @Test
    public void testAll()
    {
        matcher.setAll(new MatcherAll());
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testBodyContainsWhenMatches()
    {
        String substring = halfOf(message.body);
        matcher.setBodyContains(new MatcherBodyContains(substring));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testBodyContainsWhenNoMatch()
    {
        matcher.setBodyContains(new MatcherBodyContains(randomString));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @Test
    public void testBodyDoesNotContainWhenMatches()
    {
        matcher.setBodyDoesNotContain(new MatcherBodyDoesNotContain(randomString));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testBodyDoesNotContainWhenNoMatch()
    {
        String substring = halfOf(message.body);
        matcher.setBodyDoesNotContain(new MatcherBodyDoesNotContain(substring));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @Test
    public void testBodyIsWhenMatches()
    {
        String expected = message.body;
        matcher.setBodyIs(new MatcherBodyIs(expected));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testBodyIsWhenNoMatch()
    {
        matcher.setBodyIs(new MatcherBodyIs(randomString));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @Test
    public void testTitleContainsWhenMatch()
    {
        String substring = halfOf(message.title);
        matcher.setTitleContains(new MatcherTitleContains(substring));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testTitleContainsWhenNoMatch()
    {
        matcher.setTitleContains(new MatcherTitleContains(randomString));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @Test
    public void testTitleIsWhenMatch()
    {
        String expected = message.title;
        matcher.setTitleIs(new MatcherTitleIs(expected));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testTitleIsWhenNoMatch()
    {
        matcher.setTitleIs(new MatcherTitleIs(randomString));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @Test
    public void testHostnameIsWhenMatch()
    {
        String expected = message.hostname;
        matcher.setHostnameIs(new MatcherHostnameIs(expected));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testHostnameIsWhenNoMatch()
    {
        matcher.setHostnameIs(new MatcherHostnameIs(randomString));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @Test
    public void testUrgencyIsWhenMatch()
    {
        Urgency expected = message.urgency;
        matcher.setUrgencyEquals(new MatcherUrgencyIs(expected));
        
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testUrgencyIsWhenNoMatch()
    {
        AlchemyGenerator<Urgency> urgencies = enumValueOf(Urgency.class);
        Urgency expected = one(urgencies);
        
        while(expected == message.urgency)
        {
            expected = one(urgencies);
        }
        
        matcher.setUrgencyEquals(new MatcherUrgencyIs(expected));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    private String halfOf(String string)
    {
        return string.substring(0, string.length() / 2);
    }
}