FROM golang:1.24-bullseye

# Instalacja zależności do builda Confluent Kafka
RUN apt-get update && apt-get install -y \
    git \
    librdkafka-dev \
    gcc \
    make \
    libc6-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Ustawienie katalogu roboczego
WORKDIR /app

# Kopiowanie plików go.mod i go.sum oraz pobranie zależności
COPY go.mod go.sum ./
RUN go mod download

# Kopiowanie całego kodu źródłowego
COPY . .

# Dodatkowe go mod tidy po skopiowaniu kodu (usuwa duplikaty, dodaje brakujące wpisy)
RUN go mod tidy

# Budowanie aplikacji
WORKDIR /app/cmd/app
RUN go build -o /app/app .

# Ustawienie katalogu roboczego na /app i uruchomienie aplikacji
WORKDIR /app
CMD ["./app"]
