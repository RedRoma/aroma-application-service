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

import tech.aroma.thrift.Message;
import tech.aroma.thrift.reactions.AromaAction;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 * An Action Mapper takes an {@link AromaAction} and creates a corresponding {@link Action}.
 * 
 * @author SirWellington
 */
public interface ActionMapper 
{

    public Action create(@Required Message message, @Required AromaAction action);
    
    class Impl implements ActionMapper
    {
        private ActionFactory factory;
        
        @Override
        public Action create(Message message, AromaAction action)
        {
            if (action == null)
            {
                return factory.actionToDoNothing();
            }

            if (action.isSetForwardToSlackChannel())
            {
                return factory.actionToSendToSlackChannel(message, action.getForwardToSlackChannel());
            }

            if (action.isSetForwardToSlackUser())
            {
                return factory.actionToSendToSlackUser(message, action.getForwardToSlackUser());
            }

            if (action.isSetSendEmail())
            {
                return factory.actionToSendEmail(message, action.getSendEmail());
            }
            
            
            return factory.actionToDoNothing();
        }
        
    }
    
}
