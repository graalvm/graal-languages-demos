package org.example;


import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

public class App {
    public App() {
    }

    public static void main(String[] args) throws Exception {
        try (Context context = Context.newBuilder(new String[]{"js"})
                .allowHostAccess(HostAccess.ALL)
                .option("engine.WarnInterpreterOnly", "false")
                .option("js.esm-eval-returns-exports", "true")
                .option("js.unhandled-rejections", "throw")
                .build()) {



            Source bundleSrc = Source.newBuilder("js", App.class.getResource("/bundle/bundle.mjs")).build();
            Value exports = context.eval(bundleSrc);
            String input = args.length > 0 ? args[0] : "https://www.graalvm.org/javascript/";
            QRCode qrCode = exports.getMember("QRCode").as(QRCode.class);
            Promise resultPromise = qrCode.toString(input);

            resultPromise.then((result) -> {
                System.out.println("Successfully generated QR code for \"" + input + "\".");
                System.out.println(result.asString());
            });

            Value qrCodeValue = exports.getMember("QRCode");
            Value resultValue = qrCodeValue.invokeMember("toString", input);

            resultValue.invokeMember("then", (ProxyExecutable) (arguments) -> {
                Value result = arguments[0];
                System.out.println("Successfully generated QR code for \"" + input + "\".");
                System.out.println(result.asString());
                return result;
            });
        }
    }
}