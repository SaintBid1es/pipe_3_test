#!/bin/bash

# Скрипт для локальной проверки команд GitLab CI пайплайна

echo "=========================================="
echo "Локальная проверка GitLab CI команд"
echo "=========================================="
echo ""

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функция для проверки команды
check_command() {
    local name=$1
    local command=$2
    
    echo -e "${YELLOW}Проверка: $name${NC}"
    echo "Команда: $command"
    echo "---"
    
    if eval "$command"; then
        echo -e "${GREEN}✓ $name - УСПЕШНО${NC}"
        echo ""
        return 0
    else
        echo -e "${RED}✗ $name - ОШИБКА${NC}"
        echo ""
        return 1
    fi
}

# Проверка наличия Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Ошибка: Maven не установлен${NC}"
    exit 1
fi

echo "Maven версия:"
mvn --version
echo ""

# Счетчик успешных проверок
SUCCESS=0
FAILED=0

# 1. Проверка сборки проекта
if check_command "Проверка сборки проекта" "mvn clean compile"; then
    ((SUCCESS++))
else
    ((FAILED++))
fi

# 2. Проверка линтера
if check_command "Проверка линтера Checkstyle" "mvn checkstyle:check"; then
    ((SUCCESS++))
else
    echo -e "${YELLOW}Примечание: Checkstyle может найти ошибки стиля кода. Это нормально для первого запуска.${NC}"
    echo ""
    ((FAILED++))
fi

# 3. Проверка тестов
if check_command "Запуск тестов" "mvn test"; then
    ((SUCCESS++))
else
    ((FAILED++))
fi

# 4. Проверка валидации pom.xml
if check_command "Валидация pom.xml" "mvn validate"; then
    ((SUCCESS++))
else
    ((FAILED++))
fi

# 5. Проверка синтаксиса YAML (если установлен Python)
if command -v python3 &> /dev/null; then
    if python3 -c "import yaml" 2>/dev/null; then
        if check_command "Проверка синтаксиса application.yml" "python3 -c \"import yaml; yaml.safe_load(open('src/main/resources/application.yml'))\""; then
            ((SUCCESS++))
        else
            ((FAILED++))
        fi
    else
        echo -e "${YELLOW}Пропущено: Проверка YAML (PyYAML не установлен)${NC}"
        echo "Установите: pip install pyyaml"
        echo ""
    fi
else
    echo -e "${YELLOW}Пропущено: Проверка YAML (Python3 не установлен)${NC}"
    echo ""
fi

# Итоги
echo "=========================================="
echo "Итоги проверки:"
echo -e "${GREEN}Успешно: $SUCCESS${NC}"
echo -e "${RED}Ошибок: $FAILED${NC}"
echo "=========================================="

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}Все проверки пройдены! Пайплайн должен работать корректно.${NC}"
    exit 0
else
    echo -e "${RED}Обнаружены ошибки. Исправьте их перед коммитом.${NC}"
    exit 1
fi
