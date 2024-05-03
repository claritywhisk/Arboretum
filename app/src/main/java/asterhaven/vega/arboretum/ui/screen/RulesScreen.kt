package asterhaven.vega.arboretum.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.BuildConfig
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.lsystems.LWord
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.lsystems.Specification.Item
import asterhaven.vega.arboretum.lsystems.Specification.Parameter
import asterhaven.vega.arboretum.lsystems.Specification.Production
import asterhaven.vega.arboretum.lsystems.Specification.Symbol
import asterhaven.vega.arboretum.lsystems.SpecificationRegexAndValidation
import asterhaven.vega.arboretum.lsystems.TrueConstant
import asterhaven.vega.arboretum.ui.ArboretumScreen
import asterhaven.vega.arboretum.ui.components.CanShowErrorBelow
import asterhaven.vega.arboretum.ui.components.LabeledSection
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import java.lang.IllegalStateException
import kotlin.reflect.KClass

@Composable
fun RulesScreen(
    baseSpecification : Specification, //the selected spec which this screen shows/modifies
    updateSpecification: (Specification) -> Unit,
    leavingScreen : ArboretumScreen?
) {
    class Section(val heading : String)
    val NONE = Section("")
    val AXIOM = Section(LocalContext.current.getString(R.string.rules_label_axiom))
    val RULES = Section(LocalContext.current.getString(R.string.rules_label_productions))
    val SYMBOLS = Section(LocalContext.current.getString(R.string.rules_label_custom_symbols))
    val PARAMS = Section(LocalContext.current.getString(R.string.rules_label_parameters))

    val axiom by remember { mutableStateOf(baseSpecification.initial) }
    val productionRules = remember { mutableStateListOf<Production>().apply {
        baseSpecification.productions.forEach { add(it.copy()) }
    } }
    val symbols = remember { mutableStateListOf<Symbol>() }
    val newParams = remember { mutableStateListOf<Parameter>().apply {
        baseSpecification.params.forEach { add(it.copy()) }
    } }

    fun errOK() = mutableStateOf<ValidationResult.Failure?>(null)
    fun errsOK(k : Int) = mutableStateListOf<ValidationResult.Failure?>().apply{
        repeat(k) { add(null) }
    }
    var errorAxiom              by remember { errOK() }
    var errorsProductions       = remember { errsOK(productionRules.size) }
    var errorOverall            by remember { errOK() }
    var errorsSymbolIndividual  = remember { errsOK(symbols.size) }
    var errorSymbols            by remember { errOK() }
    var errorsParamIndividual   = remember { errsOK(newParams.size) }
    var errorParams             by remember { errOK() }

    val NOT_EDITING = -1
    var editingSection      =  remember { NONE }
    var editingRow          =  remember { NOT_EDITING }
    var editingCursorPos    =  remember { NOT_EDITING }
    var editingString       by remember { mutableStateOf("") }
    var editingIsInParens           by remember { mutableStateOf(false) }
    var formUnvalidated             by remember { mutableStateOf(false) }

    fun tryNewSpecification() {
        val newSpecification = Specification(
            name ="todo",
            initial = axiom,
            productions = productionRules.toList(),
            params = newParams.toList(),
            constants = HashMap<String, Float>().apply {
                newParams.forEach { p -> this[p.symbol] = p.initialValue }
            }
        )
        when(val result = SpecificationRegexAndValidation.validateSpecification(newSpecification)){
            is ValidationResult.Success -> {
                errorOverall = null
                updateSpecification(newSpecification)
            }
            is ValidationResult.Failure -> errorOverall = result
        }
        formUnvalidated = false
    }
    fun tryRow() {
        fun <T> valRes(item : T, vf : Validator.Runner<T>) = when(val result =
            vf(item)) {
                is ValidationResult.Success -> null
                is ValidationResult.Failure -> result
            }
        val r = editingRow
        when(editingSection){
            AXIOM -> errorAxiom = valRes(axiom, SpecificationRegexAndValidation.validateAxiom)
            RULES -> errorsProductions[r] = valRes(productionRules[r], SpecificationRegexAndValidation.validateProduction)
            SYMBOLS -> errorsSymbolIndividual[r] = valRes(symbols[r], SpecificationRegexAndValidation.validateSymbol)
            PARAMS -> errorsParamIndividual[r] = valRes(newParams[r], SpecificationRegexAndValidation.validateParameter)
            else -> {}
        }
    }

    fun Modifier.consumeClickEventsWhen(predicate : () -> Boolean) = this.pointerInput(Unit) {
        awaitPointerEventScope {
            while(true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                if (predicate()) event.changes.forEach { it.consume() }
            }
        }
    }
    fun Modifier.consumeClickEvents() = this.consumeClickEventsWhen { true }
    fun rangeOfCursorIndexOrWord() : IntRange {
        val s = editingString
        val c = editingCursorPos
        return when (s[c]) {
            '(' -> c - 1..(c..s.lastIndex).first { s[it] == ')' }
            else -> c..c
        }
    }

    @Composable
    fun AccursedText(initializingText: String, thisSection : Section, thisRow : Int,
                     modifier: Modifier = Modifier) {
        var editingThis by remember { mutableStateOf(false) }
        var myText by remember { mutableStateOf(initializingText) }

        fun isInParens() : Boolean {
            var i = editingCursorPos + 1
            while(i in myText.indices) when(myText[i]) {
                '(' -> return false
                ')' -> return true
                else -> i++
            }
            return false
        }
        LaunchedEffect(editingCursorPos) {
            if(editingThis) editingIsInParens = isInParens()
        }
        LaunchedEffect(editingRow, editingSection) {
            if(editingRow == NOT_EDITING || editingSection == NONE) editingThis = false
        }
        LaunchedEffect(editingString) {
            if(editingThis) myText = editingString
        }
        ClickableText(
            modifier = modifier.padding(8.dp),
            style = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            text = buildAnnotatedString {
                //Deliver the text and cursor/highlight
                append(myText)
                if(editingThis) {
                    val sStyle = SpanStyle(background = MaterialTheme.colorScheme.tertiary)
                    val c = editingCursorPos
                    if (myText[c] == '(') {
                        addStyle(sStyle, c - 1, c + 1)
                        val iRightParen = (c..myText.lastIndex)
                            .first { myText[it] == ')' }
                        addStyle(sStyle, iRightParen, iRightParen + 1)
                    } else addStyle(sStyle, c, c + 1)
                }
            }
        ) { clickedI ->
            val text = myText
            val nextI = clickedI + 1
            editingCursorPos = when {
                //rest on the opening paren of each parametric word
                nextI <= text.lastIndex && text[nextI] == '(' -> nextI
                text[clickedI] == ',' -> clickedI - 1
                text[clickedI] == ')' -> clickedI - 1
                else -> clickedI
            }
            editingRow = thisRow
            editingSection = thisSection
            editingString = initializingText
            editingThis = true
        }
    }
    @Composable
    fun EditTray() {
        //todo depending on section and specific field
        Row(Modifier.consumeClickEvents()) {
            fun charAtCursor(offset : Int) = editingString[editingCursorPos + offset]
            IconButton(enabled = editingCursorPos > 0, onClick = {
                editingCursorPos -= when {
                    charAtCursor( 0) == '(' -> 2
                    charAtCursor(-1) == ',' -> 2
                    else -> 1
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Move cursor left")
            }
            IconButton(enabled = editingCursorPos < editingString.length, onClick = {
                editingCursorPos += when {
                    charAtCursor(2) == '(' -> 2
                    charAtCursor(1) == ',' -> 2
                    charAtCursor(1) == ')' -> 2
                    else -> 1
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Move cursor right")
            }
            IconButton(enabled = editingCursorPos > 0, onClick = {
                editingString = editingString.removeRange(rangeOfCursorIndexOrWord())
                formUnvalidated = true
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Backspace")
            }
            if(formUnvalidated) TextButton(onClick = {
                tryRow()
                tryNewSpecification()
            }) {
                Text(LocalContext.current.getString(R.string.rules_btn_validate))
            }
            else IconButton(onClick = {}) {
                val errForCurrentRow = when(editingSection){
                    AXIOM  -> errorAxiom
                    RULES  -> errorsProductions[editingRow]
                    SYMBOLS-> errorsSymbolIndividual[editingRow]
                    PARAMS -> errorsParamIndividual[editingRow]
                    else   -> null
                }
                if (errForCurrentRow == null) {
                    Icon(Icons.Default.Check, contentDescription = "Row is valid")
                    if(errorOverall == null) Icon(Icons.Default.Check, contentDescription = "All valid!")
                    else Icon(Icons.Default.Warning, contentDescription = "Review errors")
                }
                else Icon(Icons.Default.Warning, contentDescription = "Check item for errors")
            }
        }
        Row {
            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                //TODO choose params
                LWord.standardSymbols.forEach {
                    DropdownMenuItem(
                        text = {
                            Row {
                                Text(it.symbol.toString() + "\t\t\t")
                                Text(it.desc)
                            }
                        },
                        onClick = {
                            val parameters = if(it.params == 0) "" else {
                                StringBuilder().apply {
                                    append("(")
                                    repeat(it.params - 1) { append(" ,") }
                                    append(" )")
                                }.toString()
                            }
                            val s = " " + it.symbol + parameters + " "
                            editingString.replaceRange(rangeOfCursorIndexOrWord(), s)
                            editingCursorPos += if(it.params == 0) 2 else 3
                            formUnvalidated = true
                        }
                    )
                }
            }
        }
    }
    @Composable
    fun <T> ReorderDeleteButtons( //for rules, symbols, or params lists
        items : SnapshotStateList<T>,
        errs : SnapshotStateList<ValidationResult.Failure?>,
        i : Int,
        itemDescription : String) {
        IconButton(enabled = i > 0, onClick = {
            items.add(i - 1, items.removeAt(i))
            errs.add(i - 1, errs.removeAt(i))
            formUnvalidated = true
        }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move $itemDescription Up")
        }
        IconButton(enabled = i != items.lastIndex, onClick = {
            items.add(i + 1, items.removeAt(i))
            errs.add(i + 1, errs.removeAt(i))
            formUnvalidated = true
        }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move $itemDescription Down")
        }
        IconButton(onClick = {
            productionRules.removeAt(i)
            errorsProductions.removeAt(i)
            formUnvalidated = true
        }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete $itemDescription")
        }
    }
    @Composable
    fun <T : Item> GroupLabeledSection(section : Section,
                                       c : KClass<T>,
                                       error : ValidationResult.Failure?,
                                       items : SnapshotStateList<T>,
                                       itemName : String,
                                       itemErrors : SnapshotStateList<ValidationResult.Failure?>,
                                       itemContent: @Composable (Int, T) -> Unit) {
        var myReorderDeleteButtonsVisible = remember { false }
        if(items.isNotEmpty()) LabeledSection(section.heading, error) {
            items.forEachIndexed { iItem, item ->
                fun editingMe() = editingSection == section && editingRow == iItem
                CanShowErrorBelow(error = itemErrors[iItem]) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .consumeClickEventsWhen { editingMe() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //CONTENT
                        itemContent(iItem, item)
                        if (myReorderDeleteButtonsVisible) {
                            Spacer(Modifier.weight(.01f))
                            Row(Modifier.align(Alignment.CenterVertically)) {
                                ReorderDeleteButtons(items, itemErrors, iItem,itemName)
                            }
                        }
                    }
                }
                if (editingMe()) EditTray()
            }
        }
        //Buttons controlling add, reorder/delete rules
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)) {
            Button(onClick = {
                items.add(
                    when(c) {
                        Parameter::class -> {
                            val X = 1f
                            Parameter("", "", TrueConstant(X), X) //todo weird
                        }
                        Production::class -> Production(" ", " ")
                        Symbol::class -> Symbol("A", "Apex")
                        else -> throw IllegalStateException("Type?")
                    } as T
                )
                errorsProductions.add(null)
                formUnvalidated = true
            }) {
                Text(LocalContext.current.getString(R.string.rules_btn_add, itemName))
            }
            Button(
                onClick = { myReorderDeleteButtonsVisible = !myReorderDeleteButtonsVisible }
            ) {
                Text(LocalContext.current.getString(R.string.rules_btn_alter))
            }
        }
    }
    //BEGIN Content of RulesScreen Composable
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    // Check for clicks outside (by design = unconsumed) to dismiss controls
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.none { it.isConsumed }) {
                            tryRow()
                            tryNewSpecification()
                            editingCursorPos = NOT_EDITING
                            editingRow = NOT_EDITING
                            editingString = ""
                        }
                    }
                }
            }
    ) {
        LabeledSection(LocalContext.current.getString(R.string.rules_label_axiom), errorAxiom) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .consumeClickEventsWhen { editingSection == AXIOM }) {
                AccursedText(axiom, AXIOM, 0)
            }
            if (editingSection == AXIOM) EditTray()
        }
        GroupLabeledSection(
            section = RULES,
            c = Production::class,
            error = errorOverall, //showing system errors here in the middle... for now
            items = productionRules,
            itemName = LocalContext.current.getString(R.string.rules_item_rule),
            itemErrors = errorsProductions
        ) {
            iRule, pr ->
            AccursedText(pr.before, RULES, iRule)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow")
            AccursedText(pr.after, RULES, iRule, Modifier
                .weight(1f)
                .width(IntrinsicSize.Max))
        }
        GroupLabeledSection(
            section = SYMBOLS,
            c = Symbol::class,
            error = errorSymbols,
            items = symbols,
            itemName = LocalContext.current.getString(R.string.rules_item_symbol),
            itemErrors = errorsSymbolIndividual
        ) {
            iSym, s ->
            //todo
        }
        GroupLabeledSection(
            section = PARAMS,
            c = Parameter::class,
            error = errorParams,
            items = newParams,
            itemName = LocalContext.current.getString(R.string.rules_item_param),
            itemErrors = errorsParamIndividual
        ) {
            iPara, p ->
            //todo
        }
    }
    val context = LocalContext.current
    LaunchedEffect(leavingScreen){
        if(leavingScreen == ArboretumScreen.Rules){
            tryNewSpecification()
            if(formUnvalidated && errorOverall != null) Toast.makeText(context,
                context.getString(R.string.rules_msg_abandon_invalid),
                Toast.LENGTH_LONG).show()
        }
    }
}
