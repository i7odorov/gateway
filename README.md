## 🚀 How to Run

### ✅ Prerequisites

- Docker + Docker Compose
- Java 17
- Maven

### ▶️ Steps to run


- Clone the project 
  - git clone https://github.com/i7odorov/gateway.git
- Build
  - mvn clean install
- Run
  - docker-compose up --build


### 🌐 REST APIs

- Example request for JSON current API:

curl --location 'http://localhost:8080/json/current' \
--header 'Content-Type: application/json' \
--data '{
"requestId": "json-current-001",
"timestamp": 1713194400000,
"client": "client-01",
"currency": "VUV"
}'

- Example request for JSON history API:

curl --location 'http://localhost:8080/json/history' \
--header 'Content-Type: application/json' \
--data '{
"requestId": "json-history-002",
"timestamp": 1713194400000,
"client": "client-01",
"currency": "USD",
"period": 12
}'

- Example request for XML current API

curl --location 'http://localhost:8080/xml/command' \
--header 'Content-Type: application/xml' \
--data '<request>
<requestId>xml-get-003</requestId>
<timestamp>1713194400000</timestamp>
<client>client-xml-01</client>
<get>
<currency>USD</currency>
</get>
</request>'

- Example request for XML history API

curl --location 'http://localhost:8080/xml/command' \
--header 'Content-Type: application/xml' \
--data '<request>
<requestId>xml-history-004</requestId>
<timestamp>1713194400000</timestamp>
<client>client-xml-01</client>
<history>
<currency>USD</currency>
<period>24</period>
</history>
</request>'

### Notes
- Fixer API key has a limitation of 100 request
- Fixer API free plan is working only with base currency EUR