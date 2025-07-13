import requests

# Token obtenu lors de l'authentification (remplacez par un token réel)
auth_token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIwODgzYTA0Ny1lNWQ5LTRiZGUtYWJkYy1mZGUyZTk4YzZiYmYiLCJ1c2VybmFtZSI6ImFkbWluIiwiZW1haWwiOiJhZG1pbkBuZXdzcGxhdGZvcm0ubG9jYWwiLCJyb2xlIjoiQURNSU5JU1RSQVRFVVIiLCJyb2xlRGVzY3JpcHRpb24iOiJDUlVEIFV0aWxpc2F0ZXVycyArIGdlc3Rpb24gamV0b25zIiwiaWF0IjoxNzUyNDMxMDE1LCJleHAiOjE3NTI1MTc0MTV9.q1dOZ0sNBLOyTl2HxikAZtKeDfJzR2BPxtoZFUnDRFE_2BQnDpa9U8NfHQJ7bklEsP0sdKU64sBu1y22mRbATg"

# Requête SOAP pour lister les utilisateurs
soap_request = f"""<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Header/>
    <soap:Body>
        <userRequest>
            <operation>LIST</operation>
            <authToken>{auth_token}</authToken>
            <pagination>
                <page>0</page>
                <size>100</size>
                <sortBy>username</sortBy>
                <sortDir>ASC</sortDir>
            </pagination>
        </userRequest>
    </soap:Body>
</soap:Envelope>"""

headers = {
    'Content-Type': 'text/xml; charset=utf-8',
    'SOAPAction': 'http://newsplatform.com/soap/users/manageUsers'
}

try:
    print("=== TEST REQUÊTE SOAP LISTE UTILISATEURS ===")
    print(f"URL: http://localhost:8080/soap")
    print(f"Request:\n{soap_request}")
    
    response = requests.post(
        'http://localhost:8080/soap',
        data=soap_request,
        headers=headers
    )
    
    print(f"\nStatus Code: {response.status_code}")
    print(f"Headers: {response.headers}")
    print(f"Response:\n{response.text}")
    
except Exception as e:
    print(f"Erreur: {e}") 