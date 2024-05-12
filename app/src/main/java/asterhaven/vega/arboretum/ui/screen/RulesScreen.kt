package asterhaven.vega.arboretum.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.BuildConfig
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.data.model.SymbolSet
import asterhaven.vega.arboretum.lsystems.Constant
import asterhaven.vega.arboretum.lsystems.CustomSymbol
import asterhaven.vega.arboretum.lsystems.IntParameterType
import asterhaven.vega.arboretum.lsystems.IntermediateSymbol
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.lsystems.LParameter
import asterhaven.vega.arboretum.lsystems.LProduction
import asterhaven.vega.arboretum.lsystems.LSymbol
import asterhaven.vega.arboretum.lsystems.MenuPT
import asterhaven.vega.arboretum.lsystems.MenuPTFloat
import asterhaven.vega.arboretum.lsystems.ParameterType
import asterhaven.vega.arboretum.lsystems.SpecificationRegexAndValidation
import asterhaven.vega.arboretum.ui.ArboretumScreen
import asterhaven.vega.arboretum.ui.components.CanShowErrorBelow
import asterhaven.vega.arboretum.ui.components.LabeledSection
import asterhaven.vega.arboretum.ui.components.ParamTextField
import asterhaven.vega.arboretum.ui.components.UniqueIdGenerator
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.reflect.KClass

enum class Section {
    NONE, AXIOM, RULES, SYMBOLS, PARAMS
}
sealed interface RItem
private class MutableProduction(before : String, after : String) : RItem {
    val before = mutableStateOf(before)
    val after = mutableStateOf(after)
    fun make() : LProduction = LProduction(before.value, after.value)
}
private class MutableSymbol(sym : String, np : Int, desc : String, aliasFor : String = notAliasSymbol) : RItem {
    val symbol = mutableStateOf(sym)
    val nParams = mutableIntStateOf(np)
    val desc = mutableStateOf(desc)
    val aliases = mutableStateOf(aliasFor)
    companion object {
        val notAliasSymbol = ""
    }
    fun make() : LSymbol =
        if(aliases.value == notAliasSymbol) IntermediateSymbol(symbol.value, nParams.intValue, desc.value)
        else CustomSymbol(symbol.value, nParams.intValue, desc.value, aliases.value)
}
private class MutableParam(sym: String, name: String, type: ParameterType, initialValue: Float) : RItem {
    val symbol = mutableStateOf(sym)
    val name = mutableStateOf(name)
    val type = mutableStateOf(type)
    val initialValue = mutableFloatStateOf(initialValue)
    fun make() : LParameter = LParameter(symbol.value, name.value, type.value, initialValue.floatValue)
}

@Composable
fun RulesScreen(
    baseSpecification : Specification, //the selected spec which this screen shows/modifies
    updateSpecification: (Specification) -> Unit,
    leavingScreen : ArboretumScreen?
) {
    val heading = HashMap<Section, String>()
    heading[Section.NONE] = ""
    heading[Section.AXIOM] = stringResource(R.string.rules_label_axiom)
    heading[Section.RULES] = stringResource(R.string.rules_label_productions)
    heading[Section.SYMBOLS] = stringResource(R.string.rules_label_custom_symbols)
    heading[Section.PARAMS] = stringResource(R.string.rules_label_parameters)

    val axiom = remember { mutableStateOf(baseSpecification.initial) }
    val productionRules = remember { mutableStateListOf<MutableProduction>().apply {
        baseSpecification.productions.forEach { add(MutableProduction(it.before, it.after)) }
    } }
    val symbols = remember { mutableStateListOf<MutableSymbol>() }
    val newParams = remember { mutableStateListOf<MutableParam>().apply {
        baseSpecification.params.forEach { add(MutableParam(it.symbol, it.name, it.type, it.initialValue)) }
    } }

    var formUnvalidated by remember { mutableStateOf(false) }

    fun errOK() = mutableStateOf<ValidationResult.Failure?>(null)
    fun errsOK(k : Int) = mutableStateListOf<ValidationResult.Failure?>().apply{
        repeat(k) { add(null) }
    }
    var errorAxiom              by remember { errOK() }
    val errorsProductions       = remember { errsOK(productionRules.size) }
    var errorOverall            by remember { errOK() }
    val errorsSymbolIndividual  = remember { errsOK(symbols.size) }
    var errorSymbols            by remember { errOK() }
    val errorsParamIndividual   = remember { errsOK(newParams.size) }
    var errorParams             by remember { errOK() }

    class Holder(text : MutableState<String>, val section : Section, val row : Int) {
        val text by text
        var cursorPos by mutableIntStateOf(0)
        val updateText = { new : String -> text.value = new }
        val updateCursor : (Int) -> Unit = { new : Int -> cursorPos = new }
    }
    val state : HashMap<Int, Holder> = remember { HashMap() }
    fun nextUIDPairedToHolder(text : MutableState<String>, section : Section, row : Int) : Int
        = UniqueIdGenerator.nextId().also {
            state[it] = Holder(text, section, row)
        }
    val idNotEditing = remember { nextUIDPairedToHolder(mutableStateOf(""), Section.NONE, -1) }
    var elementBeingEdited by remember { mutableIntStateOf(idNotEditing) }
    fun editingState() : Holder = state[elementBeingEdited]!!

    fun tryNewSpecification() {
        val newSpecification = Specification(
            name ="todo",
            initial = axiom.value,
            productions = productionRules.toList().map { it.make() } ,
            params = newParams.toList().map { it.make() },
            constants = HashMap<String, Float>().apply {
                newParams.map { it.make() }.forEach { p -> this[p.symbol] = p.initialValue }
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
        val r = editingState().row
        when(editingState().section){
            Section.AXIOM -> errorAxiom = valRes(axiom.value, SpecificationRegexAndValidation.validateAxiom)
            Section.RULES -> errorsProductions[r] = valRes(productionRules[r].make(), SpecificationRegexAndValidation.validateProduction)
            Section.SYMBOLS -> errorsSymbolIndividual[r] = valRes(symbols[r].make(), SpecificationRegexAndValidation.validateSymbol)
            Section.PARAMS -> errorsParamIndividual[r] = valRes(newParams[r].make(), SpecificationRegexAndValidation.validateParameter)
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

    @Composable
    fun AccursedText(uid : Int, modifier: Modifier) {
        val text = state[uid]!!.text
        ClickableText(
            modifier = modifier.padding(8.dp),
            style = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            text = buildAnnotatedString {
                //Deliver the text and cursor/highlight
                append(text)
                if(elementBeingEdited == uid){
                    val sStyle = SpanStyle(background = MaterialTheme.colorScheme.tertiary)
                    val c = editingState().cursorPos
                    if (text[c] == '(') {
                        addStyle(sStyle, c - 1, c + 1)
                        val iRightParen = (c..text.lastIndex).first { text[it] == ')' }
                        addStyle(sStyle, iRightParen, iRightParen + 1)
                    } else addStyle(sStyle, c, c + 1)
                }
            }
        ) { clickedI ->
            val ci = clickedI.coerceIn(text.indices) //todo explain why needed, delete smth click end
            val nextI = (clickedI + 1).coerceIn(text.indices)
            elementBeingEdited = uid
            editingState().updateCursor(when {
                //rest on the opening paren of each parametric word
                text[nextI] == '(' -> nextI
                text[ci] == ',' -> ci - 1
                text[ci] == ')' -> ci - 1
                else -> ci
            }.coerceIn(text.indices))
        }
    }
    @Composable
    fun AccursedTextWrapper(mainValue : MutableState<String>, section : Section, row : Int, modifier: Modifier = Modifier) {
        val uid = remember { nextUIDPairedToHolder(mainValue, section, row) }
        AccursedText(uid, modifier)
    }
    @Composable
    fun EditTray() {
        //todo depending on section and specific field
        val es = editingState().text
        val ec = editingState().cursorPos
        fun rangeOfCursorIndexOrWord() : IntRange = when (es[ec]) {
            '(' -> ec - 1..(ec..es.lastIndex).first { es[it] == ')' }
            else -> ec..ec
        }
        Row(Modifier.consumeClickEvents()) {
            fun moveCursor(x : Int) {
                val new = (ec + x).coerceIn(es.indices)
                editingState().updateCursor(new)
            }
            fun charFromCursor(offset : Int) : Char {
                val loc = (ec + offset).coerceIn(es.indices)
                return es[loc]
            }
            IconButton(enabled = ec > 0, onClick = {
                moveCursor(when {
                    charFromCursor( 0) == '(' -> -2
                    charFromCursor(-1) == ',' -> -2
                    charFromCursor(-1) == ')' -> -2
                    else -> -1
                })
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Move cursor left")
            }
            IconButton(enabled = ec < es.lastIndex, onClick = {
                moveCursor(when {
                    charFromCursor(2) == '(' -> 2
                    charFromCursor(1) == ',' -> 2
                    charFromCursor(1) == ')' -> 2
                    else -> 1
                })
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Move cursor right")
            }
            IconButton(enabled = es[ec] != ' ', onClick = {
                val range = rangeOfCursorIndexOrWord()
                val l = range.first.let {
                    if(it - 1 in es.indices && es[it - 1] == ' ') it - 1 else it
                }
                val r = range.last.let {
                    if(it + 1 in es.indices && es[it + 1] == ' ') it + 1 else it
                }
                val new = es.replaceRange(l..r, " ")
                editingState().updateCursor(l)
                editingState().updateText(new)
                formUnvalidated = true
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Backspace")
            }
            if(formUnvalidated) TextButton(onClick = {
                tryRow()
                tryNewSpecification()
            }) {
                Text(stringResource(R.string.rules_btn_validate))
            }
            else IconButton(onClick = {}) {
                val errForCurrentRow = when(editingState().section){
                    Section.AXIOM  -> errorAxiom
                    Section.RULES  -> errorsProductions[editingState().row]
                    Section.SYMBOLS-> errorsSymbolIndividual[editingState().row]
                    Section.PARAMS -> errorsParamIndividual[editingState().row]
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
                fun isInParens() : Boolean {
                    var i = ec - 1
                    while (i in es.indices) when (es[i]) {
                        '(' -> return true
                        ')' -> return false
                        else -> i--
                    }
                    return false
                }
                if(!isInParens()) SymbolSet.standard.symbols.forEach {
                    DropdownMenuItem(
                        text = {
                            Row {
                                Text(it.symbol + "\t\t\t")
                                Text(it.desc)
                            }
                        },
                        onClick = {
                            val parameters = if(it.nParams == 0) "" else {
                                StringBuilder().apply {
                                    append("(")
                                    repeat(it.nParams - 1) { append(" ,") }
                                    append(" )")
                                }.toString()
                            }
                            val s = " " + it.symbol + parameters + " "
                            editingState().updateText(es.replaceRange(rangeOfCursorIndexOrWord(), s))
                            editingState().updateCursor(ec + if(it.nParams == 0) 2 else 3)
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
            items.removeAt(i)
            errs.removeAt(i)
            formUnvalidated = true
        }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete $itemDescription")
        }
    }
    @Composable
    fun <T : RItem> GroupLabeledSection(section : Section,
                                        c : KClass<T>,
                                        error : ValidationResult.Failure?,
                                        items : SnapshotStateList<T>,
                                        itemName : String,
                                        itemErrors : SnapshotStateList<ValidationResult.Failure?>,
                                        itemContent: @Composable (Int, T) -> Unit) {
        var myReorderDeleteButtonsVisible by remember { mutableStateOf(false) }
        if(items.isNotEmpty()) LabeledSection(heading[section]!!, error) {
            items.forEachIndexed { iItem, item ->
                CanShowErrorBelow(error = itemErrors[iItem]) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .consumeClickEventsWhen {
                                editingState().let {
                                    it.section == section && it.row == iItem
                                }
                            },
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
                if(editingState().row == iItem && editingState().section == section) EditTray()
            }
        }
        //Buttons controlling add, reorder/delete rules
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)) {
            Button(onClick = {
                when(c) {
                    MutableProduction::class -> {
                        productionRules.add(MutableProduction(" ", " "))
                        errorsProductions.add(null)
                    }
                    MutableSymbol::class -> {
                        symbols.add(MutableSymbol("A", 0,"Apex"))
                        errorsSymbolIndividual.add(null)
                    }
                    else -> {
                        newParams.add(MutableParam("", "", Constant, 1f))
                        errorsParamIndividual.add(null)
                    }
                }
                formUnvalidated = true
            }) {
                Text(stringResource(R.string.rules_btn_add, itemName))
            }
            if(items.size >= 1) Button(
                onClick = { myReorderDeleteButtonsVisible = !myReorderDeleteButtonsVisible }
            ) {
                Text(stringResource(R.string.rules_btn_alter))
            }
        }
    }
    //BEGIN Content of RulesScreen Composable
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    // Check for clicks outside (by design = unconsumed) to dismiss controls
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.none { it.isConsumed }) {
                            tryRow()
                            tryNewSpecification()
                            elementBeingEdited = idNotEditing
                        }
                    }
                }
            }
    ) {
        LabeledSection(stringResource(R.string.rules_label_axiom), errorAxiom) {
            val editingAxiom = editingState().section == Section.AXIOM
            Row(
                Modifier
                    .fillMaxWidth()
                    .consumeClickEventsWhen { editingAxiom }) {
                AccursedTextWrapper(axiom, Section.AXIOM, 0)
            }
            if(editingAxiom) EditTray()
        }
        GroupLabeledSection(
            section = Section.RULES,
            c = MutableProduction::class,
            error = errorOverall, //showing system errors here in the middle... for now
            items = productionRules,
            itemName = stringResource(R.string.rules_item_rule),
            itemErrors = errorsProductions
        ) {
            iRule, pr ->
            AccursedTextWrapper(pr.before, Section.RULES, iRule)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow")
            AccursedTextWrapper(pr.after, Section.RULES, iRule, Modifier
                .weight(1f)
                .width(IntrinsicSize.Max))
        }
        GroupLabeledSection(
            section = Section.SYMBOLS,
            c = MutableSymbol::class,
            error = errorSymbols,
            items = symbols,
            itemName = stringResource(R.string.rules_item_symbol),
            itemErrors = errorsSymbolIndividual
        ) { iSym, s ->
            val isAliasSymbol = s.aliases.value != MutableSymbol.notAliasSymbol
            var isOptionsExpand = remember { mutableStateOf(false) }
            Row() {
                AccursedTextWrapper(s.symbol, Section.SYMBOLS, iSym)
                if(isAliasSymbol) {
                    Text("=")
                    AccursedTextWrapper(s.aliases, Section.SYMBOLS, iSym)
                }
                TextField("", onValueChange = { v -> s.desc.value = v })
                IconButton(enabled = true, onClick = { isOptionsExpand = !isOptionsExpand }){
                    Icon(Icons.Default.MoreVert, contentDescription = "Options for symbol type and arguments")
                }
            }
            if(isOptionsExpand) { Row() {
                Text("Normal/substitution")
                Switch(isAliasSymbol, { b ->
                    if (!b) s.aliases.value = MutableSymbol.notAliasSymbol
                    else if (!isAliasSymbol) s.aliases.value = "…"
                })
                Text("# Arguments")
                (0..3).forEach { n ->
                    RadioButton(selected = s.nParams.intValue == n, onClick = {
                        when(val np = s.nParams.intValue) {
                            in 0 until n -> { //add, sparing existing
                                val sb = StringBuilder()
                                sb.append(s.symbol.value.let {
                                    if(np == 0) "$it(" else it.substring(0, it.lastIndex) //remove )
                                })
                                repeat(n - np - 1) { sb.append(" ,") }
                                sb.append(" )")
                                s.symbol.value = sb.toString()
                            }
                            in n..n -> {} //no action
                            else -> { //cut
                                fun ans() : String {
                                    var comma = n - 1
                                    s.symbol.value.let {
                                        for (i in it.indices) when (it[i]) {
                                            '(' -> if (n == 0) return it.substring(0, i)
                                            ',' -> if(--comma == 0) return it.substring(0, i) + ")"
                                        }
                                        if(BuildConfig.DEBUG) check(false) //supposed to be reducing # args
                                        return it
                                    }
                                }
                                s.symbol.value = ans()
                            }
                        }
                        s.nParams.intValue = n
                    })
                }
            }}
        }
        GroupLabeledSection(
            section = Section.PARAMS,
            c = MutableParam::class,
            error = errorParams,
            items = newParams,
            itemName = stringResource(R.string.rules_item_param),
            itemErrors = errorsParamIndividual
        ) { iPara, p ->
            Row() {
                AccursedTextWrapper(p.symbol, Section.PARAMS, iPara, Modifier.weight(.5f))
                AccursedTextWrapper(p.name, Section.PARAMS, iPara, Modifier.weight(1.5f))
                TextField(
                    p.initialValue.value.let {
                        if (p.type is IntParameterType) it.roundToInt().toString()
                        else "%.2f".format(it)
                    },
                    { newVal -> newVal.toFloatOrNull()?.let { p.initialValue.value = it } },
                    Modifier.weight(1f),
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            var expanded = remember { mutableStateOf(false) }
            Row() {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = p.type.value.let { when {
                            it is MenuPT -> it.name
                            it is IntParameterType -> stringResource(R.string.rules_param_custom_int)
                            else -> stringResource(R.string.rules_param_custom)
                        } },
                        modifier = Modifier.clickable {
                            expanded = !expanded
                        }
                    )
                    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                        MenuPT.list.forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = {
                                p.type.value = it as ParameterType
                                expanded = false
                            })
                        }
                    }
                }
                val t = p.type.value
                val isConstant = t.range.start == t.range.endInclusive
                @Composable
                fun RangeTF(otherEnd : Float) = ParamTextField(t.range.start, t, { v ->
                    val l = min(v, otherEnd)
                    val r = max(v, otherEnd)
                    p.type.value = when {
                        isConstant -> ParameterType(v, v)
                        t is IntParameterType -> IntParameterType(l.toInt(), r.toInt())
                        else -> ParameterType(l, r)
                    }
                })
                RangeTF(t.range.endInclusive)
                if(!isConstant) {
                    Text(stringResource(R.string.rules_params_range_to))
                    RangeTF(t.range.start)
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
