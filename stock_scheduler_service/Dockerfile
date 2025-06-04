FROM golang:1.24-alpine

# Create a non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

WORKDIR /app/cmd/app
RUN go build -o /app/app .

WORKDIR /app

CMD ["./app"]