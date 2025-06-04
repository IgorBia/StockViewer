FROM golang:1.24-alpine

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

WORKDIR /app/cmd/app
RUN go build -o /app/app .

WORKDIR /app

CMD ["./app"]