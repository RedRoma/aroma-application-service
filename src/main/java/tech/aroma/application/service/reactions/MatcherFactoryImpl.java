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
import tech.aroma.thrift.reactions.AromaMatcher;
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern.Role.FACTORY;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 *
 * @author SirWellington
 */
@FactoryPattern(role = FACTORY)
final class MatcherFactoryImpl implements MatcherFactory
{
    private final static Logger LOG = LoggerFactory.getLogger(MatcherFactoryImpl.class);

    @Override
    public MessageMatcher matcherFor(AromaMatcher matcher)
    {
        if(matcher == null || matcher.isSetAll())
        {
            return MessageMatchers.matchesAll();
        }
        
        if (matcher.isSetBodyContains())
        {
            String substring = matcher.getBodyContains().getSubstring();
            
            checkThat(substring)
                .usingMessage("Matcher substring cannot be empty")
                .is(nonEmptyString());
            
            return MessageMatchers.bodyContains(substring);
        }
        
        if(matcher.isSetBodyIs())
        {
            String expectedBody = matcher.getBodyIs().getExpectedBody();
            
            checkThat(expectedBody)
                .usingMessage("Expected Body cannot be empty")
                .is(nonEmptyString());
            
            return MessageMatchers.bodyIs(expectedBody);
        }
        
        if(matcher.isSetTitleContains())
        {
            String substring = matcher.getTitleContains().getSubstring();
            
            checkThat(substring)
                .usingMessage("Matcher substring cannot be empty")
                .is(nonEmptyString());
            
            return MessageMatchers.titleContains(substring);
        }
        
        
        if(matcher.isSetTitleIs())
        {
            String expectedTitle = matcher.getTitleIs().getExpectedTitle();
            
            checkThat(expectedTitle)
                .usingMessage("Expected Message Title cannot be empty")
                .is(nonEmptyString());
            
            return MessageMatchers.titleContains(expectedTitle);
        }
        
        
        if(matcher.isSetUrgencyEquals())
        {
            Urgency expectedUrgency = matcher.getUrgencyEquals().getUrgency();
            
            checkThat(expectedUrgency)
                .usingMessage("Expected Urgency cannot be null")
                .is(notNull());
            
            return MessageMatchers.urgencyIs(expectedUrgency);
        }
        
        if(matcher.isSetHostnameIs())
        {
            String expectedHostname = matcher.getHostnameIs().getExpectedHostname();
            
            checkThat(expectedHostname)
                .usingMessage("Expected Hostname cannote be empty")
                .is(nonEmptyString());
            
            return MessageMatchers.hostnameIs(expectedHostname);
        }
        
        return MessageMatchers.matchesNone();
    }

}
