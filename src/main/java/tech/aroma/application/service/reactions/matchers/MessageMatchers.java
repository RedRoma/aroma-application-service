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

 
package tech.aroma.application.service.reactions.matchers;


import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Urgency;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
@NonInstantiable
final class MessageMatchers 
{
    private final static Logger LOG = LoggerFactory.getLogger(MessageMatchers.class);
    
    
    static MessageMatcher matchesAll()
    {
        return message -> true;
    }
    
    static MessageMatcher matchesNone()
    {
        return message -> false;
    }
    
    static MessageMatcher applicationIs(@NonEmpty String appId)
    {
        checkThat(appId)
            .is(validApplicationId());
        
        return message ->
        {
            if(message == null)
            {
                return false;
            }
            
            return Objects.equals(message.applicationId, appId);
        };
    }
    
    static MessageMatcher applicationIsNot(@NonEmpty String appId)
    {
        return not(applicationIs(appId));
    }
    
    static MessageMatcher not(@Required MessageMatcher matcher)
    {
        checkThat(matcher).is(notNull());
        
        return message ->
        {
            return !matcher.matches(message);
        };
    }
    
    static MessageMatcher titleContains(@NonEmpty String substring)
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
    
    static MessageMatcher titleIs(@NonEmpty String title)
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
    
    static MessageMatcher titleIsNot(@NonEmpty String title)
    {
        return not(titleIs(title));
    }
    
    static MessageMatcher bodyContains(@NonEmpty String substring)
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
    
    static MessageMatcher bodyDoesNotContain(@NonEmpty String substring)
    {
        return not(bodyContains(substring));
    }
    
    static MessageMatcher bodyIs(@NonEmpty String expectedBody)
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
    
    static MessageMatcher hostnameIs(@NonEmpty String expectedHostname)
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
 
    static MessageMatcher hostnameContains(@NonEmpty final String substring)
    {
        checkThat(substring)
            .usingMessage("Hostname string cannot be empty")
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
            
            return message.hostname.contains(substring);
        };
    }
    
    static MessageMatcher hostnameDoesNotContain(@NonEmpty final String substring)
    {
        return not(hostnameContains(substring));
    }

    static MessageMatcher urgencyIs(@Required Urgency urgency)
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
    
    static MessageMatcher urgencyIsOneOf(@Required Set<Urgency> urgencies)
    {
        checkThat(urgencies).is(notNull());
        
        return message ->
        {
            if (message == null)
            {
                return false;
            }
            
            //Interpreting no urgencies is matching all
            if (Sets.isEmpty(urgencies))
            {
                return true;
            }
            
            if (!message.isSetUrgency())
            {
                return false;
            }
            
            return urgencies.contains(message.urgency);
        };
    }
}
