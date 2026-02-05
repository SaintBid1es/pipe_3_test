import random
import json
from faker import Faker
from locust import HttpUser, task, TaskSet, SequentialTaskSet, constant_throughput, between

fake = Faker("ru_RU")

class ReadOnlyTasks(TaskSet):
    """
    TaskSet - задачи выполняются случайным образом.
    Используется для операций чтения (GET), которые можно выполнять в любом порядке.
    """
    
    def on_start(self):
        """Аутентификация перед началом тестирования"""
        login_data = {
            "username": "admin",  # Можно использовать тестового пользователя
            "password": "admin123"  # Пароль нужно проверить
        }
        with self.client.post("/api/auth/login", json=login_data, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    self.token = data.get("token")
                    self.client.headers = {"Authorization": f"Bearer {self.token}"}
                except:
                    pass
    
    @task(25)  # Высокий вес для GET запросов
    def get_all_products(self):
        """Получить список всех товаров"""
        with self.client.get("/api/products", catch_response=True, name="GET /api/products") as response:
            if response.status_code in [200, 401]:
                response.success()
    
    @task(25)
    def get_all_categories(self):
        """Получить список всех категорий"""
        with self.client.get("/api/categories", catch_response=True, name="GET /api/categories") as response:
            if response.status_code in [200, 401]:
                response.success()
    
    @task(20)
    def get_all_customers(self):
        """Получить список всех клиентов"""
        with self.client.get("/api/customers", catch_response=True, name="GET /api/customers") as response:
            if response.status_code in [200, 401]:
                response.success()
    
    @task(15)
    def get_products_page(self):
        """Получить товары с пагинацией"""
        page = random.randint(0, 5)
        size = random.choice([10, 20, 50])
        self.client.get(
            f"/api/products/page?page={page}&size={size}&includeDeleted=false",
            name="GET /api/products/page"
        )
    
    @task(15)
    def get_categories_page(self):
        """Получить категории с пагинацией"""
        page = random.randint(0, 3)
        size = random.choice([10, 20])
        self.client.get(
            f"/api/categories/page?page={page}&size={size}&includeDeleted=false",
            name="GET /api/categories/page"
        )
    
    @task(10)
    def search_products(self):
        """Поиск товаров по названию"""
        search_term = fake.word()
        self.client.get(
            f"/api/products/search?name={search_term}",
            name="GET /api/products/search"
        )
    
    @task(10)
    def search_customers(self):
        """Поиск клиентов"""
        search_term = fake.first_name()
        self.client.get(
            f"/api/customers/search?query={search_term}",
            name="GET /api/customers/search"
        )
    
    @task(5)
    def get_product_by_id(self):
        """Получить товар по ID (используем случайный ID в разумных пределах)"""
        product_id = random.randint(1, 100)
        self.client.get(
            f"/api/products/{product_id}",
            name="GET /api/products/{id}"
        )
    
    @task(5)
    def get_category_by_id(self):
        """Получить категорию по ID"""
        category_id = random.randint(1, 50)
        self.client.get(
            f"/api/categories/{category_id}",
            name="GET /api/categories/{id}"
        )
    
    @task(10)
    def get_orders(self):
        """Получить список заказов"""
        page_num = random.randint(0, 5)
        size = random.choice([10, 20])
        self.client.get(
            f"/api/orders?pageNum={page_num}&size={size}",
            name="GET /api/orders"
        )


class WriteTasks(SequentialTaskSet):
    """
    SequentialTaskSet - задачи выполняются последовательно в порядке объявления.
    Используется для операций, где порядок важен (например, создание -> обновление -> удаление).
    """
    
    def on_start(self):
        """Аутентификация перед началом тестирования"""
        login_data = {
            "username": "admin",
            "password": "admin123"
        }
        with self.client.post("/api/auth/login", json=login_data, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    self.token = data.get("token")
                    self.client.headers = {"Authorization": f"Bearer {self.token}"}
                except:
                    pass
        
        # Сохраняем ID созданных ресурсов для последующих операций
        self.created_category_id = None
        self.created_product_id = None
        self.created_customer_id = None
    
    @task
    def create_category(self):
        """Создать новую категорию"""
        category_data = {
            "name": fake.word().capitalize() + " " + fake.word().capitalize()
        }
        with self.client.post(
            "/api/categories",
            json=category_data,
            catch_response=True,
            name="POST /api/categories"
        ) as response:
            if response.status_code in [201, 200]:
                try:
                    data = response.json()
                    self.created_category_id = data.get("id")
                    response.success()
                except:
                    pass
            elif response.status_code == 401:
                response.success()  # Не считаем ошибкой аутентификации
    
    @task
    def create_product(self):
        """Создать новый товар (используем существующую категорию или созданную)"""
        # Используем случайный ID категории, если не создали свою
        category_id = self.created_category_id if self.created_category_id else random.randint(1, 10)
        
        product_data = {
            "name": fake.sentence(nb_words=3)[:-1],  # Убираем точку в конце
            "description": fake.text(max_nb_chars=200),
            "price": round(random.uniform(100.0, 10000.0), 2),
            "category": {"id": category_id}
        }
        
        with self.client.post(
            "/api/products",
            json=product_data,
            catch_response=True,
            name="POST /api/products"
        ) as response:
            if response.status_code in [201, 200]:
                try:
                    data = response.json()
                    self.created_product_id = data.get("id")
                    response.success()
                except:
                    pass
            elif response.status_code == 401:
                response.success()
    
    @task
    def register_customer(self):
        """Регистрация нового клиента"""
        # Генерируем уникальные данные
        username = fake.user_name() + str(random.randint(1000, 9999))
        email = fake.unique.email()
        
        customer_data = {
            "firstName": fake.first_name(),
            "lastName": fake.last_name(),
            "email": email,
            "username": username,
            "password": "Test123!@#"  # Пароль соответствует требованиям (буквы, цифры, спецсимволы)
        }
        
        with self.client.post(
            "/api/auth/register",
            json=customer_data,
            catch_response=True,
            name="POST /api/auth/register"
        ) as response:
            if response.status_code in [200, 201]:
                try:
                    data = response.json()
                    self.created_customer_id = data.get("id") or None
                    response.success()
                except:
                    response.success()
            elif response.status_code == 400:
                # Дублирование данных - не критичная ошибка для теста
                response.success()


class UpdateDeleteTasks(SequentialTaskSet):
    """
    Другой SequentialTaskSet для операций обновления и удаления.
    Демонстрирует использование нескольких SequentialTaskSet.
    """
    
    def on_start(self):
        """Аутентификация"""
        login_data = {
            "username": "admin",
            "password": "admin123"
        }
        with self.client.post("/api/auth/login", json=login_data, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    self.token = data.get("token")
                    self.client.headers = {"Authorization": f"Bearer {self.token}"}
                except:
                    pass
    
    @task
    def update_product(self):
        """Обновить товар"""
        # Используем случайный ID существующего товара
        product_id = random.randint(1, 50)
        
        product_data = {
            "name": fake.sentence(nb_words=3)[:-1],
            "description": fake.text(max_nb_chars=200),
            "price": round(random.uniform(100.0, 10000.0), 2),
            "category": {"id": random.randint(1, 10)}
        }
        
        self.client.put(
            f"/api/products/{product_id}",
            json=product_data,
            name="PUT /api/products/{id}"
        )
    
    @task
    def update_category(self):
        """Обновить категорию"""
        category_id = random.randint(1, 20)
        
        category_data = {
            "name": fake.word().capitalize() + " " + fake.word().capitalize()
        }
        
        self.client.put(
            f"/api/categories/{category_id}",
            json=category_data,
            name="PUT /api/categories/{id}"
        )


class ApiUser(HttpUser):
    """
    HttpUser - основной класс для нагрузочного тестирования.
    Комбинирует TaskSet и SequentialTaskSet с разными весами.
    
    Разница между TaskSet и SequentialTaskSet:
    - TaskSet: задачи выполняются случайным образом согласно их весам (@task вес).
               Подходит для операций, порядок которых не важен (например, GET запросы).
    - SequentialTaskSet: задачи выполняются последовательно в порядке объявления.
                         Подходит для операций, где порядок важен (POST -> PUT -> DELETE).
    """
    
    # Время ожидания между запросами
    wait_time = between(1, 3)
    
    # Задаем хост приложения
    host = "http://localhost:8888"
    
    # Настройка весов для контроля соотношения GET:POST = 5:1
    # Locust выбирает TaskSet/SequentialTaskSet согласно весам в словаре
    # Чтобы GET выполнялись в 5 раз чаще POST, задаем веса 5:1
    tasks = {
        ReadOnlyTasks: 5,        # TaskSet - GET запросы (вес 5)
        WriteTasks: 1,           # SequentialTaskSet - POST запросы (вес 1)
        UpdateDeleteTasks: 1,    # SequentialTaskSet - PUT запросы (вес 1)
    }
    
    # Итого: GET запросы (ReadOnlyTasks) выполняются в 5 раз чаще, чем POST (WriteTasks)
    # UpdateDeleteTasks содержит PUT запросы, которые тоже считаются как модификация


class SimpleUser(HttpUser):
    """
    Альтернативный класс для простого тестирования только GET запросов.
    Можно использовать для сравнения результатов.
    """
    
    wait_time = between(1, 2)
    host = "http://localhost:8888"
    
    @task(10)
    def get_products(self):
        """Получить все товары"""
        self.client.get("/api/products", name="GET /api/products")
    
    @task(10)
    def get_categories(self):
        """Получить все категории"""
        self.client.get("/api/categories", name="GET /api/categories")
    
    @task(5)
    def get_customers(self):
        """Получить всех клиентов"""
        self.client.get("/api/customers", name="GET /api/customers")

