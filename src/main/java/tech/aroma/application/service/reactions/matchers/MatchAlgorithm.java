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

import com.google.inject.ImplementedBy;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.reactions.AromaMatcher;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;

/**
 * The matching algorithm decides on what criteria to judge whether a Message Matches a set
 * of {@linkplain AromaMatcher Matchers}. For instance, an <b>AND</b> operation vs an <b>OR</b> operation.
 * 
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
@ImplementedBy(MatchAlgorithmAnd.class)
public interface MatchAlgorithm
{
    
    boolean matches(@Required Message message, @NonEmpty List<AromaMatcher> matchers);
 
    /**
     * Matches only when ALL of the conditions are met.
     * 
     * @param matcherFactory
     * @return
     * @throws IllegalArgumentException 
     */
    static MatchAlgorithm and(MatcherFactory matcherFactory) throws IllegalArgumentException
    {
        return new MatchAlgorithmAnd(matcherFactory);
    }
    
    /**
     * Matches when only ONE of the conditions are met.
     * 
     * @param matcherFactory
     * @return
     * @throws IllegalArgumentException 
     */
    static MatchAlgorithm or(MatcherFactory matcherFactory) throws IllegalArgumentException
    {
        return new MatchAlgorithmOr(matcherFactory);
    }
}
