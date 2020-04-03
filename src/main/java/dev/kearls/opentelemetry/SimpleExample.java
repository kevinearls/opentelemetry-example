package dev.kearls.opentelemetry;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;

/**
 * A simple example of using OpenTelemetry to connect to Jaeger. Currently this assumes the Jaeger instance
 * is running on localhost
 *
 * Mostly based on https://github.com/open-telemetry/opentelemetry-java/tree/master/examples/jaeger
 */
public class SimpleExample {
    private String ip = "localhost";
    private int port=14250;
    String tracerProviderName = "dev.kearls.opentelemetry.SimpleExample";
    // OTel API
    private Tracer tracer = OpenTelemetry.getTracerProvider().get(tracerProviderName);
    private JaegerGrpcSpanExporter jaegerExporter;

    private void setupJaegerExporter() {
        // Create a channel towards Jaeger end point
        ManagedChannel jaegerChannel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
        // Export traces to Jaeger
        String serviceName = "example" + System.currentTimeMillis();
        this.jaegerExporter =
                JaegerGrpcSpanExporter.newBuilder()
                        .setServiceName(serviceName)
                        .setChannel(jaegerChannel)
                        .setDeadlineMs(30000)
                        .build();

        // Set to process the spans by the Jaeger Exporter
        SimpleSpansProcessor spansProcessor = SimpleSpansProcessor.newBuilder(this.jaegerExporter).build();
        OpenTelemetrySdk.getTracerProvider().addSpanProcessor(spansProcessor);
    }

    private void createSomeSpans() {
        // Generate a span
        Span span = this.tracer.spanBuilder("Create some spans").startSpan();
        span.addEvent("Event 0");
        doWork();  // TODO why is this here?  Just to simulate a wait?
        span.addEvent("Event 1");
        span.end();
    }

    private void doWork() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) {
        SimpleExample simpleExample = new SimpleExample();
        simpleExample.setupJaegerExporter();
        simpleExample.createSomeSpans();
        // TODO why wait?  For flush?
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        System.out.println("Bye");
    }

}
