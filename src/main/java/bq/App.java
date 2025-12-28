package bq;

import java.util.List;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "bq", mixinStandardHelpOptions = true, version = "0.0.1", description = "BitQuant Tool")
public class App {

	public static void main(String[] args) throws Exception {

		System.out.println("ARGS: " + List.of(args));

		int exitCode = new CommandLine(new App()).execute(args);
		System.exit(exitCode);

	}

	@Command(name = "fetch", description = "fetch data")
	int subCommandViaMethod(
			@Parameters(arity = "1..*", paramLabel = "<countryCode>", description = "country code(s) to be resolved") String[] countryCodes) {

		return 0;
	}

	@Command(name = "update", description = "update data")
	void subCommandViaMethod2(
			@Parameters(arity = "1..*", paramLabel = "<countryCode>", description = "country code(s) to be resolved") String[] countryCodes) {
		System.out.println("update");
	}

}
