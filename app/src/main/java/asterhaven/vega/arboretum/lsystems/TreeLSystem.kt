package asterhaven.vega.arboretum.lsystems

/* L-Systems see e.g.
    Przemyslaw Prusinkiewicz
    Aristid Lindenmayer
    The Algorithmic Beauty of Plants
 */

typealias LStr = Array<LSymbol>
typealias LList = ArrayList<LSymbol>

class TreeLSystem(ω : LList, private vararg val prod : Rule) {
    class Rule(
        val beforeTemplate : LStr,
        private val afterTemplate: LStr,
        private vararg val fPWord : (FloatArray) -> Float //given the LHS params, produce each param value
    ){
        private val inParams = FloatArray(beforeTemplate.count{ it is LSymbol.ParametricWord })
        private var matchIndex = 0
        fun beginMatch() {
            matchIndex = 0
        }
        fun match(s : LSymbol) : LStr? {
            if(s::class == beforeTemplate[matchIndex]::class){
                if(s is LSymbol.ParametricWord){
                    inParams[matchIndex] = s.a
                }
                if(++matchIndex == beforeTemplate.size) {
                    matchIndex = 0
                    var iF = 0
                    return LStr(afterTemplate.size) {
                        val sym = afterTemplate[it]
                        if(sym is LSymbol.ParametricWord) sym.withValue(fPWord[iF++](inParams))
                        else sym
                    }
                }
            }
            else matchIndex = 0
            return null
        }
    }

    fun valueForStep(n : Int) : LList {
        if(valueForStep.size > n) valueForStep[n]?.let { return it }
        var x = valueForStep.highestStoredStep
        if(x > n) while(valueForStep[x] == null) x--
        return computeValueForStep(n, x)
    }
    private val valueForStep = object : ArrayList<LList?>(){
        init { add(ω) }
        var highestStoredStep = 0
        override fun set(index: Int, element: LList?): LList? {
            if(index == 0) throw UnsupportedOperationException()
            if(element != null && index > highestStoredStep) highestStoredStep = index
            if(element == null && index == highestStoredStep)
                do highestStoredStep-- while(this[highestStoredStep] == null)
            return super.set(index, element)
        }
    }
    private fun computeValueForStep(n : Int, givenStep : Int) : LList {
        var string = valueForStep[givenStep] ?: throw ConcurrentModificationException()
        fun clearMatch() {
            prod.forEach(Rule::beginMatch)
        }
        repeat(n - givenStep) {
            clearMatch()
            val new = LList()
            var l = 0
            fun takeUnmatched(j : Int) {
                for (i in l..j) new += string[i]
            }
            letter@for(r in string.indices) for (p in prod.indices)
                when(val replacement = prod[p].match(string[r])) {
                    null -> continue
                    else -> {
                        val len = prod[p].beforeTemplate.size
                        takeUnmatched(r - len )
                        l = r + 1
                        new += replacement
                        clearMatch()
                        continue@letter
                    }
                }
            takeUnmatched(string.lastIndex)
            string = new
        }
        return string
    }
}
