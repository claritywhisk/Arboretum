package asterhaven.vega.arboretum.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import asterhaven.vega.arboretum.BuildConfig
import asterhaven.vega.arboretum.R
import asterhaven.vega.arboretum.lsystems.SymbolSet
import asterhaven.vega.arboretum.lsystems.Constant
import asterhaven.vega.arboretum.lsystems.Custom
import asterhaven.vega.arboretum.lsystems.CustomSymbol
import asterhaven.vega.arboretum.lsystems.IntParameterType
import asterhaven.vega.arboretum.lsystems.Integer
import asterhaven.vega.arboretum.lsystems.IntermediateSymbol
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.lsystems.LParameter
import asterhaven.vega.arboretum.lsystems.LProduction
import asterhaven.vega.arboretum.lsystems.LSymbol
import asterhaven.vega.arboretum.lsystems.MenuPT
import asterhaven.vega.arboretum.lsystems.ParameterType
import asterhaven.vega.arboretum.lsystems.SpecificationRegexAndValidation
import asterhaven.vega.arboretum.ui.ArboretumScreen
import asterhaven.vega.arboretum.ui.components.ArbBasicTextField
import asterhaven.vega.arboretum.ui.components.ArbDropMenu
import asterhaven.vega.arboretum.ui.components.CanShowErrorBelow
import asterhaven.vega.arboretum.ui.components.LabeledSection
import asterhaven.vega.arboretum.ui.components.ParamTextField
import asterhaven.vega.arboretum.ui.components.arbClickableTextStyle
import asterhaven.vega.arboretum.ui.components.arbPlainTextStyle
import dev.nesk.akkurate.ValidationResult
import dev.nesk.akkurate.Validator
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

enum class Section {
    NONE, AXIOM, RULES, SYMBOLS, PARAMS
}

sealed interface RItem
private fun textField(s : String) : MutableState<TextFieldValue> =
    mutableStateOf(TextFieldValue(s))
private class MutableAxiom(initial : String) : RItem {
    val initial : MutableState<TextFieldValue> = textField(initial)
}
private class MutableProduction(before : String, after : String) : RItem {
    val before = textField(before)
    val after = textField(after)
    fun make() : LProduction = LProduction(before.value.text, after.value.text)
}
private interface NamesCommonSymbol : RItem {
    val symbol : MutableState<TextFieldValue>
}
private class MutableSymbol(sym : String, np : Int, desc : String, aliasFor : String = NOT_ALIAS_SYMBOL) : RItem, NamesCommonSymbol {
    override val symbol = textField(sym)
    val nParams = mutableIntStateOf(np)
    val desc = mutableStateOf(desc) //todo consistent textField()?
    val aliases = textField(aliasFor)
    companion object {
        const val NOT_ALIAS_SYMBOL = ""
    }
    fun make() : LSymbol =
        if(aliases.value.text == NOT_ALIAS_SYMBOL) IntermediateSymbol(symbol.value.text, nParams.intValue, desc.value)
        else CustomSymbol(symbol.value.text, nParams.intValue, desc.value, aliases.value.text)
}
private class MutableParam(sym: String, name: String, type: ParameterType, initialValue: Float) : RItem, NamesCommonSymbol {
    override val symbol = textField(sym)
    val name = mutableStateOf(name)
    val type = mutableStateOf(type)
    val initialValue = mutableFloatStateOf(initialValue)
    fun make() : LParameter = LParameter(symbol.value.text, name.value, type.value, initialValue.floatValue)
}
//not sure we need everything in this or just the parameter names
val commonSymbols : MutableList<String> = mutableListOf<String>().apply {
    SymbolSet.standard.list.forEach { add(it.symbol) }
}
private class SnapshotStateListWrapper<T : NamesCommonSymbol>(private val delegate: SnapshotStateList<T>)
    : MutableList<T> by delegate {
    override fun add(element: T): Boolean {
        commonSymbols.add(element.symbol.value.text)
        return delegate.add(element)
    }
    override fun remove(element: T): Boolean {
        commonSymbols.remove(element.symbol.value.text)
        return delegate.remove(element)
    }
}
private fun extractArgs(s : String) : List<String> {
    val ret = mutableListOf<String>()
    var l = s.indices.firstOrNull { s[it] == '(' } ?: return ret
    var r = l + 1
    while(s[r] != ')'){
        if(s[r] == ',') {
            ret.add(s.substring(l + 1, r))
            l = r
        }
        r++
    }
    return ret
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

    val axiom = remember { MutableAxiom(baseSpecification.initial) }
    val productionRules = remember { mutableStateListOf<MutableProduction>().apply {
        baseSpecification.productions.forEach { add(MutableProduction(it.before, it.after)) }
    } }
    val symbols = remember { SnapshotStateListWrapper(mutableStateListOf<MutableSymbol>()).apply {
        baseSpecification.nonbasicSymbols.list.forEach {
            add(MutableSymbol(it.symbol, it.nParams, it.desc,
                if(it is CustomSymbol) it.aliases else MutableSymbol.NOT_ALIAS_SYMBOL))
        }
    } }
    val newParams = remember { SnapshotStateListWrapper(mutableStateListOf<MutableParam>()).apply {
        baseSpecification.params.forEach {
            add(MutableParam(it.symbol, it.name, it.type, it.initialValue))
        }
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

    var sectionBeingEdited by remember { mutableStateOf(Section.NONE) }
    val noRow = -1
    var rowBeingEdited by remember { mutableIntStateOf(noRow) }

    val idNotEditing = remember { textField("") }
    val idBeingEdited : MutableState<MutableState<TextFieldValue>> = remember { mutableStateOf(idNotEditing) }
    fun setIDBeingEdited(element : MutableState<TextFieldValue>) {
        idBeingEdited.value = element
    }
    fun updateText(textFieldValue: TextFieldValue) {
        idBeingEdited.value.value = textFieldValue
    }
    fun currentTextFieldValue() = idBeingEdited.value.value

    fun isDetailView() = sectionBeingEdited != Section.NONE

    fun symbolsWithArgsInRow() : List<String> = commonSymbols + when (sectionBeingEdited) {
        Section.RULES   -> extractArgs(productionRules[rowBeingEdited].before.value.text)
        Section.SYMBOLS -> extractArgs(symbols[rowBeingEdited].symbol.value.text)
        else -> listOf()
    }

    fun tryNewSpecification() : Boolean {
        val newSpecification = Specification(
            name ="todo",
            initial = axiom.initial.value.text,
            productions = productionRules.toList().map { it.make() } ,
            params = newParams.toList().map { it.make() },
            nonbasicSymbols = SymbolSet().apply {
                symbols.forEach {
                    this.add(it.make())
                }
            },
            constants = HashMap<String, Float>().apply {
                newParams.map { it.make() }.forEach { p -> this[p.symbol] = p.initialValue }
            }
        )
        var success = false
        when(val result = SpecificationRegexAndValidation.validateSpecification(newSpecification)){
            is ValidationResult.Success -> {
                errorOverall = null
                updateSpecification(newSpecification)
                success = true
            }
            is ValidationResult.Failure -> errorOverall = result
        }
        formUnvalidated = false
        return success
    }
    fun tryRow() {
        fun <T> valRes(item : T, vf : Validator.Runner<T>) = when(val result =
            vf(item)) {
            is ValidationResult.Success -> null
            is ValidationResult.Failure -> result
        }
        val r = rowBeingEdited
        when(sectionBeingEdited){
            Section.AXIOM -> errorAxiom = valRes(axiom.initial.value.text, SpecificationRegexAndValidation.validateAxiom)
            Section.RULES -> errorsProductions[r] = valRes(productionRules[r].make(), SpecificationRegexAndValidation.validateProduction)
            Section.SYMBOLS -> errorsSymbolIndividual[r] = valRes(symbols[r].make(), SpecificationRegexAndValidation.validateSymbol)
            Section.PARAMS -> errorsParamIndividual[r] = valRes(newParams[r].make(), SpecificationRegexAndValidation.validateParameter)
            else -> {}
        }
    }

    @Composable
    fun EccentricTextField(msTextField: MutableState<TextFieldValue>,
                           modifier: Modifier = Modifier,
                           rightAlign : Boolean = false) {
        val focusRequester = remember { FocusRequester() } //todo any use?
        val interactionSource = remember { MutableInteractionSource() }
        val keyboardController = LocalSoftwareKeyboardController.current
        val sStyle = SpanStyle(background = MaterialTheme.colorScheme.tertiary)
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        val mod = modifier
            .padding(8.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) setIDBeingEdited(msTextField)
            }
            .focusRequester(focusRequester)
        var showCursor by remember { mutableStateOf(true) }
        if(idBeingEdited.value == msTextField) LaunchedEffect(msTextField.value) {
            showCursor = true
            while (true) {
                delay(500)
                showCursor = !showCursor
            }
        }
        val textStyle = (if(isDetailView()) arbClickableTextStyle() else arbPlainTextStyle()).let {
            if(rightAlign) it.copy(textAlign = TextAlign.End) else it
        }
        BasicTextField(
            value = msTextField.value,
            onValueChange = { newValue : TextFieldValue ->
                if(!newValue.selection.collapsed) {
                    //todo don't disregard?
                }
                else {
                    val ci = newValue.selection.min.coerceAtMost(newValue.text.indices.last)
                    val preI = (ci - 1).coerceAtLeast(0)
                    val nextI = (ci + 1).coerceAtMost(newValue.text.indices.last)
                    if(nextI - preI == 2 && //landed in middle of 2-character symbol?
                        newValue.text.substring(preI, nextI).intern() in symbolsWithArgsInRow())
                            msTextField.value = TextFieldValue(newValue.text, TextRange(preI, nextI))
                    else msTextField.value = TextFieldValue(newValue.text, TextRange(ci, ci))
                }
            },
            modifier = mod,
            enabled = isDetailView(),
            readOnly = true,
            textStyle = textStyle,
            singleLine = false,
            visualTransformation = { annoStr : AnnotatedString ->
                TransformedText(buildAnnotatedString {
                    //Deliver the text and cursor/highlight
                    append(annoStr)
                    val t = annoStr.text
                    if(idBeingEdited.value === msTextField) {
                        val c = msTextField.value.selection.min
                        if (c in t.indices) {
                            if (t[c] == '(') {
                                addStyle(sStyle, c - 1, c + 1)
                                val iRightParen = (c..t.lastIndex).first { t[it] == ')' }
                                addStyle(sStyle, iRightParen, iRightParen + 1)
                            } else addStyle(sStyle, c, msTextField.value.selection.max)
                        }
                    }
                }, OffsetMapping.Identity)
            },
            interactionSource = interactionSource,
            keyboardActions = KeyboardActions(
                onAny = {
                    keyboardController?.hide()
                }
            ),
            onTextLayout = { layoutResult ->
                textLayoutResult = layoutResult
            },
            decorationBox = { innerTextField -> //Cursor
                Box {
                    innerTextField()
                    val cl = msTextField.value.selection.start
                    if (showCursor && idBeingEdited.value === msTextField &&
                        cl == msTextField.value.selection.end) {
                        textLayoutResult?.let { layoutResult ->
                            val cursorOffset = layoutResult.getCursorRect(cl)
                            val cursorHeight = layoutResult.getLineForOffset(cl).let {
                                layoutResult.getLineBottom(it) - layoutResult.getLineTop(it)
                            }
                            @Composable
                            fun Float.toDp(): Dp = (this / LocalDensity.current.density).dp
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = cursorOffset.left.toDp(),
                                        y = cursorOffset.top.toDp()
                                    )
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                                    .width(1.dp)
                                    .height(cursorHeight.toDp())
                            )
                        }
                    }
                }
            }
        )
    }
    @Composable
    fun EditTray() {
        //todo depending on section and specific field
        val tfv = currentTextFieldValue()
        val s = tfv.text
        val cl = tfv.selection.min //cursor
        val cr = tfv.selection.max
        fun updateCursor(l : Int, r : Int) = updateText(TextFieldValue(s, TextRange(l, r)))
        fun cursedRangeInclusive() : IntRange = cl..
                if(s[cr - 1] != '(') cr - 1
                else (cr until s.length).first { s[it] == ')' }
        Row {
            IconButton(enabled = cr > 0, onClick = {
                if(cr > cl) updateCursor(cl, cl)
                else when {
                    s[cl] == '(' || s[cl - 1] == '(' -> updateCursor(cl - 1, cl - 1) //special: rest on (
                    s[cl - 1] in listOf(',',')') -> updateCursor(cl - 1, cr - 1)
                    else -> { //check for 2-character symbol
                        if(cl - 2 >= 0 && s.substring(cl - 2, cl).intern() in symbolsWithArgsInRow())
                            updateCursor(cl - 2, cr)
                        else updateCursor(cl - 1, cr)
                    }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Move cursor left")
            }
            IconButton(enabled = cl < s.lastIndex + 1, onClick = {
                if(cl < cr) updateCursor(cr, cr)
                else when {
                    s.lastIndex >= cr + 1 && s[cr + 1] == '(' -> updateCursor(cr + 1, cr + 1) //special: rest on (
                    s[cr] in listOf('(' , ',' , ')') -> updateCursor(cl + 1, cr + 1)
                    else -> { //check for 2-character symbol
                        if(cr + 2 <= s.length && s.substring(cl, cr + 2).intern() in symbolsWithArgsInRow())
                            updateCursor(cl, cr + 2)
                        else updateCursor(cl, cr + 1)
                    }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Move cursor right")
            }
            //delete button in edit tray
            val onParensSymbol = currentTextFieldValue().selection.let { sel ->
                currentTextFieldValue().text.let { tex ->
                    sel.max <= tex.lastIndex && tex[sel.max] == '('
                }
            }
            IconButton(enabled = !currentTextFieldValue().selection.collapsed || onParensSymbol, onClick = {
                updateText(TextFieldValue(
                    s.removeRange(cursedRangeInclusive()),
                    TextRange(cl, cl)
                ))
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
                val errForCurrentRow = when(sectionBeingEdited){
                    Section.AXIOM  -> errorAxiom
                    Section.RULES  -> errorsProductions[rowBeingEdited]
                    Section.SYMBOLS-> errorsSymbolIndividual[rowBeingEdited]
                    Section.PARAMS -> errorsParamIndividual[rowBeingEdited]
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
            LazyColumn {
                fun isInParens() : Boolean {
                    var i = cl - 1
                    while (i in s.indices) when(s[i]) {
                        '(' -> return true
                        ')' -> return false
                        else -> i--
                    }
                    return false
                }
                fun menuItem(sym : String, desc : String, onClick : () -> Unit) = item {
                    Row(
                        Modifier.clickable { onClick() }
                    ) {
                        Text(sym)
                        Spacer(Modifier.width(12.dp))
                        Text(desc)
                    }
                }
                fun insertTextAndCursorRight(str : String, right : Int = str.length) {
                    updateText(TextFieldValue(
                        s.replaceRange(cursedRangeInclusive(), str),
                        (cl + right).let { TextRange(it, it) }
                    ))
                    formUnvalidated = true
                }
                if(isInParens()) newParams.forEach {
                    menuItem(it.symbol.value.text, it.name.value) {
                        insertTextAndCursorRight(it.symbol.value.text)
                    }
                }
                else {
                    SymbolSet.standard.list.forEach {
                        menuItem(it.symbol, it.desc) {
                            val parameters = if (it.nParams == 0) "" else {
                                StringBuilder().apply {
                                    append("(")
                                    repeat(it.nParams - 1) { append(" ,") }
                                    append(" )")
                                }.toString()
                            }
                            val right = it.symbol.length + if(it.nParams == 0) 0 else 1 //enter (
                            insertTextAndCursorRight(it.symbol + parameters, right)
                        }
                    }
                }
            }
        }
    }
    @Composable
    fun <T> ReorderDeleteButtons( //for rules, symbols, or params lists
        items : MutableList<T>,
        errs : SnapshotStateList<ValidationResult.Failure?>,
        i : Int,
        itemDescription : String) {
        IconButton(enabled = i > 0, onClick = {
            items[i] = items[i - 1].also { items[i - 1] = items[i] }
            errs[i]  =  errs[i - 1].also {  errs[i - 1] =  errs[i] }
            formUnvalidated = true
        }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move $itemDescription Up")
        }
        IconButton(enabled = i != items.lastIndex, onClick = {
            items[i] = items[i + 1].also { items[i + 1] = items[i] }
            errs[i]  =  errs[i + 1].also {  errs[i + 1] =  errs[i] }
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
    fun ClickTakingRow(takesClicks : Boolean, section : Section, row : Int, content : @Composable RowScope.() -> Unit) {
        val mod = Modifier.fillMaxWidth()
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = if(!takesClicks) mod else mod.pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        sectionBeingEdited = section
                        rowBeingEdited = row
                    }
                )
            },
            content = content
        )
    }
    @Composable
    fun ItemWithErrorBar(section : Section, row : Int, content : @Composable RowScope.() -> Unit) {
        CanShowErrorBelow(when(section){
            Section.AXIOM -> errorAxiom
            Section.RULES -> errorsProductions[row]
            Section.SYMBOLS -> errorsSymbolIndividual[row]
            Section.PARAMS -> errorsParamIndividual[row]
            else -> errorOverall
        }) {
            ClickTakingRow(!isDetailView(), section, row, content)
        }
    }
    @Composable
    fun <T : RItem> GroupLabeledSection(section : Section,
                                        c : KClass<T>,
                                        error : ValidationResult.Failure?,
                                        items : MutableList<T>,
                                        itemName : String,
                                        itemErrors : SnapshotStateList<ValidationResult.Failure?>,
                                        itemContent: @Composable (Int, T, @Composable () -> Unit) -> Unit) {
        var myReorderDeleteButtonsVisible by remember { mutableStateOf(false) }
        if(items.isNotEmpty()) LabeledSection(heading[section]!!, error) {
            items.forEachIndexed { iItem, item ->
                key(item) {
                    ItemWithErrorBar(section, iItem) {
                        itemContent(iItem, item) {
                            if (myReorderDeleteButtonsVisible) {
                                Row(Modifier.align(Alignment.CenterVertically)) {
                                    ReorderDeleteButtons(items, itemErrors, iItem, itemName)
                                }
                            }
                        }
                    }
                }
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
                        val pNew = Constant() //default value is a constant, phi
                        newParams.add(MutableParam(" ", " ", pNew, pNew.c))
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
    @Composable
    fun ItemAxiom() {
        EccentricTextField(axiom.initial)
    }
    @Composable
    fun RowScope.ItemRule(pr : MutableProduction) {
        EccentricTextField(pr.before)
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow")
        EccentricTextField(
            pr.after, Modifier
                .weight(1f)
                .width(IntrinsicSize.Max)
        )
    }
    @Composable
    fun ItemSym(s : MutableSymbol, del : @Composable () -> Unit = {}) {
        fun isAliasSymbol() = s.aliases.value.text != MutableSymbol.NOT_ALIAS_SYMBOL
        var isOptionsExpand by remember { mutableStateOf(false) }
        var nParamsString by remember { mutableStateOf("")}
        fun addToParamsString(n : Int, b4 : Int) {
            //add, sparing existing
            val sb = StringBuilder()
            sb.append(if (b4 == 0) "( " else nParamsString.substring(0, nParamsString.lastIndex)) //remove )
            val commas = n - b4 - if(b4 == 0) 1 else 0
            repeat(commas) { sb.append(", ") }
            sb.append(")")
            nParamsString = sb.toString()
        }
        LaunchedEffect(Unit) {
            addToParamsString(s.nParams.intValue, 0)
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                //the pair of symbol and argument count indication
                EccentricTextField(s.symbol, rightAlign = true)
                Text(nParamsString, textAlign = TextAlign.Start)
                if (isAliasSymbol()) {
                    Text("=")
                    EccentricTextField(s.aliases)
                }
                ArbBasicTextField(s.desc.value, onValueChange = { v -> s.desc.value = v },
                    Modifier.weight(.1f), isEnabled = isDetailView()
                )
                del()
            }
            if (isDetailView()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    //Text("Alias")
                    Switch(isAliasSymbol(), { b ->
                        if (!b) s.aliases.value = TextFieldValue(MutableSymbol.NOT_ALIAS_SYMBOL)
                        else if (!isAliasSymbol()) s.aliases.value = TextFieldValue("…")
                    })
                    Spacer(Modifier.width(8.dp))
                    //Text("# Args")
                    (0..3).forEach { n ->
                        RadioButton(selected = s.nParams.intValue == n, onClick = {
                            when (val b4 = s.nParams.intValue) {
                                in 0 until n -> addToParamsString(n, b4)
                                in n..n -> {} //no action
                                else -> { //delete from end
                                    fun ans(): String {
                                        var commasAccepted = n - 1
                                        nParamsString.let {
                                            for (i in it.indices) when (it[i]) {
                                                '(' -> if (n == 0) return ""
                                                ',' -> if (commasAccepted-- == 0) return it.substring(
                                                    0,
                                                    i
                                                ) + ")"
                                            }
                                            if (BuildConfig.DEBUG) check(false) //supposed to be reducing # args
                                            return it
                                        }
                                    }
                                    nParamsString = ans()
                                }
                            }
                            s.nParams.intValue = n
                        })
                    }
                }
            }
        }
    }
    @Composable
    fun ItemParam(p : MutableParam, del : @Composable () -> Unit = {}) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EccentricTextField(p.symbol, Modifier.weight(.5f))
                ArbBasicTextField(p.name.value, { s -> p.name.value = s }, Modifier.weight(1.5f),
                    isEnabled = isDetailView()
                )
                ParamTextField(p.initialValue.floatValue, p.type.value, isDetailView()) { newFloatVal ->
                    p.initialValue.floatValue = newFloatVal
                }
                del()
            }
            if(isDetailView()) Row(verticalAlignment = Alignment.CenterVertically) {
                ArbDropMenu( //todo as standard menu
                    selection = p.type,
                    onSelect = { pt -> p.type.value = pt as ParameterType },
                    name = { pt ->
                         when (pt) { //todo getting mutablestates passed here; check when convert menu
                            is MenuPT -> pt.name
                            is IntParameterType -> stringResource(R.string.rules_param_custom_int)
                            else -> stringResource(R.string.rules_param_custom)
                        }
                    },
                    list = MenuPT.list
                )
                val t = p.type.value
                val isConstant = t.range.start == t.range.endInclusive

                @Composable
                fun RangeTF(initialValue : Float, otherEnd: Float) = ParamTextField(
                    initialValue, t, isDetailView()) { v ->
                    val l = min(v, otherEnd)
                    val r = max(v, otherEnd)
                    p.type.value = when {
                        isConstant -> Constant(v)
                        t is IntParameterType -> Integer(l.toInt(), r.toInt())
                        else -> Custom(l, r)
                    }
                }
                RangeTF(t.range.start, t.range.endInclusive)
                if (!isConstant) {
                    Text(stringResource(R.string.rules_params_range_to))
                    RangeTF(t.range.endInclusive, t.range.start)
                }
            }
        }
    }

    //BEGIN Content of RulesScreen Composable
    if(isDetailView()) {
        Column {
            Row(Modifier.clickable {
                sectionBeingEdited = Section.NONE
                rowBeingEdited = noRow
                setIDBeingEdited(idNotEditing)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                Text("Edit " + when(sectionBeingEdited) {
                    Section.RULES -> stringResource(R.string.rules_item_rule)
                    Section.SYMBOLS -> stringResource(R.string.rules_item_symbol)
                    Section.PARAMS -> stringResource(R.string.rules_item_param)
                    else -> stringResource(R.string.rules_label_axiom)
                })
            }
            ItemWithErrorBar(sectionBeingEdited, rowBeingEdited) {
                val r = rowBeingEdited
                when(sectionBeingEdited) {
                    Section.RULES -> ItemRule(productionRules[r])
                    Section.SYMBOLS -> ItemSym(symbols[r])
                    Section.PARAMS -> ItemParam(newParams[r])
                    else -> ItemAxiom()
                }
            }
            if(idBeingEdited.value !== idNotEditing) EditTray()
        }
    }
    else Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        LabeledSection(stringResource(R.string.rules_label_axiom), errorAxiom) {
            ItemWithErrorBar(Section.AXIOM, 0) {
                ItemAxiom()
            }
        }
        GroupLabeledSection(
            section = Section.RULES,
            c = MutableProduction::class,
            error = errorOverall, //showing system errors here in the middle... for now
            items = productionRules,
            itemName = stringResource(R.string.rules_item_rule),
            itemErrors = errorsProductions
        ) { iRule, pr, del ->
            ItemWithErrorBar(Section.RULES, iRule) {
                ItemRule(pr)
                del()
            }
        }
        GroupLabeledSection(
            section = Section.SYMBOLS,
            c = MutableSymbol::class,
            error = errorSymbols,
            items = symbols,
            itemName = stringResource(R.string.rules_item_symbol),
            itemErrors = errorsSymbolIndividual
        ) { iSym, s, del ->
            ItemWithErrorBar(Section.SYMBOLS, iSym) {
                ItemSym(s, del)
            }
        }
        GroupLabeledSection(
            section = Section.PARAMS,
            c = MutableParam::class,
            error = errorParams,
            items = newParams,
            itemName = stringResource(R.string.rules_item_param),
            itemErrors = errorsParamIndividual
        ) { iPara, p, del ->
            ItemWithErrorBar(Section.PARAMS, iPara) {
                ItemParam(p, del)
            }
        }
    }
    val context = LocalContext.current
    LaunchedEffect(leavingScreen){
        if(leavingScreen == ArboretumScreen.Rules){
            //see tryNewSpecification() logic
            if(formUnvalidated && !tryNewSpecification()) Toast.makeText(context,
                context.getString(R.string.rules_msg_abandon_invalid),
                Toast.LENGTH_LONG).show()
        }
    }
}
