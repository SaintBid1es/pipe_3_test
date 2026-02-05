package com.mpt.practyp.config;

import com.mpt.practyp.repository.CustomerRepository;
import com.mpt.practyp.repository.OrderRepository;
import com.mpt.practyp.repository.ProductRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableScheduling
public class MetricsConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsConfig.class);
    
    private final MeterRegistry meterRegistry;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

   
    private final AtomicLong productsCount = new AtomicLong(0);
    private final AtomicLong customersCount = new AtomicLong(0);
    private final AtomicLong ordersCount = new AtomicLong(0);

    public MetricsConfig(MeterRegistry meterRegistry,
                         ProductRepository productRepository,
                         CustomerRepository customerRepository,
                         OrderRepository orderRepository) {
        this.meterRegistry = meterRegistry;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    public void registerGauges() {
        logger.info("Registering InfluxDB metrics...");
        
        try {
            
            updateMetrics();
            
         
            Gauge.builder("app_db_products_total", productsCount, AtomicLong::get)
                .description("Total number of products in the database")
                .tags(Tags.of("metric_type", "business", "domain", "inventory"))
                .register(meterRegistry);

            Gauge.builder("app_db_customers_total", customersCount, AtomicLong::get)
                .description("Total number of customers in the database")
                .tags(Tags.of("metric_type", "business", "domain", "customers"))
                .register(meterRegistry);

            Gauge.builder("app_db_orders_total", ordersCount, AtomicLong::get)
                .description("Total number of orders in the database")
                .tags(Tags.of("metric_type", "business", "domain", "orders"))
                .register(meterRegistry);

            logger.info("InfluxDB metrics registered successfully!");
            
        } catch (Exception e) {
            logger.error("Failed to register InfluxDB metrics", e);
        }
    }

   
    public void updateMetrics() {
        try {
            productsCount.set(productRepository.count());
            customersCount.set(customerRepository.count());
            ordersCount.set(orderRepository.count());
            
            logger.debug("Metrics updated - Products: {}, Customers: {}, Orders: {}", 
                productsCount.get(), customersCount.get(), ordersCount.get());
        } catch (Exception e) {
            logger.error("Error updating metrics", e);
        }
    }

   
    @Scheduled(fixedRate = 15000)
    public void scheduledMetricsUpdate() {
        updateMetrics();
    }
}