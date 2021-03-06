/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.planner.logical.plans

import org.mockito.Mockito._
import org.neo4j.cypher.internal.compiler.planner._
import org.neo4j.cypher.internal.compiler.planner.logical.ExpressionEvaluator
import org.neo4j.cypher.internal.compiler.planner.logical.Metrics.QueryGraphSolverInput
import org.neo4j.cypher.internal.compiler.planner.logical.steps.labelScanLeafPlanner
import org.neo4j.cypher.internal.ir.{Predicate, QueryGraph, Selections, InterestingOrder}
import org.neo4j.cypher.internal.planner.spi.PlanningAttributes.Cardinalities
import org.neo4j.cypher.internal.logical.plans.{LogicalPlan, NodeByLabelScan}
import org.neo4j.cypher.internal.v4_0.ast.semantics.SemanticTable
import org.neo4j.cypher.internal.v4_0.expressions.PatternExpression
import org.neo4j.cypher.internal.v4_0.util.test_helpers.CypherFunSuite
import org.neo4j.cypher.internal.v4_0.util.{Cost, LabelId}

class LabelScanLeafPlannerTest extends CypherFunSuite with LogicalPlanningTestSupport {

  private val statistics = hardcodedStatistics

  test("simple label scan without compile-time label id") {
    // given
    val idName = "n"
    val qg = QueryGraph(
      selections = Selections(Set(Predicate(Set(idName), hasLabels(idName, "Awesome")))),
      patternNodes = Set(idName))

    val factory = newMockedMetricsFactory
    when(factory.newCostModel(config)).thenReturn((plan: LogicalPlan, _: QueryGraphSolverInput, _: Cardinalities) => plan match {
      case _: NodeByLabelScan => Cost(1)
      case _                  => Cost(Double.MaxValue)
    })

    val semanticTable = new SemanticTable()

    val context = newMockedLogicalPlanningContext(planContext = newMockedPlanContext(), metrics = factory.newMetrics(statistics, mock[ExpressionEvaluator], config), semanticTable = semanticTable)

    // when
    val resultPlans = labelScanLeafPlanner(qg, InterestingOrder.empty, context)

    // then
    resultPlans should equal(Seq(
      NodeByLabelScan(idName, labelName("Awesome"), Set.empty))
    )
  }

  test("simple label scan with a compile-time label ID") {
    // given
    val idName = "n"
    val labelId = LabelId(12)
    val qg = QueryGraph(
      selections = Selections(Set(Predicate(Set(idName), hasLabels("n", "Awesome")))),
      patternNodes = Set(idName))

    val factory = newMockedMetricsFactory
    when(factory.newCostModel(config)).thenReturn((plan: LogicalPlan, _: QueryGraphSolverInput, _: Cardinalities) => plan match {
      case _: NodeByLabelScan => Cost(100)
      case _                  => Cost(Double.MaxValue)
    })

    val semanticTable: SemanticTable = newMockedSemanticTable
    when(semanticTable.id(labelName("Awesome"))).thenReturn(Some(labelId))

    val context = newMockedLogicalPlanningContext(planContext = newMockedPlanContext(), metrics = factory.newMetrics(statistics, mock[ExpressionEvaluator], config), semanticTable = semanticTable)

    // when
    val resultPlans = labelScanLeafPlanner(qg, InterestingOrder.empty, context)

    // then
    resultPlans should equal(
      Seq(NodeByLabelScan(idName, labelName("Awesome"), Set.empty)))
  }
}
