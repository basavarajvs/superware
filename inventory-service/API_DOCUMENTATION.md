# API Documentation

## Accessing Swagger UI

After starting the application, you can access the Swagger UI at:

```
http://localhost:<server.port>/swagger-ui.html
```

Or the newer version at:
```
http://localhost:<server.port>/swagger-ui/index.html
```

## Accessing OpenAPI JSON

The OpenAPI specification is available at:
```
http://localhost:<server.port>/v3/api-docs
```

## API Documentation Features

- **Interactive API Documentation**: Test API endpoints directly from the browser
- **Request/Response Examples**: See example requests and responses for each endpoint
- **Authentication**: The API uses JWT for authentication
- **Model Documentation**: Detailed documentation of all request/response models

## Authentication

To use the API:

1. First, obtain a JWT token from the authentication service
2. Click the "Authorize" button in Swagger UI
3. Enter your token in the format: `Bearer <your-jwt-token>`
4. Click "Authorize" to enable authenticated requests

## Common HTTP Status Codes

- `200 OK`: Request was successful
- `201 Created`: Resource was created successfully
- `400 Bad Request`: Invalid request format or parameters
- `401 Unauthorized`: Authentication required or invalid token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Versioning

API versioning is handled through the `Accept` header. The current API version is `v1`.
