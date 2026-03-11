function fn() {
  var baseUrl = karate.properties['baseUrl'] || 'http://localhost:8081';
  return {
    baseUrl: baseUrl
  };
}
