#!/usr/bin/env python3
"""
Simple Mock Authentication API Server
Mimics the Ktor-based mock API for testing purposes
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import time
import hashlib
import random
import string
from urllib.parse import urlparse

# Mock user database
USERS = {
    "demo": "password",
    "user1": "pass123",
    "test": "test123"
}

# Store valid refresh tokens
valid_refresh_tokens = set()

def generate_token(username, token_type="access"):
    """Generate a simple JWT-like token"""
    timestamp = str(int(time.time()))
    random_str = ''.join(random.choices(string.ascii_letters + string.digits, k=32))
    payload = f"{username}:{token_type}:{timestamp}:{random_str}"
    token = hashlib.sha256(payload.encode()).hexdigest()
    return f"eyJ{token}"

def generate_user_id():
    """Generate a UUID-like user ID"""
    return ''.join(random.choices(string.hexdigits.lower(), k=32))

class MockAuthHandler(BaseHTTPRequestHandler):

    def _send_json_response(self, status_code, data):
        """Send JSON response"""
        self.send_response(status_code)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(json.dumps(data, indent=2).encode())

    def _send_text_response(self, status_code, text):
        """Send text response"""
        self.send_response(status_code)
        self.send_header('Content-type', 'text/plain')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(text.encode())

    def _read_json_body(self):
        """Read and parse JSON body"""
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length)
        return json.loads(body.decode()) if body else {}

    def do_OPTIONS(self):
        """Handle CORS preflight"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()

    def do_GET(self):
        """Handle GET requests"""
        path = urlparse(self.path).path

        if path == '/':
            self._send_text_response(200,
                "Mock Auth API - Server is running\n\n"
                "Available endpoints:\n"
                "POST /auth/login\n"
                "POST /auth/token/refresh\n"
                "POST /auth/logout\n"
                "GET /auth/user\n\n"
                "Test credentials:\n"
                "- demo/password\n"
                "- user1/pass123\n"
                "- test/test123"
            )

        elif path == '/auth/user':
            auth_header = self.headers.get('Authorization', '')
            if not auth_header.startswith('Bearer '):
                self._send_json_response(401, {
                    "error": "missing_token",
                    "message": "Authorization header required"
                })
                return

            token = auth_header.replace('Bearer ', '')
            if token.startswith('eyJ'):
                # Valid token format
                self._send_json_response(200, {
                    "userId": generate_user_id(),
                    "username": "demo",
                    "email": "demo@example.com"
                })
            else:
                self._send_json_response(401, {
                    "error": "invalid_token",
                    "message": "Invalid or expired access token"
                })

        else:
            self._send_json_response(404, {
                "error": "not_found",
                "message": "Endpoint not found"
            })

    def do_POST(self):
        """Handle POST requests"""
        path = urlparse(self.path).path

        if path == '/auth/login':
            try:
                data = self._read_json_body()
                username = data.get('username')
                password = data.get('password')

                if username in USERS and USERS[username] == password:
                    access_token = generate_token(username, 'access')
                    refresh_token = generate_token(username, 'refresh')
                    valid_refresh_tokens.add(refresh_token)

                    self._send_json_response(200, {
                        "accessToken": access_token,
                        "refreshToken": refresh_token,
                        "expiresIn": 3600,
                        "userId": generate_user_id()
                    })
                else:
                    self._send_json_response(401, {
                        "error": "unauthorized",
                        "message": "Invalid username or password"
                    })
            except Exception as e:
                self._send_json_response(400, {
                    "error": "bad_request",
                    "message": str(e)
                })

        elif path == '/auth/token/refresh':
            try:
                data = self._read_json_body()
                refresh_token = data.get('refreshToken')

                if refresh_token in valid_refresh_tokens:
                    new_access_token = generate_token('user', 'access')

                    self._send_json_response(200, {
                        "accessToken": new_access_token,
                        "refreshToken": refresh_token,
                        "expiresIn": 3600,
                        "userId": generate_user_id()
                    })
                else:
                    self._send_json_response(401, {
                        "error": "invalid_token",
                        "message": "Invalid or expired refresh token"
                    })
            except Exception as e:
                self._send_json_response(400, {
                    "error": "bad_request",
                    "message": str(e)
                })

        elif path == '/auth/logout':
            try:
                data = self._read_json_body()
                refresh_token = data.get('refreshToken')

                if refresh_token in valid_refresh_tokens:
                    valid_refresh_tokens.remove(refresh_token)

                self._send_json_response(200, {
                    "message": "Logged out successfully"
                })
            except Exception as e:
                self._send_json_response(400, {
                    "error": "bad_request",
                    "message": str(e)
                })

        else:
            self._send_json_response(404, {
                "error": "not_found",
                "message": "Endpoint not found"
            })

    def log_message(self, format, *args):
        """Custom log format"""
        print(f"[{self.log_date_time_string()}] {format % args}")

def run_server(port=8080):
    """Start the mock API server"""
    server_address = ('', port)
    httpd = HTTPServer(server_address, MockAuthHandler)
    print(f"Mock Auth API Server starting on http://localhost:{port}")
    print(f"Press Ctrl+C to stop\n")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        httpd.shutdown()

if __name__ == '__main__':
    run_server()
