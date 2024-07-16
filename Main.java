import java.util.*;
import java.time.*;

class Stock {
    enum StockType {COMMON, PREFERRED}

    private String symbol;
    private StockType type;
    private double lastDividend;
    private double fixedDividend;
    private double parValue;

    public Stock(String symbol, StockType type, double lastDividend, double fixedDividend, double parValue) {
        this.symbol = symbol;
        this.type = type;
        this.lastDividend = lastDividend;
        this.fixedDividend = fixedDividend;
        this.parValue = parValue;
    }

    public String getSymbol() {
        return symbol;
    }

    public double calculateDividendYield(double price) {
        if (type == StockType.COMMON) {
            return lastDividend / price;
        } else {
            return (fixedDividend * parValue) / price;
        }
    }

    public double calculatePERatio(double price) {
        return lastDividend == 0 ? 0 : price / lastDividend;
    }
}

class Trade {
    private LocalDateTime timestamp;
    private int quantity;
    private boolean isBuy;
    private double price;

    public Trade(int quantity, boolean isBuy, double price) {
        this.timestamp = LocalDateTime.now();
        this.quantity = quantity;
        this.isBuy = isBuy;
        this.price = price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}

class StockMarket {
    private Map<String, Stock> stocks = new HashMap<>();
    private List<Trade> trades = new ArrayList<>();

    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    public void recordTrade(Trade trade) {
        trades.add(trade);
    }

    public double calculateDividendYield(String symbol, double price) {
        Stock stock = getStock(symbol);
        return stock != null ? stock.calculateDividendYield(price) : 0;
    }

    public double calculatePERatio(String symbol, double price) {
        Stock stock = getStock(symbol);
        return stock != null ? stock.calculatePERatio(price) : 0;
    }

    public double calculateVolumeWeightedStockPrice(String symbol) {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        double totalTradePriceQuantity = 0;
        int totalQuantity = 0;

        for (Trade trade : trades) {
            if (trade.getTimestamp().isAfter(tenMinutesAgo) && trade.getPrice() > 0) {
                totalTradePriceQuantity += trade.getPrice() * trade.getQuantity();
                totalQuantity += trade.getQuantity();
            }
        }

        return totalQuantity == 0 ? 0 : totalTradePriceQuantity / totalQuantity;
    }

    public double calculateGBCEAllShareIndex() {
        double productOfPrices = 1.0;
        int count = 0;

        for (String symbol : stocks.keySet()) {
            double vwsp = calculateVolumeWeightedStockPrice(symbol);
            if (vwsp > 0) {
                productOfPrices *= vwsp;
                count++;
            }
        }

        return count == 0 ? 0 : Math.pow(productOfPrices, 1.0 / count);
    }
}

public class Main {
    public static void main(String[] args) {
        StockMarket market = new StockMarket();

        market.addStock(new Stock("TEA", Stock.StockType.COMMON, 0, 0, 100));
        market.addStock(new Stock("POP", Stock.StockType.COMMON, 8, 0, 100));
        market.addStock(new Stock("ALE", Stock.StockType.COMMON, 23, 0, 60));
        market.addStock(new Stock("GIN", Stock.StockType.PREFERRED, 8, 0.02, 100));
        market.addStock(new Stock("JOE", Stock.StockType.COMMON, 13, 0, 250));

        market.recordTrade(new Trade(100, true, 110));
        market.recordTrade(new Trade(200, false, 105));
        market.recordTrade(new Trade(50, true, 115));

        double dividendYield = market.calculateDividendYield("POP", 120);
        System.out.println("Dividend Yield for POP at price 120: " + dividendYield);

        double peRatio = market.calculatePERatio("POP", 120);
        System.out.println("P/E Ratio for POP at price 120: " + peRatio);

        double vwsp = market.calculateVolumeWeightedStockPrice("POP");
        System.out.println("Volume Weighted Stock Price for POP: " + vwsp);

        double gbceAllShareIndex = market.calculateGBCEAllShareIndex();
        System.out.println("GBCE All Share Index: " + gbceAllShareIndex);
    }
}
