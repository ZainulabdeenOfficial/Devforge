package com.example.data.model

object DevForgeData {

    val regexPresets = listOf(
        RegexPreset("Email Address", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$", "Standard RFC 5322 compliant email verification", "developer@devforge.io", "Validation"),
        RegexPreset("US Phone Number", "^\\+?1?[\\s.-]?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}\$", "Formats: +1 555-555-5555, (555) 555-5555", "+1 (555) 019-2831", "Validation"),
        RegexPreset("Strong Password", "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$", "At least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special symbol", "DevForge2026!", "Security"),
        RegexPreset("IPv4 Address", "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$", "Validates IPv4 address string (0.0.0.0 to 255.255.255.255)", "192.168.1.1", "Network"),
        RegexPreset("IPv6 Address", "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\$", "Standard 8-group hexadecimal IPv6 string", "2001:0db8:85a3:0000:0000:8a2e:0370:7334", "Network"),
        RegexPreset("URL Endpoint", "^https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)\$", "HTTP or HTTPS valid URL parser", "https://api.github.com/repos/android/compose-samples", "Network"),
        RegexPreset("UUID v4", "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}\$", "Universally Unique Identifier Version 4", "f47ac10b-58cc-4372-a567-0e02b2c3d479", "Identifiers"),
        RegexPreset("HEX Color Code", "^#?([a-fA-F0-9]{6}|[a-fA-F0-9]{3})\$", "3 or 6 digit hex color code (e.g. #00E5FF)", "#00E5FF", "Graphics"),
        RegexPreset("Credit Card Number", "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\$", "Visa, MasterCard, Amex, Discover card format", "4532015112830911", "Validation"),
        RegexPreset("JWT Token Format", "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*\$", "Validates 3-part JSON Web Token structure", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c", "Security")
    )

    val httpStatusCodes = listOf(
        HttpStatusCodeModel(100, "Continue", "1xx Informational", "Server received headers; client should proceed to send request body.", "Handling large POST requests with 'Expect: 100-continue'.", "Use streaming request bodies in OkHttp to optimize network throughput."),
        HttpStatusCodeModel(101, "Switching Protocols", "1xx Informational", "Server agrees to switch protocols (e.g., HTTP to WebSocket).", "WebSocket handshake upgrade request.", "Ensure OkHttp WebSocketListener manages reconnect events."),
        HttpStatusCodeModel(200, "OK", "2xx Success", "Standard success response for HTTP requests.", "Request succeeded, returning requested entity.", "Parse response body into structured data model."),
        HttpStatusCodeModel(201, "Created", "2xx Success", "Request succeeded and a new resource was created.", "POST or PUT request successfully created a database record.", "Look for the 'Location' header pointing to the newly created resource."),
        HttpStatusCodeModel(202, "Accepted", "2xx Success", "Request accepted for processing, but not yet completed.", "Asynchronous background task queued.", "Poll status URL or listen for webhook notifications."),
        HttpStatusCodeModel(204, "No Content", "2xx Success", "Server successfully processed request, but returns no body.", "DELETE or PUT operation complete.", "Do not expect a JSON response body for 204."),
        HttpStatusCodeModel(301, "Moved Permanently", "3xx Redirect", "Target resource has been assigned a new permanent URI.", "API endpoint domain or path migrated.", "OkHttp automatically follows redirects up to 20 redirects by default."),
        HttpStatusCodeModel(302, "Found", "3xx Redirect", "URI of requested resource has been changed temporarily.", "Temporary redirect or auth challenge redirect.", "Inspect the 'Location' header for temporary redirect URL."),
        HttpStatusCodeModel(304, "Not Modified", "3xx Redirect", "Resource has not been modified since last requested (etag/if-modified-since).", "Client cache validation hit.", "Serve cached data directly without re-downloading."),
        HttpStatusCodeModel(400, "Bad Request", "4xx Client Error", "Server cannot process request due to malformed syntax.", "Missing required JSON parameter or invalid payload format.", "Validate request JSON against API schema before sending."),
        HttpStatusCodeModel(401, "Unauthorized", "4xx Client Error", "Authentication credentials missing or invalid.", "Expired OAuth token or incorrect Bearer header.", "Implement automatic token refresh via OkHttp Authenticator."),
        HttpStatusCodeModel(403, "Forbidden", "4xx Client Error", "Server understood request but refuses to authorize it.", "User lacks required role permissions.", "Check API scope permissions or API Key restrictions."),
        HttpStatusCodeModel(404, "Not Found", "4xx Client Error", "Server cannot find requested resource.", "Invalid endpoint URL or deleted record ID.", "Double check URL path variables and query parameters."),
        HttpStatusCodeModel(405, "Method Not Allowed", "4xx Client Error", "Request method (GET, POST) not supported by endpoint.", "Sending POST to a GET-only route.", "Inspect 'Allow' header to see supported HTTP methods."),
        HttpStatusCodeModel(409, "Conflict", "4xx Client Error", "Request conflicts with current state of server.", "Duplicate unique key constraint (e.g. duplicate email).", "Handle conflict by prompting user or updating existing record."),
        HttpStatusCodeModel(422, "Unprocessable Entity", "4xx Client Error", "Request formatted correctly but contains semantic errors.", "Validation errors in submitted form fields.", "Parse error array from response body to highlight fields."),
        HttpStatusCodeModel(429, "Too Many Requests", "4xx Client Error", "User sent too many requests in a given amount of time.", "Rate limit quota exceeded.", "Check 'Retry-After' header and apply exponential backoff."),
        HttpStatusCodeModel(500, "Internal Server Error", "5xx Server Error", "Generic server-side exception occurred.", "Unhandled exception or database timeout on backend.", "Log request payload and notify backend engineering team."),
        HttpStatusCodeModel(502, "Bad Gateway", "5xx Server Error", "Server received an invalid response from upstream server.", "Proxy or load balancer failure.", "Retry after a brief delay."),
        HttpStatusCodeModel(503, "Service Unavailable", "5xx Server Error", "Server currently unable to handle request due to maintenance or overload.", "Server deployment or high traffic surge.", "Implement retry mechanism with jitter."),
        HttpStatusCodeModel(504, "Gateway Timeout", "5xx Server Error", "Upstream server failed to send timely response.", "Database deadlock or long-running query.", "Increase connection timeouts if calling complex backend jobs.")
    )

    val devTutorials = listOf(
        DevTutorial(
            id = "tut_1",
            title = "Mastering REST API Design & HTTP Methods",
            category = "REST",
            readTime = "5 min read",
            summary = "Best practices for designing clean, predictable RESTful endpoints.",
            fullContent = "REST (Representational State Transfer) is an architectural style for network applications.\n\nKey Principles:\n1. Statelessness: Each request from client to server must contain all info needed to process it.\n2. Nouns over Verbs: Use URIs like /api/v1/users instead of /api/v1/getUsers.\n3. Proper HTTP Methods:\n   • GET: Retrieve resource\n   • POST: Create new resource\n   • PUT: Replace entire resource\n   • PATCH: Partial update\n   • DELETE: Remove resource\n\n4. Status Codes:\n   • 2xx for success\n   • 4xx for client errors\n   • 5xx for server failures"
        ),
        DevTutorial(
            id = "tut_2",
            title = "OAuth 2.0 & PKCE Mobile Auth Flow",
            category = "OAuth",
            readTime = "7 min read",
            summary = "How Proof Key for Code Exchange secures mobile authorization.",
            fullContent = "PKCE (Proof Key for Code Exchange) is mandatory for mobile apps implementing OAuth 2.0.\n\nFlow:\n1. App generates a secret `code_verifier` and hashes it to create a `code_challenge`.\n2. App redirects user to Authorization Server with `code_challenge`.\n3. User authenticates and server redirects back with `auth_code`.\n4. App exchanges `auth_code` + `code_verifier` for Access & Refresh Tokens.\n5. Authorization Server verifies `hash(code_verifier) == code_challenge` before issuing tokens."
        ),
        DevTutorial(
            id = "tut_3",
            title = "Decoding & Verifying JWT Security",
            category = "JWT",
            readTime = "4 min read",
            summary = "Understanding Header, Payload claims, and Signature verification.",
            fullContent = "JSON Web Tokens (JWT) consist of 3 dot-separated parts:\n1. Header: Algorithm (HS256, RS256) and Token Type.\n2. Payload: Claims such as `sub` (subject), `exp` (expiration timestamp), `iat` (issued at), and roles.\n3. Signature: HMACSHA256(base64Url(Header) + \".\" + base64Url(Payload), secret).\n\nSecurity Checklist:\n• Never store sensitive passwords in JWT payload (Base64 is NOT encryption).\n• Always verify signature on server side.\n• Store tokens in encrypted storage (KeyStore / EncryptedSharedPreferences)."
        ),
        DevTutorial(
            id = "tut_4",
            title = "Regex Patterns for Production Input Validation",
            category = "Regex",
            readTime = "6 min read",
            summary = "Writing fast, safe Regular Expressions without catastrophic backtracking.",
            fullContent = "Regular expressions match character patterns in text strings.\n\nKey Concepts:\n• Anchors: ^ (start) and $ (end).\n• Character Classes: \\d (digit), \\w (word char), \\s (whitespace).\n• Quantifiers: * (0+), + (1+), ? (0 or 1), {n,m} (range).\n• Groups: (group) for capturing, (?:non-capturing) for performance.\n\nAvoid Catastrophic Backtracking:\nDo not nest quantifiers like (a+)+ or (.*a)+ which cause exponential CPU execution time!"
        )
    )
}
