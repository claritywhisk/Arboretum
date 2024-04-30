package asterhaven.vega.arboretum.lsystems

import asterhaven.vega.arboretum.lsystems.validation.accessors.*
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.accessors.each
import dev.nesk.akkurate.constraints.builders.isMatching
import dev.nesk.akkurate.constraints.constrain
import dev.nesk.akkurate.constraints.otherwise
import dev.nesk.akkurate.validatables.and
import java.util.regex.Pattern
import java.util.stream.Collectors

object SpecificationRegexAndValidation {
    private const val rgxLWord = "(.)([(](.*?)[)])?" // group 1 is symbol, group 3 is param
    val patWord by lazy { Pattern.compile(rgxLWord) }

    private const val validRawSentence = " ($rgxLWord )+"
    private val rgxValidRawSentence = Regex(validRawSentence)

    val validateSpecification by lazy {
        Validator<Specification> {
            initial.isMatching(rgxValidRawSentence)
            productions.each { constrain {
                validateProduction(it) is ValidationResult.Success
            }}
            params.each { constrain {
                validateParameter(unwrap()) is ValidationResult.Success
            }}
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

    val validateParameter by lazy {
        Validator<Specification.Parameter> {
            constrain { type.unwrap().range.contains(initialValue.unwrap()) } otherwise {
                "Parameter value out of provided range."
            }
        }
    }

    val validateProduction by lazy {
        Validator<Specification.Production> {
            (before and after){
                isMatching(rgxValidRawSentence)
            }
        }
    }
}