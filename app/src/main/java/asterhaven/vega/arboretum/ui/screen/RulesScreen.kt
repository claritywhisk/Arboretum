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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.lsystems.LWord
import asterhaven.vega.arboretum.lsystems.Specification.Production
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.lsystems.SpecificationRegexAndValidation
import asterhaven.vega.arboretum.ui.ArboretumScreen
import asterhaven.vega.arboretum.ui.components.LabeledSection
import dev.nesk.akkurate.ValidationResult

private const val NOT_EDITING = -1
private const val EDITING_AXIOM = Int.MAX_VALUE
private const val EMPTY_STRING = ""

@Composable
fun RulesScreen(
    baseSpecification : Specification, //the selected spec which this screen shows/modifies
    updateSpecification: (Specification) -> Unit,
    leavingScreen : ArboretumScreen?
) {
    val axiom by remember { mutableStateOf(baseSpecification.initial) }
    val productionRules = remember { mutableStateListOf<Production>().apply {
        baseSpecification.productions.forEach { add(it.copy()) }
    } }
    val newParams = remember { mutableStateListOf<Specification.Parameter>().apply {
        baseSpecification.params.forEach { add(it.copy()) }
    } }

    fun errOK() = mutableStateOf<ValidationResult.Failure?>(null)
    fun errsOK(k : Int) = mutableStateListOf<ValidationResult.Failure?>().apply{
        repeat(k) { add(null) }
    }
    var errorAxiom              by remember { errOK() }
    var errorsProductions       = remember { errsOK(productionRules.size) }
    var errorOverall            by remember { errOK() }
    var errorsParamIndividual   = remember { errsOK(newParams.size) }
    var errorParams             by remember { errOK() }

    fun noEdit() = mutableStateOf(NOT_EDITING)
    var editingCursorPos    by remember { noEdit() }
    var editingRow          by remember { noEdit() }
    var editingParam        by remember { noEdit() }
    var editingString       by remember { mutableStateOf("") }
    var formUnvalidated by remember { mutableStateOf(false) }
    var reorderDeleteButtonsVisible by remember { mutableStateOf(false) }



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
    fun tryRowOrParam() {
        when(val r = editingRow) {
            NOT_EDITING -> {}
            EDITING_AXIOM -> {
                when(val result = SpecificationRegexAndValidation.validateAxiom(axiom)){
                    is ValidationResult.Success -> errorAxiom = null
                    is ValidationResult.Failure -> errorAxiom = result
                }
            }
            else -> {
                val pRule = productionRules[r]
                when(val result = SpecificationRegexAndValidation.validateProduction(pRule)) {
                    is ValidationResult.Success -> errorsProductions[r] = null
                    is ValidationResult.Failure -> errorsProductions[r] = result
                }
            }
        }
        when(val p = editingParam) {
            NOT_EDITING -> {}
            else -> when(val result = SpecificationRegexAndValidation.validateParameter(newParams[p])) {
                is ValidationResult.Success -> errorsParamIndividual[p] = null
                is ValidationResult.Failure -> errorsParamIndividual[p] = result
            }
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
    fun AccursedText(initializingText: String, thisRow : Int, modifier: Modifier = Modifier) {
        var editingThis by remember { mutableStateOf(false) }
        var myText by remember { mutableStateOf(initializingText) }
        LaunchedEffect(editingRow) {
            if(editingRow == NOT_EDITING) editingThis = false
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
                nextI <= text.lastIndex && text[nextI] == '(' -> nextI
                text[clickedI] == ',' -> clickedI - 1
                text[clickedI] == ')' -> clickedI - 1
                else -> clickedI
            }
            editingRow = thisRow
            editingString = initializingText
            editingThis = true
        }
    }
    @Composable
    fun EditTray() {
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
                tryRowOrParam()
                tryNewSpecification()
            }) {
                Text(LocalContext.current.getString(R.string.rules_btn_validate))
            }
            else IconButton(onClick = {}) {
                val errRow = editingRow.let { r ->
                    if (r == EDITING_AXIOM) errorAxiom
                    else errorsProductions[r]
                }
                if (errRow == null)
                    Icon(Icons.Default.Check, contentDescription = "Rules are valid")
                else Icon(Icons.Default.Warning, contentDescription = "Review errors")
            }
        }
        Row {
            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                //todo choose params
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
    fun ReorderDeleteButtons(i : Int){
        IconButton(enabled = i > 0, onClick = {
            productionRules.add(i - 1, productionRules.removeAt(i))
            errorsProductions.add(i - 1, errorsProductions.removeAt(i))
            formUnvalidated = true
        }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Rule Up")
        }
        IconButton(enabled = i != productionRules.lastIndex, onClick = {
            productionRules.add(i + 1, productionRules.removeAt(i))
            errorsProductions.add(i + 1, errorsProductions.removeAt(i))
            formUnvalidated = true
        }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Rule Down")
        }
        IconButton(onClick = {
            productionRules.removeAt(i)
            errorsProductions.removeAt(i)
            formUnvalidated = true
        }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Rule")
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
                            tryRowOrParam()
                            tryNewSpecification()
                            editingCursorPos = NOT_EDITING
                            editingRow = NOT_EDITING
                            editingString = EMPTY_STRING
                        }
                    }
                }
            }
    ) {
        LabeledSection(LocalContext.current.getString(R.string.rules_label_axiom), errorAxiom) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .consumeClickEventsWhen { editingRow == EDITING_AXIOM }) {
                AccursedText(axiom, EDITING_AXIOM)
            }
            if (editingRow == EDITING_AXIOM) EditTray()
        }
        LabeledSection(LocalContext.current.getString(R.string.rules_label_productions), errorOverall) {
            productionRules.forEachIndexed { iRule, pr ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .consumeClickEventsWhen { editingRow == iRule },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccursedText(pr.before, iRule)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow")
                    AccursedText(
                        pr.after, iRule, Modifier
                            .weight(1f)
                            .width(IntrinsicSize.Max)
                    )
                    if (reorderDeleteButtonsVisible) {
                        Spacer(Modifier.weight(.01f))
                        Row(Modifier.align(Alignment.CenterVertically)) {
                            ReorderDeleteButtons(iRule)
                        }
                    }
                }
                if (editingRow == iRule) EditTray()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                        productionRules.add(Production(" ", " "))
                        formUnvalidated = true
                    }
                ) {
                    Text(LocalContext.current.getString(R.string.rules_btn_add))
                }
                Button(
                    onClick = { reorderDeleteButtonsVisible = !reorderDeleteButtonsVisible }
                ) {
                    Text(LocalContext.current.getString(R.string.rules_btn_alter))
                }
            }
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
