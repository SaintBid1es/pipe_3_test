@echo off
REM Скрипт для локальной проверки команд GitLab CI пайплайна (Windows)

echo ==========================================
echo Локальная проверка GitLab CI команд
echo ==========================================
echo.

REM Проверка наличия Maven
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Ошибка: Maven не установлен или не найден в PATH
    exit /b 1
)

echo Maven версия:
mvn --version
echo.

set SUCCESS=0
set FAILED=0

REM 1. Проверка сборки проекта
echo Проверка: Проверка сборки проекта
echo Команда: mvn clean compile
echo ---
mvn clean compile
if %ERRORLEVEL% EQU 0 (
    echo [OK] Проверка сборки проекта - УСПЕШНО
    set /a SUCCESS+=1
) else (
    echo [ERROR] Проверка сборки проекта - ОШИБКА
    set /a FAILED+=1
)
echo.

REM 2. Проверка линтера
echo Проверка: Проверка линтера Checkstyle
echo Команда: mvn checkstyle:check
echo ---
mvn checkstyle:check
if %ERRORLEVEL% EQU 0 (
    echo [OK] Проверка линтера Checkstyle - УСПЕШНО
    set /a SUCCESS+=1
) else (
    echo [WARNING] Проверка линтера Checkstyle - найдены ошибки стиля
    echo Примечание: Checkstyle может найти ошибки стиля кода. Это нормально для первого запуска.
    set /a FAILED+=1
)
echo.

REM 3. Проверка тестов
echo Проверка: Запуск тестов
echo Команда: mvn test
echo ---
mvn test
if %ERRORLEVEL% EQU 0 (
    echo [OK] Запуск тестов - УСПЕШНО
    set /a SUCCESS+=1
) else (
    echo [ERROR] Запуск тестов - ОШИБКА
    set /a FAILED+=1
)
echo.

REM 4. Проверка валидации pom.xml
echo Проверка: Валидация pom.xml
echo Команда: mvn validate
echo ---
mvn validate
if %ERRORLEVEL% EQU 0 (
    echo [OK] Валидация pom.xml - УСПЕШНО
    set /a SUCCESS+=1
) else (
    echo [ERROR] Валидация pom.xml - ОШИБКА
    set /a FAILED+=1
)
echo.

REM 5. Проверка синтаксиса YAML (если установлен Python)
where python >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    python -c "import yaml" >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo Проверка: Проверка синтаксиса application.yml
        echo Команда: python -c "import yaml; yaml.safe_load(open('src/main/resources/application.yml'))"
        echo ---
        python -c "import yaml; yaml.safe_load(open('src/main/resources/application.yml'))"
        if %ERRORLEVEL% EQU 0 (
            echo [OK] Проверка синтаксиса application.yml - УСПЕШНО
            set /a SUCCESS+=1
        ) else (
            echo [ERROR] Проверка синтаксиса application.yml - ОШИБКА
            set /a FAILED+=1
        )
        echo.
    ) else (
        echo [SKIP] Пропущено: Проверка YAML (PyYAML не установлен)
        echo Установите: pip install pyyaml
        echo.
    )
) else (
    echo [SKIP] Пропущено: Проверка YAML (Python не установлен)
    echo.
)

REM Итоги
echo ==========================================
echo Итоги проверки:
echo Успешно: %SUCCESS%
echo Ошибок: %FAILED%
echo ==========================================

if %FAILED% EQU 0 (
    echo Все проверки пройдены! Пайплайн должен работать корректно.
    exit /b 0
) else (
    echo Обнаружены ошибки. Исправьте их перед коммитом.
    exit /b 1
)
