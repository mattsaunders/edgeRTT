package com.example.latencyclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class ExperimentRunner {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .eventListenerFactory(HttpEventListener.FACTORY)
            .build();

    public void run(String[] endpoints) {

        for(String host: endpoints) {
            Request getRequest = new Request.Builder()
                    .url(host)
                    .build();

            client.newCall(getRequest).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    System.out.println("Error " + e.getMessage());
                }

                @Override public void onResponse(Call call, Response response) {
                    try (ResponseBody body = response.body()) {
                        // Consume and discard the response body.
                        body.source().readByteString();
                    } catch (Exception e) {
                        System.out.println("Error " + e.getMessage());
                    }
                }
            });
        }
    }


    private static final class HttpEventListener extends EventListener {
        private static final Factory FACTORY = new Factory() {
            final AtomicLong nextCallId = new AtomicLong(1L);

            @Override public EventListener create(Call call) {
                long callId = nextCallId.getAndIncrement();
                String endpoint = call.request().url().toString();
                System.out.printf("%04d %s%n", callId, endpoint);
                return new HttpEventListener(callId, System.nanoTime(), endpoint);
            }
        };

        final long callId;
        final long callStartNanos;
        final String endpoint;
        Map<String, Double> measurements = new HashMap<>();

        HttpEventListener(long callId, long callStartNanos, String endpoint) {
            this.callId = callId;
            this.callStartNanos = callStartNanos;
            this.endpoint = endpoint;
        }

        private void addEvent(String name) {
            long elapsedNanos = System.nanoTime() - callStartNanos;
            System.out.printf("%04d %.3f %s%n", callId, elapsedNanos / 1000000000d, name);
            measurements.put(name, elapsedNanos / 1000000000d);
        }

        @Override public void proxySelectStart(Call call, HttpUrl url) {
            addEvent("proxySelectStart");
        }

        @Override public void proxySelectEnd(Call call, HttpUrl url, List<Proxy> proxies) {
            addEvent("proxySelectEnd");
        }

        @Override public void callStart(Call call) {
            addEvent("callStart");
        }

        @Override public void dnsStart(Call call, String domainName) {
            addEvent("dnsStart");
        }

        @Override public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
            addEvent("dnsEnd");
        }

        @Override public void connectStart(
                Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
            addEvent("connectStart");
        }

        @Override public void secureConnectStart(Call call) {
            addEvent("secureConnectStart");
        }

        @Override public void secureConnectEnd(Call call, Handshake handshake) {
            addEvent("secureConnectEnd");
        }

        @Override public void connectEnd(
                Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
            addEvent("connectEnd");
        }

        @Override public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy,
                                            Protocol protocol, IOException ioe) {
            addEvent("connectFailed");
        }

        @Override public void connectionAcquired(Call call, Connection connection) {
            addEvent("connectionAcquired");
        }

        @Override public void connectionReleased(Call call, Connection connection) {
            addEvent("connectionReleased");
        }

        @Override public void requestHeadersStart(Call call) {
            addEvent("requestHeadersStart");
        }

        @Override public void requestHeadersEnd(Call call, Request request) {
            addEvent("requestHeadersEnd");
        }

        @Override public void requestBodyStart(Call call) {
            addEvent("requestBodyStart");
        }

        @Override public void requestBodyEnd(Call call, long byteCount) {
            addEvent("requestBodyEnd");
        }

        @Override public void requestFailed(Call call, IOException ioe) {
            addEvent("requestFailed");
        }

        @Override public void responseHeadersStart(Call call) {
            addEvent("responseHeadersStart");
        }

        @Override public void responseHeadersEnd(Call call, Response response) {
            addEvent("responseHeadersEnd");
        }

        @Override public void responseBodyStart(Call call) {
            addEvent("responseBodyStart");
        }

        @Override public void responseBodyEnd(Call call, long byteCount) {
            addEvent("responseBodyEnd");
        }

        @Override public void responseFailed(Call call, IOException ioe) {
            addEvent("responseFailed");
        }

        @Override public void callEnd(Call call) {
            addEvent("callEnd");
            System.out.println("Latency measurement for: " + endpoint + "\n" + measurements.toString());
        }

        @Override public void callFailed(Call call, IOException ioe) {
            addEvent("callFailed");
        }
    }
}