/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.core.action.builder

import com.excilys.ebi.gatling.core.action.{WhileAction, Action}
import com.excilys.ebi.gatling.core.config.ProtocolConfigurationRegistry
import com.excilys.ebi.gatling.core.session.Session
import com.excilys.ebi.gatling.core.structure.ChainBuilder

import akka.actor.Actor.actorOf
import akka.actor.{Uuid, ActorRef}

/**
 * Companion of the WhileActionBuilder class
 */
object WhileActionBuilder {
	/**
	 * Creates an initialized WhileActionBuilder
	 */
	def whileActionBuilder = new WhileActionBuilder(null, null, null, new Uuid().toString)
}

/**
 * This class builds a WhileActionBuilder
 *
 * @constructor create a new WhileAction
 * @param conditionFunction the function that determine the condition
 * @param loopNext chain that will be executed if conditionFunction evaluates to true
 * @param next action that will be executed if conditionFunction evaluates to false
 */
class WhileActionBuilder(conditionFunction: (Session, Action) => Boolean, loopNext: ChainBuilder, next: ActorRef, counterName: String)
		extends AbstractActionBuilder {

	/**
	 * Adds conditionFunction to this builder
	 *
	 * @param conditionFunction the condition function
	 * @return a new builder with conditionFunction set
	 */
	def withConditionFunction(conditionFunction: Session => Boolean): WhileActionBuilder = withConditionFunction((s: Session, a: Action) => conditionFunction(s))
	/**
	 * Adds conditionFunction to this builder
	 *
	 * @param conditionFunction the condition function
	 * @return a new builder with conditionFunction set
	 */
	def withConditionFunction(conditionFunction: (Session, Action) => Boolean) = new WhileActionBuilder(conditionFunction, loopNext, next, counterName)
	/**
	 * Adds loopNext to builder
	 *
	 * @param loopNext the chain executed if testFunction evaluated to true
	 * @return a new builder with loopNext set
	 */
	def withLoopNext(loopNext: ChainBuilder) = new WhileActionBuilder(conditionFunction, loopNext, next, counterName)
	/**
	 * Adds counterName to builder
	 *
	 * @param counterName the name of the counter that will be used
	 * @return a new builder with counterName set to None or Some(name)
	 */
	def withCounterName(counterName: String) = new WhileActionBuilder(conditionFunction, loopNext, next, counterName)

	def withNext(next: ActorRef) = new WhileActionBuilder(conditionFunction, loopNext, next, counterName)

	def build(protocolConfigurationRegistry: ProtocolConfigurationRegistry) = actorOf(new WhileAction(conditionFunction, (w: ActorRef) => loopNext.withNext(w).build(protocolConfigurationRegistry), next, counterName)).start
}