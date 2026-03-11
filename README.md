# RestaurantOS

A comprehensive, scalable, and production-ready restaurant management platform built with Spring Boot 4.0, Kafka, PostgreSQL, and advanced monitoring capabilities.

RestaurantOS handles complex order workflows, payments, inventory, kitchen preparation, and real-time delivery tracking with high concurrency support.

### Core Features
- **Order Management**: Create, track, and manage customer orders
- **Payment Processing**: Secure payment gateway integration with multiple payment methods
- **Inventory Management**: Real-time inventory tracking, stock reservations, and low-stock alerts
- **Preparation Tracking**: Monitor kitchen preparation status
- **Delivery Management**: Assign drivers, track delivery locations in real-time
- **Driver Notifications**: Kafka-based event notifications to drivers

### Advanced Features
- **High Concurrency Support**: Handles 10,000+ concurrent connections with optimized connection pooling
- **Event-Driven Architecture**: Kafka-based messaging system for asynchronous processing
- **Real-time Monitoring**: Prometheus metrics and Grafana dashboards
- **Health Checks**: Liveness and readiness probes
- **Database Indexing**: Optimized queries with strategic database indexing
- **Transaction Management**: ACID compliance with proper transaction handling

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 4.0.2 |
| Database | PostgreSQL | 15.0 (Free, Open Source) |
| Message Queue | Apache Kafka | 7.5.0 |
| Monitoring | Prometheus + Grafana | Latest |
| JDK | Java | 17 |
| Build Tool | Gradle | 8.0+ |

## Quick Start

### Prerequisites
- Java 17 or higher
- Docker & Docker Compose
- Git

### Installation & Setup

1. **Clone or navigate to the project**
   ```bash
   cd /Users/krishna/Downloads/demo-security
   ```

2. **Start infrastructure with Docker Compose**
   ```bash
   docker-compose up -d
   ```

   This will start:
   - PostgreSQL (localhost:5432)
   - Apache Kafka (localhost:9092)
   - Zookeeper (localhost:2181)
   - Prometheus (localhost:9090)
   - Grafana (localhost:3000)
   - Kafka UI (localhost:8080)

3. **Verify Docker containers are running**
   ```bash
   docker-compose ps
   ```

4. **Build the application**
   ```bash
   ./gradlew build
   ```

5. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

   Application will start on `http://localhost:8081`

## API Endpoints

### Order Management

#### Create Order
```http
POST /api/v1/orders
Content-Type: application/json

{
  "customerId": 1,
  "deliveryAddress": "123 Main St",
  "deliveryType": "DELIVERY",
  "items": [
    {
      "menuItemId": 1,
      "quantity": 2,
      "specialInstructions": "No onions"
    }
  ]
}
```

#### Get Order
```http
GET /api/v1/orders/{orderId}
```

#### Get Customer Orders
```http
GET /api/v1/orders/customer/{customerId}?page=0&size=10
```

#### Update Order Status
```http
PATCH /api/v1/orders/{orderId}/status?status=PREPARING
```

#### Assign Driver
```http
PATCH /api/v1/orders/{orderId}/assign-driver?driverId=5
```

### Payment Management

#### Process Payment
```http
POST /api/v1/payments
Content-Type: application/json

{
  "orderId": 1,
  "amount": 50.00,
  "paymentMethod": "CREDIT_CARD"
}
```

#### Get Payment
```http
GET /api/v1/payments/{paymentId}
```

#### Get Payment by Order
```http
GET /api/v1/payments/order/{orderId}
```

#### Refund Payment
```http
POST /api/v1/payments/{paymentId}/refund
```

### Delivery Management

#### Assign Delivery
```http
POST /api/v1/deliveries/{orderId}/assign
```

#### Pickup Order
```http
PATCH /api/v1/deliveries/{deliveryId}/pickup
```

#### Update Delivery Location
```http
PATCH /api/v1/deliveries/{deliveryId}/location?latitude=40.7128&longitude=-74.0060
```

#### Complete Delivery
```http
PATCH /api/v1/deliveries/{deliveryId}/complete
```

#### Get Driver Deliveries
```http
GET /api/v1/deliveries/driver/{driverId}
```

### Health & Monitoring

#### Health Status
```http
GET /api/v1/health
```

#### Readiness Probe
```http
GET /api/v1/health/readiness
```

#### Liveness Probe
```http
GET /api/v1/health/liveness
```

#### Prometheus Metrics
```http
GET /actuator/prometheus
```

#### Spring Boot Actuator Health
```http
GET /actuator/health
```

## Monitoring & Observability

### Prometheus
- **URL**: http://localhost:9090
- **Query Examples**:
  - `orders_created_total` - Total orders created
  - `payments_processed_total` - Total payments processed
  - `deliveries_completed_total` - Total deliveries completed
  - `order_processing_time` - Order processing time distribution

### Grafana
- **URL**: http://localhost:3000
- **Default Credentials**: admin/admin
- **Data Source**: Prometheus (http://prometheus:9090)

### Kafka UI
- **URL**: http://localhost:8080
- View Kafka topics, partitions, and messages in real-time

## Database Schema

### Tables
- `users` - Store users (customers, drivers, kitchen staff, managers)
- `orders` - Customer orders
- `order_items` - Individual items in an order
- `menu_items` - Restaurant menu items
- `inventory` - Stock management
- `payments` - Payment transactions
- `preparations` - Kitchen preparation tracking
- `deliveries` - Delivery tracking

### Indexes
- `idx_order_status` - Fast order status queries
- `idx_order_customer` - Customer order history
- `idx_inventory_quantity` - Low stock detection
- `idx_menu_category` - Category-based menu filtering
- `idx_delivery_driver` - Driver workload tracking

## Performance & Scalability

### Connection Pool Configuration
- **Max Pool Size**: 20 connections
- **Min Idle**: 5 connections
- **Connection Timeout**: 30 seconds
- **Idle Timeout**: 10 minutes
- **Max Lifetime**: 30 minutes

### Server Configuration
- **Max Threads**: 200
- **Min Spare Threads**: 10
- **Max Connections**: 10,000
- **Accept Count**: 100

### Kafka Configuration
- **Producer Batch Size**: 16KB
- **Linger Time**: 10ms
- **Compression**: Snappy
- **Acks**: All replicas
- **Retries**: 3 attempts

## Testing

### Run All Tests
```bash
./gradlew test
```

### Run Unit Tests
```bash
./gradlew test --tests "*Test"
```

### Run Integration Tests
```bash
./gradlew test --tests "*IntegrationTest"
```

### Test Coverage
- Unit Tests: OrderServiceTest, PaymentServiceTest, InventoryServiceTest
- Controller Tests: OrderControllerTest
- Integration Tests: OrderIntegrationTest, PaymentIntegrationTest, DeliveryIntegrationTest
- Kafka Tests: KafkaEventProducerTest

## Event-Driven Architecture

### Kafka Topics
1. **order.created** - Triggered when a new order is placed
2. **payment.processed** - Triggered after payment success
3. **preparation.started** - Triggered when kitchen starts preparation
4. **preparation.completed** - Triggered when order is ready
5. **delivery.started** - Triggered when driver starts delivery
6. **delivery.completed** - Triggered when order is delivered

### Event Flow
```
Customer Places Order
    ↓
order.created event published
    ↓
Payment Service listens & processes payment
    ↓
payment.processed event published
    ↓
Kitchen Service initiates preparation
    ↓
preparation.completed event published
    ↓
Delivery Service assigns driver
    ↓
delivery.completed event published
    ↓
Order marked as delivered
```

## Configuration

### application.properties
Key configurations:
- PostgreSQL connection details
- Kafka bootstrap servers
- Actuator endpoints exposure
- Server thread pool settings
- Connection pool parameters

## Troubleshooting

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker-compose ps

# View PostgreSQL logs
docker-compose logs postgres

# Connect to database
psql -h localhost -U postgres -d restaurant_db
```

### Kafka Issues
```bash
# Check Kafka is running
docker-compose logs kafka

# View Kafka topics
docker exec restaurant_kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# Check consumer groups
docker exec restaurant_kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

### Application Logs
```bash
# View application logs
tail -f build/logs/application.log

# Check specific errors
grep ERROR build/logs/application.log
```

## Development Guidelines

### Code Structure
```
src/main/java/com/example/restaurant/
├── controller/          # REST API endpoints
├── service/             # Business logic
├── repository/          # Data access layer
├── entity/              # JPA entities
├── dto/                 # Data transfer objects
├── event/               # Kafka events
├── kafka/               # Kafka producers/consumers
├── config/              # Spring configurations
└── monitoring/          # Metrics and monitoring
```

### Best Practices
- Use transactions for data consistency
- Implement proper error handling
- Log all important operations
- Use DTOs for API contracts
- Test both unit and integration tests
- Follow REST API conventions
- Implement proper pagination
- Use database indexes for queries

## Production Deployment

### Recommendations
1. Use managed PostgreSQL service (AWS RDS, Azure Database)
2. Use managed Kafka service (AWS MSK, Confluent Cloud)
3. Enable SSL/TLS for all connections
4. Set up proper authentication and authorization
5. Configure health checks and auto-scaling
6. Set up log aggregation (ELK, CloudWatch)
7. Implement API rate limiting
8. Use environment-based configurations
9. Set up backup and disaster recovery
10. Monitor system metrics continuously

### Environment Variables
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/restaurant_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=secure_password
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092,kafka-2:9092,kafka-3:9092
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
```

## Contributing

1. Create a feature branch
2. Write unit and integration tests
3. Follow code style guidelines
4. Commit with clear messages
5. Submit pull request

## License

This project is open source and available under the MIT License.

## Support

For issues, questions, or suggestions, please create an issue in the repository.

## Roadmap

- [ ] GraphQL API support
- [ ] Real-time WebSocket notifications
- [ ] Multi-language support
- [ ] Advanced analytics dashboard
- [ ] Machine learning for demand forecasting
- [ ] Mobile app integration
- [ ] AI-powered chatbot support
- [ ] Blockchain integration for transparency

## Credits

Built with modern Java technologies and best practices for production-grade applications.

