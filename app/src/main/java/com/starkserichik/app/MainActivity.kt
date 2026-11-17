package com.starkserichik.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BackgroundTop = Color(0xFF080E1B)
private val BackgroundBottom = Color(0xFF101A2B)
private val Panel = Color(0xFF131F32)
private val PanelBright = Color(0xFF1B2C43)
private val Gold = Color(0xFFFFC857)
private val GoldDeep = Color(0xFFFF9F43)
private val Cyan = Color(0xFF4DE1F2)
private val Muted = Color(0xFF9EADC2)
private val White = Color(0xFFF4F7FC)
private val Green = Color(0xFF71E6B2)
private const val PREFS = "stark_serichik_v06"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StarkSerichikApp() }
    }
}

data class Accessory(val name: String, val kind: String, val price: Int, val icon: String, val detail: String)
data class Promo(val code: String, val coins: Int)

private val accessoryCatalog = listOf(
    Accessory("Реактор на груди", "Энергия", 0, "🔆", "Базовый источник энергии брони"),
    Accessory("Красный визор", "Шлем", 150, "🕶️", "Алый интерфейс прицеливания"),
    Accessory("Золотые наплечники", "Броня", 250, "🛡️", "Усиленные плечевые пластины"),
    Accessory("Мини-дрон помощник", "Спутник", 400, "🤖", "Компактный дрон-компаньон"),
    Accessory("Энерго-крылья", "Спина", 600, "🪽", "Декоративные энергетические крылья"),
    Accessory("Голографический плащ", "Стиль", 350, "✨", "Светящийся голографический шлейф"),
    Accessory("Aegis Grid", "Защита", 0, "🔷", "Защитная решётка из фиксированного кейса")
)

private val defaultPromos = listOf(Promo("STARK100", 100), Promo("IRON2026", 250))
private val suitNames = listOf("Mark I", "Mark II", "Mark III", "Mark IV", "Mark V")
private val tabs = listOf("Главная", "Игры", "Серийчик", "Броня", "Аксессуары", "Апгрейд", "Кейсы", "Промокоды", "Чат", "Поиск")

@Composable
fun StarkSerichikApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var tab by remember { mutableStateOf("Главная") }
    var coins by remember { mutableIntStateOf(prefs.getInt("coins", 1200)) }
    var xp by remember { mutableIntStateOf(prefs.getInt("xp", 150)) }
    var suitLevel by remember { mutableIntStateOf(prefs.getInt("suit_level", 1).coerceIn(1, 5)) }
    var streak by remember { mutableIntStateOf(prefs.getInt("streak", 1).coerceAtLeast(1)) }
    var moduleLevel by remember { mutableIntStateOf(prefs.getInt("module_level", 1).coerceIn(1, 5)) }
    var selectedAccessory by remember { mutableStateOf(prefs.getString("selected_accessory", "Реактор на груди") ?: "Реактор на груди") }
    var feedCharge by remember { mutableIntStateOf(0) }
    var feedCount by remember { mutableIntStateOf(0) }
    val owned = remember {
        mutableStateListOf<String>().apply {
            addAll(prefs.getStringSet("owned_accessories", setOf("Реактор на груди")) ?: setOf("Реактор на груди"))
            if (isEmpty()) add("Реактор на груди")
        }
    }
    val usedPromos = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("used_promos", emptySet()) ?: emptySet()) } }
    val customPromos = remember {
        mutableStateListOf<Promo>().apply {
            val raw = prefs.getString("custom_promos", "") ?: ""
            raw.split("\n").filter { it.contains(":") }.forEach { line ->
                val parts = line.split(":", limit = 2)
                val reward = parts.getOrNull(1)?.toIntOrNull()
                if (parts[0].isNotBlank() && reward != null && reward in 1..5000) add(Promo(parts[0], reward))
            }
        }
    }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val claimedGames = remember {
        mutableStateListOf<String>().apply {
            if (prefs.getString("games_day", "") == today) {
                addAll(prefs.getStringSet("claimed_games", emptySet()) ?: emptySet())
            }
        }
    }
    var promoInput by remember { mutableStateOf("") }
    var newPromoCode by remember { mutableStateOf("") }
    var newPromoReward by remember { mutableStateOf("100") }
    var searchQuery by remember { mutableStateOf("") }
    var chatDraft by remember { mutableStateOf("") }
    val localMessages = remember { mutableStateListOf("Система: добро пожаловать в Stark Serichik!") }
    var status by remember { mutableStateOf("Система готова. Выбирай следующую миссию.") }
    val allPromos = defaultPromos + customPromos
    val nextUpgradeCost = listOf(0, 1000, 1800, 2800, 4000).getOrElse(suitLevel) { 0 }
    val moduleUpgradeCost = 500 * moduleLevel

    LaunchedEffect(coins, xp, suitLevel, streak, moduleLevel, selectedAccessory, owned.toList(), usedPromos.toList(), customPromos.toList()) {
        prefs.edit()
            .putInt("coins", coins)
            .putInt("xp", xp)
            .putInt("suit_level", suitLevel)
            .putInt("streak", streak)
            .putInt("module_level", moduleLevel)
            .putString("selected_accessory", selectedAccessory)
            .putStringSet("owned_accessories", owned.toSet())
            .putStringSet("used_promos", usedPromos.toSet())
            .putString("custom_promos", customPromos.joinToString("\n") { "${it.code}:${it.coins}" })
            .apply()
    }
    LaunchedEffect(claimedGames.toList()) {
        prefs.edit().putString("games_day", today).putStringSet("claimed_games", claimedGames.toSet()).apply()
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, secondary = Cyan, background = BackgroundTop, surface = Panel,
        onBackground = White, onSurface = White, onSurfaceVariant = Muted,
        outline = Color(0xFF40516A)
    )) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            AppHeader(coins = coins, xp = xp, streak = streak)
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { item ->
                    FilterChip(
                        selected = tab == item,
                        onClick = { tab = item },
                        label = { Text(item, fontWeight = if (tab == item) FontWeight.Bold else FontWeight.Medium) },
                        shape = RoundedCornerShape(13.dp),
                        colors = FilterChipDefaults.filterChipColors()
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    "Главная" -> {
                        Text("ТВОЯ ЛАБОРАТОРИЯ", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                        Text("Собери броню.\nУстанови рекорд.", color = White, fontSize = 29.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black)
                        PremiumCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("АКТИВНЫЙ КОСТЮМ", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                    Text(suitNames[suitLevel - 1], color = Gold, fontSize = 25.sp, fontWeight = FontWeight.Black)
                                    Text("${owned.size} модулей в коллекции", color = Muted, fontSize = 12.sp)
                                }
                                StatusPill("READY", Green)
                            }
                            ArmorIllustration(Modifier.fillMaxWidth().height(252.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("СЕЙЧАС НАДЕТО", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(selectedAccessory, color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text("LVL $suitLevel / 5", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        PremiumCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("РЕАКТОР", color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                                    Text("Зажми кнопку, чтобы зарядить ядро", color = Muted, fontSize = 12.sp)
                                }
                                Text("⚡ ${feedCharge}%", color = Cyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            LinearProgressIndicator(
                                progress = feedCharge / 100f,
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = Cyan,
                                trackColor = Color(0xFF293C53)
                            )
                            Box(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFFB86D28), Gold, Color(0xFFFFE7A3))))
                                    .pointerInput(Unit) {
                                        detectTapGestures(onLongPress = {
                                            feedCount += 1
                                            feedCharge = (feedCharge + 10).coerceAtMost(100)
                                            status = if (feedCharge == 100) "Реактор полностью заряжен! Это локальная тренировка." else "Импульс принят: заряд +10%. Кормление работает локально."
                                        })
                                    }
                                    .padding(vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⟡  ЗАЖМИ, ЧТОБЫ ЗАРЯДИТЬ  ⟡", color = Color(0xFF192235), fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                            Text("Импульсов: $feedCount • синхронизация со вторым игроком появится после подключения сервера", color = Muted, fontSize = 10.sp)
                        }
                        PremiumCard {
                            Text("ТВОЙ ПРОГРЕСС", color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MetricBlock("⚡", "$xp XP", "Опыт")
                                MetricBlock("🔥", "$streak", "Серия дней")
                                MetricBlock("🧩", "${owned.size}", "Модули")
                            }
                            GoldButton("Открыть апгрейд", Modifier.fillMaxWidth()) { tab = "Апгрейд" }
                        }
                    }
                    "Игры" -> {
                        ScreenTitle("АРЕНА МИССИЙ", "Выполняй тренировки и забирай фиксированные награды. Каждую миссию можно получить один раз в день.")
                        GameCard("🔩", "Сбор деталей", "Собери комплект для следующего костюма.", 10, 10, "СОБРАТЬ ДЕТАЛИ", "parts" in claimedGames) {
                            claimGame("parts", 10, 10, claimedGames, { coins += it }, { xp += it }) { status = it }
                        }
                        GameCard("🃏", "Карточная головоломка 21", "Логическая тренировка на очки, без ставок.", 25, 20, "РЕШИТЬ ЗАДАЧУ", "cards" in claimedGames) {
                            claimGame("cards", 25, 20, claimedGames, { coins += it }, { xp += it }) { status = it }
                        }
                        GameCard("🚁", "Тренировка дронов", "Удерживай курс и отрабатывай управление.", 10, 15, "НАЧАТЬ ТРЕНИРОВКУ", "drones" in claimedGames) {
                            claimGame("drones", 10, 15, claimedGames, { coins += it }, { xp += it }) { status = it }
                        }
                    }
                    "Серийчик" -> {
                        ScreenTitle("СЕРИЙЧИК", "Общий ритуал двух друзей. Здесь пока доступна локальная демонстрация механики.")
                        PremiumCard {
                            Text("🔥 ТВОЯ ТЕКУЩАЯ СЕРИЯ", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("$streak ДНЕЙ", color = White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                            Text("Для реальной общей серии нужны аккаунты, сервер и подтверждение действий обоих игроков.", color = Muted, fontSize = 12.sp)
                            Divider(color = Color(0xFF34465E))
                            Text("Локальная зарядка реактора", color = Cyan, fontWeight = FontWeight.Bold)
                            Text("Зажми и удерживай кнопку — герой получит энергетический импульс.", color = Muted, fontSize = 12.sp)
                            GoldButton("ЗАЖАТЬ И ЗАРЯДИТЬ", Modifier.fillMaxWidth(), onLongPress = {
                                feedCount += 1
                                feedCharge = (feedCharge + 10).coerceAtMost(100)
                                status = "Импульс отправлен локальному герою. Онлайн-партнёр пока не подключён."
                            }) { status = "Заряд: $feedCharge%. Для кормления зажми большую кнопку." }
                            LinearProgressIndicator(progress = feedCharge / 100f, modifier = Modifier.fillMaxWidth().height(8.dp), color = Cyan, trackColor = PanelBright)
                        }
                    }
                    "Броня" -> {
                        ScreenTitle("АРСЕНАЛ STARK", "Пять поколений брони. Открывай следующую модель за Stark Coins.")
                        suitNames.forEachIndexed { index, name ->
                            val unlocked = index < suitLevel
                            PremiumCard {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(15.dp)).background(if (unlocked) Color(0xFF34435A) else Color(0xFF1A2738)), contentAlignment = Alignment.Center) {
                                        Text(if (unlocked) "🦾" else "🔒", fontSize = 26.sp)
                                    }
                                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                        Text(name, color = if (unlocked) Gold else Muted, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                        Text(if (index == suitLevel - 1) "Экипирован" else if (unlocked) "Открыт" else "Заблокирован", color = if (index == suitLevel - 1) Green else Muted, fontSize = 11.sp)
                                    }
                                    if (index == suitLevel - 1) StatusPill("ACTIVE", Green)
                                }
                            }
                        }
                        GoldButton("Перейти к улучшению", Modifier.fillMaxWidth()) { tab = "Апгрейд" }
                    }
                    "Аксессуары" -> {
                        ScreenTitle("МОДУЛИ И ОБВЕС", "Собирай собственную конфигурацию и меняй внешний вид брони.")
                        accessoryCatalog.forEach { item ->
                            PremiumCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(PanelBright, Color(0xFF263C55)))), contentAlignment = Alignment.Center) {
                                        Text(item.icon, fontSize = 29.sp)
                                    }
                                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                        Text(item.name, color = White, fontWeight = FontWeight.Bold)
                                        Text(item.detail, color = Muted, fontSize = 11.sp)
                                        Text(if (item.price == 0) "БЕСПЛАТНО • ${item.kind}" else "${item.price} SC • ${item.kind}", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                when {
                                    selectedAccessory == item.name -> StatusPill("НАДЕТО", Green)
                                    item.name in owned -> GhostButton("Надеть модуль", Modifier.fillMaxWidth()) { selectedAccessory = item.name; status = "Экипировано: ${item.name}" }
                                    else -> GoldButton("Купить за ${item.price} SC", Modifier.fillMaxWidth()) {
                                        if (coins >= item.price) {
                                            coins -= item.price; owned.add(item.name); selectedAccessory = item.name
                                            status = "Куплено и экипировано: ${item.name}"
                                        } else status = "Не хватает ${item.price - coins} SC."
                                    }
                                }
                            }
                        }
                    }
                    "Апгрейд" -> {
                        ScreenTitle("ЛАБОРАТОРИЯ УЛУЧШЕНИЙ", "Усиливай броню и модули. Стоимость и результат известны заранее.")
                        PremiumCard {
                            Text("ARMOR EVOLUTION", color = Cyan, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            ArmorIllustration(Modifier.fillMaxWidth().height(205.dp))
                            Text(suitNames[suitLevel - 1], color = Gold, fontSize = 30.sp, fontWeight = FontWeight.Black)
                            Text("Уровень ${suitLevel} из 5 • $xp XP опыта", color = Muted)
                            LinearProgressIndicator(progress = suitLevel / 5f, modifier = Modifier.fillMaxWidth().height(8.dp), color = Gold, trackColor = PanelBright)
                            if (suitLevel < 5) {
                                Text("СЛЕДУЮЩИЙ УРОВЕНЬ: ${suitNames[suitLevel]}", color = Cyan, fontWeight = FontWeight.Bold)
                                Text("Цена улучшения: $nextUpgradeCost SC", color = White)
                                if (suitLevel == 1) Text("Mark I → Mark II стоит 1000 SC", color = Muted, fontSize = 11.sp)
                                GoldButton("УЛУЧШИТЬ БРОНЮ  •  $nextUpgradeCost SC", Modifier.fillMaxWidth()) {
                                    if (coins >= nextUpgradeCost) {
                                        coins -= nextUpgradeCost; suitLevel += 1; xp += 100
                                        status = "Броня улучшена до ${suitNames[suitLevel - 1]}! +100 XP."
                                    } else status = "Не хватает ${nextUpgradeCost - coins} SC."
                                }
                            } else StatusPill("МАКСИМАЛЬНЫЙ УРОВЕНЬ", Green)
                        }
                        PremiumCard {
                            Text("МОДУЛЬНЫЙ СТАБИЛИЗАТОР", color = Gold, fontWeight = FontWeight.ExtraBold)
                            Text("Уровень модуля $moduleLevel / 5", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(progress = moduleLevel / 5f, modifier = Modifier.fillMaxWidth().height(7.dp), color = Cyan, trackColor = PanelBright)
                            Text("Стабилизация даёт +50 XP и повышает уровень модуля. Купленный предмет не теряется.", color = Muted, fontSize = 12.sp)
                            if (moduleLevel < 5) GoldButton("СТАБИЛИЗИРОВАТЬ  •  $moduleUpgradeCost SC", Modifier.fillMaxWidth()) {
                                if (coins >= moduleUpgradeCost) { coins -= moduleUpgradeCost; moduleLevel += 1; xp += 50; status = "Модуль усилен до уровня $moduleLevel. +50 XP." }
                                else status = "Не хватает ${moduleUpgradeCost - coins} SC."
                            } else StatusPill("МОДУЛЬ УСИЛЕН", Green)
                        }
                    }
                    "Кейсы" -> {
                        ScreenTitle("ГАРАЖ КЕЙСОВ", "В этом кейсе всегда один и тот же заранее известный набор — никаких случайных платных выпадений.")
                        PremiumCard {
                            Box(Modifier.fillMaxWidth().height(175.dp).clip(RoundedCornerShape(18.dp)).background(Brush.radialGradient(listOf(Color(0xFF355A79), Color(0xFF18283D), Color(0xFF111C2C)))), contentAlignment = Alignment.Center) {
                                Text("🔷", fontSize = 84.sp)
                            }
                            Text("AEGIS GRID CASE", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Text("Фиксированный набор", color = Cyan, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("🔷 Aegis Grid", color = White)
                                Text("+50 XP", color = Cyan, fontWeight = FontWeight.Bold)
                            }
                            Text("Стоимость: 250 SC", color = Muted)
                            GoldButton(if ("Aegis Grid" in owned) "НАБОР УЖЕ ПОЛУЧЕН" else "ЗАБРАТЬ НАБОР  •  250 SC", Modifier.fillMaxWidth(), enabled = "Aegis Grid" !in owned) {
                                when {
                                    "Aegis Grid" in owned -> status = "Aegis Grid уже есть в коллекции."
                                    coins < 250 -> status = "Не хватает 250 SC для набора."
                                    else -> { coins -= 250; owned.add("Aegis Grid"); xp += 50; selectedAccessory = "Aegis Grid"; status = "Набор получен: Aegis Grid и +50 XP!" }
                                }
                            }
                        }
                    }
                    "Промокоды" -> {
                        ScreenTitle("ЦЕНТР АКТИВАЦИИ", "Вводи коды или создавай тестовые локальные промокоды для этой копии игры.")
                        PremiumCard {
                            Text("АКТИВИРОВАТЬ КОД", color = Gold, fontWeight = FontWeight.ExtraBold)
                            DarkTextField(value = promoInput, onValueChange = { promoInput = it.uppercase(Locale.ROOT).take(24) }, label = "Введи промокод", capitalization = true)
                            GoldButton("АКТИВИРОВАТЬ", Modifier.fillMaxWidth()) {
                                val code = promoInput.trim().uppercase(Locale.ROOT)
                                val promo = allPromos.firstOrNull { it.code == code }
                                when {
                                    code.isBlank() -> status = "Введи промокод."
                                    code in usedPromos -> status = "Этот код уже использован на устройстве."
                                    promo == null -> status = "Промокод не найден."
                                    else -> { coins += promo.coins; usedPromos.add(code); promoInput = ""; status = "Активирован $code: +${promo.coins} SC!" }
                                }
                            }
                        }
                        PremiumCard {
                            Text("СОЗДАТЬ СВОЙ КОД", color = Gold, fontWeight = FontWeight.ExtraBold)
                            DarkTextField(value = newPromoCode, onValueChange = { newPromoCode = it.uppercase(Locale.ROOT).filter { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '-' }.take(24) }, label = "Код (4–24 символа)")
                            DarkTextField(value = newPromoReward, onValueChange = { newPromoReward = it.filter(Char::isDigit).take(4) }, label = "Награда SC (1–5000)")
                            GoldButton("СОЗДАТЬ ПРОМОКОД", Modifier.fillMaxWidth()) {
                                val code = newPromoCode.trim()
                                val reward = newPromoReward.toIntOrNull() ?: 0
                                when {
                                    code.length !in 4..24 -> status = "Код должен содержать от 4 до 24 символов."
                                    reward !in 1..5000 -> status = "Награда должна быть от 1 до 5000 SC."
                                    allPromos.any { it.code == code } -> status = "Такой код уже существует."
                                    else -> { customPromos.add(Promo(code, reward)); newPromoCode = ""; newPromoReward = "100"; status = "Создан код $code на $reward SC." }
                                }
                            }
                            Divider(color = Color(0xFF34465E))
                            Text("ДОСТУПНЫЕ КОДЫ НА ЭТОМ УСТРОЙСТВЕ", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            allPromos.forEach { promo ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(promo.code, color = White, fontWeight = FontWeight.SemiBold)
                                    Text("+${promo.coins} SC", color = Gold, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("Локальные промокоды не распространяются на другие устройства. Для общих кодов нужен сервер.", color = Muted, fontSize = 10.sp)
                        }
                    }
                    "Чат" -> {
                        ScreenTitle("КОМАНДНЫЙ КАНАЛ", "Демонстрационный чат. Сейчас сообщения видны только в этой запущенной копии.")
                        PremiumCard {
                            localMessages.forEach { message ->
                                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PanelBright).padding(12.dp)) {
                                    Text(message, color = White, fontSize = 13.sp)
                                }
                            }
                            DarkTextField(value = chatDraft, onValueChange = { chatDraft = it.take(250) }, label = "Напиши сообщение")
                            GoldButton("ОТПРАВИТЬ", Modifier.fillMaxWidth()) {
                                val message = chatDraft.trim()
                                if (message.isNotEmpty()) { localMessages.add("Ты: $message"); chatDraft = "" }
                            }
                        }
                    }
                    "Поиск" -> {
                        ScreenTitle("ПОИСК ПО АРСЕНАЛУ", "Найди модуль по названию или категории.")
                        PremiumCard {
                            DarkTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = "Название или категория")
                            val results = accessoryCatalog.filter { it.name.contains(searchQuery, ignoreCase = true) || it.kind.contains(searchQuery, ignoreCase = true) }
                            Text("НАЙДЕНО: ${results.size}", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            results.forEach { item ->
                                Row(Modifier.fillMaxWidth().clickable { tab = "Аксессуары" }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.icon, fontSize = 25.sp)
                                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                        Text(item.name, color = White, fontWeight = FontWeight.Bold)
                                        Text(item.kind, color = Muted, fontSize = 11.sp)
                                    }
                                    Text("ОТКРЫТЬ  ›", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = Color(0xFF2C3D54))
                            }
                            if (results.isEmpty()) Text("По этому запросу ничего не найдено.", color = Muted)
                        }
                    }
                }
                StatusPanel(status)
                Text("STARK SERICHIK  •  v0.7 VISUAL UPDATE", color = Color(0xFF62738A), fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp), textAlign = TextAlign.Center)
            }
        }
    }
}

private fun claimGame(
    key: String, rewardCoins: Int, rewardXp: Int, claimed: MutableList<String>,
    setCoins: (Int) -> Unit, setXp: (Int) -> Unit, setStatus: (String) -> Unit
) {
    if (key in claimed) {
        setStatus("Эта награда уже получена сегодня. Новые миссии появятся завтра.")
    } else {
        claimed.add(key)
        // Add the fixed reward to the saved balances.
        setCoins(rewardCoins)
        setXp(rewardXp)
        setStatus("Миссия завершена: +$rewardCoins SC и +$rewardXp XP!")
    }
}

@Composable
private fun AppHeader(coins: Int, xp: Int, streak: Int) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(Brush.linearGradient(listOf(Gold, GoldDeep))), contentAlignment = Alignment.Center) {
                    Text("S", color = Color(0xFF152136), fontWeight = FontWeight.Black, fontSize = 19.sp)
                }
                Spacer(Modifier.width(9.dp))
                Text("STARK", color = White, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(" SERICHIK", color = Gold, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Text("ARMOR LAB  /  FRIEND MODE", color = Muted, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("🪙 $coins SC", color = Gold, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text("⚡ $xp XP     🔥 $streak", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ArmorIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w * .5f
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x7745DDF2), Color(0x222B77A0), Color.Transparent), center = Offset(cx, h * .48f), radius = w * .48f),
            radius = w * .48f,
            center = Offset(cx, h * .48f)
        )
        // Floating HUD rings
        drawCircle(Color(0x554DE1F2), radius = w * .34f, center = Offset(cx, h * .50f), style = Stroke(width = 1.4f))
        drawCircle(Color(0x44FFC857), radius = w * .40f, center = Offset(cx, h * .50f), style = Stroke(width = 1f, pathEffect = null))
        // Shoulder armor
        drawRoundRect(Brush.linearGradient(listOf(Color(0xFF8798A9), Color(0xFF26374C), Color(0xFF121F31)), Offset(w * .12f, h * .45f), Offset(w * .88f, h * .75f)), Offset(w * .14f, h * .47f), Size(w * .72f, h * .27f), CornerRadius(w * .09f, w * .09f))
        // Torso silhouette
        val torso = Path().apply {
            moveTo(w * .32f, h * .48f)
            lineTo(w * .68f, h * .48f)
            lineTo(w * .76f, h * .92f)
            quadraticTo(cx, h * 1.02f, w * .24f, h * .92f)
            close()
        }
        drawPath(torso, Brush.linearGradient(listOf(Color(0xFF9EABB7), Color(0xFF45576B), Color(0xFF17283E)), Offset(w * .2f, h * .5f), Offset(w * .8f, h)))
        drawPath(torso, Color(0xFFB9C9D8), style = Stroke(width = 2.2f))
        // Helmet
        drawRoundRect(Brush.linearGradient(listOf(Color(0xFFFFD776), Color(0xFFC87937), Color(0xFF713D2B)), Offset(w * .33f, h * .12f), Offset(w * .67f, h * .46f)), Offset(w * .34f, h * .10f), Size(w * .32f, h * .36f), CornerRadius(w * .09f, w * .09f))
        drawRoundRect(Color(0xFFFFDFA0), Offset(w * .38f, h * .16f), Size(w * .24f, h * .06f), CornerRadius(w * .03f, w * .03f))
        // Faceplate / eyes
        drawRoundRect(Color(0xFF122B42), Offset(w * .39f, h * .27f), Size(w * .22f, h * .09f), CornerRadius(w * .025f, w * .025f))
        drawRoundRect(Color(0xFF66E9FF), Offset(w * .405f, h * .30f), Size(w * .075f, h * .025f), CornerRadius(w * .012f, w * .012f))
        drawRoundRect(Color(0xFF66E9FF), Offset(w * .52f, h * .30f), Size(w * .075f, h * .025f), CornerRadius(w * .012f, w * .012f))
        // Chest reactor
        drawCircle(Color(0xAA4DE1F2), radius = w * .09f, center = Offset(cx, h * .64f))
        drawCircle(Color(0xFFB7FAFF), radius = w * .052f, center = Offset(cx, h * .64f))
        drawCircle(Color(0xFF3ABDD6), radius = w * .035f, center = Offset(cx, h * .64f))
        drawCircle(Color.White, radius = w * .014f, center = Offset(cx, h * .64f))
        // Armor seam details
        drawLine(Color(0x88FFC857), Offset(w * .29f, h * .56f), Offset(w * .36f, h * .82f), 2f)
        drawLine(Color(0x88FFC857), Offset(w * .71f, h * .56f), Offset(w * .64f, h * .82f), 2f)
        drawLine(Color(0x775DEBFF), Offset(w * .43f, h * .78f), Offset(w * .57f, h * .78f), 2f)
        // HUD markers
        drawCircle(Cyan, radius = 3.5f, center = Offset(w * .19f, h * .26f))
        drawCircle(Gold, radius = 3.5f, center = Offset(w * .81f, h * .34f))
        drawLine(Color(0x7773DFF1), Offset(w * .19f, h * .26f), Offset(w * .30f, h * .31f), 1.2f)
        drawLine(Color(0x77FFC857), Offset(w * .81f, h * .34f), Offset(w * .70f, h * .39f), 1.2f)
    }
}

@Composable
private fun PremiumCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier.fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF1B2B41), Color(0xFF111D2F), Color(0xFF17263A))))
            .border(1.dp, Color(0xFF30455F), shape)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun ScreenTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = Gold, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = .5.sp)
        Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(Modifier.clip(CircleShape).background(color.copy(alpha = .13f)).border(1.dp, color.copy(alpha = .45f), CircleShape).padding(horizontal = 11.dp, vertical = 6.dp)) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .6.sp)
    }
}

@Composable
private fun MetricBlock(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
        Text(icon, fontSize = 20.sp)
        Text(value, color = White, fontSize = 17.sp, fontWeight = FontWeight.Black)
        Text(label, color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun GoldButton(
    text: String, modifier: Modifier = Modifier, enabled: Boolean = true,
    onLongPress: (() -> Unit)? = null, onClick: () -> Unit
) {
    if (onLongPress == null) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(49.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = Color(0xFF172237),
                disabledContainerColor = Color(0xFF34445A),
                disabledContentColor = Muted
            )
        ) { Text(text, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = .3.sp) }
    } else {
        Box(
            modifier.height(49.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(GoldDeep, Gold, Color(0xFFFFE7A3))))
                .pointerInput(enabled) { if (enabled) detectTapGestures(onLongPress = { onLongPress() }, onTap = { onClick() }) },
            contentAlignment = Alignment.Center
        ) { Text(text, color = Color(0xFF172237), fontWeight = FontWeight.Black, fontSize = 12.sp) }
    }
}

@Composable
private fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier.height(46.dp), shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PanelBright, contentColor = Cyan)
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
}

@Composable
private fun DarkTextField(value: String, onValueChange: (String) -> Unit, label: String, capitalization: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Muted, fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(capitalization = if (capitalization) KeyboardCapitalization.Characters else KeyboardCapitalization.None),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = White,
            unfocusedTextColor = White,
            focusedBorderColor = Cyan,
            unfocusedBorderColor = Color(0xFF3A4D65),
            cursorColor = Gold,
            focusedContainerColor = Color(0xFF0D1828),
            unfocusedContainerColor = Color(0xFF0D1828)
        )
    )
}

@Composable
private fun GameCard(
    icon: String, title: String, description: String, coins: Int, xp: Int,
    action: String, completed: Boolean, onPlay: () -> Unit
) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(PanelBright), contentAlignment = Alignment.Center) { Text(icon, fontSize = 27.sp) }
            Column(Modifier.weight(1f).padding(start = 11.dp)) {
                Text(title, color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("🪙 +$coins SC", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("⚡ +$xp XP", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        if (completed) StatusPill("НАГРАДА ПОЛУЧЕНА СЕГОДНЯ", Green)
        else GoldButton(action, Modifier.fillMaxWidth(), onClick = onPlay)
    }
}

@Composable
private fun StatusPanel(status: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF17263A)).border(1.dp, Color(0xFF2B4058), RoundedCornerShape(14.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(Cyan))
        Text(status, color = Muted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(start = 9.dp))
    }
}
