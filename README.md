# OrderBookStimulator


A high-performance, event-driven trading engine simulator implementing real stock exchange matching logic with price-time priority execution.

## 🚀 Features
- **Price-Time Priority Matching** - Industry-standard NASDAQ/NYSE algorithm
- **Full & Partial Order Execution** - Realistic trade handling with audit trail
- **Real-time Order Book** - Live market depth visualization
- **PriorityQueue-based Engine** - O(log n) order matching efficiency
- **Clean OOP Architecture** - Modular, maintainable design
- **Comprehensive Demo** - 5 real trading scenarios with detailed output

## 📊 How It Works
The simulator implements a continuous double auction system where:
1. **BUY orders** are sorted by highest price → earliest timestamp
2. **SELL orders** are sorted by lowest price → earliest timestamp
3. **Matches execute** when buy price ≥ sell price
4. **Partial fills** are handled automatically
5. **Trade records** are maintained with complete audit trail

## 🏗️ Architecture
- **Order Management**: Immutable order objects with BUY/SELL types
- **Matching Engine**: PriorityQueue-based with custom comparators
- **Trade Processing**: Complete execution lifecycle
- **Market Data**: Real-time bid/ask/spread calculations

## 🛠️ Tech Stack
- **Language**: Java 11+
- **Data Structures**: PriorityQueue, ArrayList, Custom Comparators
- **Patterns**: Strategy (comparators), Immutable Objects, Iterator
- **Build**: Pure Java SDK (no external dependencies)
