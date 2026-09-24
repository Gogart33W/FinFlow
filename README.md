# FinFlow 💰

[![Android CI](https://github.com/Gogart33W/FinFlow/actions/workflows/android-ci.yml/badge.svg)](https://github.com/Gogart33W/FinFlow/actions/workflows/android-ci.yml)

**FinFlow** — це сучасний, приватний та офлайн-перший Android-застосунок для особистого обліку фінансів, відстеження доходів, витрат та мульти-рахунків.

---

## 🌟 Основні можливості

- **Облік доходів та витрат**: швидке додавання транзакцій із прив'язкою до категорій та рахунків.
- **Мульти-рахунки**: підтримка готівки, банківських карток, рахунків та заощаджень із підрахунком поточного балансу в реальному часі.
- **Категорії за замовчуванням**: 13 базових категорій з індивідуальними іконками та кольорами.
- **Фільтрація операцій**: зручне відсіювання транзакцій (Всі / Доходи / Витрати).
- **Приватність та Offline-First**: усі ваші фінансові дані зберігаються виключно локально на вашому пристрої.

---

## 🛠 Стек технологій

- **Мова**: [Kotlin](https://kotlinlang.org/)
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/)
- **Архітектура**: MVVM (ViewModel, StateFlow, Coroutines)
- **База даних**: [Room Database](https://developer.android.com/training/data-storage/room) 2.8.5 (з підтримкою міграцій та FK)
- **Навігація**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Тестування**: JUnit, Kotlinx Coroutines Test, Turbine, MigrationTestHelper

---

## 🚀 Запуск проєкту

1. Клонуйте репозиторій:
   ```bash
   git clone https://github.com/Gogart33W/FinFlow.git
   ```
2. Відкрийте проєкт в **Android Studio** (2024.1+ / Ladybug або новішій).
3. Запустіть юніт-тести:
   ```bash
   ./gradlew testDebugUnitTest
   ```
4. Зберіть debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
