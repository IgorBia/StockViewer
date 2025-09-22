package internal

import (
	"context"
	"encoding/json"

	"github.com/segmentio/kafka-go"
)

func publishCandleEvent(brokers []string, topic string, payload []int64) error {
	w := kafka.NewWriter(kafka.WriterConfig{
		Brokers: brokers,
		Topic:   topic,
	})
	defer w.Close()

	msgBytes, err := json.Marshal(payload)
	if err != nil {
		return err
	}

	msg := kafka.Message{
		Value: msgBytes,
	}
	return w.WriteMessages(context.Background(), msg)
}
