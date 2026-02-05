# Инструкция по проверке GitLab CI пайплайна

## Способы проверки работы пайплайна

### 1. Проверка синтаксиса YAML файла

#### Через GitLab веб-интерфейс:
1. Откройте проект в GitLab
2. Перейдите в **CI/CD → Editor** или **CI/CD → Pipelines**
3. GitLab автоматически проверит синтаксис при сохранении файла

#### Через GitLab API (если есть доступ):
```bash
curl --header "PRIVATE-TOKEN: <your-token>" \
  "https://gitlab.com/api/v4/projects/<project-id>/ci/lint" \
  --data "content=$(cat .gitlab-ci.yml)"
```

#### Локальная проверка синтаксиса YAML:
```bash
# Установите yamllint (если еще не установлен)
pip install yamllint

# Проверка синтаксиса
yamllint .gitlab-ci.yml
```

### 2. Локальная проверка команд Maven

Перед запуском пайплайна в GitLab, можно проверить команды локально:

#### Проверка сборки:
```bash
mvn clean compile
```

#### Проверка линтера:
```bash
mvn checkstyle:check
```

#### Проверка тестов:
```bash
mvn test
```

#### Проверка валидации конфигурации:
```bash
# Проверка pom.xml
mvn validate

# Проверка синтаксиса YAML (требуется Python и PyYAML)
python3 -c "import yaml; yaml.safe_load(open('src/main/resources/application.yml'))"
```

### 3. Запуск пайплайна через GitLab веб-интерфейс

#### Автоматический запуск:
1. Сделайте коммит и push изменений:
   ```bash
   git add .gitlab-ci.yml pom.xml checkstyle.xml
   git commit -m "Add GitLab CI pipeline"
   git push origin main
   ```

2. Пайплайн запустится автоматически после push

3. Проверьте статус:
   - Перейдите в **CI/CD → Pipelines**
   - Вы увидите список всех пайплайнов
   - Кликните на пайплайн, чтобы увидеть детали

#### Ручной запуск:
1. Перейдите в **CI/CD → Pipelines**
2. Нажмите кнопку **Run pipeline**
3. Выберите ветку и нажмите **Run pipeline**

### 4. Проверка через GitLab Runner (локально)

Если у вас установлен GitLab Runner локально:

```bash
# Проверка синтаксиса
gitlab-runner exec docker build-check

# Запуск конкретной задачи
gitlab-runner exec docker lint-checkstyle

# Запуск всех задач
gitlab-runner exec docker --docker-privileged test-execution
```

**Примечание**: Для локального запуска через `gitlab-runner exec` может потребоваться дополнительная настройка.

### 5. Проверка отдельных задач

#### Проверка задачи сборки:
```bash
# Локально
mvn clean compile

# Или через Docker (имитация CI окружения)
docker run --rm -v "$PWD":/usr/src/mymaven -w /usr/src/mymaven \
  maven:3.9-eclipse-temurin-17 mvn clean compile
```

#### Проверка задачи линтера:
```bash
# Локально
mvn checkstyle:check

# Через Docker
docker run --rm -v "$PWD":/usr/src/mymaven -w /usr/src/mymaven \
  maven:3.9-eclipse-temurin-17 mvn checkstyle:check
```

#### Проверка задачи тестов:
```bash
# Локально
mvn test

# Через Docker
docker run --rm -v "$PWD":/usr/src/mymaven -w /usr/src/mymaven \
  maven:3.9-eclipse-temurin-17 mvn test
```

### 6. Мониторинг выполнения пайплайна

После запуска пайплайна в GitLab:

1. **Просмотр логов**:
   - Откройте пайплайн в GitLab
   - Кликните на задачу (job) для просмотра логов
   - Логи обновляются в реальном времени

2. **Проверка артефактов**:
   - После завершения задач, артефакты будут доступны для скачивания
   - Отчеты Checkstyle: `target/checkstyle-result.xml`
   - Отчеты тестов: `target/surefire-reports/`

3. **Проверка кэша**:
   - Кэш Maven зависимостей будет создан после первого запуска
   - Последующие запуски будут быстрее благодаря кэшу

### 7. Устранение проблем

#### Проблема: Пайплайн не запускается
- **Решение**: Проверьте, что файл `.gitlab-ci.yml` находится в корне репозитория
- Проверьте, что раннер настроен и активен
- Убедитесь, что тег `docker` соответствует тегу вашего раннера

#### Проблема: Задача падает с ошибкой сборки
- **Решение**: Проверьте локально: `mvn clean compile`
- Убедитесь, что все зависимости указаны в `pom.xml`

#### Проблема: Checkstyle находит ошибки
- **Решение**: Просмотрите отчет `target/checkstyle-result.xml`
- Исправьте найденные ошибки в коде
- Или временно отключите строгие правила в `checkstyle.xml`

#### Проблема: Тесты не проходят
- **Решение**: Запустите тесты локально: `mvn test`
- Проверьте логи тестов в `target/surefire-reports/`

#### Проблема: Контейнер не может подключиться к PostgreSQL
- **Решение**: Убедитесь, что сервис `postgres:15-alpine` правильно настроен
- Проверьте переменные окружения для подключения к БД
- Увеличьте время ожидания (`sleep 15`) если приложение запускается медленно

### 8. Быстрая проверка (чеклист)

- [ ] Файл `.gitlab-ci.yml` находится в корне проекта
- [ ] Файл `checkstyle.xml` создан и находится в корне проекта
- [ ] Плагин `maven-checkstyle-plugin` добавлен в `pom.xml`
- [ ] Локально команда `mvn clean compile` выполняется успешно
- [ ] Локально команда `mvn checkstyle:check` выполняется (может найти ошибки - это нормально)
- [ ] Локально команда `mvn test` выполняется успешно
- [ ] Раннер настроен и активен в GitLab
- [ ] Тег `docker` соответствует тегу раннера
- [ ] После push в GitLab пайплайн запускается автоматически

### 9. Пример успешного выполнения

После успешного выполнения вы увидите:

```
✓ build-check (validate stage) - passed
✓ lint-checkstyle (validate stage) - passed  
✓ test-execution (test stage) - passed
✓ config-validation (test stage) - passed
```

Все задачи должны иметь статус **passed** (зеленый значок).

### 10. Проверка URL переадресации

В логах каждой задачи вы увидите:
```
GitLab Job URL: https://your-gitlab-instance.com/group/project/-/jobs/12345
GitLab Pipeline URL: https://your-gitlab-instance.com/group/project/-/pipelines/67890
```

Эти URL должны быть правильными и вести на вашу GitLab инстанцию, а не на ID контейнера.
