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


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.reactions.AromaMatcher;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;

import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ReactionGenerators.matchers;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.alternatingBooleans;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;

/**
 *
 * @author SirWellington
 */
@Internal
final class MatchBehavior 
{
    private final static Logger LOG = LoggerFactory.getLogger(MatchBehavior.class);

    static Map<AromaMatcher, MessageMatcher> createMatchersThatSometimesMatchFor(Message message)
    {
        return createMatchersFor(message, alternatingBooleans());
    }

    static Map<AromaMatcher, MessageMatcher> createMatchersThatAlwaysMatchFor(Message message)
    {
        return createMatchersFor(message, () -> true);
    }

    static Map<AromaMatcher, MessageMatcher> createMatchersThatNeverMatchFor(Message message)
    {
        return createMatchersFor(message, () -> false);
    }

    static Map<AromaMatcher, MessageMatcher> createMatchersFor(Message message, AlchemyGenerator<Boolean> booleans)
    {
        Map<AromaMatcher, MessageMatcher> result = Maps.create();
        List<AromaMatcher> matchers = listOf(matchers(), 20);

        for (AromaMatcher matcher : matchers)
        {
            MessageMatcher mock = mock(MessageMatcher.class);
            when(mock.matches(message)).thenReturn(one(booleans));
            
            result.put(matcher, mock);
        }

        return result;
    }

}
