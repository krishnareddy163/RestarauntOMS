import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate, Gauge } from 'k6/metrics';

// Custom metrics
const apiResponseTime = new Trend('api_response_time');
const apiErrors = new Rate('api_errors');
const apiSuccess = new Rate('api_success');
const concurrentUsers = new Gauge('concurrent_users');

export const options = {
  stages: __ENV.HIGH_LOAD === 'true' ? [
    { duration: '1m', target: 50 },
    { duration: '2m', target: 200 },
    { duration: '2m', target: 200 },
    { duration: '1m', target: 0 },
  ] : [
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
const MENU_ITEM_IDS = (__ENV.MENU_ITEM_IDS || '1').split(',').map((v) => parseInt(v.trim(), 10)).filter(Boolean);
const ENABLE_PAYMENTS = __ENV.ENABLE_PAYMENTS === 'true';
const ENABLE_DELIVERIES = __ENV.ENABLE_DELIVERIES === 'true';

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomChoice(list) {
  return list[randomInt(0, list.length - 1)];
}

function randomString(len) {
  const chars = 'abcdefghijklmnopqrstuvwxyz';
  let out = '';
  for (let i = 0; i < len; i += 1) out += chars[randomInt(0, chars.length - 1)];
  return out;
}

function randomPhone() {
  return `${randomInt(100, 999)}${randomInt(100, 999)}${randomInt(1000, 9999)}`;
}

function randomAddress() {
  return `${randomInt(1, 9999)} ${randomString(6)} St`;
}

function buildRandomOrder() {
  const itemsCount = randomInt(1, 3);
  const items = [];
  for (let i = 0; i < itemsCount; i += 1) {
    items.push({
      menuItemId: randomChoice(MENU_ITEM_IDS),
      quantity: randomInt(1, 4),
    });
  }
  return {
    customerId: randomInt(1, 1000),
    deliveryAddress: randomAddress(),
    deliveryType: Math.random() > 0.2 ? 'DELIVERY' : 'PICKUP',
    items,
  };
}

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
      const orderPayload = JSON.stringify(buildRandomOrder());

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

      if (success && ENABLE_PAYMENTS) {
        const orderId = res.json('id');
        const paymentPayload = JSON.stringify({
          orderId: orderId,
          amount: 9.99,
          paymentMethod: 'CREDIT_CARD',
        });
        const paymentRes = http.post(`${BASE_URL}/api/v1/payments`, paymentPayload, params);
        apiResponseTime.add(paymentRes.timings.duration);
        const payOk = check(paymentRes, {
          'payment created status is 201': (r) => r.status === 201,
        });
        apiSuccess.add(payOk);
        apiErrors.add(!payOk);
      }

      if (success && ENABLE_DELIVERIES) {
        const orderId = res.json('id');
        const assignRes = http.post(`${BASE_URL}/api/v1/deliveries/${orderId}/assign`, null, params);
        apiResponseTime.add(assignRes.timings.duration);
        const assignOk = check(assignRes, {
          'delivery assigned status is 201': (r) => r.status === 201,
        });
        apiSuccess.add(assignOk);
        apiErrors.add(!assignOk);
      }
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
