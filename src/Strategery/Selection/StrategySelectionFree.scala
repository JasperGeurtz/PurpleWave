package Strategery.Selection
import Lifecycle.With
import Mathematics.PurpleMath
import Strategery.Strategies.Strategy

object StrategySelectionFree extends StrategySelectionPolicy {
  def chooseBest(topLevelStrategies: Iterable[Strategy]): Iterable[Strategy] = {
    val permutations            = topLevelStrategies.flatMap(expandStrategy)
    val strategies              = permutations.flatten.toVector.distinct
    val strategyEvaluations     = strategies.map(strategy => (strategy, With.strategy.evaluate(strategy))).toMap
    val permutationEvaluations  = permutations.map(p => (p, p.map(strategyEvaluations))).toMap
    With.strategy.interest      = permutationEvaluations.map(p => (p._1, PurpleMath.geometricMean(p._2.map(_.interestTotal))))
    val mostInterest            = With.strategy.interest.values.max
    val bestPermutation         = With.strategy.interest.find(_._2 >= mostInterest).get
    bestPermutation._1
  }
  
  private def expandStrategy(strategy: Strategy): Iterable[Iterable[Strategy]] = {
    
    /*
    1. Start:
    Groups of(Pick one of these strategies)
    S:[[ABC],[DEF],[GHI],[JKL]
    
    2. Filter illegal choices I, JKL
    Groups of(Pick one of these legal strategies)
    S:[[ABC],[DEF],[GH],[]]
    
    3. Remove empty choice groups
    Non-empty groups of (Pick one of these legal strategies)
    S:
    [
      [ABC],
      [DEF],
      [GH]
    ]
    
    4. Replace all choices with their expanded counterparts
    Non-empty groups of(Pick one of these(Chain of strategies)))
    S:
    [
      [
        [A,A01,A11],
        [A,A02,A11],
        ...,
        [B,B01,B11],
        [B,B02,B11],
        ...
      ],
      [
        D...
      ]
    ]
    
    5. For each row of choices, choose one of each chain
    */
    
    // 1. Groups of(Pick one of these strategies)
    
    val choices = strategy.choices
    
    // 2. Groups of(Pick one of these legal strategies)
    val legalChoices = choices.map(With.strategy.filterStrategies)
    
    //3. Non-empty groups of (Pick one of these legal strategies)
    val extantChoices = legalChoices.filter(_.nonEmpty)
    if (extantChoices.isEmpty) {
      return Iterable(Iterable(strategy))
    }
    
    //4. Non-empty groups of(Pick one of these(Chain of strategies)))
    val extantChoicesChains = extantChoices.map(_.flatMap(expandStrategy))
    
    var output = Iterable(Iterable(strategy))
    extantChoicesChains.foreach(nextChoiceChain =>
      output = output.flatMap(outputChain =>
        nextChoiceChain.map(nextChoice =>
          outputChain ++ nextChoice))
    )
    
    output = output.map(_.toSet.toIterable)
    output
  }
}
