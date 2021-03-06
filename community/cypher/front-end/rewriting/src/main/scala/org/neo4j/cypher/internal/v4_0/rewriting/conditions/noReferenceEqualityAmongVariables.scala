/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.v4_0.rewriting.conditions

import org.neo4j.cypher.internal.v4_0.util.Ref
import org.neo4j.cypher.internal.v4_0.expressions.Variable
import org.neo4j.cypher.internal.v4_0.rewriting.Condition

case object noReferenceEqualityAmongVariables extends Condition {
  def apply(that: Any): Seq[String] = {
    val ids = collectNodesOfType[Variable].apply(that).map(Ref[Variable])
    ids.groupBy(x => x).collect {
      case (id, others) if others.size > 1 => s"The instance ${id.value} is used ${others.size} times"
    }.toIndexedSeq
  }

  override def name: String = productPrefix
}
