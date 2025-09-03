package com.portfolio.domain.exception;

public interface Errors {

    interface GetPortfolioSummary {
        String errorCode = "10";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface GetPosition {
        String errorCode = "02";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface RecalculatePosition {
        String errorCode = "03";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface UpdateMarketData {
        String errorCode = "04";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface  CreateTransaction {
        String errorCode = "05";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface  DeleteTransaction {
        String errorCode = "06";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface  UpdateTransaction {
        String errorCode = "07";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface GetTransaction {
        String errorCode = "08";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
    }

    interface MarketData {
        String errorCode = "09";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error API_ERROR = new Error(errorCode + "02");
        Error NULL_RESPONSE = new Error(errorCode + "03");
        Error NETWORK_ERROR = new Error(errorCode + "04");
        Error INVALID_TICKER = new Error(errorCode + "05");
    }

    interface GetDividends {
        String errorCode = "11";

        Error INVALID_INPUT = new Error(errorCode + "01");
        Error NOT_FOUND = new Error(errorCode + "02");
        Error PERSISTENCE_ERROR = new Error(errorCode + "03");
        Error MARKET_DATA_ERROR = new Error(errorCode + "04");
    }

}
