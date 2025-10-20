#!/bin/bash
kafka-topics --create --topic indicator_events1 --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1
