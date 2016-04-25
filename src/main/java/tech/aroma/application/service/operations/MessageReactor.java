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


package tech.aroma.application.service.operations;

import java.util.List;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.application.service.reactions.actions.Action;
import tech.aroma.application.service.reactions.actions.AromaActionFactory;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.reactions.AromaAction;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 *
 * @author SirWellington
 */
public interface MessageReactor 
{
    List<Action> createActionsFor(@Required Message message, @NonEmpty List<AromaAction> actions);
    
    class Impl implements MessageReactor
    {
        private AromaActionFactory factory;
        
        @Override
        public List<Action> createActionsFor(Message message, List<AromaAction> actions)
        {
            if (message == null)
            {
                return Lists.emptyList();
            }
            
            boolean runThroughInbox = true;
            boolean storeMessage = true;
            
            List<Action> initialActions = Lists.create();
            
            for (AromaAction action : actions)
            {
                if (action.isSetSkipInbox())
                {
                    runThroughInbox = false;
                    continue;
                }
                
                if (action.isSetDontStoreMessage())
                {
                    storeMessage = false;
                }
            }
            
            
            if (storeMessage)
            {
                Action actionToStoreMessage = factory.actionToStoreMessage(message);
                initialActions.add(actionToStoreMessage);
            }
            
            if (runThroughInbox)
            {
                Action actionToRunThroughFollowerInboxes = factory.actionToRunThroughFollowerInboxes(message, this);
                initialActions.add(actionToRunThroughFollowerInboxes);
            }
            
            
            return initialActions;
            
        }
        
    }
}
