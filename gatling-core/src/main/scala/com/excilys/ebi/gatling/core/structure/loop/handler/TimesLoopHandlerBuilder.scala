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
package com.excilys.ebi.gatling.core.structure.loop.handler

import com.excilys.ebi.gatling.core.action.builder.AbstractActionBuilder
import com.excilys.ebi.gatling.core.structure.AbstractStructureBuilder
import com.excilys.ebi.gatling.core.structure.ChainBuilder
import akka.actor.Uuid
import com.excilys.ebi.gatling.core.session.handler.CounterBasedIterationHandler
import com.excilys.ebi.gatling.core.action.builder.SimpleActionBuilder._
import com.excilys.ebi.gatling.core.session.Session

/**
 * This builder creates a 'for' loop. This is achieved by copying the chain as many times at it should run
 * and adding simple actions between the chains to initialize, increment and release the counter.
 *
 * @constructor constructs a TimesLoopHandlerBuilder
 * @param structureBuilder the structure builder on which loop was called
 * @param chain the chain of actions that should be repeated
 * @param times the number of times that the chain should be repeated
 * @param userDefinedCounterName the optional user-defined name of the counter for this loop
 */
class TimesLoopHandlerBuilder[B <: AbstractStructureBuilder[B]](structureBuilder: B, chain: ChainBuilder, times: Int, userDefinedCounterName: Option[String])
		extends AbstractLoopHandlerBuilder[B](structureBuilder) {

	/**
	 * Actually builds the current 'for' loop to the structure builder
	 */
	private[core] def build: B = {

		val handler = new CounterBasedIterationHandler {
			val counterName = userDefinedCounterName.getOrElse(new Uuid().toString)
		}

		// Adds an increment action after the chain
		val chainActions: List[AbstractActionBuilder] = chain.actionBuilders ::: List(simpleActionBuilder((session: Session) => handler.increment(session)))

		var iteratedActions: List[AbstractActionBuilder] = Nil

		for (i <- 1 to times)
			iteratedActions = chainActions ::: iteratedActions

		iteratedActions = simpleActionBuilder((session: Session) => handler.expire(session)) :: iteratedActions ::: List(simpleActionBuilder((session: Session) => handler.init(session)))

		doBuild(iteratedActions)
	}
}