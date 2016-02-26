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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.Urgency;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
class ReactionMatchers 
{
    private final static Logger LOG = LoggerFactory.getLogger(ReactionMatchers.class);
    
    
    static ReactionMatcher matchesAll()
    {
        return message -> true;
    }
    
    static ReactionMatcher matchesNone()
    {
        return message -> false;
    }
    
    static ReactionMatcher titleContains(@NonEmpty String substring)
    {
        checkThat(substring)
            .is(nonEmptyString());
        
        return (message) ->
        {
            if (message == null)
            {
                return false;
            }
            
            if (isNullOrEmpty(message.title))
            {
                return false;
            }
            
            return message.title.contains(substring);
        };
        
    }
    
    static ReactionMatcher titleEquals(@NonEmpty String title)
    {
        checkThat(title)
            .is(nonEmptyString());
        
        return message ->
        {
            if (message == null)
            {
                return false;
            }
            
            if (isNullOrEmpty(message.title))
            {
                return false;
            }
            
            return message.title.equals(title);
        };
    }
    
    static ReactionMatcher bodyContains(@NonEmpty String substring)
    {
        checkThat(substring)
            .is(nonEmptyString());
        
        return message ->
        {
            if (message == null)
            {
                return false;
            }
            
            if (isNullOrEmpty(message.body))
            {
                return false;
            }
            
            return message.body.contains(substring);
        };
    }
    
    
    static ReactionMatcher bodyEquals(@NonEmpty String expectedBody)
    {
        checkThat(expectedBody)
            .usingMessage("Expected Body cannot be empty")
            .is(nonEmptyString());
        
        return message ->
        {
            if (message == null)
            {
                return false;
            }
            
            if (isNullOrEmpty(message.body))
            {
                return false;
            }
            
            return message.body.equals(expectedBody);
        };
    }
    
    static ReactionMatcher hostnameEquals(@NonEmpty String expectedHostname)
    {
        checkThat(expectedHostname)
            .usingMessage("Expected Hostname cannot be empty")
            .is(nonEmptyString());
        
        return message ->
        {
            if (message == null)
            {
                return false;
            }
            
            if (isNullOrEmpty(message.hostname))
            {
                return false;
            }
            
            return message.hostname.equals(expectedHostname);
        };
    }
    
    static ReactionMatcher urgencyEquals(@Required Urgency urgency)
    {
        checkThat(urgency)
            .usingMessage("Urgency cannot be null")
            .is(notNull());
        
        return message ->
        {
            if (message == null)
            {
                return false;
            }
            
            if (message.urgency == null)
            {
                return false;
            }
            
            return message.urgency == urgency;
        };
    }
}
