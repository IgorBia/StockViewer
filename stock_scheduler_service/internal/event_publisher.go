package internal

import (
	"encoding/json"
	"fmt"
	"strings"

	"github.com/confluentinc/confluent-kafka-go/kafka"
	"github.com/gofrs/uuid"
)

func publishCandleEvent(brokers []string, topic string, payload []uuid.UUID) error {
	p, err := kafka.NewProducer(&kafka.ConfigMap{
		"bootstrap.servers": strings.Join(brokers, ","),
	})

	msgBytes, err := json.Marshal(payload)
	if err != nil {
		return err
	}

	deliveryChan := make(chan kafka.Event)

	msg := &kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
		Value:          msgBytes,
	}
	err = p.Produce(msg, deliveryChan)
	if err != nil {
		return err
	}
	defer p.Close()

	e := <-deliveryChan
	m := e.(*kafka.Message)
	if m.TopicPartition.Error != nil {
		fmt.Printf("Delivery failed: %v\n", m.TopicPartition.Error)
	} else {
		fmt.Printf("Delivered message to %v\n", m.TopicPartition)
	}
	close(deliveryChan)
	p.Flush(5000)
	return nil
}
