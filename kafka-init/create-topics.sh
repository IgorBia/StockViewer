#!/bin/bash
kafka-topics --create --topic indicator_events --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1
