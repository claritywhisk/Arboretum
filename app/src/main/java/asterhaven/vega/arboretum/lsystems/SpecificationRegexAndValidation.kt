package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.lsystems.validation.accessors.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.accessors.each
import dev.nesk.akkurate.constraints.builders.isMatching
import dev.nesk.akkurate.constraints.builders.isNotBlank
import dev.nesk.akkurate.constraints.builders.isNotEmpty
import dev.nesk.akkurate.constraints.constrain
import dev.nesk.akkurate.constraints.otherwise
import dev.nesk.akkurate.validatables.and
import java.util.regex.Pattern
import java.util.stream.Collectors

object SpecificationRegexAndValidation {
    private const val rgxLWord = "(.)([(](.*?)[)])?" // group 1 is symbol, group 3 is param
    val patWord by lazy { Pattern.compile(rgxLWord) }

    private const val validRawSentence = "($rgxLWord)*"  //" ($rgxLWord )*"
    private val rgxValidRawSentence = Regex(validRawSentence)

    val validateSpecification by lazy {
        Validator<Specification> {
            constrain {
                validateAxiom(it.initial) is ValidationResult.Success
            } otherwise { "Check initial/axiom" }
            productions.each { constrain {
                validateProduction(it) is ValidationResult.Success
            } otherwise {"Invalid production"} }
            params.each { constrain {
                validateParameter(unwrap()) is ValidationResult.Success
            } otherwise {"Problem with parameter"} }
            params.constrain {
                it.size == it.stream().map{p -> p.symbol}.collect(Collectors.toSet()).size
            } otherwise {
                "Parameter symbols are not unique."
            }
            params.constrain {it.firstOrNull { p -> p.type is DerivationSteps} != null } otherwise {
                "No steps parameter."
            }

        }
    }

    val rgxMsg : () -> String = {
        FirebaseCrashlytics.getInstance().apply {
            log("Unexpected non-matching of regex")
            sendUnsentReports()
        }
        "Not a valid arrangement of words"
    }
    val bracketMsg : () -> String = {
        "Brackets [] are not paired"
    }
    fun bracketsMatch(s : String) : Boolean {
        var l = 0
        for(c in s) when(c){
            '[' -> l++
            ']' -> if(l-- == 0) return false
            else -> {}
        }
        return l == 0
    }

    val validateAxiom by lazy {
        Validator<String> {
            isNotEmpty()
            isNotBlank()
            constrain { bracketsMatch(this.unwrap()) } otherwise bracketMsg
            isMatching(rgxValidRawSentence) otherwise rgxMsg
        }
    }

    val validateProduction by lazy {
        Validator<LProduction> {
            (before and after){
                isNotEmpty()
                isNotBlank()
                constrain { bracketsMatch(this.unwrap()) } otherwise bracketMsg
                isMatching(rgxValidRawSentence) otherwise rgxMsg
            }
        }
    }

    val validateSymbol by lazy {
        Validator<LSymbol> {
            //unwrap().meaning.isMatching(rgxValidRawSentence)
            //all TODO this file
        }
    }

    val validateParameter by lazy {
        Validator<LParameter> {
            constrain { type.unwrap().range.contains(initialValue.unwrap()) } otherwise {
                "Parameter value outside of provided range."
            }
        }
    }
}