/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.wallet.EthWallet;
import io.multy.storage.RealmManager;

public class CryptoFormatUtils {

    private final static DecimalFormatSymbols symbols = getSymbols();
    public final static DecimalFormat FORMAT_BTC = new DecimalFormat("#.##################", symbols);
    public final static DecimalFormat FORMAT_ETH = new DecimalFormat("#.##########", symbols);
    public final static DecimalFormat FORMAT_USD = new DecimalFormat("#.##", symbols);

    public static String satoshiToBtc(long satoshi) {
        if (satoshi == 0) {
            return "0.0";
        }
        String result = FORMAT_BTC.format(satoshi / Math.pow(10, 8));
        if (!result.equals("") && result.contains(",")) {
            result = result.replaceAll(",", ".");
        }
        return result;
    }

    public static double satoshiToBtcDouble(long satoshi) {
        if (satoshi == 0) {
            return 0.0;
        }
        return satoshi / Math.pow(10, 8);
    }

    public static String satoshiToUsd(long satoshi) {
        if (satoshi == 0) {
            return "0.0";
        }

        final CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        if (currenciesRate == null) {
            return "";
        }

        double btc = satoshi / Math.pow(10, 8);
        double fiat = currenciesRate.getBtcToUsd() * btc;

        String result = FORMAT_USD.format(fiat);
        if (!result.equals("") && result.contains(",")) {
            result = result.replaceAll(",", ".");
        }
        return result;
    }

    /**
     * @param satoshi
     * @param price   usd per btc
     * @return amount in usd
     */
    public static String satoshiToUsd(long satoshi, double price) {
        if (satoshi == 0) {
            return "0.0";
        }

        final CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        if (currenciesRate == null) {
            return "";
        }

        double btc = (satoshi / Math.pow(10, 8));
        return FORMAT_USD.format(btc * price);
    }

    public static String btcToUsd(double btc, double price) {
        if (btc == 0) {
            return "0.0";
        }

        final CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        if (currenciesRate == null) {
            return "";
        }

        return FORMAT_USD.format(btc * price);
    }

    public static long btcToSatoshi(String btc) {
        try {
            return (long) (Double.parseDouble(btc) * Math.pow(10, 8));
        } catch (Exception e) {
            return -1;
        }
    }

    public static double weiToEth(String wei) {
        return wei.equals("0") ? 0 : new BigDecimal(wei).divide(EthWallet.DIVISOR).doubleValue();
    }

    public static String wetToEthLabel(String wei) {
        return wei.equals("0") ? "0" : new BigDecimal(wei).divide(EthWallet.DIVISOR).doubleValue() + " ETH";
    }

    public static String ethToWei(String eth) {
        return (new BigDecimal(eth).multiply(EthWallet.DIVISOR)).toBigInteger().toString();
    }

    public static String ethToUsd(double eth, double price) {
        if (eth == 0) {
            return "0.0";
        }

        return FORMAT_USD.format(eth * price);
    }

    public static String ethToUsd(double eth) {
        String result = "0.0";

        final CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        if (currenciesRate != null && eth != 0) {
            result = FORMAT_USD.format(eth * currenciesRate.getEthToUsd());
        }

        return result;
    }

    public static String usdToEth(double usd) {
        String result = "0.0";

        final CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        if (currenciesRate != null && usd != 0) {
            result = FORMAT_ETH.format(usd / currenciesRate.getEthToUsd());
        }

        return result;
    }

    private static DecimalFormatSymbols getSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        return symbols;
    }
}
