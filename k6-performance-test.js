import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate, Gauge } from 'k6/metrics';

// Custom metrics
const apiResponseTime = new Trend('api_response_time');
const apiErrors = new Rate('api_errors');
const apiSuccess = new Rate('api_success');
const concurrentUsers = new Gauge('concurrent_users');

export const options = {
  stages: [
    { duration: '30s', target: 10 },    // Ramp-up to 10 users
    { duration: '1m30s', target: 50 },  // Ramp-up to 50 users
    { duration: '20s', target: 0 },     // Ramp-down to 0 users
  ],
  thresholds: {
    'api_response_time': ['p(95)<500', 'p(99)<1000'],
    'api_errors': ['rate<0.1'],
    'api_success': ['rate>0.95'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const TOKEN = __ENV.TOKEN || '';

export default function () {
  concurrentUsers.add(__VU);

  group('Health Check', () => {
    const res = http.get(`${BASE_URL}/api/v1/health`);
    apiResponseTime.add(res.timings.duration);

    check(res, {
      'health status is 200': (r) => r.status === 200,
      'health response time < 500ms': (r) => r.timings.duration < 500,
    });
  });

  sleep(1);

  group('Menu Operations', () => {
    const res = http.get(`${BASE_URL}/api/v1/menu`);
    apiResponseTime.add(res.timings.duration);

    const success = check(res, {
      'menu status is 200': (r) => r.status === 200,
      'menu has items': (r) => r.body.length > 0,
    });

    apiSuccess.add(success);
    apiErrors.add(!success);
  });

  sleep(1);

  if (TOKEN) {
    group('Order Operations', () => {
      const orderPayload = JSON.stringify({
        customerId: 1,
        deliveryAddress: '123 Main St',
        deliveryType: 'DELIVERY',
        items: [
          {
            menuItemId: 1,
            quantity: 2,
          }
        ],
      });

      const params = {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${TOKEN}`,
        },
      };

      const res = http.post(`${BASE_URL}/api/v1/orders`, orderPayload, params);
      apiResponseTime.add(res.timings.duration);

      const success = check(res, {
        'order created status is 201': (r) => r.status === 201,
        'order response time < 1000ms': (r) => r.timings.duration < 1000,
      });

      apiSuccess.add(success);
      apiErrors.add(!success);
    });

    sleep(1);
  }

  group('Metrics Operations', () => {
    const res = http.get(`${BASE_URL}/actuator/prometheus`);
    apiResponseTime.add(res.timings.duration);

    check(res, {
      'metrics status is 200': (r) => r.status === 200,
    });
  });

  sleep(2);
}

